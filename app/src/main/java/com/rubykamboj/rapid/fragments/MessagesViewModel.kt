package com.rubykamboj.rapid.fragments

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.rubykamboj.rapid.data.RapidDatabase
import com.rubykamboj.rapid.data.models.Chat
import com.rubykamboj.rapid.data.models.Message
import com.rubykamboj.rapid.data.repositories.AuthRepository
import com.rubykamboj.rapid.data.repositories.ChatsRepository
import com.rubykamboj.rapid.data.repositories.FirestoreRepository
import com.rubykamboj.rapid.data.repositories.MessagesRepository
import com.rubykamboj.rapid.utils.BODY
import com.rubykamboj.rapid.utils.CHANGE
import com.rubykamboj.rapid.utils.CHAT_ID
import com.rubykamboj.rapid.utils.CREATED_AT
import com.rubykamboj.rapid.utils.CURRENT_CHAT
import com.rubykamboj.rapid.utils.DELETE
import com.rubykamboj.rapid.utils.IS_ONLINE
import com.rubykamboj.rapid.utils.LAST_SEEN_AT
import com.rubykamboj.rapid.utils.MESSAGE
import com.rubykamboj.rapid.utils.MESSAGE_ID
import com.rubykamboj.rapid.utils.RECEIVER_ID
import com.rubykamboj.rapid.utils.REGISTRATION_TOKEN
import com.rubykamboj.rapid.utils.SEEN
import com.rubykamboj.rapid.utils.SEEN_ALL
import com.rubykamboj.rapid.utils.SENDER_ID
import com.rubykamboj.rapid.utils.TYPE
import com.rubykamboj.rapid.utils.millis
import kotlinx.coroutines.launch

class MessagesViewModel(application: Application, private val chat: Chat) : ViewModel() {
    
    private val authRepository = AuthRepository(Firebase.auth)
    private val firestoreRepository = FirestoreRepository(Firebase.firestore)
    private val functions = Firebase.functions
    private val userID = authRepository.getUser()!!.uid
    private val rapidDatabase = RapidDatabase.getDatabase(application)
    private val chatsRepository = ChatsRepository(rapidDatabase.chatsDao())
    private val messagesRepository = MessagesRepository(rapidDatabase.messagesDao())
    private val preferences = PreferenceManager.getDefaultSharedPreferences(application)
    private var createdAt = preferences.getLong(chat.id, 0)
    private var chatUserStatus: ListenerRegistration? = null
    private var updatesListener: ListenerRegistration? = null
    private val _isUserOnline = MutableLiveData<Boolean>()
    private var userCurrentChat: String? = null
    private var userRegistrationToken: String? = null
    val messages = messagesRepository.messages(chat.id)
    val isUserOnline: LiveData<Boolean> = _isUserOnline
    var userLastSeenAt: Long? = null
    
    init {
        chatUserStatus = firestoreRepository.userStatus(chat.userID)
            .addSnapshotListener {snapshot, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    userLastSeenAt = snapshot[LAST_SEEN_AT] as Long
                    userCurrentChat = snapshot[CURRENT_CHAT] as String
                    userRegistrationToken = snapshot[REGISTRATION_TOKEN] as String
                    _isUserOnline.value = snapshot[IS_ONLINE] as Boolean
                }
            }
    }
    
    private fun insertMessage(id: String, senderID: String, receiverID: String, body: String) {
        viewModelScope.launch {
            messagesRepository.insert(
                Message(id, chat.id, senderID, receiverID, body)
            )
        }
    }
    
    private fun updateMessage(id: String, body: String) {
        viewModelScope.launch {
            messagesRepository.update(id, body)
        }
    }
    
    private fun updateMessage(id: String) {
        viewModelScope.launch {
            messagesRepository.update(id)
        }
    }
    
    private fun addSeen(id: String) {
        if (isUserOnline.value!! && userCurrentChat == "Chats/${chat.id}") {
            firestoreRepository.addChatUpdate(
                chat.id, mapOf(
                    TYPE to SEEN,
                    MESSAGE_ID to id,
                    RECEIVER_ID to chat.userID,
                    CREATED_AT to millis()
                )
            ).addOnSuccessListener {
                updateMessage(id)
            }
        }
    }
    
    fun listen() {
        updatesListener?.remove()
        updatesListener = firestoreRepository.chatUpdates(chat.id, userID, createdAt).addSnapshotListener {snapshot, exception ->
            if (exception != null) {
                return@addSnapshotListener
            }
            if (snapshot != null) {
                snapshot.forEach {
                    when (it[TYPE] as String) {
                        MESSAGE -> {
                            val body = it[BODY] as String
                            insertMessage(it.id, chat.userID, userID, body)
                            addSeen(it.id)
                        }
                        SEEN -> {
                            val messageID = it[MESSAGE_ID] as String
                            updateMessage(messageID)
                        }
                        CHANGE -> {
                            val messageID = it[MESSAGE_ID] as String
                            val body = it[BODY] as String
                            updateMessage(messageID, body)
                        }
                        DELETE -> {
                            val messageID = it[MESSAGE_ID] as String
                            deleteMessage(messageID)
                        }
                    }
                    firestoreRepository.deleteChatUpdate(chat.id, it.id)
                }
                if (snapshot.size() > 0) {
                    createdAt = snapshot.documents[snapshot.size() - 1][CREATED_AT] as Long
                    preferences.edit {
                        putLong(chat.id, createdAt)
                    }
                }
            }
        }
    }
    
    fun addMessage(body: String) {
        if (!isUserOnline.value!!) {
            functions.getHttpsCallable("createNotification").call(
                mapOf(
                    TYPE to MESSAGE,
                    CHAT_ID to chat.id,
                    SENDER_ID to userID,
                    BODY to body,
                    REGISTRATION_TOKEN to userRegistrationToken
                )
            )
        }
        if (isUserOnline.value!! && userCurrentChat == "Chats/${chat.id}") {
            firestoreRepository.addChatUpdate(
                chat.id, mapOf(
                    TYPE to MESSAGE,
                    SENDER_ID to userID,
                    RECEIVER_ID to chat.userID,
                    BODY to body,
                    CREATED_AT to millis()
                )
            ).addOnSuccessListener {
                insertMessage(it.id, userID, chat.userID, body)
            }
        } else {
            firestoreRepository.addUpdate(
                chat.userID, mapOf(
                    TYPE to MESSAGE,
                    CHAT_ID to chat.id,
                    SENDER_ID to userID,
                    BODY to body,
                    CREATED_AT to millis()
                )
            ).addOnSuccessListener {
                insertMessage(it.id, userID, chat.userID, body)
            }
        }
    }
    
    fun addChange(id: String, body: String) {
        if (isUserOnline.value!! && userCurrentChat == "Chats/${chat.id}") {
            firestoreRepository.addChatUpdate(
                chat.id, mapOf(
                    TYPE to CHANGE,
                    MESSAGE_ID to id,
                    RECEIVER_ID to chat.userID,
                    BODY to body,
                    CREATED_AT to millis()
                )
            ).addOnSuccessListener {
                updateMessage(id, body)
            }
        } else {
            firestoreRepository.addUpdate(
                chat.userID, mapOf(
                    TYPE to CHANGE,
                    CHAT_ID to chat.id,
                    MESSAGE_ID to id,
                    BODY to body,
                    CREATED_AT to millis()
                )
            ).addOnSuccessListener {
                updateMessage(id, body)
            }
        }
    }
    
    fun addDelete(id: String) {
        if (isUserOnline.value!! && userCurrentChat == "Chats/${chat.id}") {
            firestoreRepository.addChatUpdate(
                chat.id, mapOf(
                    TYPE to DELETE,
                    MESSAGE_ID to id,
                    RECEIVER_ID to chat.userID,
                    CREATED_AT to millis()
                )
            ).addOnSuccessListener {
                deleteMessage(id)
            }
        } else {
            firestoreRepository.addUpdate(
                chat.userID, mapOf(
                    TYPE to DELETE,
                    CHAT_ID to chat.id,
                    MESSAGE_ID to id,
                    CREATED_AT to millis()
                )
            ).addOnSuccessListener {
                deleteMessage(id)
            }
        }
    }
    
    fun deleteMessage(id: String) {
        viewModelScope.launch {
            messagesRepository.delete(id)
        }
    }
    
    fun clearChat() {
        viewModelScope.launch {
            messagesRepository.clear(chat.id)
        }
    }
    
    fun updateChat() {
        val message = messages.value!!.maxBy {it.createdAt}
        val body = message?.body ?: ""
        val updatedAt = message?.createdAt ?: millis()
        viewModelScope.launch {
            chatsRepository.update(chat.id, body, 0, updatedAt)
        }
    }
    
    fun addSeenAll() {
        viewModelScope.launch {
            val unseenMessages = messagesRepository.unseenMessages(chat.id, userID)
            if (unseenMessages.isNotEmpty()) {
                firestoreRepository.addUpdate(
                    chat.userID, mapOf(
                        TYPE to SEEN_ALL,
                        CHAT_ID to chat.id,
                        CREATED_AT to millis()
                    )
                ).addOnSuccessListener {
                    viewModelScope.launch {
                        messagesRepository.seenAll(chat.id, chat.userID, millis())
                    }
                }
            }
        }
    }
    
    fun setCurrentChat(value: String = "Chats/${chat.id}") {
        firestoreRepository.updateUserData(
            userID, mapOf(
                CURRENT_CHAT to value
            )
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        chatUserStatus?.remove()
        updatesListener?.remove()
    }
}
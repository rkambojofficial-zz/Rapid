package com.rubykamboj.rapid

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
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
import com.rubykamboj.rapid.utils.SEEN_ALL
import com.rubykamboj.rapid.utils.SENDER_ID
import com.rubykamboj.rapid.utils.TYPE
import com.rubykamboj.rapid.utils.UPDATES
import com.rubykamboj.rapid.utils.millis
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authRepository = AuthRepository(Firebase.auth)
    private val firestoreRepository = FirestoreRepository(Firebase.firestore)
    private val userID = authRepository.getUser()!!.uid
    private val rapidDatabase = RapidDatabase.getDatabase(application)
    private val chatsRepository = ChatsRepository(rapidDatabase.chatsDao())
    private val messagesRepository = MessagesRepository(rapidDatabase.messagesDao())
    private val preferences = PreferenceManager.getDefaultSharedPreferences(application)
    private var createdAt = preferences.getLong(UPDATES, 0)
    private var updatesListener: ListenerRegistration? = null
    private val _chat = MutableLiveData<Chat>()
    val chat: LiveData<Chat> = _chat
    
    init {
        updatesListener = firestoreRepository.updates(userID, createdAt)
            .addSnapshotListener {snapshot, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    snapshot.documentChanges.forEach {
                        if (it.type == DocumentChange.Type.ADDED) {
                            val chatID = it.document[CHAT_ID] as String
                            when (it.document[TYPE] as String) {
                                MESSAGE -> {
                                    val senderID = it.document[SENDER_ID] as String
                                    val body = it.document[BODY] as String
                                    viewModelScope.launch {
                                        messagesRepository.insert(
                                            Message(it.document.id, chatID, senderID, userID, body)
                                        )
                                    }
                                    updateChat(chatID)
                                }
                                SEEN_ALL -> {
                                    val updateCreatedAt = it.document[CREATED_AT] as Long
                                    viewModelScope.launch {
                                        messagesRepository.seenAll(chatID, userID, updateCreatedAt)
                                    }
                                }
                                CHANGE -> {
                                    val messageID = it.document[MESSAGE_ID] as String
                                    val body = it.document[BODY] as String
                                    viewModelScope.launch {
                                        messagesRepository.update(messageID, body)
                                    }
                                    updateChat(chatID)
                                }
                                DELETE -> {
                                    val messageID = it.document[MESSAGE_ID] as String
                                    viewModelScope.launch {
                                        messagesRepository.delete(messageID)
                                    }
                                    updateChat(chatID)
                                }
                            }
                            firestoreRepository.deleteUpdate(userID, it.document.id)
                        }
                    }
                    if (snapshot.size() > 0) {
                        createdAt = snapshot.documents[snapshot.size() - 1][CREATED_AT] as Long
                        preferences.edit {
                            putLong(UPDATES, createdAt)
                        }
                    }
                }
            }
    }
    
    private fun updateChat(chatID: String) {
        viewModelScope.launch {
            val messages = messagesRepository.unseenMessages(chatID, userID)
            val body = messages.maxBy {it.createdAt}?.body ?: ""
            val count = messages.size.toLong()
            chatsRepository.update(chatID, body, count, millis())
        }
    }
    
    fun online() {
        firestoreRepository.updateUserData(
            userID, mapOf(
                IS_ONLINE to true
            )
        )
    }
    
    fun offline() {
        firestoreRepository.updateUserData(
            userID, mapOf(
                IS_ONLINE to false,
                LAST_SEEN_AT to millis(),
                CURRENT_CHAT to "Null"
            )
        )
    }
    
    fun openChat(id: String) {
        viewModelScope.launch {
            _chat.value = chatsRepository.getChat(id)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        updatesListener?.remove()
    }
}
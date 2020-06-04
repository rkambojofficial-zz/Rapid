package com.rubykamboj.rapid.fragments

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rubykamboj.rapid.data.RapidDatabase
import com.rubykamboj.rapid.data.models.Chat
import com.rubykamboj.rapid.data.repositories.AuthRepository
import com.rubykamboj.rapid.data.repositories.ChatsRepository
import com.rubykamboj.rapid.data.repositories.FirestoreRepository
import com.rubykamboj.rapid.data.repositories.MessagesRepository
import com.rubykamboj.rapid.data.repositories.UsersRepository
import com.rubykamboj.rapid.utils.AVATAR_URL
import com.rubykamboj.rapid.utils.CHATS
import com.rubykamboj.rapid.utils.CREATED_AT
import com.rubykamboj.rapid.utils.CURRENT_CHAT
import com.rubykamboj.rapid.utils.IS_ONLINE
import com.rubykamboj.rapid.utils.LAST_SEEN_AT
import com.rubykamboj.rapid.utils.NAME
import com.rubykamboj.rapid.utils.USERS
import com.rubykamboj.rapid.utils.millis
import kotlinx.coroutines.launch
import java.util.*

class ChatsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authRepository = AuthRepository(Firebase.auth)
    private val firestoreRepository = FirestoreRepository(Firebase.firestore)
    private val userID = authRepository.getUser()!!.uid
    private val rapidDatabase = RapidDatabase.getDatabase(application)
    private val chatsRepository = ChatsRepository(rapidDatabase.chatsDao())
    private val usersRepository = UsersRepository(rapidDatabase.usersDao())
    private val messagesRepository = MessagesRepository(rapidDatabase.messagesDao())
    private val preferences = PreferenceManager.getDefaultSharedPreferences(application)
    private var createdAt = preferences.getLong(CHATS, 0)
    private val _searchList = MutableLiveData<List<Chat>>()
    private val _isLoading = MutableLiveData<Boolean>()
    val chats = chatsRepository.chats
    val searchList: LiveData<List<Chat>> = _searchList
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun getChats() {
        _isLoading.value = true
        firestoreRepository.getChats(userID, createdAt).addOnSuccessListener {snapshot ->
            snapshot.forEach {document ->
                val users = document[USERS] as ArrayList<*>
                users.remove(userID)
                val chatUserID = users[0] as String
                val chatCreatedAt = document[CREATED_AT] as Long
                firestoreRepository.getUserData(chatUserID).addOnSuccessListener {
                    val userAvatarURL = it[AVATAR_URL] as String
                    val userName = it[NAME] as String
                    viewModelScope.launch {
                        chatsRepository.insert(
                            Chat(
                                document.id,
                                chatUserID,
                                userAvatarURL,
                                userName,
                                createdAt = chatCreatedAt
                            )
                        )
                    }
                }
            }
            if (!snapshot.isEmpty) {
                createdAt = snapshot.documents[snapshot.size() - 1][CREATED_AT] as Long
                preferences.edit {
                    putLong(CHATS, createdAt)
                }
            }
            _isLoading.value = false
        }
    }
    
    fun search(query: String) {
        val searchResults = mutableListOf<Chat>()
        chats.value?.forEach {
            if (it.userName.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) {
                searchResults.add(it)
            }
        }
        _searchList.value = searchResults
    }
    
    fun signOut(): Boolean {
        firestoreRepository.updateUserData(
            userID, mapOf(
                IS_ONLINE to false,
                LAST_SEEN_AT to millis(),
                CURRENT_CHAT to "Null"
            )
        )
        preferences.edit {
            clear()
        }
        viewModelScope.launch {
            chatsRepository.clear()
            usersRepository.clear()
            messagesRepository.clear()
        }
        authRepository.signOut()
        return authRepository.getUser() == null
    }
}
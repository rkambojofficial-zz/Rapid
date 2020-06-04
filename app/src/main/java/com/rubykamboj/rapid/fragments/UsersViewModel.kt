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
import com.rubykamboj.rapid.data.models.User
import com.rubykamboj.rapid.data.repositories.AuthRepository
import com.rubykamboj.rapid.data.repositories.ChatsRepository
import com.rubykamboj.rapid.data.repositories.FirestoreRepository
import com.rubykamboj.rapid.data.repositories.UsersRepository
import com.rubykamboj.rapid.utils.AVATAR_URL
import com.rubykamboj.rapid.utils.CREATED_AT
import com.rubykamboj.rapid.utils.EMAIL
import com.rubykamboj.rapid.utils.NAME
import com.rubykamboj.rapid.utils.USERS
import com.rubykamboj.rapid.utils.millis
import kotlinx.coroutines.launch
import java.util.*

class UsersViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authRepository = AuthRepository(Firebase.auth)
    private val firestoreRepository = FirestoreRepository(Firebase.firestore)
    private val userID = authRepository.getUser()!!.uid
    private val rapidDatabase = RapidDatabase.getDatabase(application)
    private val usersRepository = UsersRepository(rapidDatabase.usersDao())
    private val chatsRepository = ChatsRepository(rapidDatabase.chatsDao())
    private val preferences = PreferenceManager.getDefaultSharedPreferences(application)
    private var createdAt = preferences.getLong(USERS, 0)
    private val _searchList = MutableLiveData<List<User>>()
    private val _chat = MutableLiveData<Chat>()
    private val _isLoading = MutableLiveData<Boolean>()
    val users = usersRepository.users
    val searchList: LiveData<List<User>> = _searchList
    val chat: LiveData<Chat> = _chat
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        _isLoading.value = true
        firestoreRepository.getUsers(createdAt).addOnSuccessListener {snapshot ->
            snapshot.forEach {
                if (it.id == userID) {
                    return@forEach
                }
                val avatarURL = it[AVATAR_URL] as String
                val name = it[NAME] as String
                val email = it[EMAIL] as String
                val userCreatedAt = it[CREATED_AT] as Long
                viewModelScope.launch {
                    usersRepository.insert(
                        User(it.id, avatarURL, name, email, userCreatedAt)
                    )
                }
            }
            if (!snapshot.isEmpty) {
                createdAt = snapshot.documents[snapshot.size() - 1][CREATED_AT] as Long
                preferences.edit {
                    putLong(USERS, createdAt)
                }
            }
            _isLoading.value = false
        }
    }
    
    fun startChat(user: User) {
        _isLoading.value = true
        viewModelScope.launch {
            if (chatsRepository.getChatByUserID(user.id) != null) {
                _chat.value = chatsRepository.getChatByUserID(user.id)
            } else {
                firestoreRepository.addChat(
                    mapOf(
                        USERS to listOf(userID, user.id),
                        CREATED_AT to millis()
                    )
                ).addOnSuccessListener {
                    _chat.value = Chat(it.id, user.id, user.avatarURL, user.name)
                }
            }
        }
    }
    
    fun search(query: String) {
        val searchResults = mutableListOf<User>()
        users.value?.forEach {
            if (it.name.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)) || it.email.contains(query)) {
                searchResults.add(it)
            }
        }
        _searchList.value = searchResults
    }
}
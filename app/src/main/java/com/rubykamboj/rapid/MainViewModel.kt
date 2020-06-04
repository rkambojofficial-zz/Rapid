package com.rubykamboj.rapid

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.rubykamboj.rapid.data.repositories.AuthRepository
import com.rubykamboj.rapid.data.repositories.FirestoreRepository
import com.rubykamboj.rapid.utils.AVATAR_URL
import com.rubykamboj.rapid.utils.CREATED_AT
import com.rubykamboj.rapid.utils.CURRENT_CHAT
import com.rubykamboj.rapid.utils.EMAIL
import com.rubykamboj.rapid.utils.IS_ONLINE
import com.rubykamboj.rapid.utils.LAST_SEEN_AT
import com.rubykamboj.rapid.utils.NAME
import com.rubykamboj.rapid.utils.REGISTRATION_TOKEN
import com.rubykamboj.rapid.utils.millis

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authRepository = AuthRepository(Firebase.auth)
    private val firestoreRepository = FirestoreRepository(Firebase.firestore)
    private val firebaseInstanceId = FirebaseInstanceId.getInstance()
    private val _user = MutableLiveData<FirebaseUser>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _isSuccessful = MutableLiveData<Boolean>()
    private val _isResetEmailSent = MutableLiveData<Boolean>()
    val user: LiveData<FirebaseUser> = _user
    val isLoading: LiveData<Boolean> = _isLoading
    val isSuccessful: LiveData<Boolean> = _isSuccessful
    val isResetEmailSent: LiveData<Boolean> = _isResetEmailSent
    var failureMessage: String? = null
    
    fun signIn(email: String, password: String) {
        _isLoading.value = true
        authRepository.signIn(email, password).addOnCompleteListener {
            _isLoading.value = false
            if (it.isSuccessful) {
                val userID = it.result!!.user!!.uid
                firebaseInstanceId.instanceId.addOnSuccessListener {result ->
                    firestoreRepository.updateUserData(
                        userID, mapOf(
                            REGISTRATION_TOKEN to result.token
                        )
                    )
                    _isSuccessful.value = true
                }
            } else {
                failureMessage = it.exception?.message
                _isSuccessful.value = false
            }
        }
    }
    
    fun resetPassword(email: String) {
        _isLoading.value = true
        authRepository.resetPassword(email).addOnCompleteListener {
            _isLoading.value = false
            _isResetEmailSent.value = if (it.isSuccessful) {
                true
            } else {
                failureMessage = it.exception?.message
                false
            }
        }
    }
    
    fun signUp(name: String, email: String, password: String) {
        _isLoading.value = true
        authRepository.signUp(email, password).addOnCompleteListener {
            _isLoading.value = false
            if (it.isSuccessful) {
                val userID = it.result!!.user!!.uid
                firebaseInstanceId.instanceId.addOnSuccessListener {result ->
                    firestoreRepository.setUserData(
                        userID, mapOf(
                            AVATAR_URL to "",
                            NAME to name,
                            EMAIL to email,
                            IS_ONLINE to false,
                            LAST_SEEN_AT to millis(),
                            CURRENT_CHAT to "Null",
                            REGISTRATION_TOKEN to result.token,
                            CREATED_AT to millis()
                        )
                    )
                    _isSuccessful.value = true
                }
            } else {
                failureMessage = it.exception?.message
                _isSuccessful.value = false
            }
        }
    }
    
    fun getUser() {
        _user.value = authRepository.getUser()
    }
}
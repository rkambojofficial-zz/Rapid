package com.rubykamboj.rapid.data.repositories

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository(private val auth: FirebaseAuth) {
    
    fun signIn(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }
    
    fun resetPassword(email: String): Task<Void> {
        return auth.sendPasswordResetEmail(email)
    }
    
    fun signUp(email: String, password: String): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email, password)
    }
    
    fun signOut() {
        auth.signOut()
    }
    
    fun getUser(): FirebaseUser? {
        return auth.currentUser
    }
}
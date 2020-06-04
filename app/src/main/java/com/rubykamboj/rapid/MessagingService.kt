package com.rubykamboj.rapid

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService

class MessagingService : FirebaseMessagingService() {
    
    override fun onNewToken(token: String) {
        Log.i("TAG", "onNewToken: $token")
    }
}
package com.rubykamboj.rapid.fragments

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rubykamboj.rapid.data.models.Chat

class MessagesViewModelFactory(private val application: Application, private val chat: Chat) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MessagesViewModel(application, chat) as T
    }
}
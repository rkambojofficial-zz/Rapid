package com.rubykamboj.rapid.data.repositories

import com.rubykamboj.rapid.data.daos.ChatsDao
import com.rubykamboj.rapid.data.models.Chat

class ChatsRepository(private val chatsDao: ChatsDao) {
    
    val chats = chatsDao.chats()
    
    suspend fun insert(chat: Chat) {
        chatsDao.insert(chat)
    }
    
    suspend fun update(chat: Chat) {
        chatsDao.update(chat)
    }
    
    suspend fun update(id: String, body: String, count: Long, updatedAt: Long) {
        chatsDao.update(id, body, count, updatedAt)
    }
    
    suspend fun delete(chat: Chat) {
        chatsDao.delete(chat)
    }
    
    suspend fun clear() {
        chatsDao.clear()
    }
    
    suspend fun getChat(id: String): Chat? {
        return chatsDao.getChat(id)
    }
    
    suspend fun getChatByUserID(id: String): Chat? {
        return chatsDao.getChatByUserID(id)
    }
}
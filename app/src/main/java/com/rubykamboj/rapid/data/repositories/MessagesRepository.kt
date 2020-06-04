package com.rubykamboj.rapid.data.repositories

import androidx.lifecycle.LiveData
import com.rubykamboj.rapid.data.daos.MessagesDao
import com.rubykamboj.rapid.data.models.Message

class MessagesRepository(private val messagesDao: MessagesDao) {
    
    suspend fun insert(message: Message) {
        messagesDao.insert(message)
    }
    
    suspend fun update(id: String) {
        messagesDao.update(id)
    }
    
    suspend fun update(id: String, body: String) {
        messagesDao.update(id, body)
    }
    
    suspend fun delete(id: String) {
        messagesDao.delete(id)
    }
    
    suspend fun seenAll(chatID: String, senderID: String, createdAt: Long) {
        messagesDao.seenAll(chatID, senderID, createdAt)
    }
    
    suspend fun clear() {
        messagesDao.clear()
    }
    
    suspend fun clear(chatID: String) {
        messagesDao.clear(chatID)
    }
    
    fun messages(chatID: String): LiveData<List<Message>> {
        return messagesDao.messages(chatID)
    }
    
    suspend fun unseenMessages(chatID: String, receiverID: String): List<Message> {
        return messagesDao.unseenMessages(chatID, receiverID)
    }
}
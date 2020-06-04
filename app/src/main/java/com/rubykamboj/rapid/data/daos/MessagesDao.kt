package com.rubykamboj.rapid.data.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rubykamboj.rapid.data.models.Message

@Dao
interface MessagesDao {
    
    @Insert
    suspend fun insert(message: Message)
    
    @Query("UPDATE messages SET isSeen = 1 WHERE id = :id")
    suspend fun update(id: String)
    
    @Query("UPDATE messages SET body = :body, isChanged = 1 WHERE id = :id")
    suspend fun update(id: String, body: String)
    
    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun delete(id: String)
    
    @Query("UPDATE messages SET isSeen = 1 WHERE chatID = :chatID AND senderID = :senderID AND isSeen = 0 AND createdAt <= :createdAt")
    suspend fun seenAll(chatID: String, senderID: String, createdAt: Long)
    
    @Query("DELETE FROM messages")
    suspend fun clear()
    
    @Query("DELETE FROM messages WHERE chatID = :chatID")
    suspend fun clear(chatID: String)
    
    @Query("SELECT * FROM messages WHERE chatID = :chatID ORDER BY createdAt")
    fun messages(chatID: String): LiveData<List<Message>>
    
    @Query("SELECT * FROM messages WHERE chatID = :chatID AND receiverID = :receiverID AND isSeen = 0")
    suspend fun unseenMessages(chatID: String, receiverID: String): List<Message>
}
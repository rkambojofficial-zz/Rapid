package com.rubykamboj.rapid.data.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rubykamboj.rapid.data.models.Chat

@Dao
interface ChatsDao {
    
    @Insert
    suspend fun insert(chat: Chat)
    
    @Update
    suspend fun update(chat: Chat)
    
    @Query("UPDATE chats SET lastMessageBody = :body, unseenMessagesCount = :count, updatedAt = :updatedAt WHERE id = :id")
    suspend fun update(id: String, body: String, count: Long, updatedAt: Long)
    
    @Delete
    suspend fun delete(chat: Chat)
    
    @Query("DELETE FROM chats")
    suspend fun clear()
    
    @Query("SELECT * FROM chats WHERE id = :id")
    suspend fun getChat(id: String): Chat?
    
    @Query("SELECT * FROM chats WHERE userID = :userID")
    suspend fun getChatByUserID(userID: String): Chat?
    
    @Query("SELECT * FROM chats ORDER BY updatedAt DESC")
    fun chats(): LiveData<List<Chat>>
}
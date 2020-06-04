package com.rubykamboj.rapid.data.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rubykamboj.rapid.data.models.User

@Dao
interface UsersDao {
    
    @Insert
    suspend fun insert(user: User)
    
    @Update
    suspend fun update(user: User)
    
    @Delete
    suspend fun delete(user: User)
    
    @Query("DELETE FROM users")
    suspend fun clear()
    
    @Query("SELECT * FROM users ORDER BY name")
    fun users(): LiveData<List<User>>
}
package com.rubykamboj.rapid.data

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rubykamboj.rapid.data.daos.ChatsDao
import com.rubykamboj.rapid.data.daos.MessagesDao
import com.rubykamboj.rapid.data.daos.UsersDao
import com.rubykamboj.rapid.data.models.Chat
import com.rubykamboj.rapid.data.models.Message
import com.rubykamboj.rapid.data.models.User

@Database(entities = [User::class, Chat::class, Message::class], version = 1, exportSchema = false)
abstract class RapidDatabase : RoomDatabase() {
    
    abstract fun usersDao(): UsersDao
    
    abstract fun chatsDao(): ChatsDao
    
    abstract fun messagesDao(): MessagesDao
    
    companion object {
        
        private var rapidDatabase: RapidDatabase? = null
        
        fun getDatabase(application: Application): RapidDatabase {
            synchronized(this) {
                if (rapidDatabase == null) {
                    rapidDatabase = Room.databaseBuilder(
                        application,
                        RapidDatabase::class.java,
                        "Rapid"
                    ).fallbackToDestructiveMigration().build()
                }
                return rapidDatabase as RapidDatabase
            }
        }
    }
}
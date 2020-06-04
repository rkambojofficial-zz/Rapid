package com.rubykamboj.rapid.data.repositories

import com.rubykamboj.rapid.data.daos.UsersDao
import com.rubykamboj.rapid.data.models.User

class UsersRepository(private val usersDao: UsersDao) {
    
    val users = usersDao.users()
    
    suspend fun insert(user: User) {
        usersDao.insert(user)
    }
    
    suspend fun update(user: User) {
        usersDao.update(user)
    }
    
    suspend fun delete(user: User) {
        usersDao.delete(user)
    }
    
    suspend fun clear() {
        usersDao.clear()
    }
}
package com.rubykamboj.rapid.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val avatarURL: String,
    val name: String,
    val email: String,
    val createdAt: Long
)
package com.rubykamboj.rapid.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rubykamboj.rapid.utils.millis

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    val id: String,
    val chatID: String,
    val senderID: String,
    val receiverID: String,
    val body: String,
    val isSeen: Boolean = false,
    val isChanged: Boolean = false,
    val createdAt: Long = millis()
)
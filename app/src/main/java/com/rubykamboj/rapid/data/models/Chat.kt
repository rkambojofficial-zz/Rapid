package com.rubykamboj.rapid.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rubykamboj.rapid.utils.millis
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "chats")
@Parcelize
data class Chat(
    @PrimaryKey
    val id: String,
    val userID: String,
    val userAvatarURL: String,
    val userName: String,
    val lastMessageBody: String = "",
    val unseenMessagesCount: Long = 0,
    val createdAt: Long = millis(),
    val updatedAt: Long = millis()
) : Parcelable
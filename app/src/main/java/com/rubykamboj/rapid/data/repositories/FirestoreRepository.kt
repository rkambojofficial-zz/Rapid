package com.rubykamboj.rapid.data.repositories

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.rubykamboj.rapid.utils.CHATS
import com.rubykamboj.rapid.utils.CREATED_AT
import com.rubykamboj.rapid.utils.RECEIVER_ID
import com.rubykamboj.rapid.utils.UPDATES
import com.rubykamboj.rapid.utils.USERS

class FirestoreRepository(private val firestore: FirebaseFirestore) {
    
    fun setUserData(id: String, data: Map<String, Any>): Task<Void> {
        return firestore.collection(USERS)
            .document(id)
            .set(data)
    }
    
    fun getUserData(id: String): Task<DocumentSnapshot> {
        return firestore.collection(USERS)
            .document(id)
            .get()
    }
    
    fun updateUserData(id: String, data: Map<String, Any>): Task<Void> {
        return firestore.collection(USERS)
            .document(id)
            .update(data)
    }
    
    fun getChats(userID: String, createdAt: Long): Task<QuerySnapshot> {
        return firestore.collection(CHATS)
            .whereArrayContains(USERS, userID)
            .whereGreaterThan(CREATED_AT, createdAt)
            .get()
    }
    
    fun getUsers(createdAt: Long): Task<QuerySnapshot> {
        return firestore.collection(USERS)
            .whereGreaterThan(CREATED_AT, createdAt)
            .get()
    }
    
    fun userStatus(id: String): DocumentReference {
        return firestore.collection(USERS)
            .document(id)
    }
    
    fun chatUpdates(id: String, receiverID: String, createdAt: Long): Query {
        return firestore.collection(CHATS)
            .document(id)
            .collection(UPDATES)
            .whereEqualTo(RECEIVER_ID, receiverID)
            .whereGreaterThan(CREATED_AT, createdAt)
    }
    
    fun updates(id: String, createdAt: Long): Query {
        return firestore.collection(USERS)
            .document(id)
            .collection(UPDATES)
            .whereGreaterThan(CREATED_AT, createdAt)
    }
    
    fun addChatUpdate(id: String, data: Map<String, Any>): Task<DocumentReference> {
        return firestore.collection(CHATS)
            .document(id)
            .collection(UPDATES)
            .add(data)
    }
    
    fun addUpdate(id: String, data: Map<String, Any>): Task<DocumentReference> {
        return firestore.collection(USERS)
            .document(id)
            .collection(UPDATES)
            .add(data)
    }
    
    fun deleteChatUpdate(id: String, updateID: String): Task<Void> {
        return firestore.collection(CHATS)
            .document(id)
            .collection(UPDATES)
            .document(updateID)
            .delete()
    }
    
    fun deleteUpdate(id: String, updateID: String): Task<Void> {
        return firestore.collection(USERS)
            .document(id)
            .collection(UPDATES)
            .document(updateID)
            .delete()
    }
    
    fun addChat(data: Map<String, Any>): Task<DocumentReference> {
        return firestore.collection(CHATS)
            .add(data)
    }
    
    fun getChat(users: List<String>): Task<QuerySnapshot> {
        return firestore.collection(CHATS)
            .whereEqualTo("users.0", users[0])
            .whereEqualTo("users.1", users[1])
            .limit(1)
            .get()
    }
}
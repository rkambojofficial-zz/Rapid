package com.rubykamboj.rapid.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rubykamboj.rapid.data.models.Chat
import com.rubykamboj.rapid.databinding.ItemChatBinding

class ChatAdapter(
    private val onClick: OnClickChat
) : ListAdapter<Chat, ChatAdapter.ChatViewHolder>(ChatDiffUtil()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(
            ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onClick
        )
    }
    
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ChatViewHolder(
        private val binding: ItemChatBinding,
        private val onClick: OnClickChat
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(chat: Chat) {
            binding.chat = chat
            binding.onClick = onClick
            binding.executePendingBindings()
        }
    }
    
    interface OnClickChat {
        
        fun onClick(chat: Chat)
    }
    
    class ChatDiffUtil : DiffUtil.ItemCallback<Chat>() {
        
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }
    }
}
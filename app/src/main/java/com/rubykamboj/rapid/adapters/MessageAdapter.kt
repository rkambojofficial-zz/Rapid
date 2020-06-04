package com.rubykamboj.rapid.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rubykamboj.rapid.data.models.Message
import com.rubykamboj.rapid.databinding.ItemMessageEndBinding
import com.rubykamboj.rapid.databinding.ItemMessageStartBinding

class MessageAdapter(
    private val onClick: OnClickMessage,
    private val chatUserID: String
) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffUtil()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            StartMessageViewHolder(
                ItemMessageStartBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                onClick
            )
        } else {
            EndMessageViewHolder(
                ItemMessageEndBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                onClick
            )
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == 0) {
            (holder as StartMessageViewHolder).bind(getItem(position))
        } else {
            (holder as EndMessageViewHolder).bind(getItem(position))
        }
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderID == chatUserID) {
            0
        } else {
            1
        }
    }
    
    class StartMessageViewHolder(
        private val binding: ItemMessageStartBinding,
        private val onClick: OnClickMessage
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(message: Message) {
            binding.message = message
            binding.onClick = onClick
            binding.executePendingBindings()
        }
    }
    
    class EndMessageViewHolder(
        private val binding: ItemMessageEndBinding,
        private val onClick: OnClickMessage
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(message: Message) {
            binding.message = message
            binding.onClick = onClick
            binding.executePendingBindings()
        }
    }
    
    interface OnClickMessage {
        
        fun onClick(message: Message)
    }
    
    class MessageDiffUtil : DiffUtil.ItemCallback<Message>() {
        
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}
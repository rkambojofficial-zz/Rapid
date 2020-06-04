package com.rubykamboj.rapid.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rubykamboj.rapid.data.models.User
import com.rubykamboj.rapid.databinding.ItemUserBinding

class UserAdapter(
    private val onClick: OnClickUser
) : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffUtil()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
            ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onClick
        )
    }
    
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val onClick: OnClickUser
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(user: User) {
            binding.user = user
            binding.onClick = onClick
            binding.executePendingBindings()
        }
    }
    
    interface OnClickUser {
        
        fun onClick(user: User)
    }
    
    class UserDiffUtil : DiffUtil.ItemCallback<User>() {
        
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
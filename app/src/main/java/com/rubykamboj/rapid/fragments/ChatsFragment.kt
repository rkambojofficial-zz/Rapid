package com.rubykamboj.rapid.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rubykamboj.rapid.MainActivity
import com.rubykamboj.rapid.R
import com.rubykamboj.rapid.adapters.ChatAdapter
import com.rubykamboj.rapid.data.models.Chat
import com.rubykamboj.rapid.databinding.FragmentChatsBinding

class ChatsFragment : Fragment(), ChatAdapter.OnClickChat, SearchView.OnQueryTextListener {
    
    private lateinit var binding: FragmentChatsBinding
    private lateinit var viewModel: ChatsViewModel
    private lateinit var chatAdapter: ChatAdapter
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chats, container, false)
        viewModel = ViewModelProvider(this).get(ChatsViewModel::class.java)
        chatAdapter = ChatAdapter(this)
        setHasOptionsMenu(true)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.recyclerView.adapter = chatAdapter
        binding.fabToUsers.setOnClickListener {
            findNavController().navigate(
                ChatsFragmentDirections.actionFragmentChatsToFragmentUsers()
            )
        }
        viewModel.chats.observe(viewLifecycleOwner, Observer {
            chatAdapter.submitList(it)
        })
        viewModel.searchList.observe(viewLifecycleOwner, Observer {
            chatAdapter.submitList(it)
        })
        return binding.root
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_chats, menu)
        val searchItem = menu.findItem(R.id.item_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.item_sign_out) {
            MaterialAlertDialogBuilder(context).apply {
                setTitle(R.string.sign_out)
                setMessage(R.string.sign_out_warning)
                setPositiveButton(R.string.ok) {_, _ ->
                    if (viewModel.signOut()) {
                        Toast.makeText(context, R.string.signed_out_successfully, Toast.LENGTH_SHORT).show()
                        startActivity(Intent(context, MainActivity::class.java))
                        requireActivity().finish()
                    }
                }
                setNegativeButton(R.string.cancel, null)
            }.show()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
    
    override fun onStart() {
        super.onStart()
        viewModel.getChats()
    }
    
    override fun onClick(chat: Chat) {
        findNavController().navigate(
            ChatsFragmentDirections.actionFragmentChatsToFragmentMessages(chat.userName, chat)
        )
    }
    
    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }
    
    override fun onQueryTextChange(newText: String): Boolean {
        viewModel.search(newText)
        return true
    }
}
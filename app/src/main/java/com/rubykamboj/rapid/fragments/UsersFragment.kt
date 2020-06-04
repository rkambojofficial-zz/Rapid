package com.rubykamboj.rapid.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rubykamboj.rapid.R
import com.rubykamboj.rapid.adapters.UserAdapter
import com.rubykamboj.rapid.data.models.User
import com.rubykamboj.rapid.databinding.FragmentUsersBinding

class UsersFragment : Fragment(), UserAdapter.OnClickUser, SearchView.OnQueryTextListener {
    
    private lateinit var binding: FragmentUsersBinding
    private lateinit var viewModel: UsersViewModel
    private lateinit var userAdapter: UserAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_users, container, false)
        viewModel = ViewModelProvider(this).get(UsersViewModel::class.java)
        userAdapter = UserAdapter(this)
        setHasOptionsMenu(true)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.recyclerView.adapter = userAdapter
        viewModel.users.observe(viewLifecycleOwner, Observer {
            userAdapter.submitList(it)
        })
        viewModel.searchList.observe(viewLifecycleOwner, Observer {
            userAdapter.submitList(it)
        })
        viewModel.chat.observe(viewLifecycleOwner, Observer {
            findNavController().navigate(
                UsersFragmentDirections.actionFragmentUsersToFragmentMessages(it.userName, it)
            )
        })
        return binding.root
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_users, menu)
        val searchItem = menu.findItem(R.id.item_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
    }
    
    override fun onClick(user: User) {
        MaterialAlertDialogBuilder(context).apply {
            setTitle(user.name)
            setPositiveButton(R.string.start_chat) {_, _ ->
                viewModel.startChat(user)
            }
            setNegativeButton(R.string.cancel, null)
        }.show()
    }
    
    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }
    
    override fun onQueryTextChange(newText: String): Boolean {
        viewModel.search(newText)
        return true
    }
}
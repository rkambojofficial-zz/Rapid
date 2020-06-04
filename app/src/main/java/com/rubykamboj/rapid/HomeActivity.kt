package com.rubykamboj.rapid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.rubykamboj.rapid.databinding.ActivityHomeBinding
import com.rubykamboj.rapid.fragments.ChatsFragmentDirections
import com.rubykamboj.rapid.utils.CHAT_ID

class HomeActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var navController: NavController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        navController = findNavController(R.id.fragment_host)
        binding.lifecycleOwner = this
        setupActionBarWithNavController(navController)
        navController.addOnDestinationChangedListener {_, destination, _ ->
            if (destination.id != R.id.fragment_messages) {
                supportActionBar!!.subtitle = null
            }
        }
        viewModel.chat.observe(this, Observer {
            navController.navigate(
                ChatsFragmentDirections.actionFragmentChatsToFragmentMessages(it.userName, it)
            )
        })
        if (intent.hasExtra(CHAT_ID)) {
            val chatID = intent.getStringExtra(CHAT_ID)!!
            viewModel.openChat(chatID)
        }
    }
    
    override fun onStart() {
        viewModel.online()
        super.onStart()
    }
    
    override fun onStop() {
        viewModel.offline()
        super.onStop()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
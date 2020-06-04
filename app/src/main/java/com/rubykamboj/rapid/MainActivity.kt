package com.rubykamboj.rapid

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rubykamboj.rapid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.buttonToSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
        binding.buttonToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        viewModel.user.observe(this, Observer {
            if (it != null) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
        })
    }
    
    override fun onStart() {
        super.onStart()
        viewModel.getUser()
    }
}
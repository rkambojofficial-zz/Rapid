package com.rubykamboj.rapid

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rubykamboj.rapid.databinding.ActivitySignUpBinding
import com.rubykamboj.rapid.utils.isEmail
import com.rubykamboj.rapid.utils.isNotEmail
import com.rubykamboj.rapid.utils.isNotPassword
import com.rubykamboj.rapid.utils.isPassword

class SignUpActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.buttonSignUp.setOnClickListener {
            val name = binding.inputLayoutName.editText!!.text.toString()
            val email = binding.inputLayoutEmail.editText!!.text.toString()
            val password = binding.inputLayoutPassword.editText!!.text.toString()
            binding.inputLayoutName.error = if (name.isBlank()) {
                getString(R.string.name_is_required)
            } else {
                binding.inputLayoutName.isErrorEnabled = false
                null
            }
            binding.inputLayoutEmail.error = when {
                email.isBlank() -> getString(R.string.email_is_required)
                email.isNotEmail() -> getString(R.string.email_is_invalid)
                else -> {
                    binding.inputLayoutEmail.isErrorEnabled = false
                    null
                }
            }
            binding.inputLayoutPassword.error = when {
                password.isBlank() -> getString(R.string.password_is_required)
                password.isNotPassword() -> getString(R.string.password_is_short)
                else -> {
                    binding.inputLayoutPassword.isErrorEnabled = false
                    null
                }
            }
            if (name.isNotBlank() && email.isEmail() && password.isPassword()) {
                viewModel.signUp(name, email, password)
            }
        }
        viewModel.isSuccessful.observe(this, Observer {
            if (it) {
                Toast.makeText(this, R.string.signed_up_successfully, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                if (viewModel.failureMessage.isNullOrBlank()) {
                    Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, viewModel.failureMessage, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
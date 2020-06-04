package com.rubykamboj.rapid

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rubykamboj.rapid.databinding.ActivitySignInBinding
import com.rubykamboj.rapid.utils.isEmail
import com.rubykamboj.rapid.utils.isNotEmail
import com.rubykamboj.rapid.utils.isNotPassword
import com.rubykamboj.rapid.utils.isPassword

class SignInActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySignInBinding
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.buttonSignIn.setOnClickListener {
            val email = binding.inputLayoutEmail.editText!!.text.toString()
            val password = binding.inputLayoutPassword.editText!!.text.toString()
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
            if (email.isEmail() && password.isPassword()) {
                viewModel.signIn(email, password)
            }
        }
        binding.buttonResetPassword.setOnClickListener {
            val email = binding.inputLayoutEmail.editText!!.text.toString()
            binding.inputLayoutEmail.error = when {
                email.isBlank() -> getString(R.string.email_is_required)
                email.isNotEmail() -> getString(R.string.email_is_invalid)
                else -> {
                    binding.inputLayoutEmail.isErrorEnabled = false
                    null
                }
            }
            if (email.isEmail()) {
                viewModel.resetPassword(email)
            }
        }
        viewModel.isSuccessful.observe(this, Observer {
            if (it) {
                Toast.makeText(this, R.string.signed_in_successfully, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                if (viewModel.failureMessage.isNullOrBlank()) {
                    Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, viewModel.failureMessage, Toast.LENGTH_SHORT).show()
                }
            }
        })
        viewModel.isResetEmailSent.observe(this, Observer {
            if (it) {
                Toast.makeText(this, R.string.email_sent_successfully, Toast.LENGTH_SHORT).show()
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
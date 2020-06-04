package com.rubykamboj.rapid.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.rubykamboj.rapid.HomeActivity
import com.rubykamboj.rapid.R
import com.rubykamboj.rapid.adapters.MessageAdapter
import com.rubykamboj.rapid.data.models.Chat
import com.rubykamboj.rapid.data.models.Message
import com.rubykamboj.rapid.databinding.FragmentMessagesBinding
import com.rubykamboj.rapid.utils.CHAT
import com.rubykamboj.rapid.utils.date
import java.util.*

class MessagesFragment : Fragment(), MessageAdapter.OnClickMessage {
    
    private lateinit var binding: FragmentMessagesBinding
    private lateinit var chat: Chat
    private lateinit var viewModelFactory: MessagesViewModelFactory
    private lateinit var viewModel: MessagesViewModel
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var homeActivity: HomeActivity
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_messages, container, false)
        chat = requireArguments().getParcelable(CHAT)!!
        viewModelFactory = MessagesViewModelFactory(requireActivity().application, chat)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MessagesViewModel::class.java)
        messageAdapter = MessageAdapter(this, chat.userID)
        homeActivity = requireActivity() as HomeActivity
        setHasOptionsMenu(true)
        binding.recyclerView.adapter = messageAdapter
        binding.inputBody.setOnEditorActionListener {_, actionId, _ ->
            return@setOnEditorActionListener if (actionId == EditorInfo.IME_ACTION_SEND) {
                val body = binding.inputBody.text.toString()
                if (body.isNotBlank()) {
                    viewModel.addMessage(body)
                    binding.inputBody.text = null
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
        viewModel.messages.observe(viewLifecycleOwner, Observer {
            messageAdapter.submitList(it)
            viewModel.listen()
            if (it.isNotEmpty()) {
                binding.recyclerView.smoothScrollToPosition(it.size - 1)
            }
        })
        viewModel.isUserOnline.observe(viewLifecycleOwner, Observer {
            if (it) {
                homeActivity.supportActionBar!!.subtitle = "Online"
            } else {
                val date = date("dd/MMM/yyyy", viewModel.userLastSeenAt!!)
                val time = date("hh:mm a", viewModel.userLastSeenAt!!)
                val today = date("dd/MMM/yyyy")
                homeActivity.supportActionBar!!.subtitle = if (date == today) {
                    "Last seen at $time"
                } else {
                    "Last seen on $date"
                }
            }
        })
        return binding.root
    }
    
    override fun onStart() {
        viewModel.addSeenAll()
        viewModel.setCurrentChat()
        super.onStart()
    }
    
    override fun onStop() {
        viewModel.updateChat()
        viewModel.setCurrentChat("Null")
        super.onStop()
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_messages, menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.item_clear_chat) {
            viewModel.clearChat()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
    
    override fun onClick(message: Message) {
        if (message.senderID == chat.userID) {
            MaterialAlertDialogBuilder(context).apply {
                setTitle(message.body)
                setPositiveButton(R.string.delete_for_me) {_, _ ->
                    viewModel.deleteMessage(message.id)
                }
                setNegativeButton(R.string.cancel, null)
            }.show()
        } else {
            MaterialAlertDialogBuilder(context).apply {
                setTitle(message.body)
                if (Date().time - message.createdAt <= 300000) {
                    setPositiveButton(R.string.delete_message) {_, _ ->
                        MaterialAlertDialogBuilder(context).apply {
                            setTitle(message.body)
                            setPositiveButton(R.string.delete_for_me) {_, _ ->
                                viewModel.deleteMessage(message.id)
                            }
                            setNegativeButton(R.string.cancel, null)
                            setNeutralButton(R.string.delete_for_everyone) {_, _ ->
                                viewModel.addDelete(message.id)
                            }
                        }.show()
                    }
                    setNeutralButton(R.string.change_message) {_, _ ->
                        if (!message.isChanged) {
                            val inputBody = TextInputEditText(context)
                            inputBody.setHint(R.string.type_a_message)
                            inputBody.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                            inputBody.isSingleLine = true
                            inputBody.setText(message.body)
                            MaterialAlertDialogBuilder(context).apply {
                                setTitle(R.string.change_message)
                                setView(inputBody)
                                setPositiveButton(R.string.ok) {_, _ ->
                                    val body = inputBody.text.toString()
                                    if (body.isNotBlank()) {
                                        viewModel.addChange(message.id, body)
                                    }
                                }
                                setNegativeButton(R.string.cancel, null)
                            }.show()
                        } else {
                            Toast.makeText(context, R.string.changed_once, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    setPositiveButton(R.string.delete_for_me) {_, _ ->
                        viewModel.deleteMessage(message.id)
                    }
                }
                setNegativeButton(R.string.cancel, null)
            }.show()
        }
    }
}
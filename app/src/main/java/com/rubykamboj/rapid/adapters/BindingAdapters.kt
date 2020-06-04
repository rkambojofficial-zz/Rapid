package com.rubykamboj.rapid.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.rubykamboj.rapid.R
import com.rubykamboj.rapid.utils.date
import com.squareup.picasso.Picasso

@BindingAdapter("visible")
fun View.visible(visible: Boolean) {
    visibility = if (visible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

@BindingAdapter("avatarURL")
fun ImageView.avatarURL(avatarURL: String) {
    if (avatarURL.isNotBlank()) {
        Picasso.get().load(avatarURL).placeholder(R.drawable.ic_avatar).into(this)
    } else {
        setImageResource(R.drawable.ic_avatar)
    }
}

@BindingAdapter("createdAt")
fun TextView.createdAt(createdAt: Long) {
    val date = date("dd/MMM/yyyy", createdAt)
    val time = date("hh:mm a", createdAt)
    val today = date("dd/MMM/yyyy")
    text = if (date == today) {
        time
    } else {
        date
    }
}
package com.rubykamboj.rapid.utils

import android.util.Patterns
import java.text.SimpleDateFormat
import java.util.*

fun String.isEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isNotEmail(): Boolean {
    return !Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isPassword(): Boolean {
    return this.length >= 6
}

fun String.isNotPassword(): Boolean {
    return this.length < 6
}

fun date(pattern: String, date: Long = millis()): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
}

fun millis(): Long {
    return Date().time
}
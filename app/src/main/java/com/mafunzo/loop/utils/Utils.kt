package com.mafunzo.loop.utils

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat

fun View.snackbar(message: String, action: (() -> Unit)? = null) {
    val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
    action?.let {
        snackbar.setAction("Retry") {
            it()
        }
    }
    snackbar.show()
}

private const val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"


fun validateEmail(email: String): Boolean {
    return email.matches(emailPattern.toRegex())
}

fun formatDate(date: String, originalFormat: String, expectedFormat: String): String {
    return try {
        val originalDateTime = SimpleDateFormat(originalFormat).parse(date)
        SimpleDateFormat(expectedFormat).format(originalDateTime!!)
    } catch (e: Exception) {
        date
    }
}

fun View.hideKeyboard() {
    val closeKeyboard =
        this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    closeKeyboard.hideSoftInputFromWindow(this.windowToken, 0)
}

fun EditText.showKeyboard() {
    if (requestFocus()) {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        setSelection(text.length)
    }
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.enable(enabled: Boolean) {
    this.isEnabled = enabled
}

fun View.visible(){
    this.visibility = View.VISIBLE
}

fun MaterialButton.hideProgress(text: String) {
    val button = this
    button.hideProgress(text)
}

fun MaterialButton.showProgress() {
    val button = this
    button.showProgress {
        progressColor = Color.WHITE
    }
}

fun getCurrentTimeInMillis(): Long {
    return System.currentTimeMillis()
}

// a function that takes time in milliseconds and returns formatted string of date and time e.g. 4 July 2022, 9:02:33 AM
fun Long.formatDateTime(): String {
    return try {
        val dateTime = SimpleDateFormat("dd MMM yyyy, hh:mm a").format(this)
        dateTime
    } catch (e: Exception) {
        ""
    }
}

// a function that takes time in milliseconds with first paramer as the start and second parameter as the end ...
// formats it to a string of time e.g. 8:30 AM to 10:30 AM ... assumes start and end are on the same day
fun formatStartEndTime(start: Long, end: Long): String {
    return try {
        val startTime = SimpleDateFormat("hh:mm a").format(start)
        val endTime = SimpleDateFormat("hh:mm a").format(end)
        "$startTime - $endTime"
    } catch (e: Exception) {
        ""
    }
}

//takes time in milliseconds and returns formatted string of date e.g. 4 July
fun formatMonthDay(date: Long): String {
    return try {
        val dateTime = SimpleDateFormat("dd MMM").format(date)
        dateTime
    } catch (e: Exception) {
        ""
    }
}

//convert a date string in format yyyy-MM-dd to time in milliseconds
fun String.convertDateToTimeInMillis(): Long {
    return try {
        val dateTime = SimpleDateFormat("yyyy-MM-dd").parse(this)
        dateTime.time
    } catch (e: Exception) {
        0
    }
}
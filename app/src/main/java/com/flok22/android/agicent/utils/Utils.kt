package com.flok22.android.agicent.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import org.ocpsoft.prettytime.PrettyTime
import java.sql.Date
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

fun makeToast(context: Context, msg: String, duration: Int) {
    Toast.makeText(context, msg, duration).show()
}

//For OTP
fun isOtpEntered(otpText: String, application: Application): Boolean {
    return if (otpText.isEmpty() || otpText.length < 4) {
        makeToast(application, "Please Enter OTP", Toast.LENGTH_SHORT)
        false
    } else true
}

fun Activity.setSystemBarTheme(pIsDark: Boolean) {
    val lFlags = window.decorView.systemUiVisibility
    window.decorView.systemUiVisibility = if (pIsDark) {
        lFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    } else {
        lFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}

fun Context.showToast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, length).show()
}

// fro date format
fun makeDateString(day: Int, month: Int, year: Int): String {
    if (month < 10) {
        val smallerMonth = "0$month"
        return ("$smallerMonth/$day/$year")
    }
    return ("$month/$day/$year")
}

// for sign-up page
fun validateFields(
    context: Context, name: Editable?, dOB: CharSequence?, gender: Editable?, bio: Editable?,
    imageUrl: String
): Boolean {
    return if (name.isNullOrEmpty()) {
        makeToast(context, "Please enter your name!", Toast.LENGTH_SHORT)
        false
    } else if (dOB.isNullOrEmpty()) {
        makeToast(context, "Please select your dob!", Toast.LENGTH_SHORT)
        false
    } else if (gender.isNullOrEmpty()) {
        makeToast(context, "Please select your gender!", Toast.LENGTH_SHORT)
        false
    } else if (bio.isNullOrEmpty()) {
        makeToast(context, "Please enter your bio!", Toast.LENGTH_SHORT)
        false
    } else if (imageUrl.isEmpty()) {
        makeToast(context, "Please upload your profile image!", Toast.LENGTH_SHORT)
        false
    } else true
}

fun Context.showNetworkToast() {
    showToast("Oops, your connection seems off… Keep calm, check your signal, and try again.")
}

fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    if (capabilities != null) {
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
            return true
        }
    }
    return false
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun getYearMonth(): String {
    val c = Calendar.getInstance()
    val monthName = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val month = monthName[c[Calendar.MONTH]]
    val year = c.get(Calendar.YEAR)
    return "$year/${month.subSequence(0, 3)}"
}

fun Context.hideKeyboard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(view.windowToken, 0)
}

fun getAge(month: Int, dayOfMonth: Int, year: Int): Int {
    return Period.between(
        LocalDate.of(year, month, dayOfMonth),
        LocalDate.now()
    ).years
}

fun getDOB(dob: String?): String {
    val format = SimpleDateFormat("MM/dd/yyyy", Locale.US)
    val date = dob?.let { format.parse(it) }
    val calendar = Calendar.getInstance()
    if (date != null) {
        calendar.time = date
    }
    val month = checkDigit(calendar.get(Calendar.MONTH) + 1)
    val dd = checkDigit(calendar.get(Calendar.DATE))
    val year = checkDigit(calendar.get(Calendar.YEAR))
    val age = getAge(month.toInt(), dd.toInt(), year.toInt())
    return age.toString()
}

fun checkDigit(number: Int): String {
    return if (number <= 9) "0$number" else number.toString()
}

fun getTiming(timeCreated: String): String? {
    val localDateTime = getLocalDateTime(timeCreated)
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val time = format.parse(localDateTime)?.time
    val prettyTime = PrettyTime(Locale.getDefault())
    return prettyTime.format(time?.let { Date(it) })
}

fun getTimeOffset(): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault())
    val timeZone = SimpleDateFormat("Z", Locale.getDefault()).format(calendar.time)
    return timeZone.substring(0, 3) + ":" + timeZone.substring(3, 5)
}

/*fun  getRemainingTime(date: String?): Long {
    //convert created date time into milliseconds
    val localDate = getLocalDateTime(date)
    val createdDateTime = getTimeInMilli(localDate)

    //convert today date time to milliseconds
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val currentDateAndTime = getTimeInMilli(sdf.format(Date()))

    //time difference in milliseconds
    return currentDateAndTime - createdDateTime
}*/

fun getRemainingTime(dateStr: String?): Long {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val currentDateAndTime = sdf.format(Date())
    val current = sdf.parse(currentDateAndTime)

    val dateTime = getLocalDateTime(dateStr)
    val createdDateTime = sdf.parse(dateTime)
    return current?.time?.minus(createdDateTime?.time!!) ?: 0

}

fun getTimeInMilli(date: String?): Long {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val localDate = LocalDateTime.parse(date, formatter)
    return localDate.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli()
}

fun getLocalDateTime(dateStr: String?): String {
    val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    df.timeZone = TimeZone.getTimeZone("UTC")
    val date = dateStr?.let { df.parse(it) }
    df.timeZone = TimeZone.getDefault()
    return df.format(date)
}

fun convertTo12Hours(militaryTime: String): String {
    //in => "14:00:00"
    //out => "02:00 PM"
    val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val outputFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault())
    val date = inputFormat.parse(militaryTime)
    return outputFormat.format(date).uppercase(Locale.getDefault())
}

//keytool -list -v -alias Flok22 -keystore C:\Users\Agicent Prashant\Desktop\flok22_keystore.jks

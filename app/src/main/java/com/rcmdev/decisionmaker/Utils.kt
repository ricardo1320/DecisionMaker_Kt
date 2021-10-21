package com.rcmdev.decisionmaker

import android.os.Build
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Date


object Utils {
    fun getDate(): String{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
        else
            SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(Date())
    }
}
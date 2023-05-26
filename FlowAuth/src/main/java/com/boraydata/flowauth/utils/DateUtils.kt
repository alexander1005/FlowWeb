package com.boraydata.flowauth.utils

import java.text.Format
import java.text.SimpleDateFormat
import java.util.*


object DateUtils {

    fun toDate(time: Long): String {
        val date = Date(time)
        val format: Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        return format.format(date)
    }
}
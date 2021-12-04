package com.mclowicz.mcphoto

import android.net.Uri

data class Image(
    val id: Long = 0L,
    val name: String,
    val uri: Uri
)
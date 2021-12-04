package com.mclowicz.mcphoto.data

import com.mclowicz.mcphoto.Image
import kotlinx.coroutines.flow.MutableSharedFlow

data class GetImagesEvent(
    val mutableSharedFlow: MutableSharedFlow<List<Image>>
)
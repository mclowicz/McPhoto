package com.mclowicz.mcphoto.feature.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mclowicz.mcphoto.data.GetImagesEvent
import com.mclowicz.mcphoto.data.Result
import com.mclowicz.mcphoto.workManager.MediaStoreWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val getImagesEvent: GetImagesEvent,
    @ApplicationContext val context: Context
): ViewModel() {

    private val _imageCaptureState = MutableLiveData<Result>()
    fun getImageCaptureState(): LiveData<Result> = _imageCaptureState

    init {
        runWorker()
    }

    fun runWorker() {
        MediaStoreWorker.run(context)
    }

    fun onImageCaptureFinished(result: Result) {
        _imageCaptureState.value = result
    }
}
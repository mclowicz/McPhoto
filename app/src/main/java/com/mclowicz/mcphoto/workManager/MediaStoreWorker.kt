package com.mclowicz.mcphoto.workManager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.LiveData
import androidx.work.*
import com.mclowicz.mcphoto.Image
import com.mclowicz.mcphoto.data.GetImagesEvent
import com.mclowicz.mcphoto.mediaStore.MediaStoreHandler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*

@HiltWorker
class MediaStoreWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters,
    private val getImagesEvent: GetImagesEvent,
    private val mediaStoreHandler: MediaStoreHandler
) : CoroutineWorker(context, params) {

    companion object {
        fun run(context: Context) : LiveData<WorkInfo> {
            val work = OneTimeWorkRequestBuilder<MediaStoreWorker>().build()
            WorkManager.getInstance(context).enqueue(work)
            return WorkManager.getInstance(context).getWorkInfoByIdLiveData(work.id)
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        val mutableList = mutableListOf<Image>()
        val job = async { mutableList.addAll(mediaStoreHandler.getImages()) }
        try {
            job.await()
            getImagesEvent.mutableSharedFlow.emit(mutableList)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
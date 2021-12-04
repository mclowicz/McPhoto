package com.mclowicz.mcphoto.di

import android.content.Context
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.mclowicz.mcphoto.R
import com.mclowicz.mcphoto.data.GetImagesEvent
import com.mclowicz.mcphoto.mediaStore.MediaStoreHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideShared(): GetImagesEvent = GetImagesEvent(
        MutableSharedFlow(
            replay = 0,
            extraBufferCapacity = 1,
            BufferOverflow.DROP_OLDEST
        )
    )

    @Singleton
    @Provides
    fun provideCircularDrawable(
        @ApplicationContext context: Context
    ): CircularProgressDrawable = CircularProgressDrawable(context).apply {
        strokeWidth = 5f
        centerRadius = 30f
        start()
    }

    @Singleton
    @Provides
    fun provideGlide(
        @ApplicationContext context: Context,
        circularProgressDrawable: CircularProgressDrawable
    ): RequestManager = Glide.with(context)
        .setDefaultRequestOptions(
            RequestOptions()
                .placeholder(circularProgressDrawable)
                .error(R.drawable.ic_launcher_background)

        )

    @Singleton
    @Provides
    fun provideMediaStoreHandler(@ApplicationContext context: Context) = MediaStoreHandler(context)
}
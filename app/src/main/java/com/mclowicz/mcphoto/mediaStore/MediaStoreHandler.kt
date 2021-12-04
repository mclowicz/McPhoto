package com.mclowicz.mcphoto.mediaStore

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.mclowicz.mcphoto.Image
import com.mclowicz.mcphoto.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MediaStoreHandler @Inject constructor(
    @ApplicationContext val context: Context
) {

    fun getMediaStoreImagesCollections(): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
    }

    fun getMediaStoreImagesContentValues(): ContentValues {
        return ContentValues().apply {
            put(
                MediaStore.Images.Media._ID,
                context.getString(R.string.media_store_content_values_id)
            )
            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                context.getString(
                    R.string.media_store_content_values_name,
                    System.currentTimeMillis().toString()
                )
            )
            put(
                MediaStore.Images.Media.MIME_TYPE,
                context.getString(R.string.media_store_content_values_type)
            )
        }
    }

    fun getImages(): List<Image> {
        val images = mutableListOf<Image>()
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME
        )
        val query = context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            null
        )
        query?.use { cursor ->
            val columnId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val columnName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(columnId)
                val name = cursor.getString(columnName)
                val contentUri = ContentUris.withAppendedId(
                    collection,
                    id
                )
                images.add(Image(id, name, contentUri))
            }
        }
        return images
    }
}
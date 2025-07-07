package com.yapp.media.storage

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Log
import java.io.IOException
import javax.inject.Inject

class ImageSaver @Inject constructor(
    private val contentResolver: ContentResolver,
) {

    fun saveImage(byteArray: ByteArray, fileName: String): Boolean {
        return try {
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Orbit")
            }

            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return false

            contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            true
        } catch (e: SecurityException) {
            Log.e("ImageSaver", "권한 없음: ${e.message}")
            false
        } catch (e: IOException) {
            Log.e("ImageSaver", "파일 저장 실패: ${e.message}")
            false
        }
    }
}

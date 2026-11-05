package com.homio.app

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max

object ImageUtil {

    /** The server accepts up to 700 KB in free (Firestore) mode, so stay safely under that. */
    const val MAX_BYTES = 650 * 1024
    private const val MAX_SIDE = 1600

    /** Reads a picked photo, scales it down and returns JPEG bytes under MAX_BYTES (null if unreadable). */
    suspend fun compressForUpload(context: Context, uri: Uri): ByteArray? =
        withContext(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver

                // 1) Read just the size, so huge photos don't fill the phone's memory
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

                var sample = 1
                while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2

                // 2) Decode at reduced size
                val options = BitmapFactory.Options().apply { inSampleSize = sample }
                val decoded = resolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, options)
                } ?: return@withContext null

                // 3) Fix rotation, remove transparency, limit the longest side
                var bitmap = flattenOnWhite(rotateUpright(resolver, uri, decoded))
                val longest = max(bitmap.width, bitmap.height)
                if (longest > MAX_SIDE) {
                    val scale = MAX_SIDE.toFloat() / longest
                    bitmap = Bitmap.createScaledBitmap(
                        bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true
                    )
                }

                // 4) Lower the quality (then the size) until it fits
                encodeUnderLimit(bitmap)
            } catch (e: Exception) {
                null
            }
        }

    private fun encodeUnderLimit(start: Bitmap): ByteArray? {
        var bitmap = start
        var quality = 85
        repeat(12) {
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            if (out.size() <= MAX_BYTES) return out.toByteArray()

            if (quality > 40) {
                quality -= 15
            } else {
                val w = (bitmap.width * 0.8f).toInt()
                val h = (bitmap.height * 0.8f).toInt()
                if (w < 300 || h < 300) return null
                bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true)
                quality = 70
            }
        }
        return null
    }

    private fun rotateUpright(resolver: ContentResolver, uri: Uri, bitmap: Bitmap): Bitmap {
        val orientation = try {
            resolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }

        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // PNG screenshots can be transparent; JPEG would turn that black
    private fun flattenOnWhite(src: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(src, 0f, 0f, null)
        return result
    }
}
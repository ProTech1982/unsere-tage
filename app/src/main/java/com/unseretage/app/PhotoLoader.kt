package com.unseretage.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlin.math.max

/** Laedt das gewaehlte Foto sparsam, damit grosse Kamerabilder den Speicher nicht sprengen. */
object PhotoLoader {

    fun load(context: Context, uriString: String?, targetWidth: Int): Bitmap? {
        if (uriString.isNullOrBlank() || targetWidth <= 0) return null
        val uri = runCatching { Uri.parse(uriString) }.getOrNull() ?: return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        if (!decode(context, uri, bounds)) return null
        if (bounds.outWidth <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, targetWidth)
        }
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        }.getOrNull()
    }

    private fun decode(context: Context, uri: Uri, options: BitmapFactory.Options): Boolean =
        runCatching {
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            true
        }.getOrDefault(false)

    private fun sampleSize(sourceWidth: Int, targetWidth: Int): Int {
        var sample = 1
        var width = sourceWidth
        while (width / 2 >= max(targetWidth, 1)) {
            width /= 2
            sample *= 2
        }
        return sample
    }
}

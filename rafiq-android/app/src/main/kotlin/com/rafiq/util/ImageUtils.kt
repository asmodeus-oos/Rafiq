package com.rafiq.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

object ImageUtils {
    fun compressImage(originalBytes: ByteArray, maxBytes: Int = 500 * 1024): ByteArray {
        if (originalBytes.size <= maxBytes) return originalBytes

        val bitmap = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size) ?: return originalBytes
        
        var quality = 100
        var compressedBytes = originalBytes
        
        // Downscale large images
        var currentBitmap = bitmap
        val maxDimension = 1920
        if (currentBitmap.width > maxDimension || currentBitmap.height > maxDimension) {
            val scale = minOf(maxDimension.toFloat() / currentBitmap.width, maxDimension.toFloat() / currentBitmap.height)
            val newWidth = (currentBitmap.width * scale).toInt()
            val newHeight = (currentBitmap.height * scale).toInt()
            currentBitmap = Bitmap.createScaledBitmap(currentBitmap, newWidth, newHeight, true)
        }

        while (quality > 10) {
            val stream = ByteArrayOutputStream()
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            val tempBytes = stream.toByteArray()
            if (tempBytes.size <= maxBytes) {
                compressedBytes = tempBytes
                break
            }
            quality -= 15
        }
        
        if (currentBitmap != bitmap) {
            currentBitmap.recycle()
        }
        bitmap.recycle()

        return if (quality <= 10) {
            val stream = ByteArrayOutputStream()
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, 10, stream)
            stream.toByteArray()
        } else {
            compressedBytes
        }
    }
}

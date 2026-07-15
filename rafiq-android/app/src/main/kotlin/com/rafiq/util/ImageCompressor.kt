package com.rafiq.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

object ImageCompressor {
    fun compressImage(imageBytes: ByteArray, maxSizeBytes: Long = 500 * 1024): ByteArray {
        if (imageBytes.size <= maxSizeBytes) return imageBytes
        
        var quality = 100
        var compressedBytes = imageBytes
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) ?: return imageBytes
        
        while (compressedBytes.size > maxSizeBytes && quality > 10) {
            quality -= 10
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            compressedBytes = outputStream.toByteArray()
        }
        
        // If still too large, we must scale it down
        var currentBitmap = bitmap
        while (compressedBytes.size > maxSizeBytes) {
            val width = (currentBitmap.width * 0.8).toInt()
            val height = (currentBitmap.height * 0.8).toInt()
            if (width <= 0 || height <= 0) break
            currentBitmap = Bitmap.createScaledBitmap(currentBitmap, width, height, true)
            val outputStream = ByteArrayOutputStream()
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            compressedBytes = outputStream.toByteArray()
        }
        
        return compressedBytes
    }
}

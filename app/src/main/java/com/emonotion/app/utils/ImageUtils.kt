package com.emonotion.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

/**
 * Утилита для работы с изображениями профиля
 */
object ImageUtils {
    
    private const val PROFILE_IMAGES_DIR = "profile_images"
    private const val MAX_IMAGE_SIZE = 1024 // Максимальный размер изображения в пикселях
    
    /**
     * Сохраняет изображение из URI в локальное хранилище приложения
     * @param context Контекст приложения
     * @param uri URI изображения
     * @return Путь к сохраненному файлу или null в случае ошибки
     */
    fun saveImageFromUri(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap != null) {
                saveBitmap(context, bitmap)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Сохраняет Bitmap в локальное хранилище приложения
     * @param context Контекст приложения
     * @param bitmap Изображение для сохранения
     * @return Путь к сохраненному файлу или null в случае ошибки
     */
    fun saveBitmap(context: Context, bitmap: Bitmap): String? {
        return try {
            // Создаем директорию для изображений профиля
            val imagesDir = File(context.filesDir, PROFILE_IMAGES_DIR)
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            
            // Масштабируем изображение если нужно
            val scaledBitmap = scaleBitmapIfNeeded(bitmap)
            
            // Генерируем уникальное имя файла
            val fileName = "${UUID.randomUUID()}.jpg"
            val file = File(imagesDir, fileName)
            
            // Сохраняем изображение
            val outputStream = FileOutputStream(file)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.flush()
            outputStream.close()
            
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Масштабирует изображение если оно превышает максимальный размер
     * @param bitmap Исходное изображение
     * @return Масштабированное изображение
     */
    private fun scaleBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= MAX_IMAGE_SIZE && height <= MAX_IMAGE_SIZE) {
            return bitmap
        }
        
        val scale = MAX_IMAGE_SIZE.toFloat() / maxOf(width, height)
        val scaledWidth = (width * scale).toInt()
        val scaledHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    }
    
    /**
     * Обрезает изображение в круг
     * @param bitmap Исходное изображение
     * @return Круглое изображение
     */
    fun cropToCircle(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        
        val paint = android.graphics.Paint()
        paint.isAntiAlias = true
        paint.shader = android.graphics.BitmapShader(bitmap, android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP)
        
        val rect = android.graphics.RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawOval(rect, paint)
        
        return output
    }
    
    /**
     * Удаляет изображение по пути
     * @param filePath Путь к файлу изображения
     * @return true если удаление успешно, иначе false
     */
    fun deleteImage(filePath: String?): Boolean {
        if (filePath.isNullOrEmpty()) return false
        
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Удаляет все изображения профиля из локального хранилища
     * @param context Контекст приложения
     */
    fun deleteAllProfileImages(context: Context) {
        try {
            val imagesDir = File(context.filesDir, PROFILE_IMAGES_DIR)
            if (imagesDir.exists()) {
                imagesDir.listFiles()?.forEach { it.delete() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Получает размер файла изображения в байтах
     * @param filePath Путь к файлу изображения
     * @return Размер файла в байтах или 0 если файл не существует
     */
    fun getImageSize(filePath: String?): Long {
        if (filePath.isNullOrEmpty()) return 0L
        
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.length()
            } else {
                0L
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }
}

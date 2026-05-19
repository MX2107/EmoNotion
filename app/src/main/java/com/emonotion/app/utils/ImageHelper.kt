package com.emonotion.app.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.emonotion.app.R
import java.io.File
import kotlin.math.min

/**
 * Утилитный класс для работы с изображениями аватара
 */
object ImageHelper {

    private const val TAG = "ImageHelper"

    /**
     * Загружает аватар в [ImageView] асинхронно (Glide) с круглой обрезкой.
     */
    fun loadAvatarInto(imageView: ImageView, avatarPath: String?) {
        imageView.clearColorFilter()

        if (avatarPath.isNullOrBlank()) {
            showPlaceholder(imageView)
            return
        }

        val avatarFile = File(avatarPath)
        if (!avatarFile.exists()) {
            Log.w(TAG, "Файл аватара не найден: $avatarPath")
            showPlaceholder(imageView)
            return
        }

        Glide.with(imageView)
            .load(avatarFile)
            .circleCrop()
            .signature(ObjectKey(avatarFile.lastModified()))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(false)
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_profile)
            .into(imageView)
    }

    fun showPlaceholder(imageView: ImageView) {
        Glide.with(imageView).clear(imageView)
        imageView.clearColorFilter()
        imageView.setImageResource(R.drawable.ic_profile)
    }

    /**
     * Создает круглое изображение из прямоугольного (центральная обрезка).
     */
    fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = min(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        val squared = Bitmap.createBitmap(bitmap, x, y, size, size)

        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = BitmapShader(squared, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)
        return output
    }

    /**
     * Генерирует путь к оригинальному изображению на основе обрезанного
     */
    fun getOriginalPath(croppedPath: String): String {
        val lastDotIndex = croppedPath.lastIndexOf('.')
        return if (lastDotIndex != -1) {
            croppedPath.substring(0, lastDotIndex) + "_original" + croppedPath.substring(lastDotIndex)
        } else {
            croppedPath + "_original"
        }
    }

    /**
     * Путь к файлу оригинала, если он существует, иначе обрезанный.
     */
    fun resolveFullscreenPath(croppedPath: String): String {
        val originalPath = getOriginalPath(croppedPath)
        return if (File(originalPath).exists()) originalPath else croppedPath
    }

    /**
     * Открывает аватар на весь экран в диалоге
     */
    fun openAvatarFullscreen(context: Context, avatarPath: String) {
        try {
            val path = resolveFullscreenPath(avatarPath)
            val bitmap = BitmapFactory.decodeFile(path)
            if (bitmap != null) {
                val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                val view = LayoutInflater.from(context).inflate(R.layout.dialog_fullscreen_avatar, null)
                val imageView = view.findViewById<ImageView>(R.id.fullscreen_avatar_image)
                imageView.setImageBitmap(bitmap)
                imageView.setOnClickListener { dialog.dismiss() }
                dialog.setContentView(view)
                dialog.show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка открытия аватара на весь экран", e)
        }
    }
}

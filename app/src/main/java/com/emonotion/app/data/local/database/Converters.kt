package com.emonotion.app.data.local.database

import androidx.room.TypeConverter

/**
 * Type converters для Room базы данных
 */
class Converters {
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
}

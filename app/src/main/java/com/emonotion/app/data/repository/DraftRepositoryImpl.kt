package com.emonotion.app.data.repository

import com.emonotion.app.data.local.dao.DraftDao
import com.emonotion.app.data.local.entities.DraftEntity
import com.emonotion.app.domain.model.MoodEntry
import com.emonotion.app.domain.repository.DraftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository для работы с черновиками записей о настроении
 */
@Singleton
class DraftRepositoryImpl @Inject constructor(
    private val draftDao: DraftDao
) : DraftRepository {
    
    /**
     * Получает черновик для указанной даты
     */
    override suspend fun getDraftByDate(date: String): MoodEntry? {
        val draftEntity = draftDao.getDraftByDate(date)
        android.util.Log.d("DraftRepositoryImpl", "Загрузка черновика из БД для даты: $date, draftEntity=$draftEntity")
        if (draftEntity != null) {
            android.util.Log.d("DraftRepositoryImpl", "Загружен черновик из БД: id=${draftEntity.id}, mood=${draftEntity.mood}, emotions=${draftEntity.emotions}, activities=${draftEntity.activities}, notes=${draftEntity.notes}, date=${draftEntity.date}")
        }
        return draftEntity?.toDomain()
    }
    
    /**
     * Получает черновик для указанной даты как Flow
     */
    override fun getDraftByDateFlow(date: String): Flow<MoodEntry?> {
        return draftDao.getDraftByDateFlow(date).map { it?.toDomain() }
    }
    
    /**
     * Сохраняет черновик
     */
    override suspend fun saveDraft(moodEntry: MoodEntry) {
        val draftEntity = DraftEntity.fromDomain(moodEntry)
        android.util.Log.d("DraftRepositoryImpl", "Сохранение черновика в БД: id=${draftEntity.id}, mood=${draftEntity.mood}, emotions=${draftEntity.emotions}, activities=${draftEntity.activities}, notes=${draftEntity.notes}, date=${draftEntity.date}")
        draftDao.insertDraft(draftEntity)
    }
    
    /**
     * Удаляет черновик для указанной даты
     */
    override suspend fun deleteDraftByDate(date: String) {
        draftDao.deleteDraftByDate(date)
    }
    
    /**
     * Удаляет черновик
     */
    override suspend fun deleteDraft(draft: MoodEntry) {
        val draftEntity = DraftEntity.fromDomain(draft)
        draftDao.deleteDraft(draftEntity)
    }
    
    /**
     * Удаляет все черновики
     */
    override suspend fun deleteAllDrafts() {
        draftDao.deleteAllDrafts()
    }
    
    /**
     * Получает все черновики
     */
    override fun getAllDrafts(): Flow<List<MoodEntry>> {
        return draftDao.getAllDrafts().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}

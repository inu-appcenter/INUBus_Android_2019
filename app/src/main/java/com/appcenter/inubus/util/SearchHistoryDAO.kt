package com.appcenter.inubus.util

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.appcenter.inubus.model.DBSearchHistoryItem


/**
 * Created by Minjae Son on 2018-08-10.
 */

@Dao

// 검색 기록에 대한 SQL 쿼리 메소드 저장
interface SearchHistoryDAO{
    @Insert(onConflict = REPLACE)
    fun insert(contacts : DBSearchHistoryItem )

    @Delete
    fun delete(item : DBSearchHistoryItem)

    @Query("SELECT * FROM DBSearchHistoryItem")
    fun getAll(): List<DBSearchHistoryItem>

    @Query("DELETE from DBSearchHistoryItem")
    fun deleteall()
}
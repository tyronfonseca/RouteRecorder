package com.tf.routerrecorder.Database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.tf.routerrecorder.Database.Entities.Calendar;

import java.util.List;

@Dao
public interface CalendarDao {
    @Query("SELECT * FROM calendar")
    List<Calendar> getAll();

    @Query("SELECT * FROM Calendar WHERE service_id IN (:calendarIds)")
    LiveData<List<Calendar>> loadAllByIds(int[] calendarIds);

    @Query("SELECT * FROM Calendar WHERE service_id  = :id LIMIT 1")
    Calendar findById(String id);

    @Insert
    void insertAll(Calendar... calendars);

    @Delete
    void delete(Calendar calendar);
}

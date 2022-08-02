package com.tf.routerrecorder.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tf.routerrecorder.database.entities.RRData;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface RRDataDao {
    @Query("SELECT * FROM RRData")
    List<RRData> getAll();

    @Query("SELECT * FROM RRData WHERE unix_time BETWEEN :begin_time AND :end_time")
    List<RRData> getByRange(long begin_time, long end_time);

    @Query("SELECT * FROM RRData WHERE unix_time = :id LIMIT 1")
    RRData findById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllRRData(ArrayList<RRData> agencies);

    @Delete
    void delete(RRData RRData);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRRData(RRData RRData);
}

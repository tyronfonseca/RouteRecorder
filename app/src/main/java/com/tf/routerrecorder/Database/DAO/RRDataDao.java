package com.tf.routerrecorder.Database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tf.routerrecorder.Database.Entities.RRData;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface RRDataDao {
    @Query("SELECT * FROM RRData")
    List<RRData> getAll();

    @Query("SELECT * FROM RRData WHERE unix_time BETWEEN :begin_time AND :end_time")
    List<RRData> getByRange(int begin_time, int end_time);

    @Query("SELECT * FROM RRData WHERE unix_time = :id LIMIT 1")
    RRData findById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllRRData(ArrayList<RRData> agencies);

    @Delete
    void delete(RRData RRData);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRRData(RRData RRData);
}

package com.tf.routerrecorder.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tf.routerrecorder.database.entities.StopsRoutes;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface StopsRoutesDao {
    @Query("SELECT * FROM StopsRoutes")
    List<StopsRoutes> getAll();

    @Query("SELECT * FROM StopsRoutes WHERE id IN (:stopsRoutesIds)")
    List<StopsRoutes> loadAllByIds(int[] stopsRoutesIds);

    @Query("SELECT * FROM StopsRoutes WHERE id = :id LIMIT 1")
    StopsRoutes findById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllStopsRoutes(ArrayList<StopsRoutes> stopsRoutes);

    @Delete
    void delete(StopsRoutes stopsRoute);
}

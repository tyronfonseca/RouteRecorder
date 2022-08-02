package com.tf.routerrecorder.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tf.routerrecorder.database.entities.Trips;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface TripsDao {
    @Query("SELECT * FROM Trips")
    List<Trips> getAll();

    @Query("SELECT * FROM Trips WHERE trip_id IN (:TripsIds)")
    List<Trips> loadAllByIds(int[] TripsIds);

    @Query("SELECT * FROM Trips WHERE trip_id = :id LIMIT 1")
    Trips findById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllTrips(ArrayList<Trips> trips);

    @Delete
    void delete(Trips Trips);
}

package com.tf.routerrecorder.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tf.routerrecorder.database.entities.Stops;

import java.util.ArrayList;
import java.util.List;
@Dao
public interface StopsDao {
    @Query("SELECT * FROM Stops")
    List<Stops> getAll();

    @Query("SELECT * FROM Stops WHERE stop_id IN (:StopsIds)")
    List<Stops> loadAllByIds(int[] StopsIds);

    @Query("SELECT * FROM Stops WHERE stop_id= :id LIMIT 1")
    Stops findById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllStops(ArrayList<Stops> stops);

    @Delete
    void delete(Stops Stops);

    @Query("SELECT * FROM Stops s INNER JOIN StopsRoutes sr ON sr.stop_id = s.stop_id WHERE sr.route_id = :id ORDER BY sr.sequence ASC")
    List<Stops> getAllByRouteId(String id);
}

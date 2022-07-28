package com.tf.routerrecorder.Database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tf.routerrecorder.Database.Entities.Agency;
import com.tf.routerrecorder.Database.Entities.Route;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface  RouteDao {
    @Query("SELECT * FROM route")
    List<Route> getAll();

    @Query("SELECT * FROM route WHERE route_id IN (:routeIds)")
    List<Route> loadAllByIds(int[] routeIds);

    @Query("SELECT * FROM route WHERE route_id = :id LIMIT 1")
    Route findById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllRoutes(ArrayList<Route> routes);

    @Delete
    void delete(Route route);
}

package com.tf.routerrecorder.Database.DAO;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.tf.routerrecorder.Database.Entities.AgencyRoutes;
import com.tf.routerrecorder.Database.Entities.RouteTrips;

import java.util.List;

@Dao
public interface RouteTripsDao {
    @Transaction
    @Query("SELECT * FROM Route AS r JOIN Trips AS t ON r.route_id = t.route_id WHERE r.route_id = :id")
    public List<RouteTrips> getTripsByRoute(String id);
}

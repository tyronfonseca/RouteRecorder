package com.tf.routerrecorder.Database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.tf.routerrecorder.Database.Entities.AgencyRoutes;

import java.util.List;

@Dao
public interface AgencyRoutesDao {
    @Transaction
    @Query("SELECT a.agency_id, r.route_id, r.route_long_name FROM Agency AS a JOIN Route AS r ON a.agency_id = r.agency_id WHERE r.agency_id = :id")
    public LiveData<List<AgencyRoutes>> getRoutesByAgency(String id);
}

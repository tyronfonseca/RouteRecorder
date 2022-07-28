package com.tf.routerrecorder.Database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tf.routerrecorder.Database.Entities.Agency;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface AgencyDao {
    @Query("SELECT * FROM agency")
    List<Agency> getAll();

    @Query("SELECT * FROM Agency WHERE agency_id IN (:AgencyIds)")
    List<Agency> loadAllByIds(int[] AgencyIds);

    @Query("SELECT * FROM Agency WHERE agency_id = :id LIMIT 1")
    Agency findById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllAgencies(ArrayList<Agency> agencies);

    @Delete
    void delete(Agency agency);
}


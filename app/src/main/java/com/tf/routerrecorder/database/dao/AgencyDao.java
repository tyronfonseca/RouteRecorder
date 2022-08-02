package com.tf.routerrecorder.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tf.routerrecorder.database.entities.Agency;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface AgencyDao {
    @Query("SELECT * FROM agency")
    List<Agency> getAll();

    @Query("SELECT agency_id FROM agency")
    List<String> getIdAll();

    @Query("SELECT * FROM Agency WHERE agency_id IN (:AgencyIds)")
    List<Agency> loadAllByIds(int[] AgencyIds);

    @Query("SELECT * FROM Agency WHERE agency_id = :id LIMIT 1")
    Agency findById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllAgencies(ArrayList<Agency> agencies);

    @Delete
    void delete(Agency agency);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAgency(Agency agency);
}


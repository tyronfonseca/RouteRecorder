package com.tf.routerrecorder.Database.Entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Stops {
    @PrimaryKey
    @NonNull
    public String stop_id;

    @ColumnInfo(name = "stop_name")
    public String name;

    @ColumnInfo(name = "stop_desc")
    public String description;

    @ColumnInfo(name = "stop_lat")
    public Double lat;

    @ColumnInfo(name = "stop_lon")
    public Double lon;

    @ColumnInfo(name = "zone_id")
    public String zoneId;

    @ColumnInfo(name = "stop_url")
    public String url;

    @ColumnInfo(name = "location_type")
    public int locationType;

    @ColumnInfo(name = "parent_station")
    public String parentStation;

    @ColumnInfo(name = "wheelchair_boarding")
    public int wheelchairBoarding;
}

package com.tf.routerrecorder.Database.Entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Trips {
    @PrimaryKey
    @NonNull
    public String trip_id;

    @ColumnInfo(name = "trip_headsign")
    public String headsign;

    @ColumnInfo(name = "trip_short_name")
    public String shortName;

    @ColumnInfo(name = "direction_id")
    public String direction;

    @ColumnInfo(name = "wheelchair_accessible")
    public int wheelchairAccessible;

    @ColumnInfo(name = "bikes_allowed")
    public int bikesAllowed;

    public String route_id;

    public String service_id;
}

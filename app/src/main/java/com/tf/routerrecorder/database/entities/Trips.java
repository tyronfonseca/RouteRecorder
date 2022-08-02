package com.tf.routerrecorder.database.entities;

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

    public String shape_id;

    @ColumnInfo(name = "wheelchair_accessible")
    public int wheelchairAccessible;

    @ColumnInfo(name = "bikes_allowed")
    public int bikesAllowed;

    public String route_id;

    public String service_id;

    public Trips(){
    }
    public Trips (String[] item){
        route_id = item[0];
        service_id = item[1];
        trip_id = item[2];
        headsign = item[3];
        shortName = item[4];
        direction = item[5];
        shape_id = item[6];
        wheelchairAccessible = Integer.parseInt(item[7]);
        bikesAllowed = Integer.parseInt(item[8]);
    }
}

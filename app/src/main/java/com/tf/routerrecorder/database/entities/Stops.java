package com.tf.routerrecorder.database.entities;

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

    public Stops(){
    }
    public Stops (String[] item){
        stop_id = item[0];
        name = item[1];
        description = item[2];
        lat = item[3].isEmpty() ? 0 : Double.parseDouble(item[3]);
        lon = item[4].isEmpty() ? 0 : Double.parseDouble(item[4]);
        zoneId = item[5];
        url = item[6];
        locationType = item[7].isEmpty() ? 0 :Integer.parseInt(item[7]);
        parentStation = item[8];
        wheelchairBoarding = item[9].isEmpty() ? 0 : Integer.parseInt(item[9]);
    }
}

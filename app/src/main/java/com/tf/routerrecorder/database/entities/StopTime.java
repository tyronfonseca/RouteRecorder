package com.tf.routerrecorder.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class StopTime {

    @PrimaryKey
    @NonNull
    public int id;

    public String trip_id;

    @ColumnInfo(name = "arrival_time")
    public String arrivalTime;

    @ColumnInfo(name = "departure_time")
    public String departureTime;

    public String stop_id;

    @ColumnInfo(name = "stop_sequence")
    public int stopSequence;

    @ColumnInfo(name = "stop_headsign")
    public String stopHeadsign;

    @ColumnInfo(name = "pickup_type")
    public int pickupType;

    @ColumnInfo(name = "drop_off_type")
    public int dropOffType;

    @ColumnInfo(name = "shape_dist_traveled")
    public Double shapeDistTraveled;

    @ColumnInfo(name = "timepoint")
    public int timePoint;

}

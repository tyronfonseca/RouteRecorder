package com.tf.routerrecorder.Database.Entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class TripsStopTime {
    @Embedded
    public Trips trips;
    @Relation(
            parentColumn = "trip_id",
            entityColumn = "id"
    )
    public List<StopTime> stopTimes;
}

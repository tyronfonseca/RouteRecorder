package com.tf.routerrecorder.Database.Entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class CalendarTrips {
    @Embedded
    public Calendar calendar;
    @Relation(
            parentColumn = "service_id",
            entityColumn = "trip_id"
    )
    public List<Trips> trips;
}

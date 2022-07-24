package com.tf.routerrecorder.Database.Entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class RouteTrips {
    @Embedded
    public Route route;
    @Relation(
            parentColumn = "route_id",
            entityColumn = "trip_id"
    )
    public List<Trips> trips;
}

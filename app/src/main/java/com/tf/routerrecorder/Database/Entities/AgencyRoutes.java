package com.tf.routerrecorder.Database.Entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class AgencyRoutes {
    @Embedded public Agency agency;
    @Relation(
        parentColumn = "agency_id",
        entityColumn = "route_id"
    )
    public List<Route> routes;
}

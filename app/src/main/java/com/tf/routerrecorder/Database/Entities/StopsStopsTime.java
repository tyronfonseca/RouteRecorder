package com.tf.routerrecorder.Database.Entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class StopsStopsTime {
    @Embedded
    public Stops stops;
    @Relation(
            parentColumn = "stop_id",
            entityColumn = "id"
    )
    public List<StopTime> stopTimes;
}

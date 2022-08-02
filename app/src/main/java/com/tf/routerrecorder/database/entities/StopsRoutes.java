package com.tf.routerrecorder.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class StopsRoutes {
    @PrimaryKey
    @NonNull
    public long id;

    public String stop_id;
    public String route_id;
    public int sequence;
    public boolean direction;

    public StopsRoutes(){
    }
    public StopsRoutes (String[] item){
        id = Integer.parseInt(item[0]);
        stop_id = item[1];
        route_id = item[2];
        sequence = Integer.parseInt(item[3]);
        direction = Boolean.parseBoolean(item[4]);
    }

}

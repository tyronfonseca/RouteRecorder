package com.tf.routerrecorder.Database.Entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Route {
    @PrimaryKey
    @NonNull
    public String route_id;

    @ColumnInfo(name = "route_short_name")
    public String shortName;

    @ColumnInfo(name = "route_long_name")
    public String longName;

    @ColumnInfo(name = "route_desc")
    public String description;

    @ColumnInfo(name = "route_type")
    public short type;

    @ColumnInfo(name = "route_url")
    public String url;

    @ColumnInfo(name = "color_url")
    public String color;

    @ColumnInfo(name = "route_text_color")
    public String text_color;

    @ColumnInfo(name = "agency_id")
    public String agency_id;

    public Route(){
    }
    public Route (String[] item){
        route_id = item[0];
        agency_id = item[1];
        shortName = item[2];
        longName = item[3];
        description = item[4];
        type = Short.parseShort(item[5]);
        url = item[6];
        color = item[7];
        text_color = item[8];
    }
}

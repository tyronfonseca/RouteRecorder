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
}

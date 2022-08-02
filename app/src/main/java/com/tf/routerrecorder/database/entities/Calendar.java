package com.tf.routerrecorder.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Calendar {
    @PrimaryKey
    @NonNull
    public String service_id;

    @ColumnInfo(name = "monday")
    public boolean monday;

    @ColumnInfo(name = "tuesday")
    public boolean tuesday;

    @ColumnInfo(name = "wednesday")
    public boolean wednesday;

    @ColumnInfo(name = "thursday")
    public boolean thursday;

    @ColumnInfo(name = "friday")
    public boolean friday;

    @ColumnInfo(name = "saturday")
    public boolean saturday;

    @ColumnInfo(name = "sunday")
    public boolean sunday;

    @ColumnInfo(name = "start_date")
    public String startDate;

    @ColumnInfo(name = "end_date")
    public String endDate;
}

package com.tf.routerrecorder.database.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
public class RRData {
    @PrimaryKey
    public long unix_time;

    public double lat;
    public double lon;

    public String stop_id;
    public String route_id;

    @Ignore
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Ignore
    public String getSummary(String provider){
        String text = lat + "," + lon;
        LocalDateTime now = LocalDateTime.now();
        text += "- Time: " + dtf.format(now) + " ("+provider+")";

        return text;
    }

    @Ignore
    public String[] getCSVRow(){
        String[] csvRow = new String[5];

        csvRow[0] = String.valueOf(unix_time);
        csvRow[1] = String.valueOf(lat);
        csvRow[2] = String.valueOf(lon);
        csvRow[3] = stop_id;
        csvRow[4] = route_id;

        return csvRow;
    }
}

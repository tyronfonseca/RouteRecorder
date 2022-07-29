package com.tf.routerrecorder.Database.Entities;

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
    private final String SEPARATOR = ",";

    @Ignore
    public String getSummary(String provider){
        String text = lat + "," + lon;
        LocalDateTime now = LocalDateTime.now();
        text += "- Time: " + dtf.format(now) + " ("+provider+")";

        return text;
    }

    @Ignore
    public String getCSVRow(){
        String csvRow = "";

        csvRow += unix_time + SEPARATOR;
        csvRow += lat + SEPARATOR;
        csvRow += lon + SEPARATOR;
        csvRow += stop_id + SEPARATOR;
        csvRow += route_id + "\n";

        return csvRow;
    }
}

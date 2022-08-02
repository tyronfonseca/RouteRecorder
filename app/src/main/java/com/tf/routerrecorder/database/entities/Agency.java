package com.tf.routerrecorder.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Agency {
    @PrimaryKey
    @NonNull
    public String agency_id;

    @ColumnInfo(name = "agency_name")
    public String name;

    @ColumnInfo(name = "agency_url")
    public String url;

    @ColumnInfo(name = "agency_timezone")
    public String timezone;

    @ColumnInfo(name = "agency_lang")
    public String lang;

    @ColumnInfo(name = "agency_phone")
    public String phone;

    @ColumnInfo(name = "agency_fare_url")
    public String fareUrl;

    @ColumnInfo(name = "agency_email")
    public String email;

    public Agency(){
    }
    public Agency (String[] item){
        agency_id = item[0];
        name = item[1];
        url = item[2];
        timezone = item[3];
        lang = item[4];
        phone = item[5];
        fareUrl = item[6];
        email = item[7];
    }
}

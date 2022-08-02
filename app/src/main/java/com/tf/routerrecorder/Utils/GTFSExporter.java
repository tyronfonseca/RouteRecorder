package com.tf.routerrecorder.Utils;

import android.os.Environment;

import com.opencsv.CSVWriter;
import com.tf.routerrecorder.Database.Entities.RRData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class GTFSExporter {
    public GTFSExporter(){}

    /**
     * Save the data in csv format in the Download directory.
     * @param data List of RRData objects
     */
    public void exportRRData(List<RRData> data){
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String nameFile = "rrdata_"+ LocalDateTime.now().toString().replace(':', '_')+".txt";
        String filePath = dir + File.separator + nameFile;
        File file = new File(filePath);
        CSVWriter csvWriter;

        try {
            if(file.exists() && !file.isDirectory()){
                FileWriter fileWriter = new FileWriter(filePath, true);
                csvWriter = new CSVWriter(fileWriter);
            }else{
                csvWriter = new CSVWriter(new FileWriter(filePath));
            }
            csvWriter.writeNext(new String[]{"unix_time", "lat", "lon", "stop_id", "route_id"});
            for(RRData row : data){
                csvWriter.writeNext(row.getCSVRow());
            }
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

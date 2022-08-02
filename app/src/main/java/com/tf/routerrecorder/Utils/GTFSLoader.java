package com.tf.routerrecorder.Utils;

import android.content.Context;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.tf.routerrecorder.Database.Entities.Agency;
import com.tf.routerrecorder.Database.Entities.Route;
import com.tf.routerrecorder.Database.Entities.Stops;
import com.tf.routerrecorder.Database.Entities.Trips;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GTFSLoader {
    private final String AGENCY = "agency.txt";
    private final String ROUTES = "routes.txt";
    private final String TRIPS = "trips.txt";
    private final String STOPS = "stops.txt";

    private Context context;

    public GTFSLoader(Context ctx){
        context = ctx;
    }

    /**
     * Load a given zipfile in Inputstream format
     * @param inputStream ZipFile selected by the user
     * @throws IOException Error with the path
     */
    public void loadZip(InputStream inputStream) throws IOException {
        ZipInputStream zipIS = new ZipInputStream(inputStream);
        ZipEntry entry = null;

        while((entry = zipIS.getNextEntry()) != null){
            FileOutputStream out = new FileOutputStream(context.getFilesDir()+"/"+entry.getName());

            byte[] buffer = new byte[1024];
            int length = 0;
            while((length = zipIS.read(buffer)) > 0){
                out.write(buffer, 0, length);
            }
            zipIS.closeEntry();
            out.close();
        }
        zipIS.close();
    }

    /**
     * Get a InputStreamReader given the name of a file
     * @param name Name of the file
     * @return InputStreamReader of the file
     * @throws FileNotFoundException
     */
    private InputStreamReader getFile(String name) throws FileNotFoundException {
        File file = new File(context.getFilesDir() + "/" + name);
        FileInputStream inputStream = new FileInputStream(file);
        InputStreamReader reader = new InputStreamReader(inputStream);
        return reader;
    }

    /**
     * Open a given file in the csv format
     * @param name Name of the file
     * @return CSVReader to load the data.
     * @throws IOException
     */
    private CSVReader getCSVReaderByName(String name) throws IOException{
        InputStreamReader fileIS = getFile(name);
        CSVReader reader = new CSVReaderBuilder(fileIS)
                .withCSVParser(new CSVParserBuilder().withSeparator(',').build())
                .withSkipLines(1)
                .build();
        return reader;
    }

    /**
     * Get and array of all the Agencies in the agency.txt file
     * @return Array of Agency objects
     */
    public ArrayList<Agency> getAgencies(){
        CSVReader reader = null;
        try {
            reader = getCSVReaderByName(AGENCY);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Agency> list = new ArrayList<>();
        try {
            List<String[]> items = reader.readAll();
            for(String[] item : items){
                list.add(new Agency(item));
            }

        }catch (CsvException | IOException ex){
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Get and array of all the Routes in the routes.txt file
     * @return Array of Route objects
     */
    public ArrayList<Route> getRoutes(){
        CSVReader reader = null;
        try {
            reader = getCSVReaderByName(ROUTES);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Route> list = new ArrayList<>();
        try {
            List<String[]> items = reader.readAll();
            for(String[] item : items){
                list.add(new Route(item));
            }

        }catch (CsvException | IOException ex){
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Get and array of all the Trips in the trips.txt file
     * @return Array of Trips objects
     */
    public ArrayList<Trips> getTrips(){
        CSVReader reader = null;
        try {
            reader = getCSVReaderByName(TRIPS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Trips> list = new ArrayList<>();
        try {
            List<String[]> items = reader.readAll();
            for(String[] item : items){
                list.add(new Trips(item));
            }

        }catch (CsvException | IOException ex){
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Get and array of all the Trips in the trips.txt file
     * @return Array of Trips objects
     */
    public ArrayList<Stops> getStops(){
        CSVReader reader = null;
        try {
            reader = getCSVReaderByName(STOPS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Stops> list = new ArrayList<>();
        try {
            List<String[]> items = reader.readAll();
            for(String[] item : items){
                list.add(new Stops(item));
            }

        }catch (CsvException | IOException ex){
            ex.printStackTrace();
        }
        return list;
    }
}

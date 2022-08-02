package com.tf.routerrecorder.Database.Infrastructure;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.tf.routerrecorder.Database.DAO.AgencyDao;
import com.tf.routerrecorder.Database.DAO.RRDataDao;
import com.tf.routerrecorder.Database.DAO.RouteDao;
import com.tf.routerrecorder.Database.DAO.StopsDao;
import com.tf.routerrecorder.Database.DAO.TripsDao;
import com.tf.routerrecorder.Database.Entities.Agency;
import com.tf.routerrecorder.Database.Entities.Calendar;
import com.tf.routerrecorder.Database.Entities.RRData;
import com.tf.routerrecorder.Database.Entities.Route;
import com.tf.routerrecorder.Database.Entities.Stops;
import com.tf.routerrecorder.Database.Entities.Trips;

@Database(
        entities = {Agency.class, Calendar.class, Route.class, Stops.class, Trips.class,
                RRData.class}
        , version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AgencyDao agencyDao();
    public abstract RouteDao routeDao();
    public abstract TripsDao tripsDao();
    public abstract StopsDao stopsDao();
    public abstract RRDataDao rrDataDao();
}

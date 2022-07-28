package com.tf.routerrecorder.Database.Infrastructure;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.tf.routerrecorder.Database.DAO.AgencyDao;
import com.tf.routerrecorder.Database.DAO.AgencyRoutesDao;
import com.tf.routerrecorder.Database.DAO.CalendarDao;
import com.tf.routerrecorder.Database.DAO.RouteDao;
import com.tf.routerrecorder.Database.DAO.RouteTripsDao;
import com.tf.routerrecorder.Database.DAO.TripsDao;
import com.tf.routerrecorder.Database.Entities.Agency;
import com.tf.routerrecorder.Database.Entities.AgencyRoutes;
import com.tf.routerrecorder.Database.Entities.Calendar;
import com.tf.routerrecorder.Database.Entities.CalendarTrips;
import com.tf.routerrecorder.Database.Entities.Route;
import com.tf.routerrecorder.Database.Entities.RouteTrips;
import com.tf.routerrecorder.Database.Entities.Stops;
import com.tf.routerrecorder.Database.Entities.StopsStopsTime;
import com.tf.routerrecorder.Database.Entities.Trips;
import com.tf.routerrecorder.Database.Entities.TripsStopTime;

import java.util.TreeMap;

@Database(
        entities = {Agency.class, Calendar.class, Route.class, Stops.class, Trips.class,}
        , version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AgencyDao agencyDao();
    public abstract AgencyRoutesDao agencyRoutesDao();
    public abstract CalendarDao calendarDao();
    public abstract RouteDao routeDao();
    public abstract RouteTripsDao routeTripsDao();
    public abstract TripsDao tripsDao();

}

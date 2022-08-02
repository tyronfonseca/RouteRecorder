package com.tf.routerrecorder.database.infrastructure;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.tf.routerrecorder.database.dao.AgencyDao;
import com.tf.routerrecorder.database.dao.RRDataDao;
import com.tf.routerrecorder.database.dao.RouteDao;
import com.tf.routerrecorder.database.dao.StopsDao;
import com.tf.routerrecorder.database.dao.StopsRoutesDao;
import com.tf.routerrecorder.database.dao.TripsDao;
import com.tf.routerrecorder.database.entities.Agency;
import com.tf.routerrecorder.database.entities.Calendar;
import com.tf.routerrecorder.database.entities.RRData;
import com.tf.routerrecorder.database.entities.Route;
import com.tf.routerrecorder.database.entities.Stops;
import com.tf.routerrecorder.database.entities.StopsRoutes;
import com.tf.routerrecorder.database.entities.Trips;

@Database(
        entities = {Agency.class, Calendar.class, Route.class, Stops.class, Trips.class,
                RRData.class, StopsRoutes.class}
        , version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AgencyDao agencyDao();
    public abstract RouteDao routeDao();
    public abstract TripsDao tripsDao();
    public abstract StopsDao stopsDao();
    public abstract RRDataDao rrDataDao();
    public abstract StopsRoutesDao stopsRoutesDao();
}

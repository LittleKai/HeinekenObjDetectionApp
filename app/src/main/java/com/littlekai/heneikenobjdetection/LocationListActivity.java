package com.littlekai.heneikenobjdetection;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.littlekai.heneikenobjdetection.adapter.LocationAdapter;
import com.littlekai.heneikenobjdetection.database.HeneikenDatabaseHelper;
import com.littlekai.heneikenobjdetection.model.LocationInfo;

import java.util.ArrayList;

public class LocationListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LocationAdapter locationAdapter;
    private ArrayList<LocationInfo> locationInfos;
    HeneikenDatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch locations from database or other data source
        locationInfos = fetchLocationsFromDatabase();
        if (!locationInfos.isEmpty()) {
            locationAdapter = new LocationAdapter(locationInfos, this);
            recyclerView.setAdapter(locationAdapter);
        } else {
            Toast.makeText(this, "There is not any locations saved", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private ArrayList<LocationInfo> fetchLocationsFromDatabase() {
        // Implement your database fetching logic here
        // For example, you might call the getAllLocations() function from your database helper class
        db = new HeneikenDatabaseHelper(this);
        return db.getAllLocations();
    }
}




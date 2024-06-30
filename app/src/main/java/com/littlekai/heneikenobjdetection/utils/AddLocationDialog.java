package com.littlekai.heneikenobjdetection.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.littlekai.heneikenobjdetection.ObjDetectActivity;
import com.littlekai.heneikenobjdetection.R;
import com.littlekai.heneikenobjdetection.database.HeneikenDatabaseHelper;
import com.littlekai.heneikenobjdetection.model.LocationInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AddLocationDialog extends DialogFragment {

    private final ObjDetectActivity activity;
    private LocationInfo newLocationInfo = new LocationInfo();
    HeneikenDatabaseHelper databaseHelper;
    private EditText locationNameEditText;
    private ImageView locationPreview;
    private TextView latitudeTextView;

    public AddLocationDialog(ObjDetectActivity activity) {
        this.activity = activity;
        this.newLocationInfo = new LocationInfo(); // Initialize a new Location object
        databaseHelper = new HeneikenDatabaseHelper(activity);
    }

    public void setResultImage(Bitmap image) {

        newLocationInfo.setImageData(convertBitmapToByteArrayWithByteBuffer(image));
    }

    public byte[] convertBitmapToByteArrayWithByteBuffer(Bitmap bitmap) {
        if (bitmap == null)
            return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // Adjust quality (1-100)
        byte[] byteArray = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(newBitmap.getWidth() * newBitmap.getHeight() * 4);
//        newBitmap.copyPixelsToBuffer(byteBuffer);
//        byteBuffer.rewind();
//        byte[] byteArray = new byte[byteBuffer.remaining()];
//        byteBuffer.get(byteArray);
        return byteArray;
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Inflate custom layout for the dialog
        View dialogView = activity.getLayoutInflater().inflate(R.layout.add_location_dialog, null);

        // Get references to dialog UI elements
        locationNameEditText = dialogView.findViewById(R.id.location_name_edittext);
        Button getLocationButton = dialogView.findViewById(R.id.get_location_button);
        locationPreview = dialogView.findViewById(R.id.location_preview);
        latitudeTextView = dialogView.findViewById(R.id.latitude_tv);
        // Set dialog title
        builder.setTitle("Add Location");

        // Set custom dialog view
        builder.setView(dialogView);

        // "Get Location" button click listener
        getLocationButton.setOnClickListener(v -> {
            // Check if location services are enabled
            LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(activity, "Please enable location services", Toast.LENGTH_SHORT).show();
            } else {

                getLocationTask();
            }
        });

        builder.setPositiveButton("Save", this::onClick);
        // Add Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static final int REQUEST_LOCATION_PERMISSION = 441; // Request code

    private void getLocationTask() {
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ActivityCompat.checkSelfPermission(activity, permissions[0]) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, permissions[1]) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity, permissions, REQUEST_LOCATION_PERMISSION);
        } else {

            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);

            LocationRequest locationRequest = LocationRequest.create();
//            locationRequest.setInterval(5000); // Update interval in milliseconds
//            locationRequest.setFastestInterval(2000); // Fastest update interval in milliseconds
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Accuracy level

            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult.getLocations().isEmpty()) {
                        Toast.makeText(activity, "Cannot get location", Toast.LENGTH_SHORT).show();
                    } else {

                        for (Location location : locationResult.getLocations()) {
                            // Handle the received location object here
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            // Use the latitude and longitude values as needed
                            String mapUrl = buildMapUrl(latitude, longitude);

                            // Update newLocation object with retrieved information
                            newLocationInfo.setMapUrl(mapUrl);
                            newLocationInfo.setMapUrl(latitude + "," + longitude);
                            // Show a confirmation message
                            latitudeTextView.setText("latitude: " + latitude + "; longitude:" + longitude);

                            // You can save the location to the database here
                            Glide.with(activity)
                                    .load(buildMapUrl(latitude, longitude))
                                    .placeholder(R.drawable.placeholder) // Optional placeholder image
                                    .into(locationPreview);
                        }
                    }
                }
            };


            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void dismissDialog() {
        // Dismiss the dialog (if showing)
        if (getDialog() != null && getDialog().isShowing()) {
            getDialog().dismiss();
        }
    }

    private String api_key = "AIzaSyAXqMvWpBJh3V5w52p7uHPZhVdW6y05FbQ";

    private String buildMapUrl(double latitude, double longitude) {
        // Replace with your actual map URL generation logic
        // For example, using Google Maps Static API
        return "https://maps.googleapis.com/maps/api/staticmap?center=" + latitude + "," + longitude + "&zoom=14&size=400x400&key=" + api_key;
    }

    private void onClick(DialogInterface dialog, int which) {
        // Check if location name is empty
        if (locationNameEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(activity, "Please enter a location name", Toast.LENGTH_SHORT).show();
            return;
        } else {
            // Handle Save action (e.g., open edit activity)
            newLocationInfo.setUser("testUser01");
            newLocationInfo.setName(locationNameEditText.getText().toString().trim());
            newLocationInfo.setDetect(activity.getDetectContext());
            newLocationInfo.setImageData(convertBitmapToByteArrayWithByteBuffer(activity.getDetectBitmap()));
            boolean insertionSuccessful = databaseHelper.insertLocation(newLocationInfo);

            if (insertionSuccessful) {
                // Location successfully inserted
                Toast.makeText(activity, "Location saved!", Toast.LENGTH_SHORT).show();
            } else {
                // Insertion failed
                Toast.makeText(activity, "Error saving location!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

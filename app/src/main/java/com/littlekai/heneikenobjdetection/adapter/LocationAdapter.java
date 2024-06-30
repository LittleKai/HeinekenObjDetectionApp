package com.littlekai.heneikenobjdetection.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.littlekai.heneikenobjdetection.R;
import com.littlekai.heneikenobjdetection.model.LocationInfo;
import com.littlekai.heneikenobjdetection.utils.BigCircleTransform;

import java.util.ArrayList;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private ArrayList<LocationInfo> locationInfos;
    private Context context;

    public LocationAdapter(ArrayList<LocationInfo> locationInfos, Context context) {
        this.locationInfos = locationInfos;
        this.context = context;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.location_list_item, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return locationInfos.size();
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {

        TextView locationName;
        TextView locationDescription;
        ImageView locationImage;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            locationName = itemView.findViewById(R.id.locationName);
            locationDescription = itemView.findViewById(R.id.locationDetect);
            locationImage = itemView.findViewById(R.id.locationImage);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationInfo locationInfo = locationInfos.get(position);

        holder.locationName.setText(locationInfo.getName());
        String decription = "<b><u><font color='red'>Map URL:</font></u></b> " + locationInfo.getMapUrl()
                + "<br><b><u><font color='red'>Date:</font></u></b> " + locationInfo.getDateTime()
                + "<br><b><u><font color='red'>User:</font></u></b> " + locationInfo.getUser() + "<br>" + locationInfo.getDetect();

        holder.locationDescription.setText(Html.fromHtml(decription), TextView.BufferType.SPANNABLE);

        // Load and set image (you might need to use an image loading library)
        Glide.with(context)
                .load(locationInfo.getImageData())
                .placeholder(R.drawable.error_image)
//                .transform(new BigCircleTransform(context))
                .into(holder.locationImage);

        // Set click listener for the item view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocationDetailsDialog(locationInfo);
            }
        });
    }

    private void showLocationDetailsDialog(LocationInfo locationInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String decription = "Map URL: " + locationInfo.getMapUrl()
                + "\nDate: " + locationInfo.getDateTime()
                + "\nUser: " + locationInfo.getUser();
        // Set dialog title and message (using location info)
        builder.setTitle(locationInfo.getName())
                .setMessage(decription);

        // Add Edit button
        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle Edit action (e.g., open edit activity)
                Toast.makeText(context, "Edit location for " + locationInfo.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        // Add Cancel button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle Cancel action (e.g., dismiss dialog)
                dialog.dismiss();
            }
        });

        builder.show();
    }
}


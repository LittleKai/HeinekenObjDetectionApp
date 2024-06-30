package com.littlekai.heneikenobjdetection.model;

public class LocationInfo {
    private int id;
    private String name;
    private String mapUrl;
    private byte[] imageData;
    private String detect;
    private String dateTime;
    private String user;

    // Constructor
    public LocationInfo(int id, String name, String mapUrl, byte[] imageData, String detect, String dateTime, String user) {
        this.id = id;
        this.name = name;
        this.mapUrl = mapUrl;
        this.imageData = imageData;
        this.detect = detect;
        this.dateTime = dateTime;
        this.user = user;
    }

    public LocationInfo() {

    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMapUrl() {
        return mapUrl;
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = mapUrl;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public String getDetect() {
        return detect;
    }

    public void setDetect(String detect) {
        this.detect = detect;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }


}

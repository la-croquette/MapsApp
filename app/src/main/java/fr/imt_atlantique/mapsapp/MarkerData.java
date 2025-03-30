package fr.imt_atlantique.mapsapp;

public class MarkerData {
    public double latitude;
    public double longitude;
    public String title;
    public String description;
    public String imagePath;

    public MarkerData(double lat, double lng, String title, String description, String imagePath) {
        this.latitude = lat;
        this.longitude = lng;
        this.title = title;
        this.description = description;
        this.imagePath = imagePath;
    }
}

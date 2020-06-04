package com.example.homework07parta_801135224;

import java.io.Serializable;
import java.util.Objects;

public class LocationDetails implements Serializable {
    String name, address;
    Double lat,lng;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public LocationDetails() {
    }

    @Override
    public String toString() {
        return "LocationDetails{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", lat=" + lat +
                '}';
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this) return true;
        if (!(obj instanceof LocationDetails)) {
            return false;
        }
        LocationDetails place = (LocationDetails) obj;
        return name.equals(place.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

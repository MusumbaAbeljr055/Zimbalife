package com.example.azimbalife.Domain;

import java.io.Serializable;

public class DoctorsModel implements Serializable {

    private String Address;
    private String Biography;
    private int Id;
    private String Name;
    private String Picture;
    private String Special;
    private int Expriense;
    private String Cost;
    private String Date;
    private String Time;
    private String Location;
    private String Mobile;
    private String Patiens;
    private double Rating;
    private String Site;
    private String email;  // <-- new field added

    public DoctorsModel() {
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getBiography() {
        return Biography;
    }

    public void setBiography(String biography) {
        Biography = biography;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPicture() {
        return Picture;
    }

    public void setPicture(String picture) {
        Picture = picture;
    }

    public String getSpecial() {
        return Special;
    }

    public void setSpecial(String special) {
        Special = special;
    }

    public int getExpriense() {
        return Expriense;
    }

    public void setExpriense(int expriense) {
        Expriense = expriense;
    }

    public String getCost() {
        return Cost;
    }

    public void setCost(String cost) {
        Cost = cost;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String mobile) {
        Mobile = mobile;
    }

    public String getPatiens() {
        return Patiens;
    }

    public void setPatiens(String patiens) {
        Patiens = patiens;
    }

    public double getRating() {
        return Rating;
    }

    public void setRating(double rating) {
        Rating = rating;
    }

    public String getSite() {
        return Site;
    }

    public void setSite(String site) {
        Site = site;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

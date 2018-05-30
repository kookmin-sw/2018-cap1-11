package com.c.myapplication.item;

//import com.google.gson.annotations.SerializedName;

public class treeItem {

    public int member_seq;
    public double latitude;
    public double longitude;
    public double humidity;
    public double temperature;
    public String mac;



    public String getString() {
        return "treeItem{" +
                "member_seq=" + member_seq +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", humidity='" + humidity + '\'' +
                ", temperature='" + temperature + '\'' +
                ", mac'" + mac + '\'' +

                '}';
    }

}
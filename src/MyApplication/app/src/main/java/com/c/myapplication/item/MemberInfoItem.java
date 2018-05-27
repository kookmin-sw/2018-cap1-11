package com.c.myapplication.item;

import com.google.gson.annotations.SerializedName;

public class MemberInfoItem {

    public int seq;
    public String phone;
    public String name;
    public String email;
    @SerializedName("member_icon_filename") public String memberIconFilename;


    public String toString() {
        return "MemberInfoItem{" +
                "seq=" + seq +
                ", phone='" + phone + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", memberIconFilename='" + memberIconFilename + '\'' +

                '}';
    }

}

package com.server;

import org.json.JSONObject;

public class WarningMessage {

    private String nickname;
    private String latitude;
    private String longitude;
    private String dangertype;

    public WarningMessage(String nickname, String latitude, String longitude, String dangertype) {
        this.nickname = nickname;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dangertype = dangertype;

    }

    public WarningMessage(JSONObject text) {

        this.nickname = text.getString("nickname");
        this.latitude = text.getString("latitude");
        this.longitude = text.getString("longitude");
        this.dangertype = text.getString("dangertype");
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname(String nickname) {
        return nickname;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLatitude(String latitude) {
        return latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLongitude(String longitude) {
        return longitude;
    }

    public void setdangertype(String dangertype) {
        this.dangertype = dangertype;
    }

    public String getdangertype(String dangertype) {
        return dangertype;
    }

    public JSONObject json() {
        JSONObject json = new JSONObject();
        json.put("nickname", nickname);
        json.put("latitude", latitude);
        json.put("longitude", longitude);
        json.put("dangertype", dangertype);
        return json;
    }
}

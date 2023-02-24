package com.server;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;

public class WarningMessage {

    private String nickname;
    private Double latitude;
    private Double longitude;
    private String dangertype;
    private LocalDateTime sent;

    public WarningMessage(String nickname, Double latitude, Double longitude, String dangertype, LocalDateTime sent) {
        this.nickname = nickname;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dangertype = dangertype;
        this.sent = sent;
    }

    public WarningMessage() {

    }

    public WarningMessage(JSONObject text) {

        this.nickname = text.getString("nickname");
        this.latitude = text.getDouble("latitude");
        this.longitude = text.getDouble("longitude");
        this.dangertype = text.getString("dangertype");
        this.sent = LocalDateTime.parse(text.getString("sent"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
    }

    public long dateAsInt() {
        return sent.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public void setSent(long epoch) {
        sent = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
    }

    public LocalDateTime getSent(String sent) {
        ZonedDateTime zone = ZonedDateTime.parse(sent,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC")));
        return zone.toLocalDateTime();
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname(String nickname) {
        return nickname;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLatitude(Double latitude) {
        return latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLongitude(Double longitude) {
        return longitude;
    }

    public void setdangertype(String dangertype) {
        this.dangertype = dangertype;
    }

    public String getdangertype(String dangertype) {
        return dangertype;
    }

    public JSONObject json() throws SQLException {
        JSONObject json = new JSONObject();
        json.put("nickname", nickname);
        json.put("latitude", latitude);
        json.put("longitude", longitude);
        json.put("dangertype", dangertype);
        json.put("sent", dateAsInt());
        return json;
    }
}

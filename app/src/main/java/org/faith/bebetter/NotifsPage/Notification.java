package org.faith.bebetter.NotifsPage;


//THIS IS USED FOR THE NOTIFICATION LIST UNDER THE "NOTIFS" FRAGMENT.

public class Notification {
    private long timestamp;
    private String from;
    private String type;
    private String experienceKey;
    private String image_thumbnail;

    public Notification(){

    }

    public Notification(long timestamp, String from, String type, String experienceKey, String image){
        this.timestamp = timestamp;
        this.from = from;
        this.type = type;
        this.experienceKey = experienceKey;
        this.image_thumbnail = image_thumbnail;
    }

    public String getImage_thumbnail() {
        return image_thumbnail;
    }

    public void setImage_thumbnail(String image_thumbnail) {
        this.image_thumbnail = image_thumbnail;
    }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() { return type; }
    public void setType(String type) {
        this.type = type;
    }

    public String getExperienceKey() {
        return experienceKey;
    }
    public void setExperienceKey(String experienceKey) {
        this.experienceKey = experienceKey;
    }
}

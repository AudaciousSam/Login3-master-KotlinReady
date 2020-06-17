package org.faith.bebetter.FeedPage;

//THIS IS USED FOR THE NOTIFICATION LIST UNDER THE "NOTIFS" FRAGMENT AND IN THE FEED, WHEN CLICKING A MEMORY

public class ExperienceFullHD {
    private long timestamp;
    private String image;
    private String from;


    public ExperienceFullHD(){

    }

    public ExperienceFullHD(long timestamp, String image, String from){
        this.timestamp = timestamp;
        this.image = image;
        this.from = from;

    }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }
}

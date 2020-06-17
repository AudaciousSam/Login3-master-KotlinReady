package org.faith.bebetter.FeedPage;

public class FeedExperience {
    private long timestamp;
    private String experienceKey;
    private String type;

    public FeedExperience(){

    }

    public FeedExperience(long timestamp, String experienceKey, String type){
        this.timestamp = timestamp;
        this.experienceKey = experienceKey;
        this.type = type;
    }

    public long getTimestamp() { return timestamp; }

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getExperienceKey() {
        return experienceKey;
    }

    public void setExperienceKey(String experienceKey) {
        this.experienceKey = experienceKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

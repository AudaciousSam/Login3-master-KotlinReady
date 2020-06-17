package org.faith.bebetter.YouPage;

public class User {

    public String name;
    public String image;
    public String image_thumbnail;

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage_thumbnail() {
        return image_thumbnail;
    }

    public void setImage_thumbnail(String image_thumbnail) {
        this.image_thumbnail = image_thumbnail;
    }

    public User(String name, String image, String image_thumbnail) {
        this.name = name;
        this.image = image;
        this.image_thumbnail = image_thumbnail;
    }
}

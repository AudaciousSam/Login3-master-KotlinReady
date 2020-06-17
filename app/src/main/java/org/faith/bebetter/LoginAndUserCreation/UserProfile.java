package org.faith.bebetter.LoginAndUserCreation;

public class UserProfile {
    public String name;

    public UserProfile(){

    }

    public UserProfile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

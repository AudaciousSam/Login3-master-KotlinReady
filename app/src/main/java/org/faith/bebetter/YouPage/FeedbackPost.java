package org.faith.bebetter.YouPage;

public class FeedbackPost {
    public String user;
    public String title;
    public String text;

    public FeedbackPost(){}

    public FeedbackPost(String user, String title, String text){
       this.user = user;
       this.title = title;
       this.text = text;
    }
}

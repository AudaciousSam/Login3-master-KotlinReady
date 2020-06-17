package org.faith.bebetter.NotifsPage;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class NotificationChannels extends Application {

    public static final String CHANNEL_1_ID = "Friend Requests";
    public static final String CHANNEL_2_ID = "Experience Invite";

    @Override
    public void onCreate() {
        super.onCreate();

        createNootificationChannels();

    }

    private void createNootificationChannels(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel friendRequestChannel = new NotificationChannel(
                CHANNEL_1_ID,
                "Friend Requests",
                NotificationManager.IMPORTANCE_HIGH
            );

            //This is for notification settings.
            friendRequestChannel.setDescription("This is for when you sent or receive friend requests. <3 ");

            NotificationChannel experienceInviteChannel = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Experience Invite",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            //This is for notification settings.
            experienceInviteChannel.setDescription("This is for when you receive an experience invite. <3 "); //This text comes up within the android settings app.

            NotificationManager manager = getSystemService(NotificationManager.class);

            manager.createNotificationChannel(friendRequestChannel);
            manager.createNotificationChannel(experienceInviteChannel);
        }
    }

}

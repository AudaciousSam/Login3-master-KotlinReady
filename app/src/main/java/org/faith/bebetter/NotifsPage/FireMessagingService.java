package org.faith.bebetter.NotifsPage;

import android.app.PendingIntent;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.faith.bebetter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.faith.bebetter.MainActivity;

import static org.faith.bebetter.NotifsPage.NotificationChannels.CHANNEL_1_ID;

public class FireMessagingService extends FirebaseMessagingService {

    private static final String TAG = "UPDATE TOKEN";
    private DatabaseReference mNotificationDatabase;
    private FirebaseAuth mFirebaseUID;
    private String idToString;

    //If we are inside the app - Whenever a message is received, we do something.
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //TIP FOR THE FUTURE: If I want to build different notifications with different channels.
        //https://firebase.google.com/docs/reference/android/com/google/firebase/messaging/RemoteMessage.Notification.html
        //just use if statements, that checks on title, and then goes into the notifications and the channels.
        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationBody = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        String from_user_id = remoteMessage.getData().get("from_user_id");

        //Right here we sent people to the app from notification.
        Intent clickNotification = new Intent(this, MainActivity.class);
        clickNotification.setAction("OPEN_NOFIFS");
        clickNotification.putExtra("user_id", from_user_id);

        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                clickNotification,
                PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationId = (int) System.currentTimeMillis();

        NotificationCompat.Builder friendRequest = new NotificationCompat.Builder(this, CHANNEL_1_ID) // CHANNEL_1_ID = Used for high priority.
                .setSmallIcon(R.drawable.ic_bebetter_logo_black_white)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL);
        friendRequest.setContentIntent(contentIntent);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, friendRequest.build());

    }
}

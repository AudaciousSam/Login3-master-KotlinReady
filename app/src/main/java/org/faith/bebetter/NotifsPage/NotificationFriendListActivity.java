package org.faith.bebetter.NotifsPage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.faith.bebetter.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.faith.bebetter.YouPage.Friends;
import org.faith.bebetter.MainActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NotificationFriendListActivity extends AppCompatActivity {

    private RecyclerView friendList;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference friendListDatabase;
    private DatabaseReference userDatabase;
    private String currentUser;
    private DatabaseReference notificationDatabase;
    private Button doneButton;
    private DatabaseReference experienceDatabase;

    //Imagedatabase
    private StorageReference ImageStorage;
    private DatabaseReference friends;
    private DatabaseReference feedDatabase;
    private DatabaseReference friendRankDatabase;
    private DatabaseReference experienceCount;
    private String notificationKey;
    private String imageKey;
    private String experienceKey;
    private  List<String> invitations = new ArrayList<>();
    private List<Integer> localPosition = new ArrayList<>(Collections.nCopies(1000, 0));
    private List<String> participantKeys;
    private int lastCount;
    private int lastCountIncremented;

    private List<String> myList = new ArrayList<>();

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        //UI connect
        friendList = findViewById(R.id.rvFriendList);
        doneButton = findViewById(R.id.btn_friend_list_done);

        //Firebase connect
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        ImageStorage = FirebaseStorage.getInstance().getReference().child("Experiences");
        feedDatabase = FirebaseDatabase.getInstance().getReference().child("Feeds");
        experienceDatabase = FirebaseDatabase.getInstance().getReference().child("Experiences");
        experienceCount = FirebaseDatabase.getInstance().getReference().child("ExperienceCount");
        friendRankDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");


        //Friendlist
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser().getUid();
        friendListDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUser);
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        friendList.setHasFixedSize(true);
        friendList.setLayoutManager(new LinearLayoutManager(this));
        friendListDatabase.keepSynced(true); //does so it doesn't download name every time. Should try to convert firebaseReference to DatabaseReference, to get it.
        userDatabase.keepSynced(true);

        // ------------------------------------- DATA UPLOAD ------------------------------------- //

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        experienceKey = extras.getString("EXTRA_KEY");
        imageKey = extras.getString("EXTRA_IMAGEKEY");
        //String inviteFrom = extras.getString("EXTRA_FROM");
        Uri resultUri = Uri.parse(extras.getString("EXTRA_URI"));


/*
        Query feedImages = experienceDatabase.child(experienceKey).child("fullHD").limitToFirst(2);
        System.out.println(feedImages);
        feedImages.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataSnapshot.getChildrenCount();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

 */

        //Get a unique name, which will also ensure one notification.
        long serverTime = Timestamp.now().getSeconds();
        //This should be enough for every human on Earth to make one experience everyday, for 100 years.
        long beBetterLong = 3650000000000000L;
        notificationKey = String.valueOf(beBetterLong - serverTime);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //---------------------- NOTIFICATION ----------------------//

                HashMap<String, Object> experienceLink = new HashMap<>();
                experienceLink.put("timestamp", ServerValue.TIMESTAMP);
                experienceLink.put("experienceKey", experienceKey);
                experienceLink.put("type", "together");

                //---------------------- ADD YOUR PARTICIPATION ----------------------//
                experienceDatabase.child(experienceKey).child("participants").child(currentUser).child("timestamp").setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //Then we go into the experience participants list. Check on names and add experience to their memories.
                        experienceDatabase.child(experienceKey).child("participants").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Map<String, Object> friends = (HashMap<String,Object>) dataSnapshot.getValue();
                                participantKeys = new ArrayList<>(friends.keySet());
                               // lastCount = 0;

                                for (int i = 0; i < participantKeys.size() ; i++) {
                                    // keys.get(i) = userName
                                    //Here we go through all the participants and give them the experience in their feed.
                                    feedDatabase.child(participantKeys.get(i)).child(experienceKey).setValue(experienceLink);

                                    /*
                                    for (int k = 0; k < participantKeys.size() ; k++) {

                                    //ADD COUNT : When you join an experience, everyone else, who've participated, get's a plus count to your friendlist rank/count.
                                        if (friendRankDatabase.child(participantKeys.get(i)).child(participantKeys.get(k)).child("participationCount").getKey() == null){
                                            friendRankDatabase.child(participantKeys.get(i)).child(participantKeys.get(k)).child("participationCount").setValue(1);

                                        } else {
                                            lastCount = Integer.parseInt(Objects.requireNonNull(friendRankDatabase.child(participantKeys.get(i)).child(participantKeys.get(k)).child("participationCount").getKey()));
                                            lastCountIncremented = lastCount + 1;
                                            friendRankDatabase.child(participantKeys.get(i)).child(participantKeys.get(k)).child("participationCount").setValue(lastCountIncremented);
                                        }
                                    }

                                     */
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        HashMap<String, Object> notificationData = new HashMap<>();
                        notificationData.put("from", currentUser);
                        notificationData.put("type", "experience completed");
                        notificationData.put("timestamp", ServerValue.TIMESTAMP);
                        notificationData.put("experienceKey", experienceKey);
                        notificationData.put("image_thumbnail", imageKey+"_thumbnail");

                        //TODO
                        //Take list, compare it to your friendlist. Only inform those of your participation.

                        //Then we go into the experience invited list. And give them a notification.
                        experienceDatabase.child(experienceKey).child("invited").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Map<String, Object> friends = (HashMap<String,Object>) dataSnapshot.getValue();
                                List<String> keys = new ArrayList<>(friends.keySet());

                                for (int i = 0; i < keys.size() ; i++) {

                                    //Gives everyone a notification you've joined.
                                    if (keys.get(i).equals(currentUser)){} else {notificationDatabase.child(keys.get(i)).child(notificationKey).setValue(notificationData);}
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

                //---------------------- SEND NOTIFICATION ABOUT PARTICIPATION ----------------------//
                for (int i = 0; i < invitations.size() ; i++) {

                    //Notification data
                    HashMap<String, Object> notificationDataFriendList = new HashMap<>();
                    notificationDataFriendList.put("from", currentUser);
                    notificationDataFriendList.put("type", "experience invite");
                    notificationDataFriendList.put("timestamp", ServerValue.TIMESTAMP);
                    notificationDataFriendList.put("experienceKey", experienceKey);
                    notificationDataFriendList.put("image_thumbnail", imageKey+"_thumbnail");

                    //everyone clicked, is invited.
                    notificationDatabase.child(invitations.get(i)).child(experienceKey).setValue(notificationDataFriendList);

                    //everyone invited, is added to experience "invited" section
                    experienceDatabase.child(experienceKey).child("invited").child(invitations.get(i)).child("timestamp").setValue(ServerValue.TIMESTAMP);
                }

                startActivity(new Intent(NotificationFriendListActivity.this, MainActivity.class));
            }
        });


        //-------------------- RIPPLE PRODUCTS --------------------// Let's everyone see it and allow them to join.

        /*
        //Alright, so basically we take a look at currentUserId's friendlist. Then take that list, take the keys and go into UserList and write.
        friends = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);

        friends.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> friends = (HashMap<String,Object>) dataSnapshot.getValue();
                List<String> keys = new ArrayList<>(friends.keySet());
                for (int i = 0; i <keys.size() ; i++) {

                    //Here we go through all the friends of the user.
                    feedDatabase.child(keys.get(i)).child(experienceKey).setValue(experienceLink);
                }
                feedDatabase.child(currentUserId).child(experienceKey).setValue(experienceLink);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Then we go into the feed of the user we got the invite from, and do the same thing.
        friends = FirebaseDatabase.getInstance().getReference().child("Friends").child(inviteFrom);

        friends.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> friends = (HashMap<String,Object>) dataSnapshot.getValue();
                List<String> keys = new ArrayList<>(friends.keySet());
                for (int i = 0; i <keys.size() ; i++) {

                    //Here we go through all the friends of the user.
                    feedDatabase.child(keys.get(i)).child(experienceKey).setValue(experienceLink);
                }
                feedDatabase.child(inviteFrom).child(experienceKey).setValue(experienceLink);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
         */

    }

    //The Friendlist
    @Override
    public void onStart(){
        super.onStart();

        //We are essentially ordering by friends first to last.
        long searchText = 0;
        Query firebaseSearchQuery = friendListDatabase.orderByChild("timestamp").startAt(searchText).endAt(searchText + "\uf8ff");

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(firebaseSearchQuery, Friends.class)
                        .setLifecycleOwner(this) // This might be of interest, if I don't want to call the server every time, people swipe away and back to a place in app.
                        .build();

        FirebaseRecyclerAdapter<Friends, FriendViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendViewHolder>(options) {

            @NonNull
            @Override
            public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new FriendViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_view_user, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendViewHolder friendViewHolder, int position, @NonNull Friends friends) {

                if(localPosition.get(position) == 1){
                    friendViewHolder.itemView.setBackgroundResource(R.drawable.friend_clicked);
                    friendViewHolder.setTextColor(Color.BLACK);}
                else{
                    friendViewHolder.itemView.setBackgroundResource(R.drawable.friend_unclicked);
                    friendViewHolder.setTextColor(Color.WHITE);
                }

                //Now we get name from userDatabase
                String list_user_id = getRef(position).getKey();
                userDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = "";
                        if(dataSnapshot.hasChild("image_thumbnail")){
                            userThumb = dataSnapshot.child("image_thumbnail").getValue().toString();
                        }

                        //Checks if activity is destroyed.
                        Activity act = NotificationFriendListActivity.this;
                        //assert act != null;
                        if(act != null) {
                            friendViewHolder.setName(userName);
                            friendViewHolder.setUserImage(userThumb, NotificationFriendListActivity.this);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                final String user_id = getRef(position).getKey();
                //Inviting friends
                friendViewHolder.mView.setOnClickListener(new View.OnClickListener() {

                    //int state = 0;
                    @Override
                    public void onClick(View v) {

                        if (localPosition.get(position).equals(0)){

                            invitations.add(user_id);
                            localPosition.set(position, 1);
                            notifyItemChanged(position);

                        }

                        else if (localPosition.get(position).equals(1)){

                            invitations.remove(user_id);
                            localPosition.set(position, 0);
                            notifyItemChanged(position);

                        }
                    }
                });
            }
        };
        friendList.setAdapter(firebaseRecyclerAdapter);
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public FriendViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }
        //Get name
        public void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.textViewCard);
            userNameView.setText(name);
        }

        //SetTextColor
        public void setTextColor(int color) {
            TextView userNameView = mView.findViewById(R.id.textViewCard);
            userNameView.setTextColor(color);
        }

        //Get image.
        public void setUserImage(String image_thumbnail, Context ctx){
            ImageView userImageView = mView.findViewById(R.id.imageViewCard);
            Glide.with(getApplicationContext()).load(image_thumbnail).into(userImageView);
        }
    }
}

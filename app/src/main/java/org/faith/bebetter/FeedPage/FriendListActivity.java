package org.faith.bebetter.FeedPage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.faith.bebetter.R;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.faith.bebetter.YouPage.Friends;
import org.faith.bebetter.MainActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FriendListActivity extends AppCompatActivity {

    private RecyclerView friendList;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference friendListDatabase;
    private DatabaseReference userDatabase;
    private String currentUser;
    private DatabaseReference notificationDatabase;
    private Button doneButton;
    private DatabaseReference experienceDatabase;
    private List<String> invitations = new ArrayList<>();
    //Only works until you have 1000 friends.
    private List<Integer> localPosition = new ArrayList<>(Collections.nCopies(1000, 0));
    private DatabaseReference feedDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        //UI connect
        friendList = findViewById(R.id.rvFriendList);
        doneButton = findViewById(R.id.btn_friend_list_done);

        //Firebase connect
        firebaseAuth = FirebaseAuth.getInstance();
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        experienceDatabase = FirebaseDatabase.getInstance().getReference().child("Experiences");

        //Friendlist
        currentUser = firebaseAuth.getCurrentUser().getUid();
        friendListDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUser);
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        friendList.setHasFixedSize(true);
        friendList.setLayoutManager(new LinearLayoutManager(this));
        friendListDatabase.keepSynced(true); //does so it doesn't download name every time. Should try to convert firebaseReference to DatabaseReference, to get it.
        userDatabase.keepSynced(true);
        feedDatabase = FirebaseDatabase.getInstance().getReference().child("Feeds");


                /*
                //Tells you, you've made the experience. Should be moved to notifications, if ever implemented.
                HashMap<String, Object> experienceLink = new HashMap<>();
                experienceLink.put("timestamp", ServerValue.TIMESTAMP);
                experienceLink.put("experienceKey", experienceKey);
                experienceLink.put("type", "created");
                feedDatabase.child(currentUser).child(experienceKey).setValue(experienceLink);
                 */


        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(FriendListActivity.this, "Time to relax my friend, As they join, we'll show it here", Toast.LENGTH_LONG).show();

                //WE get the key.
                Intent intent = getIntent();
                Bundle extras = intent.getExtras();
                String experienceKey = extras.getString("EXTRA_KEY");

                for (int i = 0; i < invitations.size() ; i++) {

                    //Notification data
                    HashMap<String, Object> notificationDataFriendList = new HashMap<>();
                    notificationDataFriendList.put("from", currentUser);
                    notificationDataFriendList.put("type", "experience invite");
                    notificationDataFriendList.put("timestamp", ServerValue.TIMESTAMP);
                    notificationDataFriendList.put("experienceKey", experienceKey);
                    notificationDataFriendList.put("image_thumbnail", experienceKey+"_thumbnail");

                    //everyone clicked, is invited.
                    notificationDatabase.child(invitations.get(i)).child(experienceKey).setValue(notificationDataFriendList);


                    //everyone invited, is added to experience "invited" section
                    experienceDatabase.child(experienceKey).child("invited").child(invitations.get(i)).child("timestamp").setValue(ServerValue.TIMESTAMP);
                }

                startActivity(new Intent(FriendListActivity.this, MainActivity.class));

            }
        });
    }

    //The Friendlist
    @Override
    public void onStart(){
        super.onStart();

        //We are essentially ordering by friends first connect with, to last.
        long searchText = 0;
        Query firebaseSearchQuery = friendListDatabase.orderByChild("timestamp").startAt(searchText).endAt(searchText + "\uf8ff");

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(firebaseSearchQuery, Friends.class)
                        // This might be of interest, if I don't want to call the server every time, people swipe away and back to a place in app.
                        .build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendListActivity.FriendViewHolder>(options) {

            @NonNull
            @Override
            public FriendListActivity.FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new FriendListActivity.FriendViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_view_user, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendListActivity.FriendViewHolder friendViewHolder, int position, @NonNull Friends friends) {


                if(localPosition.get(position) == 1){
                    friendViewHolder.itemView.setBackgroundResource(R.drawable.friend_clicked);
                    friendViewHolder.setTextColor(Color.BLACK);}
                else{
                    friendViewHolder.itemView.setBackgroundResource(R.drawable.friend_unclicked);
                    friendViewHolder.setTextColor(Color.WHITE);
            }


            //Now we get name from userDatabase
                final String user_id = getRef(position).getKey();

                userDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String userName = dataSnapshot.child("name").getValue().toString();

                        String userThumb = "";
                        if(dataSnapshot.hasChild("image_thumbnail")){
                            userThumb = dataSnapshot.child("image_thumbnail").getValue().toString();
                        }

                        //Checks if activity is destroyed.
                        Activity act = FriendListActivity.this;
                        assert act != null;
                        if(!act.isDestroyed()) {
                            friendViewHolder.setName(userName);
                            friendViewHolder.setUserImage(userThumb, FriendListActivity.this);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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

                        /*
                        Toast.makeText(FriendListActivity.this, position, Toast.LENGTH_SHORT).show();


                        if (state == 0){

                            //Change color on click.
                            friendViewHolder.mView.setBackgroundResource(R.drawable.friend_clicked);

                            invitations.add(user_id);
                            state = 1;

                            Toast.makeText(FriendListActivity.this, invitations.toString(), Toast.LENGTH_SHORT).show();

                        } else if (state == 1){

                            invitations.remove(user_id);
                            friendViewHolder.mView.setBackgroundResource(R.drawable.friend_unclicked);

                            state = 0;

                            Toast.makeText(FriendListActivity.this, invitations.toString(), Toast.LENGTH_SHORT).show();
                        }

                         */



                    }

                });
            }
        };

        friendList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {

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
            Glide.with(ctx).load(image_thumbnail).into(userImageView);
        }
    }

}

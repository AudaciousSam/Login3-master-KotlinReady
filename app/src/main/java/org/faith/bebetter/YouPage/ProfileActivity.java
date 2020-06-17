package org.faith.bebetter.YouPage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.bumptech.glide.Glide;
import com.faith.bebetter.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName;
    private Button mProfileSendReqBtn;
    private Button mDeclineBtn;
    private RecyclerView friendListProfile;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference friendListDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference userDatabaseProfile;

    private String mCurrent_state;
    private String currentUserId;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference friendDatabase;
    private FirebaseUser mCurrent_user;
    private TextView textViewFriendListTitle;
    private TextView textViewProfileName;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Current user we are looking at, in our "search friend list"
        final String user_id = getIntent().getStringExtra("user_id");

        //UI connect
        mProfileImage = findViewById(R.id.ivProfilePicFragment);
        mProfileSendReqBtn = findViewById(R.id.profile_send_req_btn);
        mDeclineBtn = findViewById(R.id.profile_decline_req_btn);
        friendListProfile = findViewById(R.id.rvFriendListProfile);
        textViewFriendListTitle = findViewById(R.id.textViewFriendListTitle);
        textViewProfileName = findViewById(R.id.textViewProfileName);

        //Firebase connect
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        //used for deletion of specific items.
        friendDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseDatabase = FirebaseDatabase.getInstance();

        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        firebaseAuth = FirebaseAuth.getInstance();


        //Friendlist
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        friendListDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(user_id);
        userDatabaseProfile = FirebaseDatabase.getInstance().getReference().child("Users");

        friendListProfile.setAdapter(null);
        friendListProfile.setHasFixedSize(true);
        friendListProfile.setLayoutManager(new LinearLayoutManager(this));
        friendListDatabase.keepSynced(true); //does so it doesn't download name every time. Should try to convert firebaseReference to DatabaseReference, to get it.
        userDatabaseProfile.keepSynced(true);

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(ProfileActivity.this, FullHDProfileImageActivity.class);
                profileIntent.putExtra("EXTRA_USER_ID", user_id);
                startActivity(profileIntent);
                finish();
            }
        });

        //By Default the decline button is invisible and doesn't work.
        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        //friend request sent or not.
        mCurrent_state = "not_friends";

        //ProfilePicture
        FirebaseDatabase.getInstance().getReference().child("Users").child(user_id).child("image_thumbnail").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!= null){
                    String uri = dataSnapshot.getValue().toString();

                    Picasso.get().load(uri).fit().centerCrop().into(mProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // --------------- CHECK DATABASE FOR STATE ------------
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Show profile picture.
                /*if(dataSnapshot.getValue()!= null){
                    String uri = dataSnapshot.getValue().toString();

                    Picasso.get().load(uri).fit().into(mProfileImage);
                }*/
               /* String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                Picasso.get().load(image).into(mProfileImage);*/


                //--------------- FRIENDS LIST / REQUEST FEATURE ----- LOOKING AT THE DATABASE FOR FRIEND TYPE
                mFriendDatabase.child(mCurrent_user.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)){

                            mCurrent_state = Objects.requireNonNull(dataSnapshot.child(user_id).child("friend_type").getValue()).toString();

                            if (mCurrent_state.equals("not_friends")) {
                                mCurrent_state = "not_friends"; //Local state
                                mProfileSendReqBtn.setText(getString(R.string.send_friend_request));
                                mProfileSendReqBtn.setBackgroundResource(R.drawable.button_attention);
                                mProfileSendReqBtn.setTextColor(Color.BLACK);

                                //By Default the decline button is invisible and doesn't work.
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            } else if(mCurrent_state.equals("friends")) {
                                //mCurrent_state = "friends"; //Local state
                                mProfileSendReqBtn.setText(getString(R.string.Unfriend));
                                mProfileSendReqBtn.setBackgroundResource(R.drawable.button_dull);
                                mProfileSendReqBtn.setTextColor(Color.WHITE);

                                //By Default the decline button is invisible and doesn't work.
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                //--------------- FRIENDS LIST / REQUEST FEATURE ----- LOOKING AT THE DATABASE FOR REG TYPE
                mFriendReqDatabase.child(mCurrent_user.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)) {
                            mCurrent_state = Objects.requireNonNull(dataSnapshot.child(user_id).child("request_type").getValue()).toString();

                            if (mCurrent_state.equals("req_received")) {
                                //mCurrent_state = "req_received"; //Local state
                                //Change button
                                mProfileSendReqBtn.setText(getString(R.string.acceptFriendRequest));
                                mProfileSendReqBtn.setBackgroundResource(R.drawable.button_attention);
                                mProfileSendReqBtn.setTextColor(Color.BLACK);

                                //By Default the decline button is invisible and removed.
                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);

                            } else if (mCurrent_state.equals("req_sent")) {
                                //mCurrent_state = "req_sent"; //Local state
                                //Change button
                                mProfileSendReqBtn.setText(getString(R.string.CancelFriendRequest));
                                mProfileSendReqBtn.setBackgroundResource(R.drawable.button_dull);
                                mProfileSendReqBtn.setTextColor(Color.WHITE);

                                /*
                                //Change button size.
                                ViewGroup.LayoutParams params = mProfileSendReqBtn.getLayoutParams();
                                int myWidth = mProfileSendReqBtn.getWidth();
                                params.width = (myWidth/2);
                                mProfileSendReqBtn.setLayoutParams(params);
                                */


                                //By Default the decline button is invisible and doesn't work.
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }else if (mCurrent_state.equals("not_friends")) {
                                //mCurrent_state = "not_friends"; //Local state
                                //Change button
                                mProfileSendReqBtn.setText(getString(R.string.send_friend_request));
                                mProfileSendReqBtn.setBackgroundResource(R.drawable.button_attention);
                                mProfileSendReqBtn.setTextColor(Color.BLACK);

                                //By Default the decline button is invisible and doesn't work.
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // --------------- DECLINE CLICKED ------------
        mDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrent_state.equals("req_received")){

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type").setValue("not_friends").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type").setValue("not_friends").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        mCurrent_state = "not_friends"; //Local state
                                        //mProfileSendReqBtn.setText(getString(R.string.send_friend_request));
                                        //mProfileSendReqBtn.setBackgroundResource(R.drawable.button_attention);
                                        //mProfileSendReqBtn.setTextColor(Color.BLACK);

                                    }
                                });
                            }else{
                                Toast.makeText(ProfileActivity.this, "Failed sending request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        // --------------- BUTTON CLICKED ------------
        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // ---------- NOT FRIENDS STATE ----------
                if (mCurrent_state.equals("not_friends")){

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type").setValue("req_sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type").setValue("req_received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        mCurrent_state = "req_sent"; //Local state

                                        HashMap<String, Object> notificationData = new HashMap<>();
                                        notificationData.put("from", mCurrent_user.getUid());
                                        notificationData.put("type", "friend request");
                                        notificationData.put("timestamp", ServerValue.TIMESTAMP);

                                        mNotificationDatabase.child(user_id).push().setValue(notificationData);
                                        //mProfileSendReqBtn.setText(getString(R.string.CancelFriendRequest));
                                        //mProfileSendReqBtn.setBackgroundResource(R.drawable.button_dull);
                                        //mProfileSendReqBtn.setTextColor(Color.WHITE);
                                    }
                                });
                            }else{
                                Toast.makeText(ProfileActivity.this, "Failed sending request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                // ---------- CANCEL REQUEST STATE ----------
                if (mCurrent_state.equals("req_sent")){
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type").setValue("not_friends").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type").setValue("not_friends");
                            }else{
                                Toast.makeText(ProfileActivity.this, "Failed sending request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                // ---------- REQ RECEIVED STATE ---------- ACCEPT FRIEND REQUEST
                if (mCurrent_state.equals("req_received")){

                    //Creates friend.
                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/timestamp", ServerValue.TIMESTAMP);
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/friend_type", "friends");
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/participationCount", 0);
                    friendsMap.put("Friends/" + user_id + "/"  + mCurrent_user.getUid() + "/timestamp", ServerValue.TIMESTAMP);
                    friendsMap.put("Friends/" + user_id + "/"  + mCurrent_user.getUid() + "/friend_type", "friends");
                    friendsMap.put("Friends/" + user_id + "/"  + mCurrent_user.getUid() + "/participationCount", 0);

                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);


                    friendDatabase.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError == null){

                                mCurrent_state = "friends"; //Local state

                                //sending notification info to firebase
                                HashMap<String, Object> notificationData = new HashMap<>();
                                notificationData.put("from", mCurrent_user.getUid());
                                notificationData.put("type", "friend request accepted");
                                notificationData.put("timestamp", ServerValue.TIMESTAMP);
                                mNotificationDatabase.child(user_id).push().setValue(notificationData);

                            } else {

                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                    /*

                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).child("friend_type").setValue("friends").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).child("friend_type").setValue("friends").addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    // REMOVE WAITING POSITIONS FOR EACH USER
                                                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    mCurrent_state = "friends"; //Local state

                                                                    //sending notification info to firebase
                                                                    HashMap<String, Object> notificationData = new HashMap<>();
                                                                    notificationData.put("from", mCurrent_user.getUid());
                                                                    notificationData.put("type", "friend request accepted");
                                                                    notificationData.put("timestamp", ServerValue.TIMESTAMP);
                                                                    mNotificationDatabase.child(user_id).push().setValue(notificationData);

                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                     */

                }

                // ---------- UNFRIENDING STATE ----------
                if (mCurrent_state.equals("friends")){
                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).child("friend_type").setValue("not_friends").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).child("friend_type").setValue("not_friends").addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mCurrent_state = "not_friends"; //Local state

                                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue();
                                    mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue();

                                }
                            });
                        }
                    });
                }
            }
        });

        //Check if you are looking at your own profile to remove button.
        if (currentUserId.equals(user_id)){

            mProfileSendReqBtn.setVisibility(View.GONE);
            mDeclineBtn.setVisibility(View.GONE);
            textViewFriendListTitle.setText("Your Friends");
            //Calls database for name.
            databaseReference = firebaseDatabase.getReference().child("Users").child(currentUserId);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User userProfile = dataSnapshot.getValue(User.class);
                    textViewProfileName.setText(Objects.requireNonNull(userProfile).getName());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
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

        FirebaseRecyclerAdapter firebaseRecyclerAdapterProfile = new FirebaseRecyclerAdapter<Friends, ProfileFriendViewHolder>(options) {

            @NonNull
            @Override
            public ProfileFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ProfileFriendViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_view_user, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final ProfileFriendViewHolder profileFriendViewHolder, int position, @NonNull Friends friends) {
                //friends.setDate(friends.getDate());


                //Now we get name from userDatabase
                String list_user_id = getRef(position).getKey();
                userDatabaseProfile.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String userName = dataSnapshot.child("name").getValue().toString();

                        String userThumb = "";
                        if (dataSnapshot.child("image_thumbnail").exists()){
                            userThumb = dataSnapshot.child("image_thumbnail").getValue().toString();
                        }


                        profileFriendViewHolder.setName(userName);

                        //Checks if activity is destroyed.
                        Activity act = ProfileActivity.this;
                        if(!act.isDestroyed()) {
                            profileFriendViewHolder.setUserImage(userThumb, act);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                final String user_id = getRef(position).getKey();

                profileFriendViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent = new Intent(ProfileActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        startActivity(profileIntent);
                        finish();

                    }
                });
            }
        };

        friendListProfile.setAdapter(firebaseRecyclerAdapterProfile);
        firebaseRecyclerAdapterProfile.startListening();
    }

    public static class ProfileFriendViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public ProfileFriendViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        //Get name
        public void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.textViewCard);
            userNameView.setText(name);
        }

        //Get image.
        public void setUserImage(String image_thumbnail, Context ctx){
            ImageView userImageView = mView.findViewById(R.id.imageViewCard);
            Glide.with(ctx).load(image_thumbnail).into(userImageView);
        }
    }
}

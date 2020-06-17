package org.faith.bebetter.NotifsPage;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.faith.bebetter.FullHDActivity;
import org.faith.bebetter.YouPage.ProfileActivity;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotifsFragment extends Fragment {


    public NotifsFragment() {
        // Required empty public constructor
    }

    private DatabaseReference experienceDatabase;
    private DatabaseReference notificiationDatabase;
    private DatabaseReference userDatabase;
    private DatabaseReference databaseReference;
    private RecyclerView notificationList;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private LinearLayoutManager layoutManager;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    //Imagedatabase
    private StorageReference ImageStorage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_notification, container, false);

        //Connect to firebase, otherwise we can't fx: getUid();
        firebaseAuth = FirebaseAuth.getInstance();

        //Curerent user.
        currentUserId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        notificiationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications").child(currentUserId);
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        notificationList = v.findViewById(R.id.rvNotificationList);
        experienceDatabase = FirebaseDatabase.getInstance().getReference().child("Experience");
        databaseReference = FirebaseDatabase.getInstance().getReference();
        ImageStorage = FirebaseStorage.getInstance().getReference().child("Experiences");

        //Vores recyclerview ændre ikke størelse.
        notificationList.setHasFixedSize(true);
        // Make the recyclerview linear.
        // Here you modify your LinearLayoutManager
        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);

        layoutManager.setStackFromEnd(true);


        // Now set the layout manager and the adapter to the RecyclerView
        notificationList.setLayoutManager(layoutManager);


        //does so it doesn't download name every time. Should try to convert firebaseReference to DatabaseReference, to get it.
        notificiationDatabase.keepSynced(true);


        //long searchText = 0;
        Query firebaseSearchQuery = notificiationDatabase.orderByChild("timestamp");

        FirebaseRecyclerOptions<Notification> options =
                new FirebaseRecyclerOptions.Builder<Notification>()
                        .setQuery(firebaseSearchQuery, Notification.class)
                        .setLifecycleOwner(this) // This might be of interest, if I don't want to call the server every time, people swipe away and back to a place in app.
                        .build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Notification, NotifViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final NotifViewHolder notifViewHolder, int position, @NonNull Notification notification) {

                //Now we get name from userDatabase
                final String notification_id = getRef(position).getKey();
                final String from_user_id = notification.getFrom();
                final String type = notification.getType();
                final String experienceKey = notification.getExperienceKey();
                final String imageKey = notification.getImage_thumbnail();

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (type.equals("friend request")){
                            //Set thumbnail
                            String userThumb = "";
                            Object userThumbObj = dataSnapshot.child("Users").child(from_user_id).child("image_thumbnail").getValue();
                            if ( userThumbObj == null) { /*Do nothing*/} else { userThumb = userThumbObj.toString();}

                            notifViewHolder.setUserImage(userThumb, getContext());

                            //Set text for friend request accepted.
                            String from = "";
                            Object fromOjb = dataSnapshot.child("Users").child(from_user_id).child("name").getValue();
                            if ( fromOjb == null) { /*Do nothing*/ } else { from = fromOjb.toString();}
                            //Bolding specific text
                            from = "<b>" + from + "</b>";
                            String notificationText = from + " sent you a friend request!";
                            notifViewHolder.setName(notificationText);
                        }


                        if (type.equals("friend request accepted")){
                            //Set thumbnail
                            String userThumb = "";
                            Object userThumbObj = dataSnapshot.child("Users").child(from_user_id).child("image_thumbnail").getValue();
                            if ( userThumbObj == null) { /*Do nothing*/} else { userThumb = userThumbObj.toString();}

                            notifViewHolder.setUserImage(userThumb, getContext());


                            //Set text for friend request accepted.
                            String from = "";
                            Object fromOjb = dataSnapshot.child("Users").child(from_user_id).child("name").getValue();
                            if ( fromOjb == null) { /*Do nothing*/ } else { from = fromOjb.toString();}
                            //Bolding specific text
                            from = "<b>" + from + "</b>";
                            String notificationText = from + " accepted your friend request!";
                            notifViewHolder.setName(notificationText);

                        }


                        if (type.equals("experience invite")){
                            //Set thumbnail image
                            String experienceThumb = "";
                            String experienceThumb2 = "";
                            Object experienceThumbObj = dataSnapshot.child("Users").child(from_user_id).child("image_thumbnail").getValue();
                            Object experienceThumbObj2 = dataSnapshot.child("Experiences").child(experienceKey).child("thumbnails").child(imageKey).child("image").getValue();

                            //Checks for null pointers.
                            if ( experienceThumbObj == null) { /*Do nothing*/ } else { experienceThumb = experienceThumbObj.toString();}
                            if ( experienceThumbObj2 == null) { /*Do nothing*/ } else { experienceThumb2 = experienceThumbObj2.toString();}

                            notifViewHolder.setUserImage(experienceThumb, getContext());
                            notifViewHolder.setUserImage2(experienceThumb2, getContext());

                            //Looks at the notifViewHolder, finds our PofileImage, then adds clicklinstener.
                            notifViewHolder.mView.findViewById(R.id.imageViewCard).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                                    profileIntent.putExtra("user_id", from_user_id);
                                    startActivity(profileIntent);
                                }
                            });

                            //Looks at the notifViewHolder, finds our ExperienceImage, then adds clicklinstener.
                            notifViewHolder.mView.findViewById(R.id.imageViewCard2).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent profileIntent = new Intent(getActivity(), FullHDActivity.class);
                                    profileIntent.putExtra("EXTRA_KEY", experienceKey);
                                    startActivity(profileIntent);
                                }
                            });

                            //Set text
                            String name = "";
                            String description = "";
                            Object nameObj = dataSnapshot.child("Users").child(from_user_id).child("name").getValue();
                            Object descriptionObj = dataSnapshot.child("Experiences").child(experienceKey).child("description").getValue();

                            //Checks for null pointers.
                            if ( nameObj == null) { /*Do nothing*/ } else { name = nameObj.toString();}
                            if ( descriptionObj == null) { /*Do nothing*/ } else { description = descriptionObj.toString();}

                            name = name.replaceAll("\\s.*", "");
                            //Bolding specific text
                            name = "<b>" + name + "</b>";
                            description = "<b>" + description + "</b>";
                            String notificationText =  name + " invited you!<br>" + description;
                            notifViewHolder.setName(notificationText);
                        }


                        if (type.equals("experience completed")){
                            //Set thumbnail image
                            String experienceThumb = "";
                            String experienceThumb2 = "";
                            Object experienceThumbObj = dataSnapshot.child("Users").child(from_user_id).child("image_thumbnail").getValue();
                            Object experienceThumbObj2 = dataSnapshot.child("Experiences").child(experienceKey).child("thumbnails").child(imageKey).child("image").getValue();

                            //Checks for null pointers
                            if ( experienceThumbObj == null) { /*Do nothing*/ } else { experienceThumb = experienceThumbObj.toString();}
                            if ( experienceThumbObj2 == null) { /*Do nothing*/ } else { experienceThumb2 = experienceThumbObj2.toString();}

                            notifViewHolder.setUserImage(experienceThumb, getContext());
                            notifViewHolder.setUserImage2(experienceThumb2, getContext());

                            //Set text
                            String name = "";
                            String description = "";
                            Object nameObj = dataSnapshot.child("Users").child(from_user_id).child("name").getValue();
                            Object descriptionObj = dataSnapshot.child("Experiences").child(experienceKey).child("description").getValue();

                            //Checks for null pointers
                            if ( nameObj == null) { /*Do nothing*/ } else { name = nameObj.toString();}
                            if ( descriptionObj == null) { /*Do nothing*/ } else { description = descriptionObj.toString();}

                            name = name.replaceAll("\\s.*", "");
                            //Bolding specific text
                            name = "<b>" + name + "</b>";
                            description = "<b>" + description + "</b>";
                            String userName =  name + " joined:<br>" + description;
                            notifViewHolder.setName(userName);

                            //Looks at the notifViewHolder, finds our PofileImage, then adds clicklinstener.
                            notifViewHolder.mView.findViewById(R.id.imageViewCard).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                                    profileIntent.putExtra("user_id", from_user_id);
                                    startActivity(profileIntent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                notifViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (type.equals("friend request")){
                            Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                            profileIntent.putExtra("user_id", from_user_id);
                            startActivity(profileIntent);
                        }
                        if (type.equals("friend request accepted")){
                            Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                            profileIntent.putExtra("user_id", from_user_id);
                            startActivity(profileIntent);
                        }
                        if (type.equals("experience invite")) {

                            //NotifsFragment -> CameraActivity -> CameraFragment -> ImagePreview -> FriendList
                            Intent intent = new Intent(getActivity(), FullHDActivity.class);
                            Bundle extras = new Bundle();
                            extras.putString("EXTRA_KEY", experienceKey);
                            //extras.putString("EXTRA_FROM", from_user_id);
                            intent.putExtras(extras);
                            startActivity(intent);
                        }

                        if (type.equals("experience completed")) {
                            Intent intent = new Intent(getActivity(), FullHDActivity.class);
                            Bundle extras = new Bundle();
                            extras.putString("EXTRA_KEY", experienceKey);
                            extras.putString("EXTRA_FROM", from_user_id);
                            intent.putExtras(extras);
                            startActivity(intent);
                        }
                    }
                });
            }

            @Override
            public int getItemViewType(int position) {
                if (getItem(position).getType().equals("friend request")){
                    return 1;
                }
                if (getItem(position).getType().equals("friend request accepted")){
                    return 2;
                }
                if (getItem(position).getType().equals("experience invite")){
                    return 3;
                }
                if (getItem(position).getType().equals("experience completed")){
                    return 4;
                }
                else{
                    return 0;
                }
            }

            @NonNull
            @Override
            public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //return new NotifViewHolder(LayoutInflater.from(parent.getContext())
                //      .inflate(R.layout.notification_card_view, parent, false));
                if (viewType == 1) { //friend request
                    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_card_view,parent, false);
                    return new NotifViewHolder(itemView,"friend request");
                }
                if (viewType == 2) { //friend request accepted
                    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_card_view,parent, false);
                    return new NotifViewHolder(itemView,"friend request accepted");
                }
                if (viewType == 3) { //experience invite
                    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_card_view_experience,parent, false);
                    return new NotifViewHolder(itemView, "experience invite");
                }
                if (viewType == 4) { //experience completed
                    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_card_view_experience,parent, false);
                    return new NotifViewHolder(itemView, "experience completed");
                } else {
                    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_card_view,parent, false);
                    return new NotifViewHolder(itemView,"");
                }
            }
        };

        notificationList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

        return v;
    }

    public class NotifViewHolder extends RecyclerView.ViewHolder {

        View mView;
        public NotifViewHolder(View itemView, String type) {
            super(itemView);

            mView = itemView;
        }
        //Get name
        public void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.textViewCard);
            userNameView.setText(Html.fromHtml(name));
        }

        //Get image.
        public void setUserImage(String image_thumbnail, Context ctx){
            ImageView userImageView = mView.findViewById(R.id.imageViewCard);
            //Checks if activity is destroyed.
            Activity act = getActivity();
            if(act != null) {
                Glide.with(getActivity().getApplicationContext()).load(image_thumbnail).into(userImageView);
            }

        }
        //Get image.
        public void setUserImage2(String image_thumbnail2, Context ctx){
            ImageView userImageView2 = mView.findViewById(R.id.imageViewCard2);
            //Checks if activity is destroyed.
            Activity act = getActivity();
            if(act != null) {
                Glide.with(getActivity().getApplicationContext()).load(image_thumbnail2).into(userImageView2);
            }
        }
    }
}

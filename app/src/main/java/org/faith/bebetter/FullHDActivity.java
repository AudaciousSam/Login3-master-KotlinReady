package org.faith.bebetter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import org.faith.bebetter.FeedPage.ExperienceFullHD;
import org.faith.bebetter.NotifsPage.CameraActivity;
import org.faith.bebetter.YouPage.ProfileActivity;

public class FullHDActivity extends AppCompatActivity {

    private RecyclerView experienceImageList;
    private LinearLayoutManager layoutManager;
    private DatabaseReference databaseReference;
    private DatabaseReference experienceImagesDatabase;
    private DatabaseReference experienceDescriptionDatabase;
    private DatabaseReference userThumbDatabase;
    private String experienceKey;
    private ImageView imageViewCardProfileThumb;
    private String from_user_id;
    private TextView textViewFullHDExperience;
    private TextView guideFullHD;
    private String guide;
    private String currentUserId;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_full_image);

        //Get Current User
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        //UI
        experienceImageList = findViewById(R.id.rvExperienceImageList);
        guideFullHD = findViewById(R.id.guideFullHD);

        //Get experienceKey from FeedActivity
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        experienceKey = extras.getString("EXTRA_KEY");
        from_user_id = extras.getString("EXTRA_FROM");
        databaseReference = FirebaseDatabase.getInstance().getReference();
        experienceImagesDatabase = FirebaseDatabase.getInstance().getReference().child("Experiences").child(experienceKey).child("fullHD");
        userThumbDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        experienceDescriptionDatabase = FirebaseDatabase.getInstance().getReference().child("Experiences").child(experienceKey).child("description");


        //Toggle guide.
        ToggleGuide();


        //Get Experience Description - The overall one.
        experienceDescriptionDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String descriptionOG = (String) dataSnapshot.getValue();
                //Set description.
                textViewFullHDExperience = findViewById(R.id.textViewFullHDExperience);
                textViewFullHDExperience.setText(descriptionOG);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //Feedlist
        experienceImageList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        // Now set the layout manager and the adapter to the RecyclerView
        experienceImageList.setLayoutManager(layoutManager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        Query firebaseSearchQuery = experienceImagesDatabase.orderByChild("timestamp");
        FirebaseRecyclerOptions<ExperienceFullHD> options = new FirebaseRecyclerOptions.Builder<ExperienceFullHD>()
                .setQuery(firebaseSearchQuery, ExperienceFullHD.class)
                .build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ExperienceFullHD, ExperienceViewHolder>(options) {

            @NonNull
            @Override
            public ExperienceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ExperienceViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_view_fullhd_image, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final ExperienceViewHolder experienceViewHolder, int position, @NonNull ExperienceFullHD experienceFullHD) {
                //friends.setDate(friends.getDate());

                //Here we get the users "encrypted" name.
                String list_user_id = getRef(position).getKey();
                String from = experienceFullHD.getFrom();

                //Then we take that and look at users, with that "encrypted" name and show their name and thumbnail image.
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String Description = "";
                        Object DescriptionObj = dataSnapshot.child("Experiences").child(experienceKey).child("fullHD").child(list_user_id).child("description").getValue();
                        if ( DescriptionObj == null) { /*Do nothing*/} else { Description = DescriptionObj.toString();}
                        experienceViewHolder.setDescription(Description);

                        String userImage = "";
                        Object userImageObj = dataSnapshot.child("Experiences").child(experienceKey).child("fullHD").child(list_user_id).child("image").getValue();
                        if ( userImageObj == null) { /*Do nothing*/} else { userImage = userImageObj.toString();}
                        experienceViewHolder.setUserImage(userImage, FullHDActivity.this);

                        String userImageThumb = "";
                        if (from != null){

                            Object userImageThumbObj = dataSnapshot.child("Users").child(from).child("image_thumbnail").getValue();
                        if ( userImageThumbObj == null) { /*Do nothing*/} else { userImageThumb = userImageThumbObj.toString();}
                        experienceViewHolder.setUserThumbImage(userImageThumb, FullHDActivity.this);

                        }

                        //Looks at the notifViewHolder, finds our ProfileImage, then adds clicklinstener.
                        experienceViewHolder.mView.findViewById(R.id.imageViewCardProfileThumb).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent profileIntent = new Intent(FullHDActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("user_id", from);
                                startActivity(profileIntent);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                experienceViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(FullHDActivity.this, CameraActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString("EXTRA_KEY", experienceKey);
                        extras.putString("EXTRA_FROM", from);
                        intent.putExtras(extras);
                        startActivity(intent);

                    }
                });

                experienceViewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                    if (guide.equals("on")){
                        try {
                            guideFullHD.setVisibility(View.GONE);
                            databaseReference.child("Users").child(currentUserId).child("options").child("guideFullHD").setValue("off");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                        if (guide.equals("off")){
                            try {
                                guideFullHD.setVisibility(View.VISIBLE);
                                databaseReference.child("Users").child(currentUserId).child("options").child("guideFullHD").setValue("on");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }


                        return false;
                    }
                });


            }
        };

        experienceImageList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    public class ExperienceViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public ExperienceViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        //Get image.
        public void setUserImage(String userImage, Context ctx){
            ImageView userImageView = mView.findViewById(R.id.imageViewCardFullHD);
            Glide.with(getApplicationContext()).load(userImage).centerCrop().into(userImageView);
        }

        //Get image.
        public void setUserThumbImage(String userImageThumb, Context ctx){
            ImageView userImageThumbView = mView.findViewById(R.id.imageViewCardProfileThumb);
            Glide.with(getApplicationContext()).load(userImageThumb).centerCrop().into(userImageThumbView);
        }

        //Get description
        public void setDescription(String Description) {
            TextView userDescriptionView = mView.findViewById(R.id.textViewFullHD);
            userDescriptionView.setText(Description);
        }
    }


    private void ToggleGuide(){

        databaseReference.child("Users").child(currentUserId).child("options").child("guideFullHD").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 guide = (String) dataSnapshot.getValue();
                if(guide == null){
                    //INSÆT OPTIONS
                    databaseReference.child("Users").child(currentUserId).child("options").child("guideFullHD").setValue("on");
                }
                if(guide != null){
                    if (guide.equals("on")){
                        guideFullHD.setVisibility(View.VISIBLE);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



}




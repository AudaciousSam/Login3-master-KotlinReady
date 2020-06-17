package org.faith.bebetter.YouPage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.faith.bebetter.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FullHDProfileImageActivity extends AppCompatActivity {

    private RecyclerView profileImageList;
    private LinearLayoutManager layoutManager;
    private DatabaseReference databaseReference;
    private DatabaseReference profileImagesDatabase;
    private DatabaseReference userThumbDatabase;
    private ImageView imageViewCardProfileThumb;
    private String guide;
    private String currentUserId;
    private FirebaseAuth firebaseAuth;
    private StorageReference mImageStorage;
    private String profileImageName;
    private ImageButton imageButtonDelete;
    private String profileImage;
    private String profileImageThumbnail;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_full_image_profile);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String others_user_id = extras.getString("EXTRA_USER_ID");


        //UI
        profileImageList = findViewById(R.id.rvProfileImageList);
        imageButtonDelete = findViewById(R.id.imgBtnDelete);

        //Get Current User
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        mImageStorage = FirebaseStorage.getInstance().getReference().child("Users");
        profileImagesDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(others_user_id);

        //Feedlist
        profileImageList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        // Now set the layout manager and the adapter to the RecyclerView
        profileImageList.setLayoutManager(layoutManager);

        long serverTime = Timestamp.now().getSeconds();
        //This should be enough for every human on Earth to make one experience everyday, for 100 years.
        long beBetterLong = 3650000000000000L;
        profileImageName = String.valueOf(beBetterLong - serverTime);

        profileImageList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(FullHDProfileImageActivity.this, "Wow, it's dark in here.", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        Query firebaseSearchQuery = profileImagesDatabase.child("profile").orderByChild("timestamp");
        FirebaseRecyclerOptions<ProfileFullHD> options = new FirebaseRecyclerOptions.Builder<ProfileFullHD>()
                .setQuery(firebaseSearchQuery, ProfileFullHD.class)
                .build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ProfileFullHD, ProfileViewHolder>(options) {

            @NonNull
            @Override
            public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ProfileViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_view_fullhd_image_profile, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final ProfileViewHolder profileViewHolder, int position, @NonNull ProfileFullHD profileFullHD) {
                //friends.setDate(friends.getDate());

                //Here we get the users "encrypted" name.
                String profileKey = getRef(position).getKey();
                //Then we take that and look at users, with that "encrypted" name and show their name and thumbnail image.
                profileImagesDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        profileImage = "";
                        Object profileImageObj = dataSnapshot.child("profile").child(profileKey).child("profile").getValue();
                        if ( profileImageObj == null) { /*Do nothing*/} else { profileImage = profileImageObj.toString();}

                        profileViewHolder.setUserImage(profileImage, FullHDProfileImageActivity.this);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };

        profileImageList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public class ProfileViewHolder extends RecyclerView.ViewHolder {

        View mView;
        public ProfileViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }
        //Get image.
        public void setUserImage(String userImage, Context ctx){
            ImageView userImageView = mView.findViewById(R.id.imageViewCardFullHD);
            Glide.with(getApplicationContext()).load(userImage).centerCrop().into(userImageView);
        }
    }
}




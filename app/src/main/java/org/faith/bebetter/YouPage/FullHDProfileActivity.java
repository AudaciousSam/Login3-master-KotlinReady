package org.faith.bebetter.YouPage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class FullHDProfileActivity extends AppCompatActivity {

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

        //UI
        profileImageList = findViewById(R.id.rvProfileImageList);
        imageButtonDelete = findViewById(R.id.imgBtnDelete);


        //Get Current User
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        mImageStorage = FirebaseStorage.getInstance().getReference().child("Users");
        profileImagesDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

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

        FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid()).child("profile").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // HERE WE TAKE CARE OF NULL POINTERS AND THE CURRENT DEFAULT SYSTEM WHEN USER IS CREATED
                if (dataSnapshot.getValue() == null){

                    Intent galleryIntent = new Intent();
                    galleryIntent.setType("image/*");
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    //startActivityForResult(Intent.createChooser(galaryIntent,"SELECT IMAGE"), IMAGE_PICK);

                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1,2)
                            .start(FullHDProfileActivity.this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                        .inflate(R.layout.card_view_fullhd_image_profile_with_delete, parent, false));
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

                        profileImageThumbnail = "";
                        Object profileImageThumbnailObj = dataSnapshot.child("profile_thumbnail").child(profileKey+"_thumbnail").child("profile_thumbnail").getValue();
                        if ( profileImageThumbnailObj == null) { } else { profileImageThumbnail = profileImageThumbnailObj.toString();}


                        profileViewHolder.setUserImage(profileImage, FullHDProfileActivity.this);

                        //Looks at the profileViewHolder, finds our ProfileImage, then adds clicklinstener.
                        profileViewHolder.mView.findViewById(R.id.imageViewCardFullHD).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                /*
                                Intent profileIntent = new Intent(FullHDActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("user_id", from);
                                startActivity(profileIntent);
                                 */

                                Intent galleryIntent = new Intent();
                                galleryIntent.setType("image/*");
                                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                                //startActivityForResult(Intent.createChooser(galaryIntent,"SELECT IMAGE"), IMAGE_PICK);

                                CropImage.activity()
                                        .setGuidelines(CropImageView.Guidelines.ON)
                                        .setAspectRatio(1,2)
                                        .start(FullHDProfileActivity.this);

                            }
                        });
                        profileViewHolder.mView.findViewById(R.id.imgBtnDelete).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

//                                Checks if image is profileImage.
                                if (dataSnapshot.child("profile").child(profileKey).child("profile").getValue().equals(dataSnapshot.child("image").getValue())) {
                                    profileImagesDatabase.child("image").removeValue();
                                    profileImagesDatabase.child("image_thumbnail").removeValue();
                                }

                                //                                HERE WE DELETE THE IMAGE
                                profileImagesDatabase.child("profile").child(profileKey).removeValue();
                                StorageReference storageReferenceProfile = FirebaseStorage.getInstance().getReferenceFromUrl(profileImage);

                                profileImagesDatabase.child("profile_thumbnail").child(profileKey+"_thumbnail").removeValue();
                                StorageReference storageReferenceProfileThumbnail = FirebaseStorage.getInstance().getReferenceFromUrl(profileImageThumbnail);

                                storageReferenceProfileThumbnail.delete();
                                storageReferenceProfile.delete();

                            }
                        });

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

    //When user uploads an image.
    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();


                //Image we are compressing.
                File imageFile = new File(resultUri.getPath());

                //location on firebase for thumbnail
                StorageReference filepath_thumb = mImageStorage.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Images").child(profileImageName + "_thumbnail");

                //location on firebase for profile image
                StorageReference filepath_fullHD = mImageStorage.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Images").child(profileImageName);

                //Compress for thumbnail
                Bitmap compressedImageBitmap = null;
                try {
                    compressedImageBitmap = new Compressor(this).setMaxHeight(200).setMaxWidth(100).compressToBitmap(imageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Objects.requireNonNull(compressedImageBitmap).compress(Bitmap.CompressFormat.WEBP, 100, byteArrayOutputStream);
                //convert compressed image to byte
                byte[] compressedImageBitmapByteform = byteArrayOutputStream.toByteArray();

                //Compress for HD
                Bitmap compressedImageBitmapFullHD = null;
                try {
                    compressedImageBitmapFullHD = new Compressor(this).setMaxHeight(2160).setMaxWidth(1080).compressToBitmap(imageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream byteArrayOutputStreamFullHD = new ByteArrayOutputStream();
                Objects.requireNonNull(compressedImageBitmapFullHD).compress(Bitmap.CompressFormat.WEBP, 100, byteArrayOutputStreamFullHD);
                //convert compressed image to byte
                byte[] compressedImageBitmapByteformFullHD = byteArrayOutputStreamFullHD.toByteArray();


                //For the thumbnail
                UploadTask uploadTask = filepath_thumb.putBytes(compressedImageBitmapByteform);
                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(FullHDProfileActivity.this, "Looking Good!!", Toast.LENGTH_SHORT).show();

                            //update image_thumbnail link in the database section.
                            mImageStorage.child(firebaseAuth.getUid()).child("Images/" + profileImageName + "_thumbnail").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Got the download URL for 'users/me/profile.png'
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid()).child("image_thumbnail").setValue(uri.toString());

                                    HashMap<String, Object> profileImageThumbnailData = new HashMap<>();
                                    profileImageThumbnailData.put("profile_thumbnail", uri.toString());
                                    profileImageThumbnailData.put("timestamp", ServerValue.TIMESTAMP);
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid()).child("profile_thumbnail").child(profileImageName + "_thumbnail").setValue(profileImageThumbnailData);
                                }
                            });
                        }
                    }
                });


                //For the FullHD
                UploadTask uploadTaskFullHD = filepath_fullHD.putBytes(compressedImageBitmapByteformFullHD);
                uploadTaskFullHD.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {


                            //update image link in the database section.
                            mImageStorage.child(firebaseAuth.getUid()).child("Images/" + profileImageName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Got the download URL for 'users/me/profile.png'
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid()).child("image").setValue(uri.toString());

                                    HashMap<String, Object> profileImageData = new HashMap<>();
                                    profileImageData.put("profile", uri.toString());
                                    profileImageData.put("timestamp", ServerValue.TIMESTAMP);
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid()).child("profile").child(profileImageName).setValue(profileImageData);
                                }
                            });
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }

        }
    }

}




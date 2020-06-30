package org.faith.bebetter.YouPage;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.faith.bebetter.LoginAndUserCreation.LoginActivity;

import com.faith.bebetter.R;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.google.firebase.database.DataSnapshot;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class YouFragment extends Fragment {

    public YouFragment() {
        // Required empty public constructor
    }

    //for the friendlist.
    private RecyclerView friendList;
    private DatabaseReference friendListDatabase;
    private String currentUserId;
    private DatabaseReference userDatabase;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private StorageReference mImageStorage;
    private LinearLayoutManager layoutManager;

    private ImageView profilePicFragment;
    private TextView profileName;
    private ImageButton moreMenu;
    private String profileImageName;
    private Drawable noProfileImageYet;

    private static final int IMAGE_PICK = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_you, container, false);

        //Connects UI layout
        profilePicFragment = v.findViewById(R.id.ivProfilePicFragment);
        profileName = v.findViewById(R.id.tvProfileNameFragment);
        moreMenu = v.findViewById(R.id.btnMoreFragmentYou);
        friendList = v.findViewById(R.id.rvFriendListYou);

        //Connects to firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        mImageStorage = FirebaseStorage.getInstance().getReference().child("Users");

        //FriendList
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        friendListDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        layoutManager = new LinearLayoutManager(getActivity());
        friendList.setLayoutManager(layoutManager);
        friendList.setHasFixedSize(true);
        //layoutManager.setStackFromEnd(true);
        friendListDatabase.keepSynced(true); //does so it doesn't download name every time. Should try to convert firebaseReference to DatabaseReference, to get it.


        FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid()).child("image_thumbnail").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!= null){
                    String uri = dataSnapshot.getValue().toString();

                    Picasso.get().load(uri).fit().centerCrop().into(profilePicFragment);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        /*

        //Calls database for profile picture.
        mImageStorage.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Images/thumbnail").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

            }
        });

         */

        //Calls database for name.
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("Users").child(firebaseAuth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User userProfile = dataSnapshot.getValue(User.class);
                profileName.setText(Objects.requireNonNull(userProfile).getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
               /* Toast.makeText(getActivity(),databaseError.getCode(),Toast.LENGTH_SHORT).show();*/
            }
        });

        //Sent you to update info activity.
        profileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), UpdateInfo.class));
            }
        });

        moreMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(Objects.requireNonNull(getActivity()), moreMenu);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.updateEmailMenu:
                                startActivity(new Intent(getActivity(), UpdateEmail.class));
                                return true;
                            case R.id.updatePasswordMenu:
                                startActivity(new Intent(getActivity(), UpdatePassword.class));
                                return true;
                            case R.id.logoutMenu:
                                firebaseAuth.signOut();
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                            case R.id.feedbackMenu:
                                startActivity(new Intent(getActivity(), FeedbackActivity.class));


                                return true;
                            default:
                                return false;
                    }}
                });
                popupMenu.show();
            }
        });


        //Sends user to UpdateProfilePicture Activity.
        profilePicFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid()).child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
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
                                    .start(Objects.requireNonNull(getContext()), YouFragment.this);
                        }

                        else{

                            startActivity(new Intent(getActivity(), FullHDProfileActivity.class));
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                //startActivity(new Intent(getActivity(), UpdateProfilePicture.class));

            }
        });

        long serverTime = Timestamp.now().getSeconds();
        //This should be enough for every human on Earth to make one experience everyday, for 100 years.
        long beBetterLong = 3650000000000000L;
        profileImageName = String.valueOf(beBetterLong - serverTime);

        // Return v, fordi vores v er vores View af vores fragment. Hvis jeg fjernede det kunne jeg ikke bruge: FindViewById
        return v;
    }

    //When user uploads an image.
    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                //sets local image view to image.(faking quick upload)
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), resultUri);
                    profilePicFragment.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Image we are compressing.
                File imageFile = new File(resultUri.getPath());

                //location on firebase for thumbnail
                StorageReference filepath_thumb = mImageStorage.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Images").child(profileImageName + "_thumbnail");

                //location on firebase for profile image
                StorageReference filepath_fullHD = mImageStorage.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Images").child(profileImageName);

                //Compress for thumbnail
                Bitmap compressedImageBitmap = null;
                try {
                    compressedImageBitmap = new Compressor(getActivity()).setMaxHeight(200).setMaxWidth(100).compressToBitmap(imageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Objects.requireNonNull(compressedImageBitmap).compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                //convert compressed image to byte
                byte[] compressedImageBitmapByteform = byteArrayOutputStream.toByteArray();

                //Compress for HD
                Bitmap compressedImageBitmapFullHD = null;
                try {
                    compressedImageBitmapFullHD = new Compressor(getActivity()).setMaxHeight(2160).setMaxWidth(1080).compressToBitmap(imageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream byteArrayOutputStreamFullHD = new ByteArrayOutputStream();
                Objects.requireNonNull(compressedImageBitmapFullHD).compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStreamFullHD);
                //convert compressed image to byte
                byte[] compressedImageBitmapByteformFullHD = byteArrayOutputStreamFullHD.toByteArray();


                //For the thumbnail
                UploadTask uploadTask = filepath_thumb.putBytes(compressedImageBitmapByteform);
                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){

                            Toast.makeText(getActivity(), "Looking Good!!", Toast.LENGTH_SHORT).show();

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
                        if (task.isSuccessful()){


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


    //The friendlist
    @Override
    public void onStart(){
        super.onStart();

        //We basically search through the friend list without a query - the smart thing about this, is that it is ready for future implementation of search.
        long searchText = 0;

        //Here we search a users friendlist and order by date.
        Query firebaseSearchQuery = friendListDatabase.orderByChild("timestamp");//.startAt(searchText).endAt(searchText + "\uf8ff");

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(firebaseSearchQuery, Friends.class)
                        .setLifecycleOwner(this)
                        .build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapterYou = new FirebaseRecyclerAdapter<Friends, YouFriendViewHolder>(options) {

            @NonNull
            @Override
            public YouFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new YouFragment.YouFriendViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_view_user, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final YouFriendViewHolder youFriendViewHolder, int position, @NonNull Friends friends) {
                //friends.setDate(friends.getDate());

                //Here we get the users "encrypted" name.
                String list_user_id = getRef(position).getKey();

                //Then we take that and look at users, with that "encrypted" name and show their name and thumbnail image.
                userDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = "";
                        if(dataSnapshot.hasChild("image_thumbnail")){
                            userThumb = dataSnapshot.child("image_thumbnail").getValue().toString();
                        }


                        //Checks if activity is destroyed.
                        Activity act = getActivity();
                        assert act != null;
                        if(!act.isDestroyed()) {
                            youFriendViewHolder.setName(userName);
                            youFriendViewHolder.setUserImage(userThumb, getContext());
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                final String user_id = getRef(position).getKey();

                youFriendViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        startActivity(profileIntent);

                    }
                });
            }
        };

        friendList.setAdapter(firebaseRecyclerAdapterYou);

        }

    public static class YouFriendViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public YouFriendViewHolder(View itemView) {
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
            Glide.with(ctx).load(image_thumbnail).centerCrop().into(userImageView);
        }

    }


}

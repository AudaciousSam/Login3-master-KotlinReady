package org.faith.bebetter.NotifsPage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.faith.bebetter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
import com.squareup.picasso.Picasso;

import org.faith.bebetter.FeedPage.FriendListActivity;
import org.faith.bebetter.FeedPage.ImagePreview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class ImagePreviewFromNotification extends AppCompatActivity {

    //Firebase databaseReference object. Meaning: A specific place on the database.
    private StorageReference ImageStorage;
    private DatabaseReference imageDescription;
    private DatabaseReference experienceDatabase;

    private FirebaseAuth firebaseAuth;
    private String currentUser;

    private ImageView imagePreview;
    private EditText editText;
    private ImageButton next;
    private String experienceKey;
    private String imageKey;

    //For upload
    private StorageReference filepath_fullHD;
    private byte[] compressedImageBitmapByteformFullHD;
    private StorageReference filepath_feed;
    private byte[] compressedImageBitmapByteformFeed;
    private StorageReference filepath_thumb;
    private byte[] compressedImageBitmapByteformThumbnail;

    private long participantsCount;
    private String description;

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview_from_notification);
        imagePreview = findViewById(R.id.imagePreview);
        editText = findViewById(R.id.editText);
        next = findViewById(R.id.imgBtnNext);


        //Get current user
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser().getUid();

        experienceDatabase = FirebaseDatabase.getInstance().getReference().child("Experiences");
        ImageStorage = FirebaseStorage.getInstance().getReference().child("Experiences");

        //WE are getting this from feed.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        experienceKey = extras.getString("EXTRA_KEY");

        //String imageName = extras.getString("EXTRA_IMAGENAME");
        Uri resultUri = Uri.parse(extras.getString("EXTRA_URI"));

        //Set image.
        Picasso.get().load(resultUri).centerCrop().fit().into(imagePreview);


        long serverTime = Timestamp.now().getSeconds();
        //This should be enough for every human on Earth to make one experience everyday, for 100 years.
        long beBetterLong = 3650000000000000L;
        imageKey = String.valueOf(beBetterLong - serverTime);

        //Upload user as a participant
        experienceDatabase = FirebaseDatabase.getInstance().getReference().child("Experiences");

        //Where description is set.
        imageDescription = FirebaseDatabase.getInstance().getReference().child("Experiences").child(experienceKey).child("fullHD").child(imageKey).child("description");

        //Image we are compressing.
        File imageFile = new File(resultUri.getPath());

        //location on firebase storage for thumbnail
        filepath_thumb = ImageStorage.child(experienceKey).child("thumbnails").child(imageKey);

        //location on firebase storage for profile image
        filepath_feed = ImageStorage.child(experienceKey).child("feed").child(imageKey);

        //location on firebase storage for profile image
        filepath_fullHD = ImageStorage.child(experienceKey).child("fullHD").child(imageKey);



//------------------------------- CREATE COMPRESSED IMAGES ------------------------------- //

        //One for thumbnail,
        //One for feed,
        //One for fullScreen,

        Bitmap compressedImageBitmapThumbnail = null;
        Bitmap compressedImageBitmapFeed = null;
        Bitmap compressedImageBitmapFullHD = null;
        try {
            compressedImageBitmapThumbnail = new Compressor(ImagePreviewFromNotification.this).setMaxHeight(150).setMaxWidth(75).compressToBitmap(imageFile);
            compressedImageBitmapFeed = new Compressor(ImagePreviewFromNotification.this).setMaxHeight(540).setMaxWidth(270).compressToBitmap(imageFile);
            compressedImageBitmapFullHD = new Compressor(ImagePreviewFromNotification.this).setMaxHeight(1080).setMaxWidth(540).compressToBitmap(imageFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStreamThumbnail = new ByteArrayOutputStream();
        ByteArrayOutputStream byteArrayOutputStreamFeed = new ByteArrayOutputStream();
        ByteArrayOutputStream byteArrayOutputStreamFullHD = new ByteArrayOutputStream();

        Objects.requireNonNull(compressedImageBitmapThumbnail).compress(Bitmap.CompressFormat.WEBP, 90, byteArrayOutputStreamThumbnail);
        Objects.requireNonNull(compressedImageBitmapFeed).compress(Bitmap.CompressFormat.WEBP, 90, byteArrayOutputStreamFeed);
        Objects.requireNonNull(compressedImageBitmapFullHD).compress(Bitmap.CompressFormat.WEBP, 90, byteArrayOutputStreamFullHD);

        //convert compressed image to byte
        compressedImageBitmapByteformThumbnail = byteArrayOutputStreamThumbnail.toByteArray();
        compressedImageBitmapByteformFeed = byteArrayOutputStreamFeed.toByteArray();
        compressedImageBitmapByteformFullHD = byteArrayOutputStreamFullHD.toByteArray();

/*
        //Compress for thumbnail
        Bitmap compressedImageBitmap = null;
        try {
            compressedImageBitmap = new Compressor(ImagePreviewFromNotification.this).setMaxHeight(200).setMaxWidth(100).compressToBitmap(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Objects.requireNonNull(compressedImageBitmap).compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        //convert compressed image to byte
        compressedImageBitmapByteform = byteArrayOutputStream.toByteArray();


        //Compress for HD
        Bitmap compressedImageBitmapFullHD = null;
        try {
            compressedImageBitmapFullHD = new Compressor(ImagePreviewFromNotification.this).setMaxHeight(2160).setMaxWidth(1080).compressToBitmap(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream byteArrayOutputStreamFullHD = new ByteArrayOutputStream();
        Objects.requireNonNull(compressedImageBitmapFullHD).compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStreamFullHD);
        //convert compressed image to byte
        compressedImageBitmapByteformFullHD = byteArrayOutputStreamFullHD.toByteArray();*/


        //If clicking, we go next.
        editText.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {//DO STUFF!!
                        uploadImage();
                        description = editText.getText().toString();
                        //imageDescription.setValue(description);

                        bringAlongData();
                        return true;
                    }
                }
                return false;
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                description = editText.getText().toString();
                uploadImage();

                //imageDescription.setValue(description);
                bringAlongData();
            }
        });

        imagePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                description = editText.getText().toString();
                uploadImage();

                //imageDescription.setValue(description);
                bringAlongData();
            }
        });
    }

    public void bringAlongData(){
        //Get info from last activity.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String uri = extras.getString("EXTRA_URI");

        //Move them onto next activity.
        Intent intentFurther = new Intent(this, NotificationFriendListActivity.class);
        Bundle extrasFurther = new Bundle();
        extrasFurther.putString("EXTRA_KEY", experienceKey);
        extrasFurther.putString("EXTRA_IMAGEKEY", imageKey);
        extrasFurther.putString("EXTRA_URI", uri);
        intentFurther.putExtras(extrasFurther);
        startActivity(intentFurther);
    }

    public void uploadImage(){

        //For uploading the thumbnailImage
        UploadTask uploadTaskThumbnail = filepath_thumb.putBytes(compressedImageBitmapByteformThumbnail);

        uploadTaskThumbnail.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //update image_thumbnail link in the database section.
                filepath_thumb.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                      /*  // Now we add the link to the Experience.
                        Map<String, Object> childThumbnailUpdates = new HashMap<>();
                        childThumbnailUpdates.put("lastImage_thumbnail", uri.toString());
                        FirebaseDatabase.getInstance().getReference().child("Experiences").child(experienceKey).updateChildren(childThumbnailUpdates);*/

                        //Add timestamp and image_thumbnail link.
                        HashMap<String, Object> dataMapThumbnail = new HashMap<>();
                        dataMapThumbnail.put("timestamp", ServerValue.TIMESTAMP);
                        dataMapThumbnail.put("from", currentUser);
                        dataMapThumbnail.put("image", uri.toString());
                        FirebaseDatabase.getInstance().getReference().child("Experiences").child(experienceKey).child("thumbnails").child(imageKey + "_thumbnail").updateChildren(dataMapThumbnail);
                    }
                });
            }
        });


        //For uploading the feedImage
        UploadTask uploadTaskFeed = filepath_feed.putBytes(compressedImageBitmapByteformFeed);

        uploadTaskFeed.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //update image_thumbnail link in the database section.
                filepath_feed.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        FirebaseDatabase.getInstance().getReference().child("Experiences").child(experienceKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.hasChild("lastImage")){

                        // Now we add the link to the Experience.
                        Map<String, Object> childThumbnailUpdates = new HashMap<>();
                        childThumbnailUpdates.put("firstImage",  snapshot.child("lastImage").getValue());
                        childThumbnailUpdates.put("lastImage", uri.toString());
                        FirebaseDatabase.getInstance().getReference().child("Experiences").child(experienceKey).updateChildren(childThumbnailUpdates);

                                } else {
                                    FirebaseDatabase.getInstance().getReference().child("Experiences").child(experienceKey).child("lastImage").setValue(uri.toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        //Add timestamp and image_thumbnail link.
                        HashMap<String, Object> dataMapFeed = new HashMap<>();
                        dataMapFeed.put("timestamp", ServerValue.TIMESTAMP);
                        dataMapFeed.put("from", currentUser);
                        dataMapFeed.put("image", uri.toString());
                        FirebaseDatabase.getInstance().getReference().child("Experiences").child(experienceKey).child("feed").child(experienceKey + "_feed").updateChildren(dataMapFeed);
                    }
                });
            }
        });


        //For uploading the fullHD
        UploadTask uploadTaskFullHD = filepath_fullHD.putBytes(compressedImageBitmapByteformFullHD);
        uploadTaskFullHD.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //update image_thumbnail link in the database section.
                filepath_fullHD.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        //Add timestamp and image link.
                        HashMap<String, Object> dataMap = new HashMap<>();
                        dataMap.put("timestamp", ServerValue.TIMESTAMP);
                        dataMap.put("from", currentUser);
                        dataMap.put("image", uri.toString());
                        dataMap.put("description", description);
                        FirebaseDatabase.getInstance().getReference().child("Experiences").child(experienceKey).child("fullHD").child(imageKey).updateChildren(dataMap);
                    }
                });
            }
        });
    }
}

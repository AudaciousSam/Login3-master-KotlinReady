package org.faith.bebetter.LoginAndUserCreation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.faith.bebetter.R;
import org.faith.bebetter.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {

    private EditText userName, userPassword, userEmail;
    private Button regButton;
    private TextView userLogin;
    private FirebaseAuth firebaseAuth;
    String email, name, password;
    private DatabaseReference experienceDatabase;
    private DatabaseReference feedDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setupUIViews();

        firebaseAuth = FirebaseAuth.getInstance();

        //Checks if user is already logged in and sends them on, rather than forcing user to login after ever closing of app.
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            finish();
            startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
        }


        userPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (validate()){
                        //Upload to data to authentication.
                        String user_email = userEmail.getText().toString().trim();
                        String user_password = userPassword.getText().toString().trim();

                        if(user_password.length() <= 5){
                            Toast.makeText(RegistrationActivity.this, "Your password must be longer than 6 characters.", Toast.LENGTH_LONG).show();
                        }

                        firebaseAuth.createUserWithEmailAndPassword(user_email, user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()){
                                    sendEmailVerification();
                                }
                            }
                        });

                    }
                    return true;
                }
                return false;
            }
        });

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()){
                    //Upload to data to authentication.
                    String user_email = userEmail.getText().toString().trim();
                    String user_password = userPassword.getText().toString().trim();

                    if(user_password.length() <= 5){
                        Toast.makeText(RegistrationActivity.this, "Your password must be longer than 6 characters.", Toast.LENGTH_LONG).show();
                    }

                    firebaseAuth.createUserWithEmailAndPassword(user_email, user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){
                                sendEmailVerification();
                            }
                        }
                    });
                }
            }
        });

        //If you have an account already.
        userLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            }
        });
    }

    //Connects all my UI interactives with their function, from top to bottom.
    private void setupUIViews(){
        userName = (EditText)findViewById(R.id.etNameRegistration);
        userPassword = (EditText)findViewById(R.id.etPasswordRegistration);
        userEmail = (EditText)findViewById(R.id.etEmailRegistration);
        regButton = findViewById(R.id.btnClaimRegister);
        userLogin = (TextView)findViewById(R.id.etToMainRegistration);
    }

    private Boolean validate(){
        Boolean result = false;

         name = userName.getText().toString();
         password = userPassword.getText().toString();
         email = userEmail.getText().toString();

        if (name.isEmpty() || password.isEmpty() || email.isEmpty()){
            Toast.makeText(this, "Please enter all the details", Toast.LENGTH_SHORT).show();
        }else{
            result = true;
            }
        return result;
    }

    private void sendEmailVerification(){
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null){
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        sendUserData(); //Saves username to database.
                        Toast.makeText(RegistrationActivity.this, "Welcome to the secret lair. Remember to verify your email.", Toast.LENGTH_LONG).show();

                        finish();
                        startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                    } else {
                        Toast.makeText(RegistrationActivity.this, "The mail couldn't reach you! Maybe the internet isn't on?", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //Here we sent the name, to the firebase database storage section.
    private void sendUserData(){
        //finds database.
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        //creates directory.
        DatabaseReference myRef = firebaseDatabase.getReference().child("Users").child(Objects.requireNonNull(firebaseAuth.getUid()));
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        HashMap<String, String> userMap = new HashMap<>();
                        userMap.put("name", name);
                        userMap.put("user_token", token);
                        myRef.setValue(userMap);
                        myRef.child("options").child("guideFullHD").setValue("on");


//--------------------------------- ADD INTRODUCTION GUIDE EXPERIENCE ---------------------------------//

                        String currentUser = Objects.requireNonNull(firebaseAuth.getUid());


                        long serverTime = Timestamp.now().getSeconds();
                        //This should be enough for every human on Earth to make one experience everyday, for 100 years.
                        long beBetterLong = 3650000000000000L;
                        String experienceKey = String.valueOf(beBetterLong - serverTime);

                        long beBetterLongImageKey = 365000000000000L;
                        String imageKey = String.valueOf(beBetterLongImageKey - serverTime);

                        experienceDatabase = FirebaseDatabase.getInstance().getReference().child("Experiences");
                        experienceDatabase.child(experienceKey).child("timestamp").setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                experienceDatabase.child(experienceKey).child("participants").child(currentUser).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                experienceDatabase.child(experienceKey).child("invited").child(currentUser).child("timestamp").setValue(ServerValue.TIMESTAMP);
                            }
                        });

                        //----------- FIRST IMAGE
                        FirebaseDatabase.getInstance().getReference().child("Experiences").child(experienceKey).child("firstImage_thumbnail")
                                .setValue("https://firebasestorage.googleapis.com/v0/b/bebetterlogin.appspot.com/o/Experiences%2FIntroduction%2FfullHD%2FlastImage.jpg?alt=media&token=f6a147d2-c57a-41c0-a2e9-bc1c27386636");

                        //Add timestamp and image_thumbnail link.
                        HashMap<String, Object> dataMapThumbnail = new HashMap<>();
                        dataMapThumbnail.put("timestamp", ServerValue.TIMESTAMP);
                        dataMapThumbnail.put("from", "c6GoXVL2rSPPa1s4RDsQzrzX4772");
                        dataMapThumbnail.put("image", "https://firebasestorage.googleapis.com/v0/b/bebetterlogin.appspot.com/o/Experiences%2FIntroduction%2FfullHD%2FlastImage.jpg?alt=media&token=f6a147d2-c57a-41c0-a2e9-bc1c27386636");
                        FirebaseDatabase.getInstance().getReference().child("Experiences").child(experienceKey).child("thumbnails").child(experienceKey + "_thumbnail").setValue(dataMapThumbnail);

                        // Now we add the link to the Experience.
                        FirebaseDatabase.getInstance().getReference().child("Experiences").child(experienceKey).child("firstImage")
                                .setValue("https://firebasestorage.googleapis.com/v0/b/bebetterlogin.appspot.com/o/Experiences%2FIntroduction%2FfullHD%2FlastImage.jpg?alt=media&token=f6a147d2-c57a-41c0-a2e9-bc1c27386636");

                        //Add timestamp and image link.
                        HashMap<String, Object> dataMap = new HashMap<>();
                        dataMap.put("timestamp", ServerValue.TIMESTAMP);
                        dataMap.put("from", "c6GoXVL2rSPPa1s4RDsQzrzX4772");
                        dataMap.put("image", "https://firebasestorage.googleapis.com/v0/b/bebetterlogin.appspot.com/o/Experiences%2FIntroduction%2FfullHD%2FlastImage.jpg?alt=media&token=f6a147d2-c57a-41c0-a2e9-bc1c27386636");
                        FirebaseDatabase.getInstance().getReference().child("Experiences").child(experienceKey).child("fullHD").child(experienceKey).setValue(dataMap);


                        //----------- LAST IMAGE
                        FirebaseDatabase.getInstance().getReference().child("Experiences").child(experienceKey).child("lastImage_thumbnail")
                                .setValue("https://firebasestorage.googleapis.com/v0/b/bebetterlogin.appspot.com/o/Experiences%2FIntroduction%2FfullHD%2FfirstImage.jpg?alt=media&token=22d0c0ac-a177-4a17-b714-5869764ed6e5");

                        //Add timestamp and image_thumbnail link.
                        HashMap<String, Object> dataMapThumbnailLastImage = new HashMap<>();
                        dataMapThumbnailLastImage.put("timestamp", ServerValue.TIMESTAMP);
                        dataMapThumbnailLastImage.put("from", "c6GoXVL2rSPPa1s4RDsQzrzX4772");
                        dataMapThumbnailLastImage.put("image", "https://firebasestorage.googleapis.com/v0/b/bebetterlogin.appspot.com/o/Experiences%2FIntroduction%2FfullHD%2FfirstImage.jpg?alt=media&token=22d0c0ac-a177-4a17-b714-5869764ed6e5");
                        FirebaseDatabase.getInstance().getReference().child("Experiences").child(experienceKey).child("thumbnails").child(imageKey + "_thumbnail").updateChildren(dataMapThumbnailLastImage);


                        // Now we add the link to the Experience.
                        FirebaseDatabase.getInstance().getReference().child("Experiences").child(experienceKey).child("lastImage")
                                .setValue("https://firebasestorage.googleapis.com/v0/b/bebetterlogin.appspot.com/o/Experiences%2FIntroduction%2FfullHD%2FfirstImage.jpg?alt=media&token=22d0c0ac-a177-4a17-b714-5869764ed6e5");

                        //Add timestamp and image link.
                        HashMap<String, Object> dataMapLastImage = new HashMap<>();
                        dataMapLastImage.put("timestamp", ServerValue.TIMESTAMP);
                        dataMapLastImage.put("from", "c6GoXVL2rSPPa1s4RDsQzrzX4772");
                        dataMapLastImage.put("image", "https://firebasestorage.googleapis.com/v0/b/bebetterlogin.appspot.com/o/Experiences%2FIntroduction%2FfullHD%2FfirstImage.jpg?alt=media&token=22d0c0ac-a177-4a17-b714-5869764ed6e5");
                        FirebaseDatabase.getInstance().getReference().child("Experiences").child(experienceKey).child("fullHD").child(imageKey).updateChildren(dataMapLastImage);

                        //----------- ADD TO PERSONS FEED:

                        feedDatabase = FirebaseDatabase.getInstance().getReference().child("Feeds");

                        HashMap<String, Object> experienceLink = new HashMap<>();
                        experienceLink.put("timestamp", ServerValue.TIMESTAMP);
                        experienceLink.put("experienceKey", experienceKey);
                        experienceLink.put("type", "together");
                        feedDatabase.child(currentUser).child(experienceKey).setValue(experienceLink);



                    }
                });



        String userToken = FirebaseInstanceId.getInstance().getToken();
        //UserProfile userProfile = new UserProfile(name);



    }

}



















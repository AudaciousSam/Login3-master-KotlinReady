package org.faith.bebetter.YouPage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.faith.bebetter.LoginAndUserCreation.UserProfile;
import com.faith.bebetter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdateInfo extends AppCompatActivity {

    private EditText newUserName;
    private Button save;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        newUserName = findViewById(R.id.etEditNameEditActivity);
        save = findViewById(R.id.btnSaveEditActivity);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        final DatabaseReference databaseReference = firebaseDatabase.getReference().child("Users").child(firebaseAuth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                newUserName.setText(userProfile.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UpdateInfo.this,databaseError.getCode(),Toast.LENGTH_SHORT).show();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = newUserName.getText().toString();

                /*UserProfile userProfile = new UserProfile(name);
                databaseReference.setValue(userProfile);*/

                FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid()).child("name").setValue(name);

                Toast.makeText(UpdateInfo.this, "Strike me down! That's a beautiful name!", Toast.LENGTH_LONG).show();
                finish();
            }
        });


    }
}

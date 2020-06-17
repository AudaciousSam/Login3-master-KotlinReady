package org.faith.bebetter.YouPage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.faith.bebetter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UpdateEmail extends AppCompatActivity {

    private Button update;
    private EditText newEmail;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        update = findViewById(R.id.btnUpdateEmail);
        newEmail = findViewById(R.id.etUpdateEmail);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //User newly inserted email.
                String userEmailNew = newEmail.getText().toString();

                firebaseUser.updateEmail(userEmailNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isComplete()){
                            Toast.makeText(UpdateEmail.this, "E-mail updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UpdateEmail.this, "E-mail failed", Toast.LENGTH_SHORT).show();
                        }}
                });
            }
        });


    }
}

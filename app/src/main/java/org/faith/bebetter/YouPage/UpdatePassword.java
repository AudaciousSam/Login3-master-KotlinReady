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

public class UpdatePassword extends AppCompatActivity {

    private Button update;
    private EditText newPassword;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        update = findViewById(R.id.btnUpdatePassword);
        newPassword = findViewById(R.id.etUpdatePassword);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();



        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //User newly inserted password.
                String userPasswordNew = newPassword.getText().toString();
                firebaseUser.updatePassword(userPasswordNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isComplete()){
                            Toast.makeText(UpdatePassword.this, "Password updated", Toast.LENGTH_SHORT).show();
                            finish();
                        } else{
                            Toast.makeText(UpdatePassword.this, "Password failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });




    }
}

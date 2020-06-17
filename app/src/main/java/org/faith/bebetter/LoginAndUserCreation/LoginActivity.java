package org.faith.bebetter.LoginAndUserCreation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class LoginActivity extends AppCompatActivity {

    private EditText Email, Password;
    private TextView Info;
    private Button Login;
    private int counter = 5;
    private TextView userRegistration;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private TextView forgotPassword;

    private DatabaseReference mUserDatabase;

    //To check if filled
    String email, password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Email = (EditText)findViewById(R.id.etNameLogin);
        Password = (EditText)findViewById(R.id.etPasswordLogin);
        Info = (TextView) findViewById(R.id.tvInfoLogin);
        Login = (Button) findViewById(R.id.btnLoginLogin);
        userRegistration = (TextView)findViewById(R.id.tvClaimProfileLogin);
        forgotPassword = (TextView)findViewById(R.id.tvForgotPasswordLogin);

        //Info.setText("Number of attempts remaining: 5");

        //Firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();

        //Database reference.
        mUserDatabase = FirebaseDatabase.getInstance().getReference();

        progressDialog = new ProgressDialog(this);

        FirebaseUser user = firebaseAuth.getCurrentUser();

        //Checks if user is already logged in and sends them on, rather than forcing user to login after ever closing of app.
        if (user != null){
            finish();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }


        Password.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (checkIsInfoFilled()){
                        validate(Email.getText().toString(), Password.getText().toString());
                    }
                    return true;
                }
                return false;
            }
        });

        //Set button activities.
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkIsInfoFilled()){
                validate(Email.getText().toString(), Password.getText().toString());
                }
            }
        });

        userRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, PasswordActivity.class));
            }
        });
    }

    private Boolean checkIsInfoFilled(){
        Boolean result = false;

        password = Password.getText().toString();
        email = Email.getText().toString();

        if (password.isEmpty() || email.isEmpty()){
            Toast.makeText(this, "Please enter all the details", Toast.LENGTH_SHORT).show();
        }else{
            result = true;
        }
        return result;
    }



    private void validate(String userName, String userPassword){
        firebaseAuth.signInWithEmailAndPassword(userName, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                progressDialog.dismiss(); //Terminates our progressDialog message.

                if (task.isSuccessful()){
                    checkEmailVerification();

                    //set Notification token
                    firebaseAuth = FirebaseAuth.getInstance();
                    mUserDatabase = FirebaseDatabase.getInstance().getReference();
                    String current_user_id = firebaseAuth.getCurrentUser().getUid();
                    FirebaseInstanceId.getInstance().getInstanceId()
                            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {

                                    // Get new Instance ID token
                                    String token = task.getResult().getToken();
                                    mUserDatabase.child("Users").child(current_user_id).child("user_token").setValue(token);
                                }
                            });

                    //Sets device token
                    progressDialog.setMessage("You´re now being being beamed in!");
                    progressDialog.show();
                } else {
                    Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                    counter--;

                    Info.setText("Number of attempts remaining: " + String.valueOf(counter));

                    if(counter == 0){
                        Login.setEnabled(false);
                    }
                }
            }
        });

    }

    private void checkEmailVerification(){
        FirebaseUser firebaseUser = firebaseAuth.getInstance().getCurrentUser();
        Boolean emailflag = firebaseUser.isEmailVerified();

        if(emailflag){
            finish();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        } else {
            Toast.makeText(this, "You still need to verify your email.", Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
        }

    }

}

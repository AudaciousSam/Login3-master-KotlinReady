package org.faith.bebetter.YouPage;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.faith.bebetter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class FeedbackActivity extends AppCompatActivity {
    private Button submitFeedback;
    private EditText Title;
    private EditText Text;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        submitFeedback = (Button) findViewById(R.id.btnFeedbackSubmit);
        Title = (EditText) findViewById(R.id.editSubmitFeedbackHeader);
        Text = (EditText) findViewById(R.id.editSubmitFeedbackText);


        submitFeedback.setOnClickListener(view -> submitFeedback(Title.getText().toString(),Text.getText().toString()));
    }

   private void submitFeedback(String title, String text){
       DatabaseReference feedbackDatabase = FirebaseDatabase.getInstance().getReference().child("Feedback");

       firebaseAuth = FirebaseAuth.getInstance();
       String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
       feedbackDatabase.push().setValue(new FeedbackPost(current_user_id,title, text));
   }
}

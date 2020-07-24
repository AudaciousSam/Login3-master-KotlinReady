package org.faith.bebetter.YouPage;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.faith.bebetter.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FeedbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        DatabaseReference feedbackDatabase = FirebaseDatabase.getInstance().getReference().child("Feedback").child("Test");
        feedbackDatabase.setValue("323");
    }
}

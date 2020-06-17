package org.faith.bebetter.FeedPage;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.faith.bebetter.R;

import org.faith.bebetter.NotifsPage.CameraFragment;

public class CameraActivityFromFeed extends AppCompatActivity {

    private CameraFragment myfragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            myfragment =  CameraFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, myfragment)
                    .commit();
        }

        //Get data from NofitsFragment.
        Bundle extras = getIntent().getExtras();
        String experienceKey = extras.getString("EXTRA_KEY");
        String from = extras.getString("EXTRA_FROM");

        //Send data om.
        Bundle bundleFurther = new Bundle();
        bundleFurther.putString("EXTRA_KEY", experienceKey);
        bundleFurther.putString("EXTRA_FROM", from);
        //set Fragmentclass Arguments
        myfragment.setArguments(bundleFurther);

    }
}

package org.faith.bebetter.NotifsPage;

        import androidx.appcompat.app.AppCompatActivity;

        import android.os.Bundle;

        import com.faith.bebetter.R;

public class CameraActivity extends AppCompatActivity {

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

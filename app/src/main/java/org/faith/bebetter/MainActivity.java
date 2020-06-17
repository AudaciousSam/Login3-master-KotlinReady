package org.faith.bebetter;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.faith.bebetter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference usersDatabase;
    private String currentUser;
    private DatabaseReference userDatabase;


    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set Notification token
        firebaseAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference();
        String current_user_id = firebaseAuth.getCurrentUser().getUid();
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        userDatabase.child("Users").child(current_user_id).child("user_token").setValue(token);
                    }
                });

        setContentView(R.layout.activity_main);

        //tabs
        mViewPager = findViewById(R.id.main_tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_notifications_none_white_50dp);
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_people_alt_white24px);
        mTabLayout.getTabAt(2).setIcon(R.drawable.ic_search__white_50px);
        mTabLayout.getTabAt(3).setIcon(R.drawable.ic_person_outline_white_50dp);


        //CHANGE ICON COLORS
        mTabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.TextColorWhite);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                }
        );

        mViewPager.setOffscreenPageLimit(4);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        TabLayout.Tab tab = mTabLayout.getTabAt(1);
        tab.select();

        if(getIntent().getAction() != null) {
            // Open Tab
            TabLayout.Tab tabFromNotif = mTabLayout.getTabAt(0);
            tabFromNotif.select();
        }


        findViewById(R.id.main_tabPager).requestFocus();


        }
}

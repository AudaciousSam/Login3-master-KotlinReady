package org.faith.bebetter;

import android.graphics.drawable.Drawable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.faith.bebetter.FeedPage.FeedFragment;
import org.faith.bebetter.FriendsPage.FriendsFragment;
import org.faith.bebetter.NotifsPage.NotifsFragment;
import org.faith.bebetter.YouPage.YouFragment;
import org.faith.bebetter.ExperiencePage.ExperienceFragment;

class SectionsPagerAdapter extends FragmentPagerAdapter {


    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }


//    @Override
//    public Fragment getItem(int position) {
//
//        switch (position){
//            case 0:
//                return new NotifsFragment();
//            case 1:
//                return new FeedFragment();
//            case 2:
//                return new ExperienceFragment();
//            case 3:
//                return new FriendsFragment();
//            case 4:
//                return new YouFragment();
//
//                default:
//                    //return null;
//                return new FeedFragment();
//        }
//    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                return new NotifsFragment();
            case 1:
                return new FeedFragment();
            case 2:
                return new FriendsFragment();
            case 3:
                return new YouFragment();

            default:
                //return null;
                return new FeedFragment();
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
    Drawable myDrawable;

//    //Titles on pages.
//     public CharSequence getPageTitle(int position){
//        switch (position){
//            case 0:
//                return "Notifs";
//            case 1:
//                return "Memories";
//            case 2:
//                return "Experience";
//            case 3:
//                return "Search";
//            case 4:
//                return "You";
//
//
//            default:
//                return null;
//        }
//    };

    //Titles on pages.
    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "Notifs";
            case 1:
                return "Memories";
            case 2:
                return "Search";
            case 3:
                return "You";

            default:
                return null;
        }
    };

}

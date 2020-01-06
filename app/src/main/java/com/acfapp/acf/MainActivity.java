package com.acfapp.acf;

import android.os.Bundle;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import me.riddhimanadib.library.BottomBarHolderActivity;
import me.riddhimanadib.library.NavigationPage;

public class MainActivity extends BottomBarHolderActivity implements HomeFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NavigationPage page1 = new NavigationPage("Home", ContextCompat.getDrawable(this, R.drawable.ic_home_black_24dp), HomeFragment.newInstance());
        NavigationPage page2 = new NavigationPage("Corruption", ContextCompat.getDrawable(this, R.drawable.ic_corruption), CorruptionFragment.newInstance());
        //NavigationPage page3 = new NavigationPage("Adulteration", ContextCompat.getDrawable(this, R.drawable.ic_adulteration), AdulterationFragment.newInstance());
        NavigationPage page4 = new NavigationPage("Find N Fix", ContextCompat.getDrawable(this, R.drawable.ic_findnfix), FindnFixFragment.newInstance());
        NavigationPage page5 = new NavigationPage("Social Evil", ContextCompat.getDrawable(this, R.drawable.ic_social_evil), SocialEvilFragment.newInstance());
        NavigationPage page6 = new NavigationPage("More", ContextCompat.getDrawable(this, R.drawable.ic_more), GridFragment.newInstance());

        List<NavigationPage> navigationPages = new ArrayList<>();
        navigationPages.add(page1);
        navigationPages.add(page2);
        //navigationPages.add(page3);
        navigationPages.add(page4);
        navigationPages.add(page5);
        navigationPages.add(page6);

        super.setupBottomBarHolderActivity(navigationPages);
    }

}
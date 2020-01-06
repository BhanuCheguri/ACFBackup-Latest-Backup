package com.acfapp.acf;


import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.acfapp.acf.databinding.FragmentFindnfixBinding;

/**
 * Created by priya.cheguri on 8/14/2019.
 */

public class FindnFixFragment extends BaseFragment {

    FragmentFindnfixBinding dataBiding;

    public static FindnFixFragment newInstance() {
        return new FindnFixFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.fragment_findnfix, container, false);

        dataBiding = DataBindingUtil.inflate(inflater, R.layout.fragment_findnfix, null, false);

        setActionBarTitle(getString(R.string.title_findandFix));
        return dataBiding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
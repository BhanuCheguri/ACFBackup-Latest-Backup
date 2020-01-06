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

import com.acfapp.acf.databinding.FragmentCorruptionBinding;


/**
 * Created by priya.cheguri on 8/14/2019.
 */

public class CorruptionFragment extends BaseFragment {

    FragmentCorruptionBinding dataBiding;

    public static CorruptionFragment newInstance() {
        return new CorruptionFragment();
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.fragment_corruption, container, false);
        dataBiding = DataBindingUtil.inflate(inflater, R.layout.fragment_corruption, null, false);

        setActionBarTitle(getString(R.string.title_corruption));

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
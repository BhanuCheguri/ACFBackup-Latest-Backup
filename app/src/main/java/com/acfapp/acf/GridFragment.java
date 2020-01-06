package com.acfapp.acf;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acfapp.acf.databinding.FragmentGridBinding;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

public class GridFragment extends BaseFragment {

    FragmentGridBinding dataBiding;
    String[] web = {
            "Corruption",
            "Adulteration",
            "Find N Fix",
            "Social Evil"
    } ;
    int[] imageId = {
            R.mipmap.ic_corruption_colored,
            R.drawable.ic_adulteration,
            R.drawable.ic_findnfix,
            R.drawable.ic_social_evil,
    };

    public static GridFragment newInstance() {
        return new GridFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        dataBiding = DataBindingUtil.inflate(inflater, R.layout.fragment_grid, null, false);
        setActionBarTitle(getString(R.string.title_more));

        CustomGrid adapter = new CustomGrid(getActivity(), web, imageId);
        dataBiding.grid.setAdapter(adapter);
        dataBiding.grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getActivity(), "You Clicked at " +web[+ position], Toast.LENGTH_SHORT).show();
            }
        });

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


    public class CustomGrid extends BaseAdapter{
        private Context mContext;
        private final String[] web;
        private final int[] Imageid;

        public CustomGrid(Context c,String[] web,int[] Imageid ) {
            mContext = c;
            this.Imageid = Imageid;
            this.web = web;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return web.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View grid;
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {

                grid = new View(mContext);
                grid = inflater.inflate(R.layout.grid_row_layout, null);
                TextView textView = (TextView) grid.findViewById(R.id.grid_text);
                ImageView imageView = (ImageView)grid.findViewById(R.id.grid_img);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 200);
                imageView.setLayoutParams(layoutParams);
                textView.setText(web[position]);
                imageView.setImageResource(Imageid[position]);

            } else {
                grid = (View) convertView;
            }

            return grid;
        }
    }
}

package com.acfapp.acf.ServiceProvider.Petitions;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.acfapp.acf.R;

import java.util.ArrayList;

public class PetitionActivity extends AppCompatActivity {

    ArrayList<PetitionModel> model;
   // ActivityPetetionBinding dataBiding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_petetion);

        /*dataBiding = DataBindingUtil.setContentView(this, R.layout.activity_petetion);

        model= new ArrayList<>();

        model.add(new PetitionModel("1","RK Reddy","Not Verified","Complain on cheating","12th Aug 2019 , 7:20PM" ,"0"));
        model.add(new PetitionModel("2","RK Reddy","Not Verified","Complain on cheating","14th Aug 2019 , 8:20PM" ,"0"));
        model.add(new PetitionModel("3","RK Reddy","Verified","Complain on cheating","18th Aug 2019 , 2:20PM" ,"0"));
        model.add(new PetitionModel("4","RK Reddy","Not Verified","Complain on cheating","19th Aug 2019 , 1:20PM" ,"0"));
        model.add(new PetitionModel("5","RK Reddy","Verified","Complain on cheating","22th Aug 2019 , 9:20PM" ,"0"));

        PetitionsListAdapter adaper = new PetitionsListAdapter(PetitionActivity.this,model);
        dataBiding.lvPetitions.setAdapter(adaper);*/

    }
}

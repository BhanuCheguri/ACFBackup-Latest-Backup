package com.acfapp.acf;

import androidx.fragment.app.FragmentActivity;

import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class HomePageAdapter  extends ArrayAdapter<WallPostsModel> {
    public ArrayList<WallPostsModel> dataSet;
    FragmentActivity context;
    private static LayoutInflater inflater=null;


    public HomePageAdapter(FragmentActivity context,ArrayList<WallPostsModel> data) {
        super(context, R.layout.custom_home_layout, data);
        this.dataSet = data;
        this.context = context;
    }

    public class ViewHolder
    {
        TextView txtTitle;
        TextView txtLocation;
        TextView txtDescription;
        TextView txtDateTime;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        ViewHolder holder = null; // view lookup cache stored in tag
        View rowView = null;

        WallPostsModel dataModel = (WallPostsModel) getItem(position);

        holder = new ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        rowView = inflater.inflate(R.layout.custom_home_layout, null);

        holder.txtTitle = (TextView) rowView.findViewById(R.id.tv_title);
        holder.txtLocation = (TextView) rowView.findViewById(R.id.tv_location);
        holder.txtDescription = (TextView) rowView.findViewById(R.id.tv_description);
        holder.txtDateTime = (TextView) rowView.findViewById(R.id.tv_DateTime);

        holder.txtTitle.setText(dataModel.getTitle());
        holder.txtLocation.setText(dataModel.getLocation());
        holder.txtDateTime.setText(getPostedDate(dataModel.getPostedDate()));
        holder.txtDescription.setText(dataModel.getDescription());


        final ViewHolder finalHolder = holder;

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"Clicked :" + finalHolder.txtTitle.getText().toString(),Toast.LENGTH_LONG).show();
            }
        });

        return rowView;
    }

    //Date date1 = simpleDateFormat.parse("10/10/2013 11:30:10");
   // Date date2 = simpleDateFormat.parse("13/10/2013 20:35:55");

    private String getPostedDate(String postedDate)
    {
        if(postedDate.contains("T"))
        {
            String[] strDateZ = null;
            String date ="";
            Duration diff = null;

            String[] strPostedDate = postedDate.split("T");
            String strDate = strPostedDate[0] + " " + strPostedDate[1];
            if(strDate.contains("Z"))
            {
                strDateZ = strDate.split("Z");
                date = strDateZ[0];
            }
            try {
                if(strDateZ != null  && !date.equalsIgnoreCase("")) {
                    SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd hh:mm:sss");
                    try {

                        Date oldDate = spf.parse(date);
                        System.out.println(oldDate);
                       /* long difference = currentDate.getTime() - oldDate.getTime();
                        long seconds = difference / 1000;
                        long minutes = seconds / 60;
                        long hours = minutes / 60;
                        long days = hours / 24;*/

                       /* if (oldDate.before(currentDate)) {

                            Log.e("oldDate", "is previous date");
                            Log.e("Difference: ", " seconds: " + seconds + " minutes: " + minutes
                                    + " hours: " + hours + " days: " + days);

                            spf = new SimpleDateFormat("dd MMM yyyy");
                            date = spf.format(newDate);
                            System.out.println(date);
                        }*/


                        int day = 0;
                        int hh = 0;
                        int mm = 0;
                        Date currentDate = new Date();
                        Long timeDiff = currentDate.getTime() - oldDate.getTime();
                        day = (int) TimeUnit.MILLISECONDS.toDays(timeDiff);
                        hh = (int) (TimeUnit.MILLISECONDS.toHours(timeDiff) - TimeUnit.DAYS.toHours(day));
                        mm = (int) (TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeDiff)));


                        if (mm <= 60 && hh!= 0) {
                            //if (hh <= 60 && day != 0) {
                            if (hh <= 60 && day != 0) {
                                if(day > 5){
                                    spf = new SimpleDateFormat("dd MMM yyyy");
                                    date = spf.format(oldDate);
                                    return date;
                                }
                                else
                                    return day + " Days";
                            } else {
                                return hh + " Hrs";
                            }
                        } else {
                            return mm + " Min";
                        }

                        // Log.e("toyBornTime", "" + toyBornTime);

                    } catch (ParseException e) {

                        e.printStackTrace();
                    }

                }
                return date;
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return postedDate;
    }

}

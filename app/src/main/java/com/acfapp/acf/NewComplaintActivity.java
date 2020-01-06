package com.acfapp.acf;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.acfapp.acf.databinding.ActivityNewComplaintBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewComplaintActivity extends BaseActivity implements LocationListener {

    ActivityNewComplaintBinding binding;
    String[] Names = {"Corruption", "Adulteration", "Social Evil", "Find and Fix"};
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ArrayList<String> tImagesPath = new ArrayList<String>();
    private int GALLERY = 1, CAMERA = 2;
    private int VIDEO_GALLERY = 3, VIDEO_CAMERA = 4;
    private int PICKFILE_RESULT_CODE = 5;
    private int AUDIO = 6;
    private int LOCATION = 7;
    public static final String complaint_ImagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ACF/";

    NewComplaintModel model;
    ArrayList<NewComplaintModel> lstModelData;
    LocationManager locationManager;
    AppLocationService appLocationService;

    String strTitle,strComplaintType,strIssue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarTitle(getString(R.string.title_new_Item));


        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_complaint);
        init();
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        try {
            Location gpsLocation = appLocationService
                    .getLocation(LocationManager.GPS_PROVIDER);
            if (gpsLocation != null) {
                double latitude = gpsLocation.getLatitude();
                double longitude = gpsLocation.getLongitude();
                LocationAddress locationAddress = new LocationAddress();
                locationAddress.getAddressFromLocation(latitude, longitude,
                        getApplicationContext(), new GeocoderHandler());
            } else {
                //showSettingsAlert();
                Toast.makeText(NewComplaintActivity.this,"Please turn on your Location to get current location.",Toast.LENGTH_LONG).show();
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                NewComplaintActivity.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        NewComplaintActivity.this.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            binding.currentLocation.setText(locationAddress);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        binding.currentLocation.setText("Current Location: " + location.getLatitude() + ", " + location.getLongitude());
        String cityName = null;
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
            if (addresses.size() > 0) {
                System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        String s = cityName;
       // binding.currentLocation.setText(s);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(NewComplaintActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }


    private void init() {
        lstModelData = new ArrayList<>();
        appLocationService = new AppLocationService(NewComplaintActivity.this);

        strTitle = binding.etTitle.getText().toString();
        strIssue = binding.issue.getText().toString();

        String currentTime = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());
        binding.currentDate.setText(currentTime);

        ArrayAdapter adapter = new ArrayAdapter(this,R.layout.custom_textview_layout,R.id.text,Names);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_item);
        binding.spinner.setAdapter(adapter);
        binding.spinner.setThreshold(1);
        binding.spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                binding.spinner.showDropDown();
                return false;
            }
        });
        binding.spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(NewComplaintActivity.this,Names[i],Toast.LENGTH_LONG).show();
                strComplaintType = Names[i];

            }
        });

        binding.fabDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lstModelData.size() < 10) {
                    showAttachementDialog();
                }else
                {
                    Toast.makeText(NewComplaintActivity.this,getString(R.string.error_max_limit_exceeds),Toast.LENGTH_LONG).show();
                }
            }
        });
        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubmitDialog();
            }
        });
    }

    private void showAttachementDialog() {

        Integer[] mThumbIds = {
                R.drawable.camera, R.drawable.video_camera,
                R.drawable.document,R.drawable.audio
        };
        String[] mNames= {"Camera","Video","Document","Audio"};
        List<Integer>  mList = new ArrayList<Integer>();
        for (int i = 1; i < 6; i++) {
            mList.add(i);
        }

        final Dialog dialog = new Dialog(this, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.grid_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        GridView gridView = (GridView) dialog.findViewById(R.id.gridView);
        gridView.setAdapter(new ImageListAdapter(this,mThumbIds,mNames));
        gridView.setNumColumns(3);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.cancel();
                switch (position)
                {
                    case 0:
                        try {
                            requestMultiplePermissions();
                            selectImage();
                        }catch(ActivityNotFoundException anfe){
                            String errorMessage = "Whoops – your device doesn’t support capturing images!";
                            Toast toast = Toast.makeText(NewComplaintActivity.this, errorMessage, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    break;
                    case 1:
                        try {
                            requestMultiplePermissions();
                            selectVideo();
                        }catch(ActivityNotFoundException anfe){
                            String errorMessage = "Whoops – your device doesn’t support capturing videos!";
                            Toast toast = Toast.makeText(NewComplaintActivity.this, errorMessage, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        break;

                    case 2:

                        String[] mimeTypes =
                                {"application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                                        "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                                        "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                                        "text/plain",
                                        "application/pdf"};

                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
                            if (mimeTypes.length > 0) {
                                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                            }
                        } else {
                            String mimeTypesStr = "";
                            for (String mimeType : mimeTypes) {
                                mimeTypesStr += mimeType + "|";
                            }
                            intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
                        }
                        startActivityForResult(intent,PICKFILE_RESULT_CODE);
                        break;

                    case 3:

                        Intent audio_intent = new Intent();
                        audio_intent.setType("audio/*");
                        audio_intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(audio_intent,PICKFILE_RESULT_CODE);

                        break;

                }
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }


    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(NewComplaintActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=Utility.checkPermission(NewComplaintActivity.this);
                if (items[item].equals("Take Photo")) {
                    if(result)
                        takePhotoFromCamera();
                } else if (items[item].equals("Choose from Library")) {
                    if(result)
                        choosePhotoFromGallary();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void selectVideo() {

        final CharSequence[] items = { "Take Video", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(NewComplaintActivity.this);
        builder.setTitle("Add Video!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=Utility.checkPermission(NewComplaintActivity.this);
                if (items[item].equals("Take Video")) {
                    if(result)
                        takeVideoFromCamera();
                } else if (items[item].equals("Choose from Library")) {
                    if(result)
                        chooseVideoFromGallary();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }


    public void chooseVideoFromGallary() {
        Intent video_galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(video_galleryIntent, VIDEO_GALLERY);
    }

    private void takeVideoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, VIDEO_CAMERA);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            Uri contentURI = data.getData();
            try {
                if (data != null) {
                    String[] filePath = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(contentURI, filePath, null, null, null);
                    cursor.moveToFirst();
                    String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                    Uri uri = FileProvider.getUriForFile(NewComplaintActivity.this, BuildConfig.APPLICATION_ID + ".provider", new File(imagePath));

                    setAttachmentData(imagePath,imagePath,uri,getExtensionType(contentURI));

                }
            }catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(NewComplaintActivity.this, "Attachment Failed", Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == CAMERA) {
            Uri contentURI = data.getData();
            try {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                String imgcurTime = dateFormat.format(new Date());
                File imageDirectory = new File(complaint_ImagePath);
                imageDirectory.mkdirs();
                String imagePath = complaint_ImagePath + imgcurTime + ".jpg";
                FileOutputStream out = new FileOutputStream(imagePath);
                photo.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.close();

                Uri uri = FileProvider.getUriForFile(NewComplaintActivity.this, BuildConfig.APPLICATION_ID + ".provider", imageDirectory);

                setAttachmentData(imagePath,imagePath,uri,"jpg");

            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(NewComplaintActivity.this, "Attachment Failed", Toast.LENGTH_SHORT).show();
            }

        }
        else if (requestCode == VIDEO_GALLERY) {
            Uri contentURI = data.getData();

            Log.d("what","gale");
            if (data != null) {
                try {
                    String selectedVideoPath = getPath(contentURI);
                    Log.d("path", selectedVideoPath);

                    String[] filePath = {MediaStore.Video.Media.DATA};
                    Cursor cursor = getContentResolver().query(contentURI, filePath, null, null, null);
                    cursor.moveToFirst();
                    String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                    Uri uri = FileProvider.getUriForFile(NewComplaintActivity.this, BuildConfig.APPLICATION_ID + ".provider", new File(imagePath));

                    setAttachmentData(selectedVideoPath,imagePath,uri,getExtensionType(contentURI));

                }catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(NewComplaintActivity.this, "Attachment Failed", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == VIDEO_CAMERA) {
            Uri contentURI = data.getData();
            try {
                String recordedVideoPath = getPath(contentURI);
                Log.d("frrr", recordedVideoPath);

                String[] filePath = {MediaStore.Video.Media.DATA};
                Cursor cursor = getContentResolver().query(contentURI, filePath, null, null, null);
                cursor.moveToFirst();
                String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                Uri uri = FileProvider.getUriForFile(NewComplaintActivity.this, BuildConfig.APPLICATION_ID + ".provider", new File(imagePath));

                setAttachmentData(recordedVideoPath,imagePath,uri,getExtensionType(contentURI));

            }catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(NewComplaintActivity.this, "Attachment Failed", Toast.LENGTH_SHORT).show();
            }

        }else if(requestCode == PICKFILE_RESULT_CODE) {
            Uri contentURI = data.getData();
            try {
                String uriString = contentURI.toString();
                File myFile = new File(uriString);
                String path = myFile.getAbsolutePath();

                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtensionType(contentURI));

                setAttachmentData(path,path,contentURI,mimeType);

            }catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(NewComplaintActivity.this, "Attachment Failed", Toast.LENGTH_SHORT).show();
            }
        }

        else if(requestCode == AUDIO) {
            Uri contentURI = data.getData();
            try {
                String uriString = contentURI.toString();
                File myFile = new File(uriString);
                String path = myFile.getAbsolutePath();

                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtensionType(contentURI));

                setAttachmentData(path,path,contentURI,mimeType);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(NewComplaintActivity.this, "Attachment Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setAttachmentData(String Content, String FilePath, Uri contentURI, String mimeType) {

        model = new NewComplaintModel();
        model.setContent(Content);
        model.setFilePath(FilePath);
        model.setUri(contentURI);
        model.setExtensionType(mimeType);
        lstModelData.add(model);

        binding.grid.setAdapter(new CustomImageAdapter(this, lstModelData));
    }


    public String getExtensionType(Uri contentURI)
    {
        String extension = "";
        if(contentURI != null) {
            ContentResolver contentResolver = getContentResolver();
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(contentURI));
            System.out.print("extension ::" + extension);
        }

        return extension;
    }


    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Video.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

   /* private List<String> RetriveCapturedImagePath() {
        List<String> tFileList = new ArrayList<String>();
        File f = new File(complaint_ImagePath);
        if (f.exists()) {
            File[] files=f.listFiles();
            Arrays.sort(files);

            for(int i=0; i<files.length; i++){
                File file = files[i];
                if(file.isDirectory())
                    continue;
                tFileList.add(file.getPath());
            }
        }
        return tFileList;
    }


    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(complaint_ImagePath);
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::---&gt;" + f.getAbsolutePath());
            tImagesPath.add(wallpaperDirectory.toString());
            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }*/

    private void  requestMultiplePermissions(){
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {

                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    public class CustomImageAdapter extends BaseAdapter
    {
        private Context context;
        private ArrayList<NewComplaintModel> lstModelData;
        private LayoutInflater inflater=null;
        Bitmap bm = null;

        public CustomImageAdapter(NewComplaintActivity c, ArrayList<NewComplaintModel> lstData) {
            context = c;
            lstModelData = lstData;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            if(lstModelData != null)
                return lstModelData.size();
            else
                return 0;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }


        public class Holder
        {
            ImageView grid_close;
            ImageView grid_img;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {

            Holder holder = new Holder();
            View rowView;

            rowView = inflater.inflate(R.layout.new_complaint_grid_row_layout, null);
            holder.grid_img = (ImageView) rowView.findViewById(R.id.grid_img);
            //holder.grid_close = (ImageView) rowView.findViewById(R.id.img_close);


            final String strMimeType = lstModelData.get(position).getExtensionType();
            if(strMimeType.equalsIgnoreCase("jpg") || strMimeType.equalsIgnoreCase("png")) {
                getBitmap(lstModelData.get(position).getFilePath());
                holder.grid_img.setImageBitmap(bm);
                holder.grid_img.setId(position);
            }
            else if(strMimeType.equalsIgnoreCase("mp4")) {
                File file = new File(lstModelData.get(position).getFilePath());
                Bitmap bMap = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND);
                if (bMap != null) {
                    holder.grid_img.setImageBitmap(bMap);
                    holder.grid_img.setId(position);
                }
            }
            else if(strMimeType.equalsIgnoreCase("application/pdf")) {
                holder.grid_img.setImageResource(R.drawable.pdf);
                holder.grid_img.setId(position);
            }
            else if(strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document") || strMimeType.equalsIgnoreCase("application/msword")) {
                holder.grid_img.setImageResource(R.drawable.doc);
                holder.grid_img.setId(position);
            }
            else if(strMimeType.equalsIgnoreCase("application/vnd.ms-excel") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                holder.grid_img.setImageResource(R.drawable.excel);
                holder.grid_img.setId(position);
            }
            else if(strMimeType.equalsIgnoreCase("application/vnd.ms-powerpoint") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                holder.grid_img.setImageResource(R.drawable.powerpoint);
                holder.grid_img.setId(position);
            }
            else if(strMimeType.equalsIgnoreCase("text/plain")) {
                holder.grid_img.setImageResource(R.drawable.txt);
                holder.grid_img.setId(position);
            }else if(strMimeType.equalsIgnoreCase("audio/mpeg")) {
                holder.grid_img.setImageResource(R.drawable.mp3);
                holder.grid_img.setId(position);
            }
            else
            {
                Toast.makeText(context,"This attachment doesn't supported by ACF" ,Toast.LENGTH_LONG).show();
            }

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        
                        if(!strMimeType.equalsIgnoreCase("mp4") && !strMimeType.equalsIgnoreCase("jpg") && !strMimeType.equalsIgnoreCase("png")) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(lstModelData.get(position).getUri(), strMimeType);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                        }
                        else
                        {
                            if(strMimeType.equalsIgnoreCase("mp4"))
                            {
                                Intent intent = new Intent(context,ViewMediaActivity.class);
                                intent.putExtra("FilePath",lstModelData.get(position).getFilePath());
                                intent.putExtra("FileType","mp4");
                                context.startActivity(intent);
                            }else  if(strMimeType.equalsIgnoreCase("jpg") ||  strMimeType.equalsIgnoreCase("png")){
                                Intent intent = new Intent(context,ViewMediaActivity.class);
                                intent.putExtra("FilePath",lstModelData.get(position).getFilePath());
                                intent.putExtra("FileType","jpg");
                                context.startActivity(intent);
                            }
                        }
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(context,"No Activity found to handle this file.Need to install supported Application",Toast.LENGTH_LONG).show();
                    }
                }
            });

            return rowView;
        }


        public Bitmap getBitmap(String url) {
            FileInputStream fIn = null;
            try {
                //fIn = new FileInputStream(new File(imgData.get(position).toString()));
                BitmapFactory.Options bfOptions = new BitmapFactory.Options();
                bfOptions.inDither = false;                     //Disable Dithering mode
                bfOptions.inPurgeable = true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
                bfOptions.inInputShareable = true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
                bfOptions.inTempStorage = new byte[32 * 1024];

                fIn = new FileInputStream(new File(url));

                if (fIn != null) {
                    bm = BitmapFactory.decodeFileDescriptor(fIn.getFD(), null, bfOptions);
                    if (bm != null) {

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fIn != null) {
                    try {
                        fIn.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return bm;
        }
    }

    public void SubmitDialog()
    {
        if (!strIssue.equalsIgnoreCase("") && !strTitle.equalsIgnoreCase("") && !strComplaintType.equalsIgnoreCase("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //builder.setTitle("Upload");
            //Setting message manually and performing action on button click
            builder.setMessage("Do you want to upload the complaint?")
                    .setCancelable(false)
                    .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //finish();
                            dialog.cancel();
                       /* Toast.makeText(getApplicationContext(),"Under development",
                                Toast.LENGTH_SHORT).show();*/
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //  Action for 'NO' Button
                            dialog.cancel();
                        }
                    });
            //Creating dialog box
            AlertDialog alert = builder.create();
            //Setting the title manually
            alert.setTitle("Upload Alert");
            alert.show();
        }else {
            Toast.makeText(NewComplaintActivity.this, "Please fill all the fields", Toast.LENGTH_LONG).show();
        }
    }
}

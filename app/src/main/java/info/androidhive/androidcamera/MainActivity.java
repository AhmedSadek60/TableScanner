package info.androidhive.androidcamera;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {

    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;

    // key to store image path in savedInstance state
    public static final String KEY_IMAGE_STORAGE_PATH = "image_path";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    // Bitmap sampling size
    public static final int BITMAP_SAMPLE_SIZE = 8;

    // Gallery directory name to store the images or videos
    public static final String GALLERY_DIRECTORY_NAME = "Hello Camera";

    // Image and Video file extensions
    public static final String IMAGE_EXTENSION = "jpg";
    public static final String VIDEO_EXTENSION = "mp4";

    private static String imageStoragePath;

    private TextView txtDescription;
    private ImageView imgPreview;
    private VideoView videoPreview;
    private Button btnCapturePicture, btnRecordVideo;
    String encodedImage;
    int flag=0;
    String TAG  = "tag";
    File sourceFile;
    int rowsNo,colNo;
    EditText fileNameEdit;
    String fileName;
    String CurrentFilePath;
    EditText textView;
    String image;
    CircularProgressButton circularProgressButton;
    String parentPath;
    ArrayList<String> rowDataArray = new ArrayList<>();
    String ocrChoice="printed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Table Scanner");
        setSupportActionBar(toolbar);
        AndroidNetworking.initialize(getApplicationContext());

        // Checking availability of the camera
        if (!CameraUtils.isDeviceSupportCamera(getApplicationContext())) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device doesn't have camera
            finish();
        }

       // fileNameEdit= (EditText) findViewById(R.id.fileName);
        txtDescription = findViewById(R.id.txt_desc);
        imgPreview = findViewById(R.id.imgPreview);
        videoPreview = findViewById(R.id.videoPreview);
        btnCapturePicture = findViewById(R.id.btnCapturePicture);
        btnRecordVideo = findViewById(R.id.btnRecordVideo);

        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        final View mView = getLayoutInflater().inflate(R.layout.wait_activity, null);
        circularProgressButton = (CircularProgressButton)mView.findViewById(R.id.btndown);

        final AlertDialog[] dialog = {mBuilder.create()};
        // v.setVisibility(View.GONE);

        /**
         * Capture image on button click
         */

        btnCapturePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (CameraUtils.checkPermissions(getApplicationContext())) {
                    captureImage();

                } else {
                    requestCameraPermission(MEDIA_TYPE_IMAGE);
                }
            }
        });

        // restoring storage image path from saved instance state
        // otherwise the path will be null on device rotation
        restoreFromBundle(savedInstanceState);


        btnRecordVideo = findViewById(R.id.convert);
        btnRecordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(flag==1){
                    fileName = "firstCSV";
                    AndroidNetworking.upload("https://tablescanner.herokuapp.com/upload/")
                    //AndroidNetworking.upload(" http://8371c9d3.ngrok.io/upload/")
                            .addMultipartParameter("data", encodedImage)
                            .addMultipartParameter("ocr", ocrChoice)
                            .setTag("uploadTest")
                            .setPriority(Priority.HIGH)
                            .build()
                            .setUploadProgressListener(new UploadProgressListener() {
                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                @Override
                                public void onProgress(long bytesUploaded, long totalBytes) {
                                    // do anything with progress
                                    mBuilder.setView(mView);
                                    dialog[0] = mBuilder.create();
                                    dialog[0].getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                                    //dialog[0].show();
                                }
                            })
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @Override
                                public void onResponse(JSONObject response) {
                                    // save();
                                    // do anything with response
                                    imgPreview.setVisibility(View.GONE);
                                    txtDescription.setVisibility(View.VISIBLE);
                                    Log.i(TAG, "onResponse: "+response.toString());
                                    //Toast.makeText(getApplicationContext(),"Posted",LENGTH_SHORT).show();
                                    try {
                                         rowsNo = response.getInt("rows");
                                         colNo = response.getInt("columns");
                                        Log.i(TAG, "onResponse: rows="+rowsNo+ " columns="+colNo);
                                        JSONArray rows = response.getJSONArray("rowsdata");
                                        for(int counter=0; counter<rows.length();counter++){
                                            String rowData="";
                                            JSONObject oneRow = rows.getJSONObject(counter);
                                            for(int iter=0;iter<colNo;iter++){
                                                int index=iter+1;
                                                String data = oneRow.getString("col"+index);
                                                rowData=rowData+data+",";
                                                Log.i(TAG, "field: "+data);

                                            }
                                            rowDataArray.add(rowData+" "+"\n");
                                            Log.i(TAG, "row: "+rowData);
                                            rowData="";
                                        }
                                        rows=new JSONArray();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    if(colNo !=0 && rowsNo !=0) {
                                        File csvFile = null;
                                        BufferedWriter bw=null;
                                        try {
                                            csvFile = createFile();
                                        } catch (IOException ex) {
                                        }
                                        Log.i(TAG, "Entered: Here");
                                        String totalContent="";
                                        for(int x=0;x<rowsNo;x++) {
                                            String content = rowDataArray.get(x);
                                            totalContent=totalContent+" "+content;
                                            Log.i(TAG, "Content"+content);
                                            Log.i(TAG, "totalContent"+totalContent);
                                            content="";
                                        }

                                        FileWriter fw = null;
                                        try {
                                            fw = new FileWriter(csvFile.getAbsoluteFile());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Log.i(TAG, "onResponse: writeFile error =" + e.toString());
                                        }
                                        bw = new BufferedWriter(fw);
                                        fw=null;
                                        try {
                                            bw.write(totalContent);
                                            totalContent="";

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Log.i(TAG, "onResponse: bufferFile error =" + e.toString());
                                        }
                                        try {
                                            bw.close();
                                            bw=null;
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Log.i(TAG, "onResponse: closeFile error =" + e.toString());
                                        }
                                        flag=0;

                                        Intent i = new Intent(getApplicationContext(),FilesActivity.class);
                                        i.putExtra("path",parentPath);
                                        startActivity(i);
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(),"No data inside the table!",LENGTH_SHORT).show();
                                    }
                                    response=null;
                                }
                                @Override
                                public void onError(ANError error) {
                                  //  Toast.makeText(MainActivity.this,"Download done",Toast.LENGTH_SHORT).show();
                                    circularProgressButton.doneLoadingAnimation(Color.parseColor("#333639"), BitmapFactory.decodeResource(getResources(),R.drawable.ic_done_white_48dp));
                                    circularProgressButton.startAnimation();
                                    dialog[0].dismiss();
                                    // handle error
                                    Log.i(TAG, "onError: "+error.toString());
                                    if(error.toString().contains("java.net.UnknownHostException: Unable to resolve host")){
                                        Toast.makeText(getApplicationContext(),"Please check your internet connection!",LENGTH_SHORT).show();
                                    }
                                    else if(error.toString().contains("com.androidnetworking.error.ANError")){
                                        Toast.makeText(getApplicationContext(),"Sorry! Server is not running now.",LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                else {
                    Toast.makeText(getApplicationContext(),"Please choose image first!",LENGTH_SHORT).show();
                }
            }
        });
    }


    public void showDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setPositiveButton("Printed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               ocrChoice="printed";
            }
        }).setNegativeButton("Handwritten", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ocrChoice="handwritten";
            }
        }).show();
    }

    public void save (){
        FileOutputStream fos = null;

        try {
            fos = openFileOutput(fileName, MODE_PRIVATE);
            fos.write(getOrderText("string from edittext", "string from edittext", "string from edittext").getBytes());
            fos.write("\n".getBytes());
            Toast.makeText(this, "Saved to " + getFilesDir() + "/" + fileName, Toast.LENGTH_LONG).show();
            Log.i(TAG, "save: "+"Saved to " + getFilesDir() + "/" + fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private File createFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TableScanner_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".csv",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        CurrentFilePath = image.getAbsolutePath();
         parentPath=image.getParent();
        return image;
    }

    private String getOrderText(String aa, String bb, String cc){

        StringBuilder sb = new StringBuilder();
        sb.append(aa+ ";");
        sb.append(" " + bb+ ";");
        sb.append(" " + cc);

        return sb.toString();
    }

    /**
     * Restoring store image path from saved instance state
     */
    private void restoreFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_IMAGE_STORAGE_PATH)) {
                imageStoragePath = savedInstanceState.getString(KEY_IMAGE_STORAGE_PATH);
                if (!TextUtils.isEmpty(imageStoragePath)) {
                    if (imageStoragePath.substring(imageStoragePath.lastIndexOf(".")).equals("." + IMAGE_EXTENSION)) {
                        previewCapturedImage();
                    } else if (imageStoragePath.substring(imageStoragePath.lastIndexOf(".")).equals("." + VIDEO_EXTENSION)) {
                        previewVideo();
                    }
                }
            }
        }
    }

    /**
     * Requesting permissions using Dexter library
     */
    private void requestCameraPermission(final int type) {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {

                            if (type == MEDIA_TYPE_IMAGE) {
                                // capture picture
                                captureImage();
                            } else {
                                captureVideo();
                            }

                        } else if (report.isAnyPermissionPermanentlyDenied()) {
                            showPermissionsAlert();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    /**
     * Capturing Camera Image will launch camera app requested image capture
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = CameraUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (file != null) {
            imageStoragePath = file.getAbsolutePath();
        }
        Uri fileUri = CameraUtils.getOutputMediaFileUri(getApplicationContext(), file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Saving stored image path to saved instance state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putString(KEY_IMAGE_STORAGE_PATH, imageStoragePath);
    }

    /**
     * Restoring image path from saved instance state
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        imageStoragePath = savedInstanceState.getString(KEY_IMAGE_STORAGE_PATH);
    }

    /**
     * Launching camera app to record video
     */
    private void captureVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        File file = CameraUtils.getOutputMediaFile(MEDIA_TYPE_VIDEO);
        if (file != null) {
            imageStoragePath = file.getAbsolutePath();
        }

        Uri fileUri = CameraUtils.getOutputMediaFileUri(getApplicationContext(), file);

        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file

        // start the video capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    public static String ConvertBitmapToString(Bitmap bitmap){
        String encodedImage = "";

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        try {
            encodedImage= URLEncoder.encode(Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return encodedImage;
    }

    /**
     * Activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Refreshing the gallery
                CameraUtils.refreshGallery(getApplicationContext(), imageStoragePath);

                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Refreshing the gallery
                CameraUtils.refreshGallery(getApplicationContext(), imageStoragePath);

                // video successfully recorded
                // preview the recorded video
                previewVideo();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled recording
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to record video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * Display image from gallery
     */
    private void previewCapturedImage() {
        try {
            // hide video preview
            txtDescription.setVisibility(View.GONE);
            videoPreview.setVisibility(View.GONE);

            imgPreview.setVisibility(View.VISIBLE);

            Bitmap bitmap = CameraUtils.optimizeBitmap(BITMAP_SAMPLE_SIZE, imageStoragePath);

            imgPreview.setImageBitmap(bitmap);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
            byte[] b = bytes.toByteArray();
            encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

            Log.i(TAG, "previewCapturedImage: encodedImage:"+encodedImage);
            Log.i(TAG, "previewCapturedImage: h="+bitmap.getHeight()+" w="+bitmap.getWidth());
            //TODO: give bitmap to python
            flag=1;
            showDialog("Is the text handwritten or printed ?");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displaying video in VideoView
     */
    private void previewVideo() {
        try {
            // hide image preview
            txtDescription.setVisibility(View.GONE);
            imgPreview.setVisibility(View.GONE);

            videoPreview.setVisibility(View.VISIBLE);
            videoPreview.setVideoPath(imageStoragePath);
            // start playing
            videoPreview.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Alert dialog to navigate to app settings
     * to enable necessary permissions
     */
    private void showPermissionsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissions required!")
                .setMessage("Camera needs few permissions to work properly. Grant them in settings.")
                .setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CameraUtils.openSettings(MainActivity.this);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
}



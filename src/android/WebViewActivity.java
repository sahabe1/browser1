/*
 Copyright 2014 Giovanni Di Gregorio.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.purchasingpower.inappbrowser;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.os.Bundle;
import android.content.Intent;


import android.widget.ProgressBar;
import android.view.View;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hello.MainActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class WebViewActivity extends Activity {
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int FILECHOOSER_RESULTCODE = 1;
    private static final String TAG = com.purchasingpower.inappbrowser.WebViewActivity.class.getSimpleName();
    ImageView back;
    ImageView forward;
    TextView done;
    ProgressBar pbar;
    private WebSettings webSettings;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            //setContentView(R.layout.activity_main);
        setContentView(getResources().getIdentifier("nativebrowserplugin", "layout", getPackageName()));
        if (!checkPermission()) {
            requestPermission();
        }
        Intent intent = getIntent();
        String url = intent.getExtras().getString("url");
        final WebView myWebView = (WebView) findViewById(getResources().getIdentifier("webview", "id", getPackageName()));
        myWebView.setWebViewClient(new MyWebViewClient());

        webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowFileAccess(true);
        myWebView.setWebChromeClient(new PQChromeClient());
            //if SDK version is greater of 19 then activate hardware acceleration otherwise activate software acceleration
        if (Build.VERSION.SDK_INT >= 19) {
            myWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 19) {
            myWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        myWebView.loadUrl(url);

        back = (ImageView) findViewById(getResources().getIdentifier("back", "id", getPackageName()));
        back.setImageResource(getResources().getIdentifier("back_gray", "drawable", getPackageName()));
        forward = (ImageView) findViewById(getResources().getIdentifier("forward", "id", getPackageName()));
        forward.setImageResource(getResources().getIdentifier("forward_gray", "drawable", getPackageName()));
        done = (TextView) findViewById(getResources().getIdentifier("done", "id", getPackageName()));
        pbar = (ProgressBar) findViewById(getResources().getIdentifier("pbar", "id", getPackageName()));
        pbar.setVisibility(View.VISIBLE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myWebView.canGoBack()) {
                    myWebView.goBack();
                }
            }
        });
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myWebView.canGoForward()) {
                    myWebView.goForward();
                }
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("Done", "Clicked");
                finish();
            }
        });
    }

        // Checking permission at Runtime for Marshmallow and above.
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

        // Requesting permission at Runtime for Marshmallow and above.
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);//
        }
    }

        // Granting permission at Runtime for Marshmallow and above.
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }

            Uri[] results = null;

                // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                        // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }

            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;

        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }

            if (requestCode == FILECHOOSER_RESULTCODE) {

                if (null == this.mUploadMessage) {
                    return;

                }

                Uri result = null;

                try {
                    if (resultCode != RESULT_OK) {

                        result = null;

                    } else {

                            // retrieve from the private variable if the intent is null
                        result = data == null ? mCapturedImageURI : data.getData();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "activity :" + e,
                                   Toast.LENGTH_LONG).show();
                }

                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;

            }
        }

        return;
    }

    private File createImageFile() throws IOException {
            // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                                                                        Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                                             imageFileName,  /* prefix */
                                             ".jpg",         /* suffix */
                                             storageDir      /* directory */
                                             );
        return imageFile;
    }

    public class PQChromeClient extends WebChromeClient {

            // For Android 5.0
        public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
                // Double check that we don't have any existing callbacks
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePath;

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                } catch (IOException ex) {
                        // Error occurred while creating the File
                    Log.e(TAG, "Unable to create Image File", ex);
                }

                    // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                               Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("*/*");

            Intent[] intentArray;
            if (takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
            return true;

        }

            // openFileChooser for Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {

            mUploadMessage = uploadMsg;
                // Create AndroidExampleFolder at sdcard
                // Create AndroidExampleFolder at sdcard

            File imageStorageDir = new File(
                                            Environment.getExternalStoragePublicDirectory(
                                                                                          Environment.DIRECTORY_PICTURES)
                                            , "AndroidExampleFolder");

            if (!imageStorageDir.exists()) {
                    // Create AndroidExampleFolder at sdcard
                imageStorageDir.mkdirs();
            }

                // Create camera captured image file path and name
            File file = new File(
                                 imageStorageDir + File.separator + "IMG_"
                                 + String.valueOf(System.currentTimeMillis())
                                 + ".jpg");

            mCapturedImageURI = Uri.fromFile(file);

                // Camera capture image intent
            final Intent captureIntent = new Intent(
                                                    android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");

                // Create file chooser intent
            Intent chooserIntent = Intent.createChooser(i, "Image Chooser");

                // Set camera intent to file chooser
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                                   , new Parcelable[]{captureIntent});

                // On select image call onActivityResult method of activity
            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);


        }

            // openFileChooser for Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "");
        }

            //openFileChooser for other Android versions
        public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                    String acceptType,
                                    String capture) {

            openFileChooser(uploadMsg, acceptType);
        }

    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            pbar.setVisibility(View.VISIBLE);
            back.setEnabled(view.canGoBack());
            forward.setEnabled(view.canGoForward());
            if (view.canGoBack()) {
                back.setImageResource(getResources().getIdentifier("back_blue", "drawable", getPackageName()));
            } else {
                back.setImageResource(getResources().getIdentifier("back_gray", "drawable", getPackageName()));
            }

            if (view.canGoForward()) {
                forward.setImageResource(getResources().getIdentifier("forward_blue", "drawable", getPackageName()));
            } else {
                forward.setImageResource(getResources().getIdentifier("forward_gray", "drawable", getPackageName()));
            }

                //You can add some custom functionality here
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub

            if (url.contains("mailto:")) {

                    // Could be cleverer and use a regex
                    //Open links in new browser
                view.getContext().startActivity(
                                                new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

                    // Here we can open new activity
                
                return true;
                
            } else {
                
                    // Stay within this webview and load url
                view.loadUrl(url);
                return true;
            }
            
        }
        
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            pbar.setVisibility(View.GONE);
            back.setEnabled(view.canGoBack());
            forward.setEnabled(view.canGoForward());
            if (view.canGoBack()) {
                back.setImageResource(getResources().getIdentifier("back_blue", "drawable", getPackageName()));
            } else {
                back.setImageResource(getResources().getIdentifier("back_gray", "drawable", getPackageName()));
            }
            
            if (view.canGoForward()) {
                forward.setImageResource(getResources().getIdentifier("forward_blue", "drawable", getPackageName()));
            } else {
                forward.setImageResource(getResources().getIdentifier("forward_gray", "drawable", getPackageName()));
            }
                //You can add some custom functionality here
        }
        
        
    }
}

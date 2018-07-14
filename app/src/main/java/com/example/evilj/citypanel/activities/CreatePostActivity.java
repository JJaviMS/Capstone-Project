package com.example.evilj.citypanel.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.evilj.citypanel.R;
import com.example.evilj.citypanel.services.CreatePostService;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreatePostActivity extends AppCompatActivity {
    private final static int GALLERY_INTENT_ID = 0;
    private final static int CAMERA_INTENT_ID = 1;
    private final static int EXTERNAL_STORAGE_WRITTE_REQUEST = 2;
    private final static int EXTERNAL_STORAGE_READ_REQUEST = 3;
    private final static int CAMERA_REQUEST = 4;
    private final static int CAMERA_WRITTE_STORAGE_REQUEST = 5;
    private final static String TAG = CreatePostActivity.class.getSimpleName();
    private final static int LOG_IN_REQ = 6;
    private final static String KEY_PATH = "path";

    public final static String EXTRA_CITY = "city";

    @BindView(R.id.post_edit_text)
    EditText mPostEdit;
    @BindView(R.id.add_image_iv)
    ImageView mAddImageIv;
    @BindView(R.id.camera_image_view)
    ImageView mCameraImageView;

    private String mCurrentUri;
    private String city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) goToLogin();
        setContentView(R.layout.activity_create_post);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        if (intent == null) throw new IllegalStateException();
        city = intent.getStringExtra(EXTRA_CITY);
    }

    @OnClick(R.id.post_button)
    void publishPost() {
        String post = mPostEdit.getEditableText().toString().trim();
        if (post.isEmpty() && mCurrentUri == null) {
            Toast.makeText(this, R.string.please_input, Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) throw new IllegalStateException();
        String userName = user.getDisplayName();
        String userImg = user.getPhotoUrl() == null ? null : user.getPhotoUrl().toString();
        String userUid = user.getUid();
        if (mCurrentUri != null) {
            CreatePostService.startActionImage(this, post, userName, userImg, city, userUid, mCurrentUri);
        } else {
            CreatePostService.startActionPostNoImage(this, post, userName, userImg, city, userUid);
        }
        finish();


    }

    /**
     * Creates the request to get an image, check permissions
     */
    @OnClick(R.id.add_image_iv)
    void addImage() {
        if (checkReadStoragePermission()) {
            if (mCurrentUri == null) {
                Intent gallery = new Intent(Intent.ACTION_PICK);
                gallery.setType("image/*");
                startActivityForResult(gallery, GALLERY_INTENT_ID);
            } else {
                mCurrentUri = null;
                changePickedImageState();
            }
        } else {
            requestReadPermissions();
            //The method should be called again
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY_INTENT_ID: {
                    Uri uri = data.getData();
                    if (uri != null) {
                        mCurrentUri = uri.toString();
                        changePickedImageState();
                    } else {
                        mCurrentUri = null;
                        changePickedImageState();
                    }
                    break;

                }
                case CAMERA_INTENT_ID: {
                    changePickedImageState();
                    break;
                }
                case LOG_IN_REQ: {
                    invalidateOptionsMenu();
                    break;
                }
            }
        } else {
            switch (requestCode) {
                case LOG_IN_REQ:
                    Toast.makeText(this, R.string.must_be_logged, Toast.LENGTH_SHORT).show();
                    finish();
            }
        }
    }

    /**
     * This method will change the buttons depending on the state of the imageBitmap
     */
    private void changePickedImageState() {
        if (mCurrentUri != null) {
            mCameraImageView.setVisibility(View.GONE);
            mAddImageIv.setImageResource(R.drawable.ic_cancel_black_24dp);
        } else {
            mCameraImageView.setVisibility(View.VISIBLE);
            mAddImageIv.setImageResource(R.drawable.ic_add_to_photos_black_24dp);
        }
    }

    /**
     * This method will create the request to take a picture, it checks for permissions
     */
    @OnClick(R.id.camera_image_view)
    void cameraImage() {
        boolean cameraPermission = checkCameraPermission();
        boolean storage = checkWritteStoragePermission();
        if (!(cameraPermission && storage)) {
            cameraPermissionsRequest(cameraPermission, storage);
            return;//The method should be called again
        }
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (camera.resolveActivity(getPackageManager()) != null) {
            File file;
            try {
                file = createFile();
            } catch (IOException e) {
                file = null;
                e.printStackTrace();
            }
            if (file == null) {
                Log.e(TAG, "Problem getting file");
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                return;
            }
            Uri photoUri = FileProvider.getUriForFile(this, this.getPackageName(), file);
            mCurrentUri = photoUri.toString();
            camera.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(camera, CAMERA_INTENT_ID);
        }
    }

    /**
     * Check the reading the storage permissions
     *
     * @return True if the permissions are granted
     */
    private boolean checkReadStoragePermission() {
        int permissionState = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check the written storage permissions
     *
     * @return True if the permissions are granted
     */
    private boolean checkWritteStoragePermission() {
        int permissionState = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check for camera permissions
     *
     * @return True if the permissions are granted
     */
    private boolean checkCameraPermission() {
        int permissionState = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request the reading storage permissions
     */
    private void requestReadPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_READ_REQUEST);
    }

    /**
     * Request camera and writting storage permissions if needed
     *
     * @param camera  If the app have camera permissions
     * @param storage If the app have written storage
     */
    private void cameraPermissionsRequest(boolean camera, boolean storage) {
        String[] permissions = new String[!camera && !storage ? 2 : 1];
        int code;
        if (!camera && !storage) {
            permissions[0] = Manifest.permission.CAMERA;
            permissions[1] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            code = CAMERA_WRITTE_STORAGE_REQUEST;
        } else if (!camera) {
            permissions[0] = Manifest.permission.CAMERA;
            code = CAMERA_REQUEST;
        } else {
            permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            code = EXTERNAL_STORAGE_WRITTE_REQUEST;
        }
        ActivityCompat.requestPermissions(this, permissions, code);
    }

    /**
     * Creates the reference to save an image
     *
     * @return Reference of the storage
     * @throws IOException Error creating the reference
     */
    @NonNull
    private File createFile() throws IOException {
        @SuppressLint("SimpleDateFormat")
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "JPEG_" + time + "_";
        File storageInf = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(imageName, ".jpg", storageInf);
        mCurrentUri = file.getAbsolutePath();
        return file;
    }

    /**
     * Show the image in the gallery
     */
    private void addToGallery() {
        if (mCurrentUri == null) {
            Log.d(TAG, "Path is null, can´t send image to gallery");
            return;
        }
        Intent mediaScan = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(mCurrentUri);
        Uri uri = Uri.fromFile(file);
        mediaScan.setData(uri);
        this.sendBroadcast(mediaScan);
    }

    @Nullable
    private Bitmap getBitmapFromPath() {
        File file = new File(mCurrentUri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            return BitmapFactory.decodeStream(new FileInputStream(file), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void goToLogin() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), LOG_IN_REQ);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_PATH, mCurrentUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentUri = savedInstanceState.getString(KEY_PATH);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            getMenuInflater().inflate(R.menu.sign_out, menu);
            return true;
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_button: {
                FirebaseAuth.getInstance().signOut();
                invalidateOptionsMenu();
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}

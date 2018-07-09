package com.example.evilj.citypanel;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.example.evilj.citypanel.Models.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CreatePostService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_POST_NO_IMAGE = "com.example.evilj.citypanel.action.POST_NO_IMAGE";
    private static final String ACTION_POST_IMAGE = "com.example.evilj.citypanel.action.POST_IMAGE";

    // TODO: Rename parameters
    private static final String EXTRA_IMAGE = "com.example.evilj.citypanel.extra.IMAGE";
    private static final String EXTRA_POST = "com.example.evilj.citypanel.extra.POST";
    private static final String EXTRA_USER = "com.example.evilj.citypanel.extra.USER";
    private static final String EXTRA_USER_IMAGE = "com.example.evilj.citypanel.extra.USER_IMAGE";
    private static final String EXTRA_CITY = "com.example.evilj.citypanel.extra.CITY";
    private static final String EXTRA_USER_UID = "com.example.evilj.citypanel.extra.USER_UID";

    public CreatePostService() {
        super("CreatePostService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionPostNoImage(Context context, String post, String user,String userImage, String city, String userUid) {
        Intent intent = new Intent(context, CreatePostService.class);
        intent.setAction(ACTION_POST_NO_IMAGE);
        intent.putExtra(EXTRA_POST, post);
        intent.putExtra(EXTRA_USER, user);
        intent.putExtra(EXTRA_USER_IMAGE,userImage);
        intent.putExtra(EXTRA_CITY,city);
        intent.putExtra(EXTRA_USER_UID,userUid);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionImage(Context context, String post, String user,String userImage, String city, String userUid, byte[] image ) {
        Intent intent = new Intent(context, CreatePostService.class);
        intent.setAction(ACTION_POST_NO_IMAGE);
        intent.putExtra(EXTRA_POST, post);
        intent.putExtra(EXTRA_USER, user);
        intent.putExtra(EXTRA_USER_IMAGE,userImage);
        intent.putExtra(EXTRA_CITY,city);
        intent.putExtra(EXTRA_USER_UID,userUid);
        intent.putExtra(EXTRA_IMAGE,image);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_POST_IMAGE.equals(action)) {
                final String extraPost = intent.getStringExtra(EXTRA_POST);
                final String extraUser = intent.getStringExtra(EXTRA_USER);
                final String extraUserImg = intent.getStringExtra(EXTRA_USER_IMAGE);
                final String extraCity = intent.getStringExtra(EXTRA_CITY);
                final String extraUserUid = intent.getStringExtra(EXTRA_USER_UID);
                final byte [] image = intent.getByteArrayExtra(EXTRA_IMAGE);
                Post post = new Post(extraPost,null,extraUserUid,extraCity,extraUser,extraUserImg);
                handleActionImage(post,image);
            } else if (ACTION_POST_NO_IMAGE.equals(action)) {
                final String extraPost = intent.getStringExtra(EXTRA_POST);
                final String extraUser = intent.getStringExtra(EXTRA_USER);
                final String extraUserImg = intent.getStringExtra(EXTRA_USER_IMAGE);
                final String extraCity = intent.getStringExtra(EXTRA_CITY);
                final String extraUserUid = intent.getStringExtra(EXTRA_USER_UID);
                Post post = new Post(extraPost,null,extraUserUid,extraCity,extraUser,extraUserImg);
                handleActionNoImage(post);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionNoImage(Post post) {
        DatabaseReference rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference postRef = rootDatabaseRef.child("post").child(post.getCity()).push();
        postRef.setValue(post);
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionImage(final Post post, byte[] image) {
        StorageReference rootRef = FirebaseStorage.getInstance().getReference();
        @SuppressLint("SimpleDateFormat")
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = post.getCreadorUID() + time;
        final StorageReference imagePostRef = rootRef.child("images/"+post.getCity()+"/"+filename);
        UploadTask uploadTask = imagePostRef.putBytes(image);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreatePostService.this, R.string.error_create_post, Toast.LENGTH_SHORT).show();
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    post.setImageURL(imagePostRef.getDownloadUrl().toString());
                    DatabaseReference rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference postRef = rootDatabaseRef.child("post").child(post.getCity()).push();
                    postRef.setValue(post);
                }
            }
        });
    }
}

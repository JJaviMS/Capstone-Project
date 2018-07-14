package com.example.evilj.citypanel.widget;

import android.Manifest;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.example.evilj.citypanel.Models.Post;
import com.example.evilj.citypanel.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class WidgetPostService extends IntentService {
    private static final String ACTION_POST = "com.example.evilj.citypanel.widget.action.POST";
    private static final int LOCATION_REQUEST = 0;


    public WidgetPostService() {
        super("WidgetPostService");
    }

    public static void startActionPost(Context context) {
        Intent intent = new Intent(context, WidgetPostService.class);
        intent.setAction(ACTION_POST);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChanner(context);
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_POST.equals(action)) {
                handleActionPost();
            }
        }
    }

    private void handleActionPost() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,getString(R.string.widget_notification_channel_id))
                    .setContentTitle(getString(R.string.widget_updating)).setContentText(getString(R.string.widget_is_updating));
            Random random = new Random();
            int id = random.nextInt();
            startForeground(id,builder.build());
        }
        String city = getCurrentCity();
        if (city == null) {
            updateWidget(null);
            return;
        }
        Query ref = FirebaseDatabase.getInstance().getReference().child("post")
                .child(city).limitToLast(1);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post = null;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    post = snapshot.getValue(Post.class);
                }
                updateWidget(post);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                PostWidget.sPost = null;
            }
        };
        ref.addListenerForSingleValueEvent(listener);//Get value
        ref.removeEventListener(listener);//Remove listener


    }

    @Nullable
    private String getCurrentCity() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location location;
            if (locationManager != null) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                        || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                        || !locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER))
                    return null;
                location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            } else return null;
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            String city;
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                city = addresses.get(0).getLocality();
            } catch (IOException e) {
                city = null;
                e.printStackTrace();
            }
            return city;
        }
    }

    private static void createNotificationChanner(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence sec = context.getString(R.string.widget);
            String desc = context.getString(R.string.widget_notification_desc);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel =
                    new NotificationChannel(context.getString(R.string.widget_notification_channel_id), sec, importance);
            notificationChannel.setDescription(desc);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }
    private void updateWidget(Post post){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(WidgetPostService.this);
        int[] appWidgId = appWidgetManager.getAppWidgetIds(new ComponentName(WidgetPostService.this, PostWidget.class));
        PostWidget.updateWidget(post, appWidgetManager, appWidgId, WidgetPostService.this);
    }
}

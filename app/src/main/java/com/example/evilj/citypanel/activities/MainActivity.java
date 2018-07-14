package com.example.evilj.citypanel.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.example.evilj.citypanel.Models.Post;
import com.example.evilj.citypanel.R;
import com.example.evilj.citypanel.adaapters.PostFirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements PostFirebaseRecyclerAdapter.RecyclerInterface {
    @BindView(R.id.recycler_post)
    RecyclerView mPostRecyclerView;
    @BindView(R.id.empty_linear_layout)
    LinearLayout mEmptyLayout;

    private static final int LOCATION_REQUEST = 0;
    private static final String RECYCLER_POS = "pos";

    private Location mLocation;
    private LocationManager mLocationManager;
    private String mCurrentCity;
    private DatabaseReference mRootRef;
    private PostFirebaseRecyclerAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    @Nullable
    private GPSReciever mGPSReciever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        //FirebaseRecyclerViewAdapter
        mCurrentCity = getCurrentCity();
        if (mCurrentCity == null) showEmptyView();
        populateRecyclerView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST: {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.ask_permission)
                            .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
                                    dialogInterface.dismiss();
                                }
                            }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                            dialogInterface.dismiss();
                        }
                    }).show();
                } else {
                    mCurrentCity = getCurrentCity();
                    populateRecyclerView();

                }
            }
        }
    }

    /**
     * Returns the name of the city where the user is into, if it´s needed the method will request for permissions
     *
     * @return Name of the city where the user is into
     */
    @Nullable
    private String getCurrentCity() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return null;
        } else {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (mLocationManager != null) {
                if (!isGpsEnabled()) return null;
                mLocation = getLastLocation();
            } else return null;
            if (mLocation==null) return null;
            double longitude = mLocation.getLongitude();
            double latitude = mLocation.getLatitude();
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

    /**
     * Creates the adapter and set it to the RecyclerView
     */
    private void populateRecyclerView() {
        if (mGPSReciever != null) {
            unregisterReceiver(mGPSReciever);
            mGPSReciever = null;
        }
        if (mCurrentCity != null) {
            SnapshotParser<Post> parser = new SnapshotParser<Post>() {
                @NonNull
                @Override
                public Post parseSnapshot(@NonNull DataSnapshot snapshot) {
                    Post post = snapshot.getValue(Post.class);
                    if (post == null) throw new RuntimeException("Post cant be null");
                    post.setId(snapshot.getKey());


                    return post;
                }
            };
            DatabaseReference reference = mRootRef.child("post").child(mCurrentCity);
            FirebaseRecyclerOptions<Post> recyclerOptions =
                    new FirebaseRecyclerOptions.Builder<Post>().setQuery(reference, parser).build();
            mAdapter = new PostFirebaseRecyclerAdapter(recyclerOptions, this, this);
            mPostRecyclerView.setAdapter(mAdapter);
            mLinearLayoutManager = new LinearLayoutManager(this);
            mPostRecyclerView.setLayoutManager(mLinearLayoutManager);
            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    showEmptyView();
                }
            });
            mPostRecyclerView.addItemDecoration(new DividerItemDecoration(this, mLinearLayoutManager.getOrientation()));
            mAdapter.startListening();
            showEmptyView();
        } else {
            mGPSReciever = new GPSReciever();
            registerReceiver(mGPSReciever, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        }
    }

    private void showEmptyView() {
        if (mAdapter == null || mAdapter.getItemCount() == 0) {
            mPostRecyclerView.setVisibility(View.GONE);
            mEmptyLayout.setVisibility(View.VISIBLE);
        } else {
            mPostRecyclerView.setVisibility(View.VISIBLE);
            mEmptyLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAdapter != null) mAdapter.startListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) mAdapter.stopListening();
    }

    @OnClick(R.id.fab_new_post)
    void launchNewPostActivity() {
        Intent intent = new Intent(this, CreatePostActivity.class);
        intent.putExtra(CreatePostActivity.EXTRA_CITY, mCurrentCity);
        startActivity(intent);
    }

    @Override
    public void dataChanged() {
        showEmptyView();
    }

    @Override
    public void onClick(Post post) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.POST_EXTRA, post);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mLinearLayoutManager != null)
            outState.putParcelable(RECYCLER_POS, mLinearLayoutManager.onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (mLinearLayoutManager != null)
            mLinearLayoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(RECYCLER_POS));
    }

    /**
     * Check if the GPS is enabled
     *
     * @return True if GPS is enabled, false otherwise
     */
    private boolean isGpsEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                || mLocationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
    }

    class GPSReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            String action = intent.getAction();
            if (action == null) return;
            if (action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                mCurrentCity = getCurrentCity();
                populateRecyclerView();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            getMenuInflater().inflate(R.menu.sign_out,menu);
            return true;
        }else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out_button:{
                FirebaseAuth.getInstance().signOut();
                invalidateOptionsMenu();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Get the last position of the user, assumes that the app already have permissions
     * @return The last position of the user
     */
    @SuppressLint("MissingPermission")//Assumes that is checked before calling
    private Location getLastLocation (){
        List<String> providers = mLocationManager.getProviders(true);
        Location loc=null;
        for (int i=0; i<providers.size() && loc==null;i++){
            loc = mLocationManager.getLastKnownLocation(providers.get(i));
        }
        return loc;
    }
}

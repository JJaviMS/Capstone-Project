package com.example.evilj.citypanel.activities;

import android.content.Intent;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Fade;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.evilj.citypanel.Models.Post;
import com.example.evilj.citypanel.R;
import com.example.evilj.citypanel.fragments.ImageFragment;
import com.example.evilj.citypanel.transition.DetailsTransition;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailActivity extends AppCompatActivity {
    public static String POST_EXTRA = "post";

    private Post mPost;
    @BindView(R.id.post_iv)
    ImageView mPostIv;
    @BindView(R.id.message_tv)
    TextView mMessageTv;
    @BindView(R.id.photo_iv)
    ImageView mPhotoIv;
    @BindView(R.id.name_tv)
    TextView mNameTv;
    @BindView(R.id.fragment_holder)
    FrameLayout mFrameLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setTitle(R.string.post);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        if (intent==null)throw new IllegalStateException("The post can´t be null");
        mPost = intent.getParcelableExtra(POST_EXTRA);

        if (mPost.getImageURL()==null){
            mPostIv.setVisibility(View.GONE);
        }else{
            mPostIv.setVisibility(View.VISIBLE);
            Glide.with(this).load(mPost.getImageURL()).apply(RequestOptions.centerCropTransform()).into(mPostIv);
        }
        mMessageTv.setText(mPost.getMessage());
        String userPhoto = mPost.getCreadorImageURL();
        if (userPhoto==null){
            Glide.with(this).load(getDrawable(R.drawable.ic_mood_black_24dp)).apply(RequestOptions.centerCropTransform()).into(mPhotoIv);
        }else {
            Glide.with(this).load(userPhoto).apply(RequestOptions.circleCropTransform()).into(mPhotoIv);
        }
        mNameTv.setText(mPost.getCreadorName());
    }
    @OnClick(R.id.post_iv)
    void fullScreenImage(){
        ImageFragment imageFragment = ImageFragment.newInstance(mPost.getImageURL());
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            imageFragment.setSharedElementEnterTransition(new DetailsTransition());
            imageFragment.setEnterTransition(new Fade());
            imageFragment.setSharedElementReturnTransition(new DetailsTransition());
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().addSharedElement(mPostIv, ViewCompat.getTransitionName(mPostIv))
                .replace(R.id.fragment_holder, imageFragment).addToBackStack(null).commit();
    }

}

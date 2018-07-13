package com.example.evilj.citypanel.Activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.evilj.citypanel.Models.Post;
import com.example.evilj.citypanel.R;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setTitle(R.string.post);
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
        Glide.with(this).load(mPost.getCreadorImageURL()).apply(RequestOptions.circleCropTransform()).into(mPhotoIv);
        mNameTv.setText(mPost.getMessage());
    }
}

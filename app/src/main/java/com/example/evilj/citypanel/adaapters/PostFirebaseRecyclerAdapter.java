package com.example.evilj.citypanel.adaapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.evilj.citypanel.Models.Post;
import com.example.evilj.citypanel.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by JjaviMS on 19/06/2018.
 *
 * @author JJaviMS
 */
public class PostFirebaseRecyclerAdapter extends FirebaseRecyclerAdapter<Post, PostFirebaseRecyclerAdapter.PostViewHolder> {
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public PostFirebaseRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Post> options) {
        super(options);
    }
    public interface RecyclerInterface{
        void dataChanged ();
    }

    private Context mContext;
    private RecyclerInterface mRecyclerInterface;

    public PostFirebaseRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Post> options, Context context,RecyclerInterface recyclerInterface) {
        super(options);
        mContext = context;
        mRecyclerInterface = recyclerInterface;
    }

    @Override
    protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull Post model) {
        if (model.getImageURL() == null) holder.mPostIv.setVisibility(View.GONE);
        else {
            holder.mPostIv.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(model.getImageURL()).apply(RequestOptions.centerCropTransform())
                    .into(holder.mPostIv);
        }
        holder.mPostAutor.setText(model.getCreadorName());
        holder.mPostMessage.setText(model.getMessage());
        Glide.with(mContext).load(model.getCreadorImageURL()).apply(RequestOptions.centerCropTransform())
                .apply(RequestOptions.circleCropTransform()).into(holder.mAutorIv);
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.post_view_holder, parent, false));
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.post_created_by_tv)
        TextView mPostAutor;
        @BindView(R.id.post_message_tv)
        TextView mPostMessage;
        @BindView(R.id.autor_image_view)
        ImageView mAutorIv;
        @BindView(R.id.post_image_view)
        ImageView mPostIv;

        PostViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        mRecyclerInterface.dataChanged();
    }
}

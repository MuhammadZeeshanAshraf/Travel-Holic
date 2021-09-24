package com.ztech.travelholic.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.ztech.travelholic.Activities.AlbumDetailActivity;
import com.ztech.travelholic.Activities.PhotoViewerActivity;
import com.ztech.travelholic.R;

import java.io.File;
import java.util.ArrayList;

public class AddAlbumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    ArrayList<String> list;
    int checker = 0;

    public AddAlbumAdapter(Context context, ArrayList<String> list, Activity activity, int checker) {
        this.context = context;
        this.list = list;
        this.checker = checker;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.item_add_album, parent, false);
        return new albumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        String model = list.get(position);
        ((albumViewHolder) holder).setImage(model, checker);
        ((albumViewHolder) holder).albumImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PhotoViewerActivity.class);
                intent.putExtra("Album", model);
                intent.putExtra("checker", checker);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class albumViewHolder extends RecyclerView.ViewHolder {

        ImageView albumImage;

        albumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumImage = itemView.findViewById(R.id.iv_ItemImage);
        }

        void setImage(String it, int checker) {
            if (checker == 0) {
                Uri uriFromPath = Uri.fromFile(new File(it));
                Picasso.get().load(uriFromPath).placeholder(R.drawable.duikar).error(R.drawable.duikar).into(albumImage);
            } else {
                Picasso.get().load(Uri.parse(it)).placeholder(R.drawable.duikar).error(R.drawable.duikar).into(albumImage);
            }
        }
    }


}


package com.ztech.travelholic.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.ztech.travelholic.Activities.AlbumDetailActivity;
import com.ztech.travelholic.Activities.LocationDetailActivity;
import com.ztech.travelholic.Model.Location;
import com.ztech.travelholic.R;

import java.util.ArrayList;

public class FeaturedAdapter extends RecyclerView.Adapter<FeaturedAdapter.FeaturedViewHolder> {

    ArrayList<Location> featuredLocations;
    Context context;

    public FeaturedAdapter(Context context, ArrayList<Location> featuredLocations) {
        this.featuredLocations = featuredLocations;
        this.context = context;
    }

    @NonNull
    @Override
    public FeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.featured_card_design, parent, false);
        FeaturedViewHolder featuredViewHolder = new FeaturedViewHolder(view);
        return featuredViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedViewHolder holder, int position) {
        Location model = featuredLocations.get(position);

        holder.title.setText(model.getTitle());
        holder.desc.setText(model.getCategory());
        holder.ratingBar.setRating(model.getRate());
        String str = model.getTitle();
        str = str.replaceAll("[^a-zA-Z0-9]", "");
        str = str.replace(" ", "");
        str = str.toLowerCase();

        if (model.getUri().isEmpty()) {
            int identifier = context.getResources().getIdentifier(str, "drawable", context.getPackageName());
            if (identifier != 0) {
                @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = context.getResources().getDrawable(context.getResources()
                        .getIdentifier(str, "drawable", context.getPackageName()));
                holder.image.setBackground(drawable);
            } else {
                holder.image.setImageResource(R.drawable.duikar);
            }
        } else {
            Picasso.get().load(model.getUri()).placeholder(R.drawable.duikar).error(R.drawable.duikar).into(holder.image);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, LocationDetailActivity.class);
                intent.putExtra("Location", model);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return featuredLocations.size();
    }

    public static class FeaturedViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title, desc;
        RatingBar ratingBar;

        public FeaturedViewHolder(@NonNull View itemView) {
            super(itemView);

            //Hooks
            image = itemView.findViewById(R.id.featured_image);
            title = itemView.findViewById(R.id.featured_title);
            desc = itemView.findViewById(R.id.featured_desc);
            ratingBar = itemView.findViewById(R.id.ratingBar);

        }
    }

}

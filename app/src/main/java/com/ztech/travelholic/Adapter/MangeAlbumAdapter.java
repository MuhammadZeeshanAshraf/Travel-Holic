package com.ztech.travelholic.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;
import com.ztech.travelholic.Activities.AlbumDetailActivity;
import com.ztech.travelholic.Activities.EditAlbumActivity;
import com.ztech.travelholic.Activities.HomeActivity;
import com.ztech.travelholic.Model.Album;
import com.ztech.travelholic.R;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class MangeAlbumAdapter extends RecyclerView.Adapter<MangeAlbumAdapter.ViewHolder> {
    Context context;
    ArrayList<Album> list;
    Activity activity;
    int checker  = 0;
    DatabaseReference rootRef;

    public MangeAlbumAdapter(Context context, ArrayList<Album> list, Activity activity , int i ) {
        this.context = context;
        this.list = list;
        this.activity = activity;
        rootRef = FirebaseDatabase.getInstance().getReference();
        this.checker = i;

    }

    @NonNull
    @Override
    public MangeAlbumAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MangeAlbumAdapter.ViewHolder holder, int position) {
        final Album model = list.get(position);

        holder.title.setText(model.getTitle());
        holder.category.setText(model.getCategory());

        Picasso.get().load(model.getUri().get(0)).placeholder(R.drawable.duikar).error(R.drawable.duikar).into(holder.image);

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AlbumDetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("Album", model);
                context.startActivity(intent);
            }
        });

        holder.sliderView.setSliderAdapter(new SliderAdapter(context, model.getUri()));
        holder.sliderView.startAutoCycle();
        holder.sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        holder.sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        holder.sliderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AlbumDetailActivity.class);
                intent.putExtra("Album", model);
                context.startActivity(intent);
            }
        });
        if (checker == 1)
        {
            holder.more.setVisibility(View.VISIBLE);
        }else
        {
            holder.more.setVisibility(View.GONE);
        }
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenu(view, model);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, category;
        ImageView image;
        SliderView sliderView;
        ImageView more;
        RelativeLayout mainLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_Title);
            category = itemView.findViewById(R.id.tv_Category);
            image = itemView.findViewById(R.id.iv_Image);
            sliderView = itemView.findViewById(R.id.imageSlider);
            more = itemView.findViewById(R.id.albumOptions);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }


    public void showMenu(View view, final Album model) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.manage_album_menu);
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.cat_details:
                        Intent intent = new Intent(context, AlbumDetailActivity.class);
                        intent.putExtra("Album", model);
                        context.startActivity(intent);
                        break;
                    case R.id.cat_edit:
                        Intent i = new Intent(context, EditAlbumActivity.class);
                        i.putExtra("Album", model);
                        context.startActivity(i);
                        activity.finish();
                        break;
                    case R.id.cat_del:
                        DeleteAlbum(model);
                        break;
                }

                return true;
            }
        });

    }

    private void DeleteAlbum(Album model) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(model.getUri().get(0));
        StorageReference storageReference2 = FirebaseStorage.getInstance().getReferenceFromUrl(model.getUri().get(1));
        StorageReference storageReference3 = FirebaseStorage.getInstance().getReferenceFromUrl(model.getUri().get(2));

        storageReference.delete();
        storageReference2.delete();
        storageReference3.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    try {
                        rootRef.child("Albums").child(model.getID()).removeValue();
                        rootRef.child("UserAlbums").child(HomeActivity.HOME_USER.getID()).child(model.getID()).removeValue();
                        rootRef.child("CategoryAlbums").child(model.getCategory()).child(model.getID()).removeValue();

                        activity.finish();
                        activity.overridePendingTransition(0, 0);
                        activity.startActivity(activity.getIntent());
                        activity.overridePendingTransition(0, 0);
                    } catch (Exception e) {

                    }
                } else {
                    Toasty.error(context, "Some Problem occur while Deleting...", Toasty.LENGTH_SHORT).show();
                }
            }
        });


    }


}


package com.ztech.travelholic.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;
import com.ztech.travelholic.R;

import java.util.ArrayList;

public class SliderAdapter extends SliderViewAdapter<SliderAdapter.ViewHolder> {

    Context context;
    ArrayList<String> uriList;

    public SliderAdapter(Context context, ArrayList<String> uri) {
        this.context = context;
        this.uriList = uri;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_item_layout, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        switch (position) {
            case 0:
                Picasso.get().load(android.net.Uri.parse(uriList.get(0))).placeholder(R.drawable.duikar).error(R.drawable.duikar).into(viewHolder.sliderImage);
                break;
            case 1:
                Picasso.get().load(android.net.Uri.parse(uriList.get(1))).placeholder(R.drawable.duikar).error(R.drawable.duikar).into(viewHolder.sliderImage);
                break;
            case 2:
                Picasso.get().load(android.net.Uri.parse(uriList.get(2))).placeholder(R.drawable.duikar).error(R.drawable.duikar).into(viewHolder.sliderImage);
                break;


        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    class ViewHolder extends SliderViewAdapter.ViewHolder {

        ImageView sliderImage;

        public ViewHolder(View itemView) {
            super(itemView);

            sliderImage = itemView.findViewById(R.id.sliderImage);
        }
    }
}

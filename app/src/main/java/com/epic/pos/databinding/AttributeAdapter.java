package com.epic.pos.databinding;

import androidx.databinding.BindingAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class AttributeAdapter {
    @BindingAdapter("android:src")
    public static void setImageUrl(ImageView view, String url){
        Glide.with(view.getContext())
                .load(url)
                .into(view);
    }
}

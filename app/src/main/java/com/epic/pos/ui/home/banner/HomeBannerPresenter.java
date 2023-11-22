package com.epic.pos.ui.home.banner;

import android.graphics.Bitmap;

import com.epic.pos.ui.BasePresenter;

import javax.inject.Inject;

public class HomeBannerPresenter extends BasePresenter<HomeBannerContract.View> implements HomeBannerContract.Presenter {

    public int bannerResId;
    public Bitmap bitmap;

    @Inject
    public HomeBannerPresenter() {
    }

}
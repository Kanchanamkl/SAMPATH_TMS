package com.epic.pos.ui.home.banner;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.databinding.FragmentHomeBannerBinding;
import com.epic.pos.ui.BaseFragment;
import com.epic.pos.util.AppLog;

public class HomeBannerFragment extends BaseFragment<HomeBannerPresenter> implements HomeBannerContract.View {

    private FragmentHomeBannerBinding binding;
    private static final String TAG = FragmentHomeBannerBinding.class.getName();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";



    public static HomeBannerFragment newInstance(int bannerResId) {
        HomeBannerFragment fragment = new HomeBannerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, bannerResId);
        fragment.setArguments(args);
        return fragment;
    }

    public static HomeBannerFragment newInstance(byte[] toByteArray) {
        HomeBannerFragment fragment = new HomeBannerFragment();
        Bundle args = new Bundle();
        args.putByteArray(ARG_PARAM2, toByteArray);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPresenter.bannerResId = getArguments().getInt(ARG_PARAM1);
            byte[] imgByte = getArguments().getByteArray(ARG_PARAM2);
            if(imgByte != null){
                mPresenter.bitmap =  BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_banner, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if( mPresenter.bitmap != null){
            binding.ivBanner.setImageBitmap( mPresenter.bitmap);
        } else {
            binding.ivBanner.setImageResource(mPresenter.bannerResId);
        }
    }
    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    public void onDestroyView() {
        //Unbind DataBinding
        if (binding != null) {
            binding.unbind();
            binding = null;
        }

        super.onDestroyView();

    }


    private void log(String msg) {
        AppLog.i(TAG, msg);
    }

}
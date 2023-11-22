package com.epic.pos.ui.home.menu;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.databinding.FragmentHomeMenuBinding;
import com.epic.pos.domain.entity.HomeMenuBean;
import com.epic.pos.ui.BaseFragment;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeMenuFragment extends BaseFragment<HomeMenuPresenter> implements HomeMenuContract.View {

    OnFragmentInteractionListener mListener;
    private FragmentHomeMenuBinding binding;
    private static final String ARG_PARAM1 = "param1";

    public static HomeMenuFragment newInstance(ArrayList<HomeMenuBean> menu) {
        HomeMenuFragment fragment = new HomeMenuFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM1, menu);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPresenter.menu = getArguments().getParcelableArrayList(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_menu, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (mPresenter.menu.size() >= 1) {
            binding.tv1.setText(mPresenter.menu.get(0).getName());
            binding.iv1.setImageResource(mPresenter.menu.get(0).getIcon());
            binding.ll1.setVisibility(View.VISIBLE);
        }

        if (mPresenter.menu.size() >= 2) {
            binding.tv2.setText(mPresenter.menu.get(1).getName());
            binding.iv2.setImageResource(mPresenter.menu.get(1).getIcon());
            binding.ll2.setVisibility(View.VISIBLE);
        }

        if (mPresenter.menu.size() >= 3) {
            binding.tv3.setText(mPresenter.menu.get(2).getName());
            binding.iv3.setImageResource(mPresenter.menu.get(2).getIcon());
            binding.ll3.setVisibility(View.VISIBLE);
        }

        if (mPresenter.menu.size() >= 4) {
            binding.tv4.setText(mPresenter.menu.get(3).getName());
            binding.iv4.setImageResource(mPresenter.menu.get(3).getIcon());
            binding.ll4.setVisibility(View.VISIBLE);
        }

        if (mPresenter.menu.size() >= 5) {
            binding.tv5.setText(mPresenter.menu.get(4).getName());
            binding.iv5.setImageResource(mPresenter.menu.get(4).getIcon());
            binding.ll5.setVisibility(View.VISIBLE);
        }

        if (mPresenter.menu.size() >= 6){
            binding.tv6.setText(mPresenter.menu.get(5).getName());
            binding.iv6.setImageResource(mPresenter.menu.get(5).getIcon());
            binding.ll6.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new ClassCastException("No Implementation");
        }
    }

    @OnClick(R.id.ll1)
    public void onClickLayout1(View view) {
        mListener.onMenuIconClicked(mPresenter.menu.get(0));
    }

    @OnClick(R.id.ll2)
    public void onClickLayout2(View view) {
        mListener.onMenuIconClicked(mPresenter.menu.get(1));
    }

    @OnClick(R.id.ll3)
    public void onClickLayout3(View view) {
        mListener.onMenuIconClicked(mPresenter.menu.get(2));
    }

    @OnClick(R.id.ll4)
    public void onClickLayout4(View view) {
        mListener.onMenuIconClicked(mPresenter.menu.get(3));
    }

    @OnClick(R.id.ll5)
    public void onClickLayout5(View view) {
        mListener.onMenuIconClicked(mPresenter.menu.get(4));
    }

    @OnClick(R.id.ll6)
    public void onClickLayout6(View view) {
        mListener.onMenuIconClicked(mPresenter.menu.get(5));
    }


    public interface OnFragmentInteractionListener {
        void onMenuIconClicked(HomeMenuBean menuItem);
    }

    @Override
    public void onDestroyView() {
        if(binding!=null){
        binding.unbind();
        binding=null;}
        super.onDestroyView();
    }
}
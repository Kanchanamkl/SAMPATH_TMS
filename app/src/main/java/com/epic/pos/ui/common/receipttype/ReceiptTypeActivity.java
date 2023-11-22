package com.epic.pos.ui.common.receipttype;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.databinding.ActivityReceiptTypeBinding;
import com.epic.pos.ui.BaseActivity;

import butterknife.ButterKnife;

public class ReceiptTypeActivity extends BaseActivity<ReceiptTypePresenter> implements ReceiptTypeContract.View, View.OnClickListener {

    ActivityReceiptTypeBinding binding;

    public static void startActivity(BaseActivity activity) {
        activity.startActivity(new Intent(activity, ReceiptTypeActivity.class));

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  setContentView(R.layout.activity_receipt_type);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_receipt_type);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, "", true);
        mPresenter.initData();
        initViews();
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }


    @Override
    public void onClick(View view) {

            Log.d("FFFF","CCCCC");
            switch (view.getId()) {
                case R.id.btnPrintCus: {
                    mPresenter.customercopyclicked();
                    break;
                }
                case R.id.btnPrintMer: {
                    mPresenter.merchantcopyclicked();
                    break;
                }
            }

    }
    private void initViews() {
        $(R.id.btnPrintCus).setOnClickListener(this);
        $(R.id.btnPrintMer).setOnClickListener(this);
    }
    protected <T extends View> T $(@IdRes int id) {
        return (T) super.findViewById(id);
    }
}
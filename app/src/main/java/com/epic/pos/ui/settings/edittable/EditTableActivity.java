package com.epic.pos.ui.settings.edittable;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;

import com.epic.pos.R;
import com.epic.pos.adapter.TableAttributeAdapter;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.databinding.ActivityEditTableBinding;
import com.epic.pos.domain.entity.ColumnInfoEntity;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.util.UiUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindArray;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class EditTableActivity extends BaseActivity<EditTablePresenter> implements EditTableContract.View, TableAttributeAdapter.Listener {

    private final String TAG = EditTableActivity.class.getSimpleName();
    private ActivityEditTableBinding binding;

    @BindArray(R.array.table_names)
    String[] tables;

    public static void startActivity(BaseActivity activity) {
        activity.startActivity(new Intent(activity, EditTableActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_table);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, getString(R.string.toolbar_edittable), true);
    }

    @Override
    public void setActionsEnabled(boolean isEnabled) {
        binding.spinnerTables.setEnabled(isEnabled);
        binding.ivBack.setEnabled(isEnabled);
        binding.tvGoTo.setEnabled(isEnabled);
        binding.etRowNo.setEnabled(isEnabled);
        binding.ivNext.setEnabled(isEnabled);
        binding.btnSave.setEnabled(isEnabled);
    }

    @Override
    public void onRowValueChanged(ColumnInfoEntity entity) {
        mPresenter.onColumnInfoChanged(entity);
    }

    @Override
    public void onColumnsUpdated() {
        TableAttributeAdapter mAdapter = new TableAttributeAdapter(mPresenter.getColumns());
        mAdapter.setListener(this);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);
        binding.recyclerView.setHasFixedSize(true);
        mAdapter.notifyDataSetChanged();
        UiUtil.hideKeyboard(this);
    }

    @Override
    public void onRowIndexUpdated(int rowIndex) {
        binding.etRowNo.setText(String.valueOf(rowIndex));
    }

    @OnItemSelected(R.id.spinnerTables)
    void onItemSelected(int position) {
        mPresenter.onTableSelected(tables[position]);
    }

    @OnClick(R.id.root)
    void onRootClicked(){
        UiUtil.hideKeyboard(this);
    }

    @OnClick(R.id.ivBack)
    void onBackClicked(){
        mPresenter.onBackClicked();
    }

    @OnClick(R.id.ivNext)
    void onNextClicked(){
        mPresenter.onNextClicked();
    }

    @OnClick(R.id.tvGoTo)
    void onGoToClicked(){
        mPresenter.gotoRecord(binding.etRowNo.getText().toString());
    }

    @OnClick(R.id.btnSave)
    void onSaveClicked(){
        mPresenter.onSaveClicked();
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }




}
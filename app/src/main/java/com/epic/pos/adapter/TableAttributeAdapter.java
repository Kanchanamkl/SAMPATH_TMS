package com.epic.pos.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.epic.pos.R;
import com.epic.pos.databinding.RowItemTableAttributeBinding;
import com.epic.pos.domain.entity.ColumnInfoEntity;

import java.util.List;

import butterknife.ButterKnife;

public class TableAttributeAdapter extends RecyclerView.Adapter<TableAttributeAdapter.MyViewHolder> {

    private List<ColumnInfoEntity> entities;
    private Listener listener = null;

    public TableAttributeAdapter(List<ColumnInfoEntity> entities) {
        this.entities = entities;
    }

    public void updateData(List<ColumnInfoEntity> entities) {
        this.entities = entities;
        notifyDataSetChanged();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowItemTableAttributeBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(parent.getContext()), R.layout.row_item_table_attribute, parent, false);
        MyViewHolder holder = new MyViewHolder(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        ColumnInfoEntity c = entities.get(position);
        holder.setData(c);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return entities.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private RowItemTableAttributeBinding binding;

        public MyViewHolder(RowItemTableAttributeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            ButterKnife.bind(this, binding.getRoot());
        }

        void setData(ColumnInfoEntity c){
            boolean isEnable = !c.isPrimaryKey()
                    && !(c.getTable().equals("FEATURE") && c.getColumnName().equals("name"));
            binding.tvTitle.setText(c.getColumnName());
            binding.et.setEnabled(isEnable);
            binding.et.setText(c.getValue());
            binding.et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    c.setValue(s.toString());
                    if (listener != null && binding.et.hasFocus()){
                        listener.onRowValueChanged(c);
                    }
                }
            });
        }

    }

    public interface Listener{
        void onRowValueChanged(ColumnInfoEntity entity);
    }

}
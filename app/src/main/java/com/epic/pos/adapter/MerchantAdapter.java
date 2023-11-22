package com.epic.pos.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.epic.pos.R;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.databinding.RowItemMerchantBinding;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MerchantAdapter extends RecyclerView.Adapter<MerchantAdapter.ViewHolder> {

    private List<Merchant> merchants;
    private OnClickListener onClickListener;

    public MerchantAdapter(List<Merchant> merchants, OnClickListener onClickListener) {
        this.merchants = merchants;
        this.onClickListener = onClickListener;
    }

    public void updateData(List<Merchant> merchants) {
        this.merchants = merchants;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowItemMerchantBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(parent.getContext()), R.layout.row_item_merchant, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Merchant t = merchants.get(position);
        holder.binding.tvMerchantName.setText(t.getMerchantName());
    }

    @Override
    public int getItemCount() {
        return merchants.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private RowItemMerchantBinding binding;

        public ViewHolder(RowItemMerchantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            ButterKnife.bind(this, binding.getRoot());
        }

        @OnClick(R.id.terminal)
        public void submit() {
            final int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                onClickListener.onTerminalClicked(merchants.get(position));
            }
        }
    }

    public interface OnClickListener {
        void onTerminalClicked(Merchant merchant);
    }
}
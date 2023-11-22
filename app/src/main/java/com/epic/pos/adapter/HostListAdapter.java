package com.epic.pos.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.epic.pos.R;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.databinding.RowItemCommonBinding;
import com.epic.pos.databinding.RowItemCommonSelectedBinding;
import com.epic.pos.domain.entity.CommonListEntity;
import com.epic.pos.domain.entity.HostEntity;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class HostListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  implements Filterable {

    private List<Host> hostList, filteredList;
    private HistoryFilter mFilter;
    private OnClickListener onClickListener;
    public static final int VIEW_TYPE_COMMON = 0;
    public static final int VIEW_TYPE_COMMON_SELECTED = 1;

    public HostListAdapter(List<Host> hostList, OnClickListener onClickListener) {
        this.hostList = hostList;
        this.filteredList = hostList;
        this.onClickListener = onClickListener;
    }

    public void updateData(List<Host> newTransactionList) {
        this.hostList = newTransactionList;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_COMMON: {
                RowItemCommonBinding binding = DataBindingUtil.inflate(LayoutInflater
                        .from(parent.getContext()), R.layout.row_item_common, parent, false);
                return new CommonRowViewHolder(binding);
            }
            case VIEW_TYPE_COMMON_SELECTED: {
                RowItemCommonSelectedBinding binding = DataBindingUtil.inflate(LayoutInflater
                        .from(parent.getContext()), R.layout.row_item_common_selected, parent, false);
                return new CommonRowSelectedViewHolder(binding);
            }
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Host t = filteredList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_COMMON) {
            CommonRowViewHolder _holder = ((CommonRowViewHolder) holder);
            if (t.getHostName() != null)
                _holder.binding.tvText.setText(t.getHostName());
        } else {
            CommonRowSelectedViewHolder _holder = ((CommonRowSelectedViewHolder) holder);
            if (t.getHostName() != null)
                _holder.binding.tvText.setText(t.getHostName());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (hostList.get(position).isSelected()) {
            return VIEW_TYPE_COMMON_SELECTED;
        } else {
            return VIEW_TYPE_COMMON;
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new HistoryFilter();
        }
        return mFilter;
    }


    private class HistoryFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            List<Host> filteredResults = new ArrayList<>();
            if (!charSequence.toString().isEmpty()) {
                for (Host user : hostList) {
                    if (user.getHostName().equalsIgnoreCase(charSequence.toString())) {
                        filteredResults.add(user);
                    }
                }
            } else {
                filteredResults = hostList;
            }

            results.values = filteredResults;
            results.count = filteredResults.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            filteredList = (List<Host>) filterResults.values;
            notifyDataSetChanged();
        }
    }


    public class CommonRowViewHolder extends RecyclerView.ViewHolder {

        private RowItemCommonBinding binding;

        public CommonRowViewHolder(RowItemCommonBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            ButterKnife.bind(this, binding.getRoot());
        }

        @OnClick(R.id.common)
        public void click() {
            final int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                onClickListener.onListItemClicked(hostList.get(position));
            }
        }
    }

    public class CommonRowSelectedViewHolder extends RecyclerView.ViewHolder {

        private RowItemCommonSelectedBinding binding;

        public CommonRowSelectedViewHolder(RowItemCommonSelectedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            ButterKnife.bind(this, binding.getRoot());
        }

        @OnClick(R.id.common)
        public void click() {
            final int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                onClickListener.onListItemClicked(hostList.get(position));
            }
        }

    }

    public interface OnClickListener {
        void onListItemClicked(Host item);
    }
}
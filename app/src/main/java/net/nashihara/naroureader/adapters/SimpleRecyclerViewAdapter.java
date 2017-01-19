package net.nashihara.naroureader.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.ItemSimpleRecyclerBinding;
import net.nashihara.naroureader.listeners.OnItemClickListener;
import net.nashihara.naroureader.models.entities.Novel4Realm;

import java.util.ArrayList;
import java.util.List;

public class SimpleRecyclerViewAdapter extends RecyclerView.Adapter<SimpleRecyclerViewAdapter.BindingHolder> {

    private LayoutInflater inflater;

    private ArrayList<Novel4Realm> arrayList;

    private OnItemClickListener listener;

    private RecyclerView recyclerView;

    public SimpleRecyclerViewAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        arrayList = new ArrayList<>();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        recyclerView = null;
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = inflater.inflate(R.layout.item_simple_recycler, parent, false);
        return new BindingHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        if (arrayList != null && arrayList.size() > position && arrayList.get(position) != null) {
            ItemSimpleRecyclerBinding binding = holder.getBinding();

            binding.title.setText(arrayList.get(position).getTitle());
        }
    }

    @Override
    public int getItemCount() {
        if (arrayList == null) {
            return 0;
        }
        return arrayList.size();
    }

    public void addDataOf(List<Novel4Realm> dataList) {
        int beforePos = arrayList.size();
        arrayList.addAll(dataList);
        notifyItemRangeInserted(beforePos, dataList.size());
    }

    public void clearData() {
        arrayList.clear();
    }

    public ArrayList<Novel4Realm> getList() {
        return this.arrayList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    static class BindingHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ItemSimpleRecyclerBinding binding;
        private OnItemClickListener mListener;

        public BindingHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            this.mListener = listener;
            binding = DataBindingUtil.bind(itemView);

            binding.itemContainer.setOnClickListener(this);
        }

        public ItemSimpleRecyclerBinding getBinding(){
            return this.binding;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getLayoutPosition());
            }
        }
    }
}

package net.nashihara.naroureader.views.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.ItemSimpleRecyclerBinding;
import net.nashihara.naroureader.models.entities.Novel4Realm;
import net.nashihara.naroureader.models.entities.NovelItem;
import net.nashihara.naroureader.utils.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class SimpleRecyclerViewAdapter extends RecyclerView.Adapter<SimpleRecyclerViewAdapter.BindingHolder> {
    private static final String TAG = SimpleRecyclerViewAdapter.class.getSimpleName();

    private LayoutInflater mInflater;
    private ArrayList<Novel4Realm> mArrayList;
    private OnItemClickListener mListener;
    private RecyclerView mRecyclerView;
    private Context mContext;

    public SimpleRecyclerViewAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        mArrayList = new ArrayList<>();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = mInflater.inflate(R.layout.item_simple_recycler, parent, false);
        return new BindingHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        if (mArrayList != null && mArrayList.size() > position && mArrayList.get(position) != null) {
            ItemSimpleRecyclerBinding binding = holder.getBinding();

            binding.title.setText(mArrayList.get(position).getTitle());
        }
    }

    @Override
    public int getItemCount() {
        if (mArrayList == null) {
            return 0;
        }
        return mArrayList.size();
    }

    public void addDataOf(List<Novel4Realm> dataList) {
        mArrayList.addAll(dataList);
    }

    public void removeDataOf(List<NovelItem> dataList) {
        for (NovelItem item : dataList) {
            mArrayList.remove(item);
        }
    }

    public void clearData() {
        mArrayList.clear();
    }

    public ArrayList<Novel4Realm> getList() {
        return this.mArrayList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
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

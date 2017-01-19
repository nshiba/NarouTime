package net.nashihara.naroureader.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.ItemTableRecyclerBinding;

import java.util.ArrayList;
import java.util.List;

import narou4j.entities.NovelBody;

public class NovelTableRecyclerViewAdapter extends RecyclerView.Adapter<NovelTableRecyclerViewAdapter.BindingHolder> {
    private static final String TAG = NovelTableRecyclerViewAdapter.class.getSimpleName();

    private LayoutInflater inflater;

    private ArrayList<NovelBody> arrayList;

    private OnItemClickListener listener;

    private RecyclerView recyclerView;

    public NovelTableRecyclerViewAdapter(Context context) {
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
        this.recyclerView = null;
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = inflater.inflate(R.layout.item_table_recycler, parent, false);
        return new BindingHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        if (arrayList != null && arrayList.size() > position && arrayList.get(position) != null) {
            ItemTableRecyclerBinding binding = holder.getBinding();

            NovelBody body = arrayList.get(position);
            if (body.isChapter()) {
                binding.chapter.setText(body.getTitle());
                binding.chapter.setVisibility(View.VISIBLE);
                binding.pageTitle.setVisibility(View.GONE);
            }
            else {
                binding.pageTitle.setText(body.getTitle());
                binding.pageTitle.setVisibility(View.VISIBLE);
                binding.chapter.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (arrayList == null) {
            return 0;
        }
        return arrayList.size();
    }

    public void addDataOf(List<NovelBody> dataList) {
        int startPos = dataList.size();
        arrayList.addAll(dataList);
        notifyItemRangeInserted(startPos, dataList.size());
    }

    public void clearData() {
        arrayList.clear();
    }

    public ArrayList<NovelBody> getList() {
        return this.arrayList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, ItemTableRecyclerBinding binding);
    }

    static class BindingHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ItemTableRecyclerBinding binding;
        private OnItemClickListener mListener;

        public BindingHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            this.mListener = listener;
            binding = DataBindingUtil.bind(itemView);

            binding.pageTitle.setOnClickListener(this);
        }

        public ItemTableRecyclerBinding getBinding(){
            return this.binding;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getLayoutPosition(), binding);
            }
        }
    }
}

package net.nashihara.naroureader.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.entities.NovelItem;

import java.util.List;

public class RankingRecycerViewAdapter extends RecyclerView.Adapter<RankingRecycerViewAdapter.ViewHolder> implements View.OnClickListener {
    private static final String TAG = RankingRecycerViewAdapter.class.getSimpleName();

    private LayoutInflater mInflater;
    private RecyclerView mRecycerView;
    private OnItemClickListener mListener;
    private SortedList<NovelItem> mSortedList;

    public RankingRecycerViewAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        mSortedList = new SortedList<>(NovelItem.class, new SortedListCallback(this));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecycerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecycerView = null;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = mInflater.inflate(R.layout.list_item, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: ");
        if (mSortedList != null && mSortedList.size() > position && mSortedList.get(position) != null) {
            NovelItem novelItem = mSortedList.get(position);
            holder.title.setText(novelItem.getNovelDetail().getTitle());
            holder.rankingPoint.setText(String.valueOf(novelItem.getRankingPoint()));
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        if (mSortedList == null) {
            return 0;
        }
        return mSortedList.size();
    }

    public void addDataOf(List<NovelItem> dataList) {
        mSortedList.addAll(dataList);
    }

    public void removeDataOf(List<NovelItem> dataList) {
        mSortedList.beginBatchedUpdates();
        for (NovelItem item : dataList) {
            mSortedList.remove(item);
        }
        mSortedList.endBatchedUpdates();
    }

    public void clearData() {
        mSortedList.clear();
    }

    public SortedList<NovelItem> getList() {
        return mSortedList;
    }

    @Override
    public void onClick(View v) {
        if (mRecycerView == null || mListener == null) {
            return;
        }

        int position = mRecycerView.getChildAdapterPosition(v);
        NovelItem item = mSortedList.get(position);
        mListener.onItemClick(this, position, item);
    }

    public static interface OnItemClickListener {
        public void onItemClick(RankingRecycerViewAdapter adapter, int position, NovelItem item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView rankingPoint;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            rankingPoint = (TextView) itemView.findViewById(R.id.ranking_point);
        }
    }


    private static class SortedListCallback extends SortedList.Callback<NovelItem> {
        private RankingRecycerViewAdapter adapter;

        public SortedListCallback(@Nullable RankingRecycerViewAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public int compare(NovelItem o1, NovelItem o2) {
            if (o2.getRankingPoint() < o1.getRankingPoint()) {
                return -1;
            }
            if (o1.getRankingPoint() == o2.getRankingPoint()) {
                return 0;
            }
            return 1;
        }

        @Override
        public void onInserted(int position, int count) {
            adapter.notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            adapter.notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            adapter.notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count) {
            adapter.notifyItemChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(NovelItem oldItem, NovelItem newItem) {
            return oldItem.getRankingPoint() == newItem.getRankingPoint();
        }

        @Override
        public boolean areItemsTheSame(NovelItem item1, NovelItem item2) {
            if (item1.getNovelDetail() == null) {
                return item2.getNovelDetail() == null;
            }
            return item1.getNovelDetail().getNcode() == item2.getNovelDetail().getNcode();
        }
    }
}

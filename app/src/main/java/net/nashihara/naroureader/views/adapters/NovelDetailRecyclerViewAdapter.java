package net.nashihara.naroureader.views.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.ItemRankingRecyclerBinding;
import net.nashihara.naroureader.models.entities.NovelItem;

import java.util.List;

import narou4j.entities.Novel;
import narou4j.entities.NovelRank;

public class NovelDetailRecyclerViewAdapter extends RecyclerView.Adapter<NovelDetailRecyclerViewAdapter.BindingHolder> {
    private static final String TAG = NovelDetailRecyclerViewAdapter.class.getSimpleName();

    private LayoutInflater mInflater;
    private SortedList<NovelItem> mSortedList;
    private OnItemClickListener mListener;
    private RecyclerView mRecyclerView;
    private boolean isSearch;

    public NovelDetailRecyclerViewAdapter(Context context, boolean isSearch) {
        this.mInflater = LayoutInflater.from(context);
        this.isSearch = isSearch;
        mSortedList = new SortedList<>(NovelItem.class, new SortedListCallback(this, isSearch));
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
        final View v = mInflater.inflate(R.layout.item_ranking_recycler, parent, false);
        return new BindingHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        if (mSortedList != null && mSortedList.size() > position && mSortedList.get(position) != null) {
            ItemRankingRecyclerBinding binding = holder.getBinding();

            NovelItem novelItem = mSortedList.get(position);
            Novel novel = novelItem.getNovelDetail();

            if (isSearch) {
                setView4Search(binding, novel);
            }
            else {
                NovelRank rank = novelItem.getRank();
                NovelRank prevRank = novelItem.getPrevRank();
                setView4Ranking(binding, novel, rank, prevRank, position);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mSortedList == null) {
            return 0;
        }
        return mSortedList.size();
    }

    private void setView4Search(ItemRankingRecyclerBinding binding, Novel novel) {
        binding.ranking.setVisibility(View.GONE);
        binding.rankNew.setVisibility(View.GONE);
        binding.rankDiffKigou.setVisibility(View.GONE);
        binding.prevRankText.setVisibility(View.GONE);
        binding.rankNew.setVisibility(View.GONE);
        binding.rankingPoint.setVisibility(View.GONE);

        binding.title.setText(novel.getTitle());
        binding.ncode.setText(novel.getNcode());
        binding.writer.setText(novel.getWriter());
        binding.genre.setText(novel.getGenre().getText());
        binding.allStory.setText(novel.getStory());
        binding.allStory.setVisibility(View.GONE);
        binding.keyword.setText("キーワード：" + novel.getKeyword());
        binding.keyword.setVisibility(View.GONE);
        binding.btnExpand.setAlpha(0.7f);

        if (novel.getIsNovelContinue() == 1) {
            Resources res = mRecyclerView.getResources();
            binding.isContinue.setTextColor(res.getColor(android.support.v7.appcompat.R.color.secondary_text_default_material_light));
            binding.isContinue.setText("連載中");
        } else {
            Resources res = mRecyclerView.getResources();
            binding.isContinue.setTextColor(res.getColor(R.color.colorAccent));
            binding.isContinue.setText("完結済");
        }
        binding.page.setText("全" + String.valueOf(novel.getAllNumberOfNovel()) + "部分");
        binding.length.setText(int2String(novel.getNumberOfChar()) + " 文字");
    }

    private void setView4Ranking(ItemRankingRecyclerBinding binding, Novel novel, NovelRank rank, NovelRank prevRank, int position) {
        if (rank.getRankingType() == null) {
            binding.ranking.setText(String.valueOf(position +1) + "位");
        }
        else {
            binding.ranking.setText(rank.getRank() + "位");
            if (prevRank != null) {
                binding.rankNew.setVisibility(View.GONE);
                binding.rankDiffKigou.setVisibility(View.VISIBLE);
                switch (rank.getRankingType()) {
                    case DAILY: {
                        binding.prevRankText.setText("前日：" + String.valueOf(prevRank.getRank()) + "位");
                        break;
                    }
                    case WEEKLY: {
                        binding.prevRankText.setText("前週：" + String.valueOf(prevRank.getRank()) + "位");
                        break;
                    }
                    case MONTHLY: {
                        binding.prevRankText.setText("前月：" + String.valueOf(prevRank.getRank()) + "位");
                        break;
                    }
                    case QUARTET: {
                        binding.prevRankText.setText("前月：" + String.valueOf(prevRank.getRank()) + "位");
                        break;
                    }
                }
                if (rank.getRank() < prevRank.getRank()) {
//                    binding.prevRankText.setText("前回のランキングから" + String.valueOf(diff) + "位上昇しました！");
                    binding.rankDiffKigou.setImageResource(R.drawable.ic_up);
                }
                else if (rank.getRank() > prevRank.getRank()) {
//                    binding.prevRankText.setText("前回のランキングから" + String.valueOf(diff) + "位下降しました...");
                    binding.rankDiffKigou.setImageResource(R.drawable.ic_down);
                }
                else if (rank.getRank() == prevRank.getRank()) {
//                    binding.prevRankText.setText("前回のランキングと同じだよ");
                    binding.rankDiffKigou.setImageResource(R.drawable.ic_sonomama);
                }
            }
            else {
                binding.prevRankText.setText("前回：ー");
                binding.rankDiffKigou.setVisibility(View.GONE);
                binding.rankNew.setVisibility(View.VISIBLE);
                binding.rankNew.setTextColor(Color.RED);
            }
        }

        binding.title.setText(novel.getTitle());
        binding.rankingPoint.setText(int2String(rank.getPt()) + "pt");
        binding.ncode.setText(novel.getNcode());
        binding.writer.setText(novel.getWriter());
        binding.genre.setText(novel.getGenre().getText());
        binding.allStory.setText(novel.getStory());
        binding.allStory.setVisibility(View.GONE);
        binding.keyword.setText("キーワード：" + novel.getKeyword());
        binding.keyword.setVisibility(View.GONE);
        binding.btnExpand.setAlpha(0.7f);

        if (novel.getIsNovelContinue() == 1) {
            Resources res = mRecyclerView.getResources();
            binding.isContinue.setTextColor(res.getColor(android.support.v7.appcompat.R.color.secondary_text_default_material_light));
            binding.isContinue.setText("連載中");
        } else {
            Resources res = mRecyclerView.getResources();
            binding.isContinue.setTextColor(res.getColor(R.color.colorAccent));
            binding.isContinue.setText("完結済");
        }
        binding.page.setText("全" + String.valueOf(novel.getAllNumberOfNovel()) + "部分");
        binding.length.setText(int2String(novel.getNumberOfChar()) + " 文字");
    }

    private String int2String(int number) {
        StringBuilder builder = new StringBuilder();
        String pageString = String.valueOf(number);
        builder.append(pageString);
        int c = 0;
        for (int i = pageString.length(); i > 0; i--) {
            if (c % 3 == 0 && c != 0) {
                builder.insert(i, ",");
            }
            c++;
        }
        return builder.toString();
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
        return this.mSortedList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, ItemRankingRecyclerBinding binding);
        void onItemLongClick(View view, int position, ItemRankingRecyclerBinding binding);
    }

    static class BindingHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final ItemRankingRecyclerBinding binding;
        private OnItemClickListener mListener;

        public BindingHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            this.mListener = listener;
            binding = DataBindingUtil.bind(itemView);

//            binding.btnStory.setOnClickListener(this);
            binding.btnExpand.setOnClickListener(this);
            binding.getRoot().setOnClickListener(this);
            binding.getRoot().setOnLongClickListener(this);
        }

        public ItemRankingRecyclerBinding getBinding(){
            return this.binding;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getLayoutPosition(), binding);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mListener == null) {
                return false;
            }

            mListener.onItemLongClick(v, getLayoutPosition(), binding);
            return true;
        }
    }

    private static class SortedListCallback extends SortedList.Callback<NovelItem> {
        private NovelDetailRecyclerViewAdapter adapter;
        private boolean isSearch;

        public SortedListCallback(@Nullable NovelDetailRecyclerViewAdapter adapter, @Nullable boolean isSearch) {
            this.adapter = adapter;
            this.isSearch = isSearch;
        }

        @Override
        public int compare(NovelItem o1, NovelItem o2) {
            if (o2.getRank().getRank() > o1.getRank().getRank()) {
                return -1;
            }
            if (o1.getRank().getRank() == o2.getRank().getRank()) {
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
            return oldItem.getRank().getRank() == newItem.getRank().getRank();
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

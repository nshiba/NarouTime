package net.nashihara.naroureader.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.ListItemBinding;
import net.nashihara.naroureader.entities.NovelItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import narou4j.entities.Novel;
import narou4j.entities.NovelRank;
import narou4j.enums.NovelGenre;

public class RankingRecycerViewAdapter extends RecyclerView.Adapter<RankingRecycerViewAdapter.BindingHolder> {
    private static final String TAG = RankingRecycerViewAdapter.class.getSimpleName();

    private LayoutInflater mInflater;
    private RecyclerView mRecycerView;
    private SortedList<NovelItem> mSortedList;
    private View.OnClickListener readClickListener;

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
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = mInflater.inflate(R.layout.list_item, parent, false);
        return new BindingHolder(v);
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        if (mSortedList != null && mSortedList.size() > position && mSortedList.get(position) != null) {
            ListItemBinding binding = holder.getBinding();

            NovelItem novelItem = mSortedList.get(position);
            Novel novel = novelItem.getNovelDetail();
            NovelRank rank = novelItem.getRank();
            NovelRank prevRank = novelItem.getPrevRank();

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
            binding.rankingPoint.setText(int2String(novelItem.getRank().getPt()) + "pt");
            binding.writer.setText(novel.getWriter());
            binding.genre.setText(int2Genre(novel.getGenre()));
            binding.allStory.setText(novel.getStory());
            binding.allStory.setVisibility(View.GONE);
            binding.keyword.setText("キーワード：" + novel.getKeyword());
            binding.keyword.setVisibility(View.GONE);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat format2 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String dateString = "";
            try {
                Date date = format.parse(novel.getLastUploadDate());
                dateString = format2.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (dateString.equals("")) {
                binding.lastup.setText(novel.getLastUploadDate());
            } else {
                binding.lastup.setText(dateString);
            }

            if (novel.getIsNovelContinue() == 1) {
                binding.isContinue.setText("連載中");
            } else {
                binding.isContinue.setText("完結済");
            }
            binding.page.setText("全" + String.valueOf(novel.getAllNumberOfNovel()) + "部分");
            binding.length.setText(int2String(novel.getNumberOfChar()) + " 文字");
        }
    }

    @Override
    public int getItemCount() {
        if (mSortedList == null) {
            return 0;
        }
        return mSortedList.size();
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
        return mSortedList;
    }

    private String int2Genre(NovelGenre target) {
        switch (target) {
            case LITERATURE: {
                return "文学";
            }
            case LOVE: {
                return "恋愛";
            }
            case HISTORY: {
                return "歴史";
            }
            case DETECTIVE: {
                return "推理";
            }
            case FANTASY: {
                return "ファンタジー";
            }
            case SF: {
                return "SF";
            }
            case HORROR: {
                return "ホラー";
            }
            case COMEDY: {
                return "コメディー";
            }
            case ADVENTURE: {
                return "冒険";
            }
            case ACADEMY: {
                return "学園";
            }
            case MILITARY_HISTORY: {
                return "戦記";
            }
            case FAIRYTALE: {
                return "童話";
            }
            case POEM: {
                return "詩";
            }
            case ESSAY: {
                return "エッセイ";
            }
            case REPLAY: {
                return "リプレイ";
            }
            case OTHER: {
                return "その他";
            }
            default: {
                return "no such a genre";
            }
        }
    }

    public static class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
        private OnItemClickListener mListener;

        public interface OnItemClickListener {
            /**
             * Fires when recycler view receives a single tap event on any item
             *
             * @param view     tapped view
             * @param position item position in the list
             */
            public void onItemClick(View view, int position);

            /**
             * Fires when recycler view receives a long tap event on item
             *
             * @param view     long tapped view
             * @param position item position in the list
             */
            public void onItemLongClick(View view, int position);
        }

        GestureDetector mGestureDetector;
        ExtendedGestureListener mGestureListener;

        public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
            mListener = listener;
            mGestureListener = new ExtendedGestureListener();
            mGestureDetector = new GestureDetector(context, mGestureListener);
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null) {
                mGestureListener.setHelpers(childView, view.getChildPosition(childView));
                mGestureDetector.onTouchEvent(e);
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }

        /**
         * Extended Gesture listener react for both item clicks and item long clicks
         */
        private class ExtendedGestureListener extends GestureDetector.SimpleOnGestureListener {
            private View view;
            private int position;

            public void setHelpers(View view, int position) {
                this.view = view;
                this.position = position;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
//                view.setBackgroundColor(Color.WHITE);
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
//                int color = 235;
//                view.setBackgroundColor(Color.argb(255, color, color, color));
                mListener.onItemClick(view, position);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
//                int color = 235;
//                view.setBackgroundColor(Color.argb(255, color, color, color));
                mListener.onItemLongClick(view, position);
            }
        }
    }

    static class BindingHolder extends RecyclerView.ViewHolder {
        private final ListItemBinding binding;

        public BindingHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public ListItemBinding getBinding(){
            return this.binding;
        }
    }


    private static class SortedListCallback extends SortedList.Callback<NovelItem> {
        private RankingRecycerViewAdapter adapter;

        public SortedListCallback(@Nullable RankingRecycerViewAdapter adapter) {
            this.adapter = adapter;
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

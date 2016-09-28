package com.hotbitmapgg.rxzhihu.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hotbitmapgg.rxzhihu.R;
import com.hotbitmapgg.rxzhihu.db.DailyDao;
import com.hotbitmapgg.rxzhihu.model.DailyBean;
import com.hotbitmapgg.rxzhihu.ui.activity.DailyDetailActivity;
import com.hotbitmapgg.rxzhihu.utils.DateUtil;
import com.hotbitmapgg.rxzhihu.utils.LogUtil;
import com.hotbitmapgg.rxzhihu.utils.WeekUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by hcc on 16/4/2.
 */
public class DailyListAdapter extends RecyclerView.Adapter<DailyListAdapter.ItemContentViewHolder> {
    //不带上时间的item
    private static final int ITEM_CONTENT = 0;
    //带时间的item
    private static final int ITEM_TIME = 1;

    private List<DailyBean> dailys = new ArrayList<>();

    private DailyDao mDailyDao;

    private Context mContext;


    /**
     *
     * @param context
     *   Context
     * @param dailys
     *   List<DailyBean>
     */
    public DailyListAdapter(Context context, List<DailyBean> dailys) {

        this.dailys = dailys;
        this.mContext = context;
        this.mDailyDao = new DailyDao(context);
    }

    @Override
    public int getItemViewType(int position) {

        //第一个item的类型带时间
        if (position == 0) {
            return ITEM_TIME;
        }
        String time = dailys.get(position).getDate();
        int index = position - 1;
        boolean isDifferent = !dailys.get(index).getDate().equals(time);
        int pos = isDifferent ? ITEM_TIME : ITEM_CONTENT;
        //这里返回的类型，在下面onCreateViewHolder里viewType
        return pos;
    }

    @Override
    public ItemContentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == ITEM_TIME) {
            //带日期的item的viewholder,给viewholder传入布局
            return new ItemTimeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_list_time, parent, false));
        } else {
            //不带日期的item的viewholder
            return new ItemContentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_list, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(ItemContentViewHolder holder, int position) {

        DailyBean dailyBean = dailys.get(position);
        if (dailyBean == null) {
            return;
        }

        if (holder instanceof ItemTimeViewHolder) {
            setDailyDate(holder, dailyBean);
            ItemTimeViewHolder itemTimeViewHolder = (ItemTimeViewHolder) holder;
            String timeStr = "";
            if (position == 0) {
                //第一个position的显示
                timeStr = "今日热闻";
            } else {
                //后面的显示日期
                timeStr = DateUtil.formatDate(dailyBean.getDate()) + "  " + WeekUtil.getWeek(dailyBean.getDate());
            }
            itemTimeViewHolder.mTime.setText(timeStr);
        } else {
            setDailyDate(holder, dailyBean);
        }
    }


    /**
     * 设置数据给普通内容Item
     *
     * @param holder
     *   ItemContentViewHolder
     * @param dailyBean
     *   DailyBean
     */
    private void setDailyDate(final ItemContentViewHolder holder, final DailyBean dailyBean) {

        holder.mTitle.setText(dailyBean.getTitle());//设置标题

        List<String> images = dailyBean.getImages();
        if (images != null && images.size() > 0) {
            //context,图片url，默认图片，图片放在那里
            Glide.with(mContext).load(images.get(0)).placeholder(R.drawable.account_avatar).into(holder.mPic);
        }
        boolean multipic = dailyBean.isMultipic();

        if (multipic) {
            //多图图片显示
            holder.mMorePic.setVisibility(View.VISIBLE);
        } else {
            //隐藏
            holder.mMorePic.setVisibility(View.GONE);
        }

        //新闻是否被阅读
        if (!dailyBean.isRead()) {

            holder.mTitle.setTextColor(ContextCompat.getColor(mContext, R.color.color_unread));
        } else {
            holder.mTitle.setTextColor(ContextCompat.getColor(mContext, R.color.color_read));
        }
        //CardView的点击事件
        holder.mLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                LogUtil.all("点击");
                if (!dailyBean.isRead()) {
                    dailyBean.setRead(true);
                    holder.mTitle.setTextColor(ContextCompat.getColor(mContext, R.color.color_read));
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            //开启线程，向数据库插入被阅读了的新闻的id
                            mDailyDao.insertReadNew(dailyBean.getId() + "");
                        }
                    }).start();
                }
                //跳转到详情界面
                DailyDetailActivity.lanuch(mContext, dailyBean);
            }
        });
    }


    /**
     * 刷新数据
     * @param dailys
     * List<DailyBean>
     */
    public void updateData(List<DailyBean> dailys) {

        this.dailys = dailys;
        notifyDataSetChanged();
    }


    /**
     * 添加数据
     * @param dailys
     * List<DailyBean>
     */
    public void addData(List<DailyBean> dailys) {

        if (this.dailys == null) {
            updateData(dailys);
        } else {
            this.dailys.addAll(dailys);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {

        return dailys.size() == 0 ? 0 : dailys.size();
    }

    /**
     *
     * @return
     * 返回adapter中list数据的个数
     */
    public List<DailyBean> getmDailyList() {

        return dailys;
    }

    /**
     * 带时间的ItemTimeViewHolder
     */
    public class ItemTimeViewHolder extends ItemContentViewHolder {

        @Bind(R.id.item_time)
        TextView mTime;

        public ItemTimeViewHolder(View itemView) {

            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    /**
     * 不带时间的ItemContentViewHolder
     */
    public class ItemContentViewHolder extends RecyclerView.ViewHolder {


        @Bind(R.id.card_view)
        CardView mLayout;

        @Bind(R.id.item_image)
        ImageView mPic;

        @Bind(R.id.item_title)
        TextView mTitle;

        @Bind(R.id.item_more_pic)
        ImageView mMorePic;


        public ItemContentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, this.itemView);
        }
    }
}

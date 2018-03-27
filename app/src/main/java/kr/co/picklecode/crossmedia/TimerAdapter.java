package kr.co.picklecode.crossmedia;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bases.BaseActivity;
import bases.Constants;
import bases.imageTransform.RoundedTransform;
import bases.utils.AlarmUtils;
import kr.co.picklecode.crossmedia.models.AdapterCall;
import kr.co.picklecode.crossmedia.models.TimerItem;
import utils.PreferenceUtil;

public class TimerAdapter extends RecyclerView.Adapter<TimerAdapter.ViewHolder> {

    public static final int HEADER = 3, DEFAULT = 0;

    private AdapterCall<TimerItem> adapterCall;

    public Context mContext = null;
    public List<TimerItem> mListData = new ArrayList<>();
    public int item_layout;
    public int addition = 0;

    public TimerAdapter(Context mContext, int item_layout, AdapterCall<TimerItem> adapterCall) {
        this(mContext, item_layout);
        this.adapterCall = adapterCall;
    }

    public TimerAdapter(Context mContext, int item_layout) {
        super();
        this.mContext = mContext;
        this.item_layout = item_layout;
    }

    public TimerAdapter(Context mContext, int item_layout, int addition) {
        this.mContext = mContext;
        this.item_layout = item_layout;
        this.addition = addition;
    }

    @Override
    public int getItemViewType(int position){
        if(position == 0) return HEADER;
        else return DEFAULT;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        switch(viewType){
            default:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_timer, parent, false);
                break;
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final TimerItem mData = mListData.get(position);

        holder._title.setText(mData.getDisplayName());
        holder._title.setSelected(true);
        if(mData.isCancel() ||
                (mData.getTimeInMins() == PreferenceUtil.getInt(Constants.PREFERENCE.ALARM_TIME_FOR_ADAPTER, 0)
                        && AlarmUtils.getInstance().getCurrentSetAlarm(mContext) != null
                        && PreferenceUtil.getBoolean(Constants.PREFERENCE.IS_ALARM_SET, false)
                )){
            holder._title.setTextColor(mContext.getResources().getColor(R.color.red));
        }else {
            holder._title.setTextColor(mContext.getResources().getColor(R.color.black));
        }

        holder.view.setOnClickListener(new CardView.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mData.isCancel()){
                    PreferenceUtil.setBoolean(Constants.PREFERENCE.IS_ALARM_SET, false);
                    AlarmUtils.getInstance().cancelAll(mContext);
                }else{
                    AlarmUtils.getInstance().startAlarm(mContext, mData.getTimeInMins() * 1000 * 60);
                    PreferenceUtil.setBoolean(Constants.PREFERENCE.IS_ALARM_SET, true);
                    PreferenceUtil.setInt(Constants.PREFERENCE.ALARM_TIME_FOR_ADAPTER, mData.getTimeInMins());
                }

                final Intent activityIntent1 = new Intent(Constants.ACTIVITY_INTENT_FILTER);
                activityIntent1.putExtra("action", "refresh");
                mContext.sendBroadcast(activityIntent1);

                if(adapterCall != null){
                    adapterCall.onCall(mData);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView _title;
        public View view;

        public ViewHolder(View itemView) {
            super(itemView);
            _title = itemView.findViewById(R.id.title);
            view = itemView.findViewById(R.id.view);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(TimerItem addInfo){
        mListData.add(addInfo);
    }

    public void dataChange(){
        this.notifyDataSetChanged();
    }

}
package kr.co.picklecode.crossmedia;


import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import bases.imageTransform.RoundedTransform;
import kr.co.picklecode.crossmedia.models.AdapterCall;
import kr.co.picklecode.crossmedia.models.ChannelScheme;

public class SchemeAdapter extends RecyclerView.Adapter<SchemeAdapter.ViewHolder> {

    public static final int HEADER = 3, DEFAULT = 0;

    private AdapterCall adapterCall;

    public Context mContext = null;
    public List<ChannelScheme> mListData = new ArrayList<>();
    public int item_layout;
    public int addition = 0;

    public SchemeAdapter(Context mContext, int item_layout, AdapterCall itemTouchCallback){
        this(mContext, item_layout);
        this.adapterCall = itemTouchCallback;
    }

    public SchemeAdapter(Context mContext, int item_layout) {
        super();
        this.mContext = mContext;
        this.item_layout = item_layout;
    }

    public SchemeAdapter(Context mContext, int item_layout, int addition) {
        this.mContext = mContext;
        this.item_layout = item_layout;
        this.addition = addition;
    }

    public int getClickedPos() {
        return clickedPos;
    }

    public void setClickedPos(int clickedPos) {
        this.clickedPos = clickedPos;
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
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_scheme, parent, false);
                break;
        }
        return new ViewHolder(v);
    }

    private int clickedPos = 0;

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ChannelScheme mData = mListData.get(position);

        holder._title.setText(mData.getTitle());
        holder._title.setSelected(true);
        if(position == clickedPos) {
            holder._title.setTypeface(null, Typeface.BOLD);
        }else{
            holder._title.setTypeface(null, Typeface.NORMAL);
        }

        holder.view.setOnClickListener(new CardView.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedPos = holder.getAdapterPosition();
                notifyDataSetChanged();
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

    public void addItem(ChannelScheme addInfo){
        mListData.add(addInfo);
    }

    public void dataChange(){
        this.notifyDataSetChanged();
    }

}
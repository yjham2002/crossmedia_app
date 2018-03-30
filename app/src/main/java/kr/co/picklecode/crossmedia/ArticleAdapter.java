package kr.co.picklecode.crossmedia;


import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import bases.imageTransform.CircleTransform;
import bases.imageTransform.RoundedTransform;
import kr.co.picklecode.crossmedia.models.AdapterCall;
import kr.co.picklecode.crossmedia.models.Article;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    public static final int HEADER = 3, DEFAULT = 0;

    public Context mContext = null;
    public int item_layout;
    public int addition = 0;

    private AdapterCall<Article> adapterCall;

    public ArticleAdapter(Context mContext, int item_layout, AdapterCall itemTouchCallback) {
        this(mContext, item_layout);
        this.adapterCall = itemTouchCallback;
    }

    public ArticleAdapter(Context mContext, int item_layout) {
        super();
        this.mContext = mContext;
        this.item_layout = item_layout;
    }

    public ArticleAdapter(Context mContext, int item_layout, int addition) {
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
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_article, parent, false);
                break;
        }
        return new ViewHolder(v);
    }

    private int clickedPos = 0;

    public void setClickedPos(int clickedPos) {
        this.clickedPos = clickedPos;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final Article mData = UISyncManager.getInstance().getChList().get(position);

        if(mData.getImgPath() != null && !mData.getImgPath().trim().equals("")) {
            Picasso
                    .get()
                    .load(mData.getImgPath())
                    .centerCrop()
                    .resize(45, 45)
                    .placeholder(R.drawable.icon_hour_glass)
                    .transform(new RoundedTransform(5, 0)).into(holder._img);
        }
        holder._title.setText(mData.getTitle());
        holder._subTitle.setText(mData.getContent());

        if(UISyncManager.getInstance().getService() != null
                && UISyncManager.getInstance().getService().getNowPlayingMusic() != null
                && UISyncManager.getInstance().getService().getNowPlayingMusic().getParent() != null
                && UISyncManager.getInstance().getService().getNowPlayingMusic().getParent().getId() == mData.getId()){
            holder._title.setTypeface(null, Typeface.BOLD);
            holder._title.setSelected(true);
            holder._subTitle.setSelected(true);
        }else{
            holder._title.setTypeface(null, Typeface.NORMAL);
            holder._title.setSelected(false);
            holder._subTitle.setSelected(false);
        }

        holder.view.setOnClickListener(new CardView.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapterCall != null){
                    adapterCall.onCall(mData);
                }
                clickedPos = holder.getAdapterPosition();
                notifyDataSetChanged();
            }
        });

        ToggleButton.OnCheckedChangeListener onCheckedChangeListener = new ToggleButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    FavorSQLManager.getInstance(mContext).insert(mData);
                }else {
                    FavorSQLManager.getInstance(mContext).delete(mData.getId());
                    if(mContext instanceof FavorActivity) {
                        deleteItem(position);
                    }
                }
            }
        };

        if(FavorSQLManager.getInstance(mContext).getPrimaryKeySet().contains(mData.getId())){
            holder.toggleButton.setOnCheckedChangeListener(null);
            holder.toggleButton.setChecked(true);
        }else{
            holder.toggleButton.setOnCheckedChangeListener(null);
            holder.toggleButton.setChecked(false);
        }

        holder.toggleButton.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    public void deleteItem(int pos){
        UISyncManager.getInstance().getChList().remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, getItemCount() - pos);
    }

    @Override
    public int getItemCount() {
        return UISyncManager.getInstance().getChList().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView _img;
        public TextView _title;
        public TextView _subTitle;
        public ToggleButton toggleButton;
        public View view;

        public ViewHolder(View itemView) {
            super(itemView);
            _img = itemView.findViewById(R.id.channelLogo);
            _title = itemView.findViewById(R.id.title);
            _subTitle = itemView.findViewById(R.id.subTitle);
            view = itemView.findViewById(R.id.view);
            toggleButton = itemView.findViewById(R.id.btn_fav);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(Article addInfo){
        UISyncManager.getInstance().getChList().add(addInfo);
    }

    public void dataChange(){
        FavorSQLManager.getInstance(mContext).refreshPrimaryKeySet();
        this.notifyDataSetChanged();
    }

}
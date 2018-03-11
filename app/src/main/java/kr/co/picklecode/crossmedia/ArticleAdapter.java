package kr.co.picklecode.crossmedia;


import android.content.Context;
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

import bases.imageTransform.CircleTransform;
import bases.imageTransform.RoundedTransform;
import kr.co.picklecode.crossmedia.models.Article;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    public static final int HEADER = 3, DEFAULT = 0;

    public Context mContext = null;
    public List<Article> mListData = new ArrayList<>();
    public int item_layout;
    public int addition = 0;

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

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Article mData = mListData.get(position);
        switch (mData.getType()){
            default: {
//                holder._favicon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.div_01));
                break;
            }
        }

        if(mData.getImgPath() != null) {
            Picasso
                    .get()
                    .load(mData.getImgPath())
                    .placeholder(R.drawable.icon_hour_glass)
                    .transform(new RoundedTransform(10, 0)).into(holder._img);
        }
        holder._title.setText(mData.getTitle());
        holder._subTitle.setText(mData.getContent());
        holder._title.setSelected(true);
        holder._subTitle.setSelected(true);

        holder.view.setOnClickListener(new CardView.OnClickListener() {
            @Override
            public void onClick(View v) {
//                final Article mData = mListData.get(position);
//                Intent i = new Intent(mContext, DetailActivity.class);
//                i.putExtra("URL", mData.Url);
//                i.putExtra("id", mData.id);
//                mContext.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView _img;
        public TextView _title;
        public TextView _subTitle;
        public View view;

        public ViewHolder(View itemView) {
            super(itemView);
            _img = itemView.findViewById(R.id.channelLogo);
            _title = itemView.findViewById(R.id.title);
            _subTitle = itemView.findViewById(R.id.subTitle);
            view = itemView.findViewById(R.id.view);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(Article addInfo){
        mListData.add(addInfo);
    }

    public void dataChange(){
        this.notifyDataSetChanged();
    }

}
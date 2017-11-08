package com.dotwin.router_stat.tencentmap;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.dotwin.router_stat.R;
import java.util.ArrayList;

import static com.dotwin.router_stat.R.layout.item;

/**
 * Created by ff135 on 2017/9/21.
 */

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Item> data;
    private MyCilckListener cilckListener;
    public static final int TYPE_ITEM=0;
    public static final int TYPE_FOOTER=1;
    public static final int TYPE_END=2;
    public static int lastFinish=TYPE_FOOTER;

    public MyAdapter(ArrayList<Item> data){
        this.data =data;

    }

    public void changeEnd(int type){
        lastFinish = type;
    }

    @Override
    public int getItemCount() {
        return  data == null ? 0 : data.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position+1 == getItemCount()){
            return lastFinish;
        }else {
            return TYPE_ITEM;
        }
    }

    public void setOnClickListener(MyCilckListener listener){
        this.cilckListener = listener;
    }

    public void updateData(ArrayList<Item> data){
        this.data = data;
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType==TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(item, parent,false);
            return new ItemViewHolder(v);
        }else if (viewType == TYPE_FOOTER){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_foot, parent,false);
            return new FootViewHolder(v);
        }else  if (viewType == TYPE_END){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_end, parent,false);
            return new EndViewHolder(v);
        }
        return null;
    }



    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemholder = (ItemViewHolder)holder;
            Item item = data.get(position);
            itemholder.tv_addr.setImageResource(item.getTvAddr());
            itemholder.addr.setText(item.getAddr());
            itemholder.name.setText(item.getName());
            itemholder.tv_gou.setImageResource(item.getTvgou());
            if (cilckListener != null){
                itemholder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getLayoutPosition();
                        cilckListener.onClick(position,v);
                    }
                });
            }
        }

    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView addr;
        ImageView tv_addr;
        ImageView tv_gou;
        public ItemViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tx_name);
            addr = (TextView) itemView.findViewById(R.id.tx_addr);
            tv_addr = (ImageView) itemView.findViewById(R.id.tv_addr);
            tv_gou = (ImageView)itemView.findViewById(R.id.tv_gou);
        }
    }

    private class FootViewHolder extends  RecyclerView.ViewHolder {

        public FootViewHolder(View view) {
            super(view);
        }
    }

    private class EndViewHolder extends  RecyclerView.ViewHolder {

        public EndViewHolder(View view) {
            super(view);
        }
    }
}

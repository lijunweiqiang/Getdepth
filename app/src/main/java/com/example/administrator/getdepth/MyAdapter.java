package com.example.administrator.getdepth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/12/2.
 */
public class MyAdapter extends BaseAdapter {
    private int isCheckBoxVisiable = View.GONE;

    private HashMap<Integer, Boolean> isSelectedMap;

    private Context context;
    private List<Data> ContactLists;

    private LayoutInflater inflater;
    public MyAdapter(Context context,
                     List<Data> ContactLists,
                     boolean isMultiChoose){
        this.context = context;
        inflater = LayoutInflater.from(context);

        this.ContactLists = ContactLists;

        isSelectedMap = new HashMap<Integer, Boolean>();

        if(isMultiChoose){
            isCheckBoxVisiable = CheckBox.VISIBLE;
        }else{
            isCheckBoxVisiable = View.GONE;
        }
    }
    @Override
    public int getCount() {
        return ContactLists.size();
    }

    @Override
    public Object getItem(int position) {
        return ContactLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //context.getLayoutInflater().inflate(R.layout.items, parent, false);
        final ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.items, parent,false);
            holder.cbContactMulti = (CheckBox)convertView.findViewById(R.id.checkBox);
            holder.tvContactName = (TextView)convertView.findViewById(R.id.tv_length);
            holder.tvContactRemark = (TextView)convertView.findViewById(R.id.tv_RSS);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }

        holder.cbContactMulti.setVisibility(isCheckBoxVisiable);
        holder.cbContactMulti.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                       isSelectedMap.put(position, isChecked);
                           notifyDataSetChanged();
                            }else{
                                if(getisSelectedAt(position)){
                                        isSelectedMap.remove(position);
                                                              }
                            }
            }
        });
        String Length = ContactLists.get(position).Length;
        holder.tvContactName.setText(Length);
        String RSS = ContactLists.get(position).Rss;
        holder.tvContactRemark.setText(RSS);
        holder.cbContactMulti.setChecked(getisSelectedAt(position));

        return convertView;
    }

    private class ViewHolder{
        CheckBox cbContactMulti;
        TextView tvContactName;
        TextView tvContactRemark;
    }

    public void setDate(List<Data> ContactLists){
        this.ContactLists = ContactLists;
        notifyDataSetChanged();
    }

    public boolean getisSelectedAt(int position){

        //如果当前位置的key值为空，则表示该item未被选择过，返回false，否则返回true
        if(isSelectedMap.get(position) != null){
            return isSelectedMap.get(position);
        }
        return false;
    }

    public void setItemisSelectedMap(int position, boolean isSelectedMap){
        this.isSelectedMap.put(position, isSelectedMap);
        notifyDataSetChanged();
    }

    public HashMap<Integer, Boolean> getAllSelected(){
        return isSelectedMap;
    }

    public void removeSelected(int position){
        //被选择才能删除
        if(getisSelectedAt(position)){
            isSelectedMap.remove(position);
        }
        notifyDataSetChanged();
    }
}

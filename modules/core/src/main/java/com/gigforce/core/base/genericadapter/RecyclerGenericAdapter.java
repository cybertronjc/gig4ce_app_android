package com.gigforce.core.base.genericadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Akshay on 5/15/2018.
 */

public class RecyclerGenericAdapter<T> extends PFRecyclerViewAdapter<T> {
    int layoutID;
    ItemInterface<T> obj;
    RecyclerView recylerView;

    public interface ItemInterface<T>{
        public void setItem(T obj, PFRecyclerViewAdapter.ViewHolder viewHolder,int position);
    }
    public RecyclerGenericAdapter(Context context, OnViewHolderClick listener, ItemInterface<T> item) {
        super(context, listener);
        this.obj = item;
    }
    public void setLayout(int layoutID){
        this.layoutID = layoutID;
    }
    @Override
    protected View createView(Context context, ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layoutID, viewGroup, false);
        return view;
    }

    public void setRecylerView(RecyclerView recylerView){
        this.recylerView = recylerView;
    }

    public RecyclerView getRecylerView(){
        return recylerView;
    }

    @Override
    protected void bindView(T item, PFRecyclerViewAdapter.ViewHolder viewHolder,int position) {
        if (item != null) {
            obj.setItem(item,viewHolder,position);
        }
    }
}

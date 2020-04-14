package com.gigforce.app.core.genericadapter;

/**
 * Created by Akshay on 5/15/2018.
 */

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Plumillon Forge.
 */
public abstract class PFRecyclerViewAdapter<T> extends RecyclerView.Adapter<PFRecyclerViewAdapter.ViewHolder> {
    public List<T> items;
    private Context context;
    private OnViewHolderClick<T> listener;

    public interface OnViewHolderClick<T> {
        void onClick(View view, int position, T item);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Map<Integer, View> views;

        public ViewHolder(View view, OnViewHolderClick listener) {
            super(view);
            views = new HashMap<>();
            views.put(0, view);

            if (listener != null)
                view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null)
                listener.onClick(view, getAdapterPosition(), getItem(getAdapterPosition()));
        }

        public void initViewList(int[] idList) {
            for (int id : idList)
                initViewById(id);
        }

        public void initViewById(int id) {
            View view = (getView() != null ? getView().findViewById(id) : null);

            if (view != null)
                views.put(id, view);
        }

        public View getView() {
            return getView(0);
        }

        public View getView(int id) {
            if (views.containsKey(id))
                return views.get(id);
            else
                initViewById(id);

            return views.get(id);
        }
    }

    protected abstract View createView(Context context, ViewGroup viewGroup, int viewType);

    protected abstract void bindView(T item, PFRecyclerViewAdapter.ViewHolder viewHolder);

    public PFRecyclerViewAdapter(Context context) {
        this(context, null);
    }

    public PFRecyclerViewAdapter(Context context, OnViewHolderClick<T> listener) {
        super();
        this.context = context;
        this.listener = listener;
        items = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ViewHolder(createView(context, viewGroup, viewType), listener);
    }

    @Override
    public void onBindViewHolder(PFRecyclerViewAdapter.ViewHolder holder, int position) {
        bindView(getItem(position), holder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public T getItem(int index) {
        return ((items != null && index < items.size()) ? items.get(index) : null);
    }

    public Context getContext() {
        return context;
    }

    public void setList(List<T> list) {
        items = list;
    }

    public List<T> getList() {
        return items;
    }

    public void setClickListener(OnViewHolderClick listener) {
        this.listener = listener;
    }

    public void addAll(List<T> list) {
        items.addAll(list);
        notifyDataSetChanged();
    }

    public void reset() {
        items.clear();
        notifyDataSetChanged();
    }
}

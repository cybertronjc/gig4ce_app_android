package com.gigforce.giger_gigs.tl_login_details.views;

import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class PaginationScrollListenerNew extends RecyclerView.OnScrollListener
{
    private static final String TAG = PaginationScrollListenerNew.class.getSimpleName();
    private LinearLayoutManager layoutManager;
    protected PaginationScrollListenerNew(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    /*
     Method gets callback when user scroll the search list
     */
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        if (!isLoading() && !isLastPage()) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0) {
                Log.i(TAG, "Loading more items");
                loadMoreItems();
            }
        }

    }
    protected abstract void loadMoreItems();

    //optional
    public abstract int getTotalPageCount();

    public abstract boolean isLastPage();

    public abstract boolean isLoading();
}
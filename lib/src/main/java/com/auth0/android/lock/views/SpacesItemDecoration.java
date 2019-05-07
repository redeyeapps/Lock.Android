package com.auth0.android.lock.views;

import android.graphics.Rect;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private final int orientation;
    private final int space;

    public SpacesItemDecoration(int space, @LinearLayoutCompat.OrientationMode int orientation) {
        this.space = space;
        this.orientation = orientation;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (orientation == RecyclerView.HORIZONTAL) {
            outRect.left = space / 2;
            outRect.right = space / 2;
        } else {
            boolean firstItem = parent.getChildAdapterPosition(view) == 0;
            boolean lastItem = parent.getChildAdapterPosition(view) == parent.getChildCount() - 1;
            outRect.top = firstItem ? 0 : space / 2;
            outRect.bottom = lastItem ? 0 : space / 2;
        }
    }
}
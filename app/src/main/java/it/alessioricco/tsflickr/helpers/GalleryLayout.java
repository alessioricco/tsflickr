package it.alessioricco.tsflickr.helpers;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * will display pictures as squares
 *
 * http://stackoverflow.com/questions/8981029/simple-way-to-do-dynamic-but-square-layout
 */

class GalleryLayout extends RelativeLayout {

    public GalleryLayout(Context context) {
        super(context);
    }

    public GalleryLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GalleryLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GalleryLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(final int width, final int height) {
        super.onMeasure(width, width);
    }
}
package it.alessioricco.tsflickr.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import it.alessioricco.tsflickr.R;
import it.alessioricco.tsflickr.models.GalleryImage;
import it.alessioricco.tsflickr.models.GalleryImages;
import it.alessioricco.tsflickr.utils.ImageDownloader;
import lombok.Getter;
import lombok.Setter;


/**
 * Gallery Adapter
 *
 * todo: improve thumbnail quality
 */


public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ThumbnailViewHolder>  {

    private final String TAG = GalleryAdapter.class.getSimpleName();

    private final GalleryImages images;
    private final Context context;

    public GalleryAdapter(Context context, GalleryImages images) {
        this.context = context;
        this.images = images;
    }

    @Override
    public ThumbnailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_thumbnails, parent, false);

        return new ThumbnailViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ThumbnailViewHolder holder, int position) {
        final GalleryImage image = images.get(position);

        ImageDownloader.go(context, image.getThumbnailImageURL(), holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    /**
     * RecyclerView ViewHolder for the thumbnail gallery_thumbnails
     */
    protected final class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        private @Getter @Setter ImageView thumbnail;

        public ThumbnailViewHolder(View view) {
            super(view);
            thumbnail = (ImageView) view.findViewById(R.id.thumb);
        }
    }

    /**
     * RecyclerView touch listener
     */
    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private GalleryAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final GalleryAdapter.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildLayoutPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildLayoutPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

}



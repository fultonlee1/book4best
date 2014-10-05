package book4best.android.best.com.book4best.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.util.List;

import book4best.android.best.com.book4best.R;
import book4best.android.best.com.book4best.model.Book;
import book4best.android.best.com.book4best.model.BookStatus;

/**
 * Created by bl02637
 * DESCRIPTION:
 * DATE: 2014/10/3
 * TIME: 14:56
 */
public class GridViewAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater = null;
    private RequestQueue mQueue;
    private ImageLoader mImageLoader = null;
    private List<Book> mBooks;

    public GridViewAdapter(Context context, List<Book> books) {
        mBooks = books;
        mLayoutInflater = LayoutInflater.from(context);
        mQueue = Volley.newRequestQueue(context);
        mImageLoader = new ImageLoader(mQueue, new BitmapCache());
        // RequestQueue mQueue = Volley.newRequestQueue(mContext);
        // imageLoader = new ImageLoader(mQueue, new ImageCache());
    }

    @Override
    public int getCount() {
        return mBooks.size();
    }

    @Override
    public Object getItem(int position) {
        return mBooks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.listview_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.ivBookCover = (ImageView)convertView.findViewById(R.id.listview_item_ivBookCover);
            viewHolder.tvTitle = (TextView)convertView.findViewById(R.id.listview_item_tvTitle);
            viewHolder.tvStatus = (TextView)convertView.findViewById(R.id.listview_item_tvStatus);
            convertView.setTag(viewHolder);
        }

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        switch (mBooks.get(position).BookStatus) {
            case BookStatus.IN:
                viewHolder.tvStatus.setText("可借");
                break;
            case BookStatus.OUT:
                viewHolder.tvStatus.setText("不可借");
                break;
        }
        viewHolder.tvTitle.setText(mBooks.get(position).Title);
        if (!TextUtils.isEmpty(mBooks.get(position).Image)) {
            ImageLoader.ImageListener listener = ImageLoader.getImageListener(viewHolder.ivBookCover, R.drawable.ic_launcher, R.drawable.ic_launcher);
            mImageLoader.get(mBooks.get(position).Image, listener);
        }

        return convertView;
    }

    class ViewHolder {
        ImageView ivBookCover;
        TextView tvTitle;
        TextView tvStatus;
    }

    public class BitmapCache implements ImageLoader.ImageCache {

        private LruCache<String, Bitmap> cache;

        public BitmapCache() {
            cache = new LruCache<String, Bitmap>(8 * 1024 * 1024) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getRowBytes() * bitmap.getHeight();
                }
            };
        }

        @Override
        public Bitmap getBitmap(String url) {
            return cache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            cache.put(url, bitmap);
        }
    }
}

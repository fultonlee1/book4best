package book4best.android.best.com.book4best.view.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.joda.time.DateTime;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import book4best.android.best.com.book4best.R;
import book4best.android.best.com.book4best.db.DatabaseHelper;
import book4best.android.best.com.book4best.model.Book;
import book4best.android.best.com.book4best.model.BookJsonRequest;
import book4best.android.best.com.book4best.model.BookRequest;
import book4best.android.best.com.book4best.model.BookStatus;
import book4best.android.best.com.book4best.model.RequestType;
import book4best.android.best.com.book4best.model.User;
import de.greenrobot.event.EventBus;
import info.hoang8f.widget.FButton;

/**
 * Created by bl02637
 * DESCRIPTION:
 * DATE: 2014/10/4
 * TIME: 12:14
 */
public class BookDetailActivity extends Activity {
    Context mContext = BookDetailActivity.this;
    final String URL = "http://10.45.17.156:9090/bookmanager";
    final String TAG = "BookDetailActivity";

    ImageView ivBook;
    TextView tvTitle;
    TextView tvSubtitle;
    TextView tvAuthor;
    TextView tvTranslator;
    TextView tvPublisher;
    TextView tvPubDate;
    TextView tvISBN;
    TextView tvDonater;
    TextView tvBookStatus;
    FButton btnBookBorrow;

    Book mBook;
    RequestQueue mRequestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookdetail);

        EventBus.getDefault().register(this);

        initView();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(Book book) {
        Log.d(TAG, "I am in");
    }

    private void initView() {
        ivBook = (ImageView) findViewById(R.id.activity_bookdetail_ivBook);
        tvTitle = (TextView) findViewById(R.id.activity_bookdetail_tvTitle);
        tvSubtitle = (TextView) findViewById(R.id.activity_bookdetail_tvSubtitle);
        tvAuthor = (TextView) findViewById(R.id.activity_bookdetail_tvAuthor);
        tvTranslator = (TextView) findViewById(R.id.activity_bookdetail_tvTranslator);
        tvPublisher = (TextView) findViewById(R.id.activity_bookdetail_tvPublisher);
        tvPubDate = (TextView) findViewById(R.id.activity_bookdetail_tvPubDate);
        tvISBN = (TextView) findViewById(R.id.activity_bookdetail_tvISBN);
        tvDonater = (TextView) findViewById(R.id.activity_bookdetail_tvDonater);
        tvBookStatus = (TextView) findViewById(R.id.activity_bookdetail_tvBookStatus);
        btnBookBorrow = (FButton) findViewById(R.id.activity_bookdetail_btnBorrow);
        btnBookBorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBook.BookStatus = BookStatus.OUT;
                mBook.BorrowTime = DateTime.now().getMillis();
                try {
                    mBook.Borrower = DatabaseHelper.getInstance().getDao(User.class).queryForAll().get(0).UserName;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                EventBus.getDefault().post(mBook);
                updateBookStatus();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void initData() {
        Gson gson = new Gson();
        mBook = gson.fromJson(getIntent().getStringExtra("book"), Book.class);
        if (mBook != null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
            setValues();
            getPicFromDouBan();
        }
    }

    private void setValues() {
        tvTitle.setText(mBook.Title);
        tvSubtitle.setText(mBook.Subtitle);
        tvAuthor.setText(mBook.Author);
        tvTranslator.setText(mBook.Translator);
        tvPublisher.setText(mBook.Publisher);
        tvPubDate.setText(mBook.PubDate);
        tvISBN.setText(mBook.ISBN);
        tvDonater.setText(mBook.Donater);
        switch (mBook.BookStatus) {
            case BookStatus.IN:
                tvBookStatus.setText("可借");
                break;
            case BookStatus.OUT:
                tvBookStatus.setText("已借出");
                btnBookBorrow.setVisibility(View.GONE);
                break;
        }
    }

    private ImageRequest buildImageRequst() {
        try {
            ImageRequest imageRequest = new ImageRequest(
                    mBook.Image,
                    new Response.Listener<Bitmap>() {

                        @Override
                        public void onResponse(Bitmap response) {
                            if (response == null)
                                return;

                            ivBook.setImageBitmap(response);
                        }
                    }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(mContext, "load image error", Toast.LENGTH_LONG).show();
                }
            }
            );

            return imageRequest;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void getPicFromDouBan() {
        mRequestQueue.add(buildImageRequst());
    }

    private void updateBookStatus() {
        BookJsonRequest bookJsonRequest = new BookJsonRequest(
                Request.Method.POST,
                URL,
                buildGsonRequst(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "volley response:" + response);

                        Gson gson = new Gson();
                        boolean isOk = gson.fromJson(response, Boolean.class);
                        if (!isOk ) {
                            Toast.makeText(mContext, "借书失败", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            DatabaseHelper.getInstance().getDao(Book.class).update(mBook);

                        } catch (SQLException e) {
                            e.printStackTrace();
                            Log.d(TAG, e.toString());
                        }

                        Toast.makeText(mContext, "借书成功", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "volley error:" + error);

                        Toast.makeText(mContext,
                                error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        mRequestQueue.add(bookJsonRequest);
    }

    private String buildGsonRequst() {
        BookRequest bookRequest = new BookRequest();
        try {
            bookRequest.UserName = DatabaseHelper.getInstance().getDao(User.class).queryForAll().get(0).UserName;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        String bookJson = gson.toJson(mBook);
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put(RequestType.BOOK_BORROW, bookJson);
        bookRequest.RequestMap = requestMap;

        String json = gson.toJson(bookRequest);

        Log.d("volley", json);

        return json;
    }
}

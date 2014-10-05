package book4best.android.best.com.book4best.view.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import book4best.android.best.com.book4best.R;
import book4best.android.best.com.book4best.db.DatabaseHelper;
import book4best.android.best.com.book4best.model.Book;
import book4best.android.best.com.book4best.model.BookJsonRequest;
import book4best.android.best.com.book4best.model.BookRequest;
import book4best.android.best.com.book4best.model.RequestType;
import book4best.android.best.com.book4best.model.User;
import info.hoang8f.widget.FButton;

/**
 * Created by bl02637
 * DESCRIPTION: 捐书
 * DATE: 2014/10/3
 * TIME: 13:38
 */
public class BookDonaterFragment extends Fragment {
    final String URL = "http://10.45.17.156:9090/bookmanager";
    final String TAG = "BookDonaterFragment";

    EditText etISBN;
    FButton btnDonater;
    FButton btnScan;
    LinearLayout linearLayout;
    ImageView ivBook;
    TextView tvAuthor;
    TextView tvTitle;
    TextView tvSubtitle;
    TextView tvTranslator;
    TextView tvPubDate;
    TextView tvPublisher;
    TextView tvISBN;
    TextView tvDonater;

    RequestQueue mRequestQueue;
    Book mBook;
    String mUserName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookdonater, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                etISBN.setText(result.getContents());
                Toast.makeText(getActivity(), "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initView(View view) {
        etISBN = (EditText) view.findViewById(R.id.fragment_bookdonater_etISBN);
        btnDonater = (FButton) view.findViewById(R.id.fragment_bookdonater_btnSearch);
        btnScan = (FButton) view.findViewById(R.id.fragment_bookdonater_btnScan);
        linearLayout = (LinearLayout) view.findViewById(R.id.fragment_bookdonater_linearLayout);
        ivBook = (ImageView) view.findViewById(R.id.fragment_bookdonater_ivBook);
        tvAuthor = (TextView) view.findViewById(R.id.fragment_bookdonater_tvAuthor);
        tvTitle = (TextView) view.findViewById(R.id.fragment_bookdonater_tvTitle);
        tvSubtitle = (TextView) view.findViewById(R.id.fragment_bookdonater_tvSubtitle);
        tvTranslator = (TextView) view.findViewById(R.id.fragment_bookdonater_tvTranslator);
        tvPubDate = (TextView) view.findViewById(R.id.fragment_bookdonater_tvPubDate);
        tvPublisher = (TextView) view.findViewById(R.id.fragment_bookdonater_tvPublisher);
        tvISBN = (TextView) view.findViewById(R.id.fragment_bookdonater_tvISBN);
        tvDonater = (TextView) view.findViewById(R.id.fragment_bookdonater_tvDonater);

        btnDonater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "btnsearch");
                donaterBook();
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "btnScan");
                scanBarcode();
            }
        });
    }

    private void initData() {
        mRequestQueue = Volley.newRequestQueue(getActivity());
        try {
            mUserName = DatabaseHelper.getInstance().getDao(User.class).queryForAll().get(0).UserName;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void scanBarcode() {
        IntentIntegrator.forFragment(this).initiateScan();
    }

    private void donaterBook() {
        mRequestQueue.add(buildSearchRequest());
    }

    private String buildGsonRequst() {
        BookRequest bookRequest = new BookRequest();
        bookRequest.UserName = mUserName;
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put(RequestType.BOOK_DONATION, getISBN());
        bookRequest.RequestMap = requestMap;

        Gson gson = new Gson();
        String json = gson.toJson(bookRequest);

        Log.d("volley", json);

        return json;
    }

    private String getISBN() {
        return etISBN.getText().toString();
    }

    private JsonRequest<String> buildSearchRequest() {
        BookJsonRequest bookJsonRequest = new BookJsonRequest(
                Request.Method.POST,
                URL,
                buildGsonRequst(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "volley response:" + response);

                        Gson gson = new Gson();
                        mBook = gson.fromJson(response, Book.class);
                        if (mBook == null || TextUtils.equals(mBook.Author, mUserName)) {
                            Toast.makeText(getActivity(), "该书已经存在", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Log.d(TAG, "Auther" + mBook.Author);
                        Log.d(TAG, "ISBN" + mBook.ISBN);

                        try {
                            long count = DatabaseHelper.getInstance().getDao(Book.class).queryBuilder()
                                    .where()
                                    .eq("ISBN", mBook.ISBN)
                                    .and()
                                    .eq("Donater", mBook.Donater)
                                    .countOf();
                            if (count == 0) {
                                DatabaseHelper.getInstance().getDao(Book.class).create(mBook);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            Log.d(TAG, e.toString());
                        }

                        setValues();
                        getPicFromDouBan();

                        Toast.makeText(getActivity(),
                                response.toString(), Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "volley error:" + error);

                        Toast.makeText(getActivity(),
                                error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        return bookJsonRequest;
    }

    private void setValues() {
        tvAuthor.setText(mBook.Author);
        tvDonater.setText(mBook.Donater);
        tvISBN.setText(mBook.ISBN);
        tvPubDate.setText(mBook.PubDate);
        tvPublisher.setText(mBook.Publisher);
        tvSubtitle.setText(mBook.Subtitle);
        tvTitle.setText(mBook.Title);
        tvTranslator.setText(mBook.Translator);
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
                    Toast.makeText(getActivity(), "load image error", Toast.LENGTH_LONG).show();
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
}

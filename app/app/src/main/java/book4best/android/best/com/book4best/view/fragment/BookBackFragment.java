package book4best.android.best.com.book4best.view.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.j256.ormlite.stmt.DeleteBuilder;

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

/**
 * Created by bl02637
 * DESCRIPTION:
 * DATE: 2014/10/4
 * TIME: 14:52
 */
public class BookBackFragment extends Fragment {
    final String URL = "http://10.45.17.156:9090/bookmanager";
    final String TAG = "BookBackFragment";

    EditText etISBN;
    Button btnSearch;

    Book mBook;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookback, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        initData();
    }

    private void initView(View view) {
        etISBN = (EditText) view.findViewById(R.id.fragment_bookback_etISBN);
        btnSearch = (Button) view.findViewById(R.id.fragment_bookback_btnSearch);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backBook();
            }
        });
    }

    private void initData() {
        try {
            String userName = DatabaseHelper.getInstance().getDao(User.class).queryForAll().get(0).UserName;
            long count = DatabaseHelper.getInstance().getDao(Book.class).queryBuilder()
                    .where()
                    .eq("BookStatus", BookStatus.OUT)
                    .and()
                    .eq("Borrower", userName)
                    .countOf();
            if (count == 0) {
                Toast.makeText(getActivity(), "未借任何书", Toast.LENGTH_LONG).show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void backBook() {
        if (TextUtils.isEmpty(etISBN.getText())) {
            Toast.makeText(getActivity(), "请输入ISBN", Toast.LENGTH_LONG).show();
            return;
        }

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
                        if (!isOk) {
                            Toast.makeText(getActivity(), "还书失败", Toast.LENGTH_LONG).show();
                            return;
                        }

                        try {
                            DeleteBuilder<Book, ?> deleteBuilder = DatabaseHelper.getInstance().getDao(Book.class).deleteBuilder();
                            deleteBuilder.where()
                                    .eq("ISBN", mBook.ISBN)
                                    .and()
                                    .eq("Borrower", mBook.Borrower);
                            deleteBuilder.delete();

                            DatabaseHelper.getInstance().getDao(Book.class).create(mBook);
                            Toast.makeText(getActivity(),"还书成功", Toast.LENGTH_SHORT).show();

                        } catch (SQLException e) {
                            e.printStackTrace();
                            Log.d(TAG, e.toString());
                        }
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

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(bookJsonRequest);
    }

    private String buildGsonRequst() {
        Gson gson = new Gson();
        BookRequest bookRequest = new BookRequest();
        try {
            bookRequest.UserName = DatabaseHelper.getInstance().getDao(User.class).queryForAll().get(0).UserName;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Map<String, String> requestMap = new HashMap<String, String>();
        try {
            Book book = DatabaseHelper.getInstance().getDao(Book.class).queryBuilder()
                    .where()
                    .eq("Borrower", bookRequest.UserName)
                    .and()
                    .eq("ISBN", etISBN.getText())
                    .queryForFirst();

            if (book == null) {
                Toast.makeText(getActivity(), "你没有借这本书", Toast.LENGTH_LONG).show();
                return null;
            }

            book.BookStatus = BookStatus.IN;
            book.BackTime = DateTime.now().getMillis();
            book.Borrower = null;

            mBook = book;
            String bookJson = gson.toJson(book);
            Log.d(TAG, bookJson);

            requestMap.put(RequestType.BOOK_BACK, bookJson);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        bookRequest.RequestMap = requestMap;

        String json = gson.toJson(bookRequest);

        Log.d(TAG, json);

        return json;
    }

}

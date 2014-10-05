package book4best.android.best.com.book4best.view.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import book4best.android.best.com.book4best.R;
import book4best.android.best.com.book4best.db.DatabaseHelper;
import book4best.android.best.com.book4best.model.Book;
import book4best.android.best.com.book4best.utils.GridViewAdapter;
import book4best.android.best.com.book4best.view.activity.BookDetailActivity;
import de.greenrobot.event.EventBus;

/**
 * Created by bl02637
 * DESCRIPTION: 查询界面
 * DATE: 2014/10/3
 * TIME: 9:23
 */
public class BookSearchFragment extends Fragment {

    final String URL = "http://10.45.17.156:9090/bookmanager";
    final String TAG = "BookSearchFragment";

    EditText etISBN;
    Button   btnSearch;
    ListView lvBooks;

    GridViewAdapter mAdapter;
    Book mBook;
    List<Book> mData;
    List<Book> mFilterData;
    public final int REQUEST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booksearch, container, false);
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
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    private void initView(View view) {
        etISBN      = (EditText) view.findViewById(R.id.fragment_booksearch_etISBN);
        btnSearch   = (Button) view.findViewById(R.id.fragment_booksearch_btnSearch);
        lvBooks      = (ListView) view.findViewById(R.id.fragment_booksearch_lvBooks);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchISBN();
            }
        });
        lvBooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                Gson gson = new Gson();
                mBook = mFilterData.get(i);
                String jsonBook = gson.toJson(mBook);
                intent.putExtra("book", jsonBook);
                intent.setClass(getActivity(), BookDetailActivity.class);
                startActivityForResult(intent, REQUEST);

            }
        });
    }

    public void initData() {
        try {
            mData = DatabaseHelper.getInstance().getDao(Book.class).queryForAll();
            if (mData != null) {
                mFilterData = mData;
                mAdapter = new GridViewAdapter(getActivity(), mFilterData);
                lvBooks.setAdapter(mAdapter);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchISBN() {
        if (TextUtils.isEmpty(etISBN.getText())) {
            mFilterData = mData;
            mAdapter = new GridViewAdapter(getActivity(), mFilterData);
            lvBooks.setAdapter(mAdapter);
            return;
        }

        String title = etISBN.getText().toString();
        ArrayList<Book> resultBooks = new ArrayList<Book>();
        for (Book book : mData) {
            if (book.Title.contains(title)) {
                resultBooks.add(book);
            }
        }
        mFilterData = resultBooks;
        mAdapter = new GridViewAdapter(getActivity(), mFilterData);
        lvBooks.setAdapter(mAdapter);
//        mRequestQueue.add(buildSearchRequest());
    }

    public void onEventMainThread(Book book) {
        if (book == null)
            Log.d(TAG, "book is null");

        Log.d(TAG, "book is not null");
        int index = mData.indexOf(mBook);
        mData.get(index).BookStatus = book.BookStatus;
        mData.get(index).Borrower = book.Borrower;
        mData.get(index).BorrowTime = book.BorrowTime;
        mAdapter.notifyDataSetChanged();
    }

//    private String buildGsonRequst() {
//        BookRequest bookRequest = new BookRequest();
//        bookRequest.UserName = "pighead";
//        Map<String, String> requestMap = new HashMap<String, String>();
//        requestMap.put(RequestType.BOOK_DONATION, getISBN());
//        bookRequest.RequestMap = requestMap;
//
//        Gson gson = new Gson();
//        String json = gson.toJson(bookRequest);
//
//        Log.d("volley", json);
//
//        return json;
//    }
//
//    private String getISBN() {
//        return etISBN.getText().toString();
//    }
//
//    private JsonRequest<String> buildSearchRequest() {
//        BookJsonRequest bookJsonRequest = new BookJsonRequest(
//                Request.Method.POST,
//                URL,
//                buildGsonRequst(),
//                new Response.Listener<String>() {
//
//                    @Override
//                    public void onResponse(String response) {
//                        Log.d(TAG, "volley response:" + response);
//
//                        Gson gson = new Gson();
//                        mBook = gson.fromJson(response, Book.class);
//                        if (mBook == null)
//                            return;
//                        Log.d(TAG, "Auther" + mBook.Author);
//                        Log.d(TAG, "ISBN" + mBook.ISBN);
//
//                        try {
//                            long count = DatabaseHelper.getInstance().getDao(Book.class).queryBuilder()
//                                    .where()
//                                    .eq("ISBN", mBook.ISBN)
//                                    .countOf();
//                            if (count == 0) {
//                                DatabaseHelper.getInstance().getDao(Book.class).create(mBook);
//                            }
//                        } catch (SQLException e) {
//                            e.printStackTrace();
//                            Log.d(TAG, e.toString());
//                        }
//
//                        Toast.makeText(getActivity(),
//                                response.toString(), Toast.LENGTH_SHORT).show();
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.d(TAG, "volley error:" + error);
//
//                        Toast.makeText(getActivity(),
//                                error.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//        return bookJsonRequest;
//    }

}

package book4best.android.best.com.book4best;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.table.TableUtils;

import org.joda.time.DateTime;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import book4best.android.best.com.book4best.db.DatabaseHelper;
import book4best.android.best.com.book4best.model.Book;
import book4best.android.best.com.book4best.model.BookJsonRequest;
import book4best.android.best.com.book4best.model.BookRequest;
import book4best.android.best.com.book4best.model.RequestType;
import book4best.android.best.com.book4best.model.User;
import book4best.android.best.com.book4best.view.fragment.BookBackFragment;
import book4best.android.best.com.book4best.view.fragment.BookDonaterFragment;
import book4best.android.best.com.book4best.view.fragment.BookSearchFragment;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title.
     */
    private CharSequence mTitle;
    private Context mContext = MainActivity.this;
    private final String URL = "http://10.45.17.156:9090/bookmanager";
    private final String TAG = "MainActivity";

    private User mUser;
    private RequestQueue mRequestQueue;
    private BookSearchFragment mBookSearchFragment;
    private BookDonaterFragment mBookDonaterFragment;
    private BookBackFragment mBookBackFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                getActionBar().setTitle("查询");
                if (mBookSearchFragment == null)
                    mBookSearchFragment = new BookSearchFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, mBookSearchFragment)
                        .commit();
                break;
            case 1:
                getActionBar().setTitle("捐书");

                if (mBookDonaterFragment == null)
                    mBookDonaterFragment = new BookDonaterFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, mBookDonaterFragment)
                        .commit();
                break;
            case 2:
                getActionBar().setTitle("还书");

                if (mBookBackFragment == null)
                    mBookBackFragment = new BookBackFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, mBookBackFragment)
                        .commit();
                break;

        }

    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_search);
                break;
            case 2:
                mTitle = getString(R.string.title_donatebook);
                break;
            case 3:
                mTitle = getString(R.string.title_backbook);
                break;
            case 4:
                mTitle = getString(R.string.title_feedback);
                break;
            case 5:
                mTitle = getString(R.string.title_update);
                break;
            case 6:
                mTitle = getString(R.string.title_setting);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getActionBar().getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    private void initData() {
        mRequestQueue = Volley.newRequestQueue(mContext);
        if (!hasShow())
            addUser();
        updateBooks();
    }

    private boolean hasShow() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean hasShowed = sp.getBoolean("UserName", false);

        return hasShowed;
    }

    private void saveShowSP() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean("UserName", true);
        edit.commit();
    }

    private void addUser() {
        final EditText editText = new EditText(mContext);
        editText.setHint("请输入姓名");

        new AlertDialog.Builder(mContext)
                .setView(editText)
                .setTitle("用户登记")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(editText.getText())) {
                            Toast.makeText(mContext, "请输入姓名", Toast.LENGTH_LONG).show();
                            return;
                        }
                        mUser = new User();
                        mUser.UserName = editText.getText().toString();
                        mUser.SystemType = "android";
                        mUser.SystemVersion = String.valueOf(Build.VERSION.SDK_INT) + "," + Build.VERSION.RELEASE;;
                        mUser.CreateTime = DateTime.now().getMillis();

                        uploadUser(mUser);
                    }
                })
                .setNegativeButton("取消", null)
                .setCancelable(false)
                .show();

    }

    private void insertUser(User user) {
        try {
            DatabaseHelper.getInstance().getDao(User.class).create(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void uploadUser(User user) {
        Gson gson = new Gson();
        String json = gson.toJson(user);
        BookRequest bookRequest = new BookRequest();
        bookRequest.UserName = user.UserName;
        bookRequest.RequestMap = new HashMap<String, String>();
        bookRequest.RequestMap.put(RequestType.USER_LOGIN, json);
        String requestJson = gson.toJson(bookRequest);

        JsonRequest<String> request = new JsonRequest<String>(
                Request.Method.POST,
                URL,
                requestJson,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "volley response:" + response);
                        Gson gson1 = new Gson();
                        boolean isSuccess = gson1.fromJson(response, Boolean.class);
                        if (isSuccess) {
                            insertUser(mUser);
                            saveShowSP();
                            Toast.makeText(getApplicationContext(),
                                    "登记成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "该用户名已经存在", Toast.LENGTH_SHORT).show();
                            addUser();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "volley error:" + error);

                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString =
                            new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(jsonString,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (Exception je) {
                    return Response.error(new ParseError(je));
                }
            }
        };

        mRequestQueue.add(request);
    }

    private void updateBooks() {
        String userName = null;
        try {
            userName = DatabaseHelper.getInstance().getDao(User.class).queryForAll().get(0).UserName;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        BookRequest bookRequest = new BookRequest();
        bookRequest.UserName = userName;
        bookRequest.RequestMap = new HashMap<String, String>();
        bookRequest.RequestMap.put(RequestType.BOOK_UPDATE, userName);
        String requestJson = gson.toJson(bookRequest);
        Log.d(TAG, "requestjson:" + requestJson);

        BookJsonRequest bookJsonRequest = new BookJsonRequest(
                Request.Method.POST,
                URL,
                requestJson,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "volley response:" + response);
                        Gson gson1 = new Gson();
                        List<Book> books = gson1.fromJson(response,
                                new TypeToken<List<Book>>() {}.getType());
                        if (books != null) {
                            try {

                                int rows = TableUtils.clearTable(DatabaseHelper.getInstance().getConnectionSource(), Book.class);
                                Log.d(TAG, "rows :" + rows);
                                for (Book book : books) {
                                    DatabaseHelper.getInstance().getDao(Book.class).create(book);
                                    Log.d(TAG, "title:" + book.Title);
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "volley error:" + error);

                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        mRequestQueue.add(bookJsonRequest);
    }

}

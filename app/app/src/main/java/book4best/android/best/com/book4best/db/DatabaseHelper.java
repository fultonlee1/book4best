package book4best.android.best.com.book4best.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import book4best.android.best.com.book4best.BookApplication;
import book4best.android.best.com.book4best.model.Book;
import book4best.android.best.com.book4best.model.User;

/**
 * Created by bl02637
 * DESCRIPTION: 数据库管理类
 * DATE: 2014/9/22
 * TIME: 16:26
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final byte[] lock = new byte[0];
    private static DatabaseHelper _Instance = null;
    private static final String TAG = "DatabaseHelper";

    public static DatabaseHelper getInstance() {
        if (_Instance == null) {
            _Instance = new DatabaseHelper(BookApplication.getInstance());
        }
        return _Instance;
    }

    public static final String DATABASE_NAME = "bookmanager.db";

    // any time you make changes to your database objects, you may have to
    // increase the database version
    public static final int DATABASE_VERSION = 1;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        CreateAllTables(connectionSource);

        Log.d(TAG, "table create");
    }

    private void CreateAllTables(ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, User.class);

            TableUtils.createTableIfNotExists(connectionSource, Book.class);

        } catch (java.sql.SQLException e) {
            Log.d(TAG, "create tables failed:" + e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }
}

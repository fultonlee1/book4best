package book4best.android.best.com.book4best;

import android.app.Application;

/**
 * Created by bl02637
 * DESCRIPTION:
 * DATE: 2014/9/22
 * TIME: 16:31
 */
public class BookApplication extends Application {
    private static BookApplication instance;
    private static final String tag = "BexApplication";

    public static BookApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
    }
}

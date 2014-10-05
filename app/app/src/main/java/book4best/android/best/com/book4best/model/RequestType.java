package book4best.android.best.com.book4best.model;

/**
 * Created by bl02637
 * DESCRIPTION: 请求类型
 * DATE: 2014/10/3
 * TIME: 9:56
 */
public class RequestType {
    /**
     * 登录，传对象
     */
    public static final String USER_LOGIN = "user_login";

    /**
     * 捐书，传ISBN
     */
    public static final String BOOK_DONATION = "book_donate";

    /**
     * 查询
     */
    public static final String BOOK_SERACH = "book_search";

    /**
     * 借书，传对象
     */
    public static final String BOOK_BORROW = "book_borrow";

    /**
     * 还书,传对象
     */
    public static final String BOOK_BACK = "book_back";

    /**
     * 更新，传日期，服务端会根据上传的日期对比CreateTime,BorrowTime,BackTime,大于上传值的数据都会下传
     */
    public static final String BOOK_UPDATE = "book_update";

    /**
     * APP更新
     */
    public static final String APP_UPDATE = "app_update";

    /**
     * 用户反馈
     */
    public static final String USER_FEEDBACK = "user_feedback";
}

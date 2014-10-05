package book4best.android.best.com.book4best.model;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by bl02637
 * DESCRIPTION: 用户表
 * DATE: 2014/9/22
 * TIME: 16:24
 */
public class User {
    public User() {
        //need by ormlite
    }

    @DatabaseField(generatedId = true)
    public long ID;

    @DatabaseField
    public String UserName;

    @DatabaseField
    public String SystemType;

    @DatabaseField
    public String SystemVersion;

    /**
     * 用户登记时间（也就是用户进入系统的时间）
     */
    @DatabaseField
    public long CreateTime;

}

package book4best.android.best.com.book4best.model;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by bl02637
 * DESCRIPTION:
 * DATE: 2014/10/3
 * TIME: 10:36
 */
public class Book {
    public Book() {
        //need by ormlite
    }

    @DatabaseField(generatedId = true)
    public long ID;

    @DatabaseField
    public String Author;

    @DatabaseField
    public String Title;

    @DatabaseField
    public String Subtitle;

    @DatabaseField
    public String Translator;

    @DatabaseField
    public String PubDate;

    @DatabaseField
    public String Publisher;

    @DatabaseField
    public String Image;

    @DatabaseField
    public String ISBN;

    @DatabaseField
    public String Donater;

    @DatabaseField
    public String Borrower;

    @DatabaseField
    public int BookStatus;

    @DatabaseField
    public long CreateTime;

    @DatabaseField
    public long BorrowTime;

    @DatabaseField
    public long BackTime;
}

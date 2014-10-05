package book4best

import (
	"database/sql"
	"encoding/json"
	"fmt"
	_ "github.com/mattn/go-sqlite3"
	"io/ioutil"
	"net/http"
	//"strconv"
	"time"
)

const (
	BOOKIN = iota
	BOOKOUT
	BOOKReserve
)

type User struct {
	UserName       string
	SystemType     string
	SysteemVersion string
	CreateTime     int64
}

type Book struct {
	Author     string
	Title      string
	Subtitle   string
	Translator string
	PubDate    string
	Publisher  string
	Image      string
	ISBN       string
	Donater    string
	Borrower   string
	BookStatus int
	CreateTime int64
	BorrowTime int64
	BackTime   int64
}

type DouBanBook struct {
	Author     []string
	Subtitle   string
	PubDate    string
	Image      string
	Translator []string
	Title      string
	Publisher  string
}

type BookRequest struct {
	UserName   string
	RequestMap map[string]string
}

type BookResponse struct {
	ResponseMap map[string]string
}

var db *sql.DB

func init() {
	db, _ = sql.Open("sqlite3", "bookmanager.db")
}

func checkErr(err error) {
	if err != nil {
		panic(err)
	}
}

func insertUser(user User) bool {
	var systemType string
	err := db.QueryRow("SELECT SystemType FROM user WHERE UserName = ?", user.UserName).Scan(&systemType)

	if err == sql.ErrNoRows {
		//插入数据
		stmt, error := db.Prepare("INSERT INTO user(UserName, SystemType, SystemVersion, CreateTime) values(?,?,?,?)")
		checkErr(error)

		_, error = stmt.Exec(user.UserName, user.SystemType, user.SysteemVersion, user.CreateTime)
		checkErr(error)

		return true
	} else {
		checkErr(err)

		return false
	}

}

//容许插入同一个ISBN的书籍，但是捐书人不同（先不考虑同ISBN，同捐书人的情况）

func insertBook(isbn, donater string) Book {
	var title string
	err := db.QueryRow("SELECT Title FROM book WHERE ISBN = ? AND DONATER = ?", isbn, donater).Scan(&title)

	if err == sql.ErrNoRows {

		response, _ := http.Get("https://api.douban.com/v2/book/isbn/:" + isbn)
		defer response.Body.Close()

		body, _ := ioutil.ReadAll(response.Body)

		var doubanbook DouBanBook
		err := json.Unmarshal(body, &doubanbook)
		checkErr(err)

		var book Book
		for _, value := range doubanbook.Author {
			book.Author += value
		}

		for index, value := range doubanbook.Translator {
			if index == len(doubanbook.Translator)-1 {
				book.Translator += value
			} else {
				book.Translator += value + ", "
			}
		}
		book.Title = doubanbook.Title
		book.Subtitle = doubanbook.Subtitle
		book.PubDate = doubanbook.PubDate
		book.Publisher = doubanbook.Publisher
		book.Image = doubanbook.Image
		book.ISBN = isbn
		book.Donater = donater
		book.CreateTime = time.Now().UnixNano()
		book.BookStatus = BOOKIN
		insertBookHistory(book)

		//插入数据
		stmt, err := db.Prepare("INSERT INTO book(Author, Title, Subtitle, Translator, PubDate, Publisher, Image, ISBN, Donater, Borrower, BookStatus, CreateTime, BorrowTime, BackTime) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
		checkErr(err)

		_, err = stmt.Exec(book.Author, book.Title, book.Subtitle, book.Translator, book.PubDate, book.Publisher, book.Image, book.ISBN, book.Donater, book.Borrower, book.BookStatus, book.CreateTime, book.BorrowTime, book.BackTime)
		checkErr(err)

		return book
	} else {
		var book Book
		book.Author = donater
		return book
	}
}

func insertBookHistory(book Book) {

	//插入数据
	stmt, err := db.Prepare("INSERT INTO bookhistory(Author, Title, Subtitle, Translator, PubDate, Publisher, Image, ISBN, Donater, Borrower, BookStatus, CreateTime, BorrowTime, BackTime) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
	checkErr(err)

	result, err := stmt.Exec(book.Author, book.Title, book.Subtitle, book.Translator, book.PubDate, book.Publisher, book.Image, book.ISBN, book.Donater, book.Borrower, book.BookStatus, book.CreateTime, book.BorrowTime, book.BackTime)
	checkErr(err)

	lastid, err := result.LastInsertId()
	fmt.Println("lastid:", lastid)
	rowid, err := result.RowsAffected()
	fmt.Println("rowid:", rowid)
}

func updateBook(book Book) bool {
	insertBookHistory(book)

	//插入数据
	stmt, err := db.Prepare("UPDATE book set BookStatus=?, Borrower=?, BorrowTime=?, BackTime=? where ISBN=? AND Donater=?")
	checkErr(err)

	_, err = stmt.Exec(book.BookStatus, book.Borrower, book.BorrowTime, book.BackTime, book.ISBN, book.Donater)
	if err == nil {
		return true
	} else {
		return false
	}
}

func updateAllBook() []Book {
	//selecttime, err := strconv.ParseInt(value, 10, 64)
	//rows, err := db.Query("SELECT * FROM book WHERE BackTime > ? OR BorrowTime > ? OR CreateTime", selecttime, selecttime, selecttime)
	rows, err := db.Query("SELECT * FROM book ")
	defer rows.Close()

	var books []Book

	var Author string
	var Title string
	var Subtitle string
	var Translator string
	var PubDate string
	var Publisher string
	var Image string
	var ISBN string
	var Donater string
	var Borrower string
	var BookStatus int
	var CreateTime int64
	var BorrowTime int64
	var BackTime int64

	for rows.Next() {

		err = rows.Scan(&Author, &Title, &Subtitle, &Translator, &PubDate, &Publisher, &Image, &ISBN, &Donater, &Borrower, &BookStatus, &CreateTime, &BorrowTime, &BackTime)
		checkErr(err)

		var book Book
		book.Author = Author
		book.Title = Title
		book.Subtitle = Subtitle
		book.Translator = Translator
		book.PubDate = PubDate
		book.Publisher = Publisher
		book.Image = Image
		book.ISBN = ISBN
		book.Donater = Donater
		book.Borrower = Borrower
		book.BookStatus = BookStatus
		book.CreateTime = CreateTime
		book.BorrowTime = BorrowTime
		book.BackTime = BackTime

		books = append(books, book)

	}

	return books
}
func Login(value string) bool {
	var user User
	err := json.Unmarshal([]byte(value), &user)
	checkErr(err)

	return insertUser(user)
}

func BorrowBook(value string) bool {
	fmt.Println("borrowbook", string(value))
	var book Book
	err := json.Unmarshal([]byte(value), &book)
	checkErr(err)

	return updateBook(book)
}

func BackBook(value string) bool {
	fmt.Println("backbook", string(value))
	var book Book
	err := json.Unmarshal([]byte(value), &book)
	checkErr(err)

	return updateBook(book)
}

func DonateBook(value, donater string) Book {

	return insertBook(value, donater)
}

func SyncBook() []Book {

	return updateAllBook()
}

package book4best

import (
	"encoding/json"
	"fmt"
	"net/http"
	"strings"
)

const (
	version = 1
)

func Handleappupdate(w http.ResponseWriter, bookrequest BookRequest) {
	var response BookResponse
	result := bookrequest.RequestMap["app_update"]
	if strings.Contains(result, "com.best.android.book4best") {
		if version > int(result[1]) {
			response.ResponseMap = map[string]string{
				"app_update": "\\download\\bookk4best-v2.apk",
			}

		} else {
			response.ResponseMap = map[string]string{
				"app_update": "当前版本已经是最新版本",
			}
		}

		str, err := json.Marshal(response)
		checkErr(err)

		fmt.Fprint(w, string(str))
	} else {
		fmt.Fprint(w, "错误的请求地址")
	}
}

func Handleuserlogin(w http.ResponseWriter, bookrequest BookRequest) {
	var isok bool
	isok = Login(bookrequest.RequestMap["user_login"])
	result, err := json.Marshal(isok)
	checkErr(err)

	fmt.Fprint(w, string(result))
}

func Handlebookborrow(w http.ResponseWriter, bookrequest BookRequest) {
	var isok bool
	isok = BorrowBook(bookrequest.RequestMap["book_borrow"])
	result, err := json.Marshal(isok)
	checkErr(err)

	fmt.Fprint(w, string(result))
}

func Handlebookdonate(w http.ResponseWriter, bookrequest BookRequest) {

	book := DonateBook(bookrequest.RequestMap["book_donate"], bookrequest.UserName)
	result, err := json.Marshal(book)
	checkErr(err)
	fmt.Println("result:", string(result))

	fmt.Fprint(w, string(result))
}

func Handlebookback(w http.ResponseWriter, bookrequest BookRequest) {
	var isok bool
	isok = BackBook(bookrequest.RequestMap["book_back"])
	result, err := json.Marshal(isok)
	checkErr(err)

	fmt.Fprint(w, string(result))
}

func Handlebookupdate(w http.ResponseWriter, bookrequest BookRequest) {
	books := SyncBook()

	result, err := json.Marshal(books)
	checkErr(err)
	fmt.Fprint(w, string(result))
}

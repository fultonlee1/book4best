package main

import "net/http"
import "log"
import "encoding/json"
import "fmt"
import "io/ioutil"
import "book4best"

func checkErr(err error) {
	if err != nil {
		panic(err)
	}
}

func HandleBookManager(w http.ResponseWriter, r *http.Request) {
	r.ParseForm() //解析参数，默认是不会解析的

	fmt.Println("head", r.Header)

	body, err := ioutil.ReadAll(r.Body)
	checkErr(err)

	var bookrequest book4best.BookRequest
	err = json.Unmarshal(body, &bookrequest)
	checkErr(err)

	fmt.Println(bookrequest)

	for key, _ := range bookrequest.RequestMap {
		switch key {
		case "app_update":
			book4best.Handleappupdate(w, bookrequest)
		case "book_back":
			book4best.Handlebookback(w, bookrequest)
		case "book_borrow":
			book4best.Handlebookborrow(w, bookrequest)
		case "book_donate":
			book4best.Handlebookdonate(w, bookrequest)
		case "book_update":
			book4best.Handlebookupdate(w, bookrequest)
		case "user_feedback":
			//book4best.Handlefeedback(w, bookrequest)
		case "user_login":
			book4best.Handleuserlogin(w, bookrequest)
		}
	}

}

//func init() {
//	runtime.GOMAXPROCS(runtime.NumCPU())

//}
func main() {
	http.HandleFunc("/", HandleBookManager)
	err := http.ListenAndServe(":9090", nil)

	if err != nil {
		log.Fatal("ListenAndServer err: ", err)
	}
}

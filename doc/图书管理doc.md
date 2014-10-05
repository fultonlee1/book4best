#图书管理系统文档

##android端
###功能说明
1. 查询功能：查找书籍，并显示详情，如果书籍未借出，则提供“借书”功能；如果已经借出，则要显示出谁借了
2. 捐书功能：通过输入或者扫描ISBN码，向服务端发送查询功能，服务端返回此ISBN书的详细内容，包括书名，子标题，作者，出版社，封面
3. 还书功能：通过输入或者扫描ISBN码，完成还书功能
4. 反馈功能：供用户报bug，报各种不爽的途径--服务端提供发邮件的功能
5. 设置功能：后续服务端有可能切换，给用户提供切换服务端地址的便利
6. 更新功能：app版本升级

###界面说明
1. 抽屉式，可参考知乎android客户端
2. 看进度，如果时间允许，引入zxing扫描
3. 看进度，如果时间允许，引入讯飞的语音


###框架选用
1. network:volley
2. json:jackson或者Gson都行
3. orm:ormlite
4. annotation:androidannotations
5. color:Colours颜色框架


###建议使用
1. eventbus组件
2. 线程池管理


##go端
1. 第一版能用就行，martini框架的学习成本不低，暂时放弃
2. 捐书，借书，还书的行为都会记录
3. request的header会记录
###后续版本考虑：
1. token的引入
2. session的引入
3. websocket的引入
4. socketio的引入
5. auth的引入
6. martini框架的引入


##android端与服务端通信的type:发送和返回都是Json格式
1. 用户第一次登录的时候：requesttypye:user_login	requestcontent:User对象	response:成功返回true，失败返回false，说明数据库中已经存在该用户名
2. 用户捐书：requesttype:book_donate	requestcontent:字符串ISBN	response:返回Book对象
3. 用户借书：requesttype:book_borrow	requestcontent:Book对象	response:成功返回true,失败返回false
4. 用户还书：requesttype:book_back	requestcontent:Book对象	response:成功返回true,失败返回false
5. 更新书籍：requesttype:book_update	requestcontent:最新时间（Book对象中CreateTime,BorrowTime,BackTime中最大值）	response:返回Book对象list
6. app更新：requesttype:app_update requestcontent:app的包名+versionCode，格式为："com.best.android.book4best-1" response:如果有，返回下载app的地址；反之，则返回字符串“当前版本已经是最新版本”;都是字符串
7. 用户反馈：requesttype：user_feedback requestcontent:输入内容的字符串 response:成功返回true，失败返回false

##model
###User:
1. UserName string
2. SystemType string
3. SystemVersion string (example:“10，2.3.3”)
4. CreateTime long
###Book:
1. Author 	string
2. Title	string(书名)
2. Subtitle string(副标题)
3. Translator string(翻译人)
4. PubDate	string(出版日期)
5. Publisher	string(出版社)
6. Image	string(书本封面的豆瓣地址)
7. ISBN		string
8. Donater	string(捐书人)
9. Borrower string(借书人)
10. CreateTime	long(捐书时间)
11. BorrowTime	long(借书时间)
12. BackTime	long(还书时间)

##使用说明
1. 一个用户一个账户，且只限一台手机
2. 每个用户只能借一本书
package book4best.android.best.com.book4best.model;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import java.io.UnsupportedEncodingException;

/**
 * Created by bl02637
 * DESCRIPTION: 公共request请求类
 * DATE: 2014/10/3
 * TIME: 10:42
 */
public class BookJsonRequest extends JsonRequest<String> {

    public BookJsonRequest(int method, String url, String requestBody, Response.Listener<String> listener,
                           Response.ErrorListener errorListener) {
       super(method, url, requestBody, listener, errorListener);
    }

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
}

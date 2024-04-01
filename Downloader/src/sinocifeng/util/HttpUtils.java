package sinocifeng.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {


    // 获取下载文件大小
    public static long getHttpContentLength(String url) throws IOException {
        int contentLength;
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = getHttpURLConnection(url);
            contentLength = httpURLConnection.getContentLength();
        } finally {
            if (httpURLConnection != null){
                httpURLConnection.disconnect();
            }
        }
        return contentLength;
    }

    // 分块下载
    public static HttpURLConnection getHttpURLConnection(String url, long startPos, long endPos) throws IOException {
        HttpURLConnection httpUrlConnection = getHttpURLConnection(url);
        LogUtils.info("下载区间为 {} - {}", startPos, endPos);
        if (endPos != 0) {
            httpUrlConnection.setRequestProperty("RANGE", "bytes=" + startPos + "-" + endPos);
        }else {
            httpUrlConnection.setRequestProperty("RANGE", "bytes=" + startPos + "-" );
        }
        return httpUrlConnection;
    }


    //获取HttpURLConnection对象
    public static HttpURLConnection getHttpURLConnection(String url) throws IOException {
        URL httpUrl = new URL(url);
        HttpURLConnection httpUrlConnection = (HttpURLConnection)httpUrl.openConnection();
        // 文件服务器发送标识信息
        String label = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko)Chrome/14.0.835.163 Safari/535.147";
        httpUrlConnection.setRequestProperty("User-Agent", label);

        return httpUrlConnection;
    }


    // 获取文件名
    public static String getHttpFileName(String url){
        int index = url.lastIndexOf("/");
        return url.substring(index + 1);
    }


}

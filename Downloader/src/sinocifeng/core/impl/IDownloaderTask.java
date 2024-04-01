package sinocifeng.core.impl;

import sinocifeng.constant.Constant;
import sinocifeng.core.DownloaderTask;
import sinocifeng.util.HttpUtils;
import sinocifeng.util.LogUtils;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

// 分块下载任务
public class IDownloaderTask implements Callable, DownloaderTask {

    private String url;

    private long startPos;

    private long endPos;

    // 标识当前为第几块
    private int part;

    private CountDownLatch countDownLatch;

    public IDownloaderTask(String url, long startPos, long endPos, int part, CountDownLatch countDownLatch) {
        super();
        this.url = url;
        this.startPos = startPos;
        this.endPos = endPos;
        this.part = part;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public Object call() throws Exception {
        String httpFileName = HttpUtils.getHttpFileName(url);
        // 分块的文件名
        httpFileName = httpFileName + ".temp" + part;
        // 下载路径
        httpFileName = Constant.PATH + httpFileName;
        // 获取分块下载链接
        HttpURLConnection httpURLConnection = HttpUtils.getHttpURLConnection(url, startPos, endPos);

        try (
                InputStream input = httpURLConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(input);
                RandomAccessFile accessFile = new RandomAccessFile(httpFileName, "rw");
            ){
            byte[] buffer = new byte[Constant.BYTE_SIZE];
            int len = -1;
            // 循环读取数据
            while ((len = bis.read(buffer)) != -1){
                // 累加一秒内下载数据之和,通过原子类操作
                IDownloaderInfo.downSize.add(len);
                accessFile.write(buffer, 0, len);
            }


        } catch (FileNotFoundException e){
            LogUtils.error("下载文件{}不存在", url);
            return false;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        } finally {
            httpURLConnection.disconnect();
            countDownLatch.countDown();
        }


        return true;
    }
}

package sinocifeng.core.impl;

import sinocifeng.constant.Constant;
import sinocifeng.core.Downloader;
import sinocifeng.util.FileUtils;
import sinocifeng.util.HttpUtils;
import sinocifeng.util.LogUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.concurrent.*;

public class IDownloader implements Downloader {

    private ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
    // 线程池对象
    private ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(Constant.THREAD_NUM, Constant.THREAD_NUM, 0, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(Constant.THREAD_NUM));

    private CountDownLatch countLatch = new CountDownLatch(Constant.THREAD_NUM);

    public void download(String url){
        // 获取文件名
        String httpFileName = HttpUtils.getHttpFileName(url);
        // 文件本地下载路径
        httpFileName = Constant.PATH + httpFileName;
        // 获取本地文件大小
        long localFileLength = FileUtils.getFileContentLength(httpFileName);

        // 获取连接对象
        HttpURLConnection httpURLConnection = null;
        // 创建获取下载信息的任务对象
        IDownloaderInfo downloaderInfoThread = null;
        try {
            httpURLConnection = HttpUtils.getHttpURLConnection(url);
            //获取下载文件总大小
            int contentLength = httpURLConnection.getContentLength();
            // 判断文件是否已经下载过
            if (localFileLength >= contentLength){
                LogUtils.info("{}已经下载完毕，无需重新下载", httpFileName);
                return;
            }

            downloaderInfoThread = new IDownloaderInfo(contentLength);
            // 将任务交给线程每隔一秒执行一次
            service.scheduleAtFixedRate(downloaderInfoThread,1, 1, TimeUnit.SECONDS);
            // 切分任务
            ArrayList<Future> list = new ArrayList<>();
            split(url, list);

            countLatch.await();

            // 合并文件、清除临时文件
            if ( merge(httpFileName) ){
                clearTempFiles(httpFileName);
            }



        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.print("\r");
            System.out.println("下载完成");
            if(httpURLConnection != null){
                httpURLConnection.disconnect();
            }
            // 关闭线程池
            service.shutdownNow();
            poolExecutor.shutdown();
        }
    }


    // 文件切分
    private void split(String url, ArrayList<Future> futureArrayList){
        // 获取下载文件大小
        try {
            long contentLength = HttpUtils.getHttpContentLength(url);

            //计算切分后文件大小
            long size = contentLength / Constant.THREAD_NUM;

            //计算分块个数
            for (int i = 0; i < Constant.THREAD_NUM; i++) {
                // 计算下载起始位置
                long startPos = i * size;
                long endPos;
                if (i == Constant.THREAD_NUM - 1){
                    endPos = 0;
                }else {
                    endPos = startPos + size;
                }
                // 如果不是第一块，起始位置加一
                if (startPos != 0){
                    startPos++;
                }
                // 创建任务
                IDownloaderTask downloaderTask = new IDownloaderTask(url, startPos, endPos, i, countLatch);
                // 提交线程池
                Future<Boolean> future = poolExecutor.submit(downloaderTask);
                futureArrayList.add(future);
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // 文件合并
    private boolean merge(String fileName){
        LogUtils.info("开始合并文件{}", fileName);
        byte[] buffer = new byte[Constant.BYTE_SIZE];
        int len = -1;
        try (
                RandomAccessFile accessFile = new RandomAccessFile(fileName, "rw")
                ){
            for (int i = 0; i < Constant.THREAD_NUM; i++) {
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName + ".temp" + i))){
                    while ((len = bis.read(buffer)) != -1 ){
                        accessFile.write(buffer, 0, len);
                    }

                }

            }
            LogUtils.info("文件{}合并完成", fileName);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //临时文件清除
    private boolean clearTempFiles(String fileName){
        for (int i = 0; i < Constant.THREAD_NUM; i++) {
            File removeFile = new File(fileName + ".temp" + i);
            removeFile.delete();
        }
        return true;
    }

}

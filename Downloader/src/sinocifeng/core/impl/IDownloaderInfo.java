package sinocifeng.core.impl;

import sinocifeng.constant.Constant;
import sinocifeng.core.DownloaderInfo;

import java.util.concurrent.atomic.LongAdder;

public class IDownloaderInfo implements Runnable, DownloaderInfo {

    //下载文件总大小
    private long httpFileContentLength;

    //本地已下载文件的大小
    public static LongAdder finishedSize = new LongAdder();

    //本次下载的大小
    public static volatile LongAdder downSize = new LongAdder();

    //前一次累计下载的大小
    public double prevSize;

    public IDownloaderInfo(long httpFileContentLength) {
        super();
        this.httpFileContentLength = httpFileContentLength;
    }

    @Override
    public void run() {
        // 计算文件总大小
        String httpFileSize = String.format("%.2f",httpFileContentLength / Constant.MB);
        // 计算每秒下载速度
        int speed = (int) ((downSize.doubleValue() - prevSize) / 1024d);
        prevSize = downSize.doubleValue();
        
        // 剩余文件大小
        double remainSize = httpFileContentLength - finishedSize.doubleValue() - downSize.doubleValue();
        
        // 计算剩余时间
        String remainTime = String.format("%.1f", remainSize / 1024d / speed);
        if ("Infinity".equalsIgnoreCase(remainTime)){
            remainTime = " - ";
        }

        // 已下载大小
        String currentFileSize = String.format("%.2f", (downSize.doubleValue() - finishedSize.doubleValue()) / Constant.MB);

        String downInfo = String.format("已下载 %s MB / %s MB, 速度：%s KB/s，剩余时间 %s s",
                currentFileSize, httpFileSize, speed, remainTime);

        System.out.print("\r");
        System.out.print(downInfo);

    }
}

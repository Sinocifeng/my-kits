import sinocifeng.core.Downloader;
import sinocifeng.core.impl.IDownloader;
import sinocifeng.util.LogUtils;

public class Main {

    public static void main(String[] args) {

        String url = "https://dldir1.qq.com/qqfile/qq/QQNT/Windows/QQ_9.9.8_240325_x64_01.exe";
        Downloader downloader = new IDownloader();
        downloader.download(url);

        LogUtils.info("下载{}完成",url);
    }
}
package sinocifeng.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LogUtils {

    public static void info(String message, Object... args){
        print(message, "-info-", args);
    }

    public static void error(String message, Object... args){
        print(message, "-error-", args);
    }

    private static void print(String message, String level, Object... args){
        if (args != null && args.length >0){
            message = String.format(message.replace("{}","%s"), args);
        }

        String name = Thread.currentThread().getName();
        System.out.println(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"))
                + " " + name + " " + level + " " + message);
    }

}

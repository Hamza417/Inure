package app.simple.inure.shizuku;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {
    public static String throwableToString(Throwable throwable) {
        StringWriter sw = new StringWriter(1024);
        PrintWriter pw = new PrintWriter(sw);
        
        throwable.printStackTrace(pw);
        pw.close();
        
        return sw.toString();
    }
}

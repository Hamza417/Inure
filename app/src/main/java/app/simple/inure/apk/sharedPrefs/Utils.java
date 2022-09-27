package app.simple.inure.apk.sharedPrefs;

public class Utils {
    public static final String CMD_FIND_XML_FILES = "find /data/data/%s -type f -name \\*.xml";
    public static final String CMD_CHOWN = "chown %s.%s \"%s\"";
    public static final String CMD_CAT_FILE = "cat \"%s\"";
    public static final String CMD_CP = "cp \"%s\" \"%s\"";
    public static final String TMP_FILE = ".temp";
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String PACKAGE_NAME_PATTERN = "^[a-zA-Z_$][\\w$]*(?:\\.[a-zA-Z_$][\\w$]*)*$";
}

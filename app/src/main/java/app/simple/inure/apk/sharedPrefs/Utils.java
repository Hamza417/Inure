package app.simple.inure.apk.sharedPrefs;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class Utils {
    
    public static final String TAG = Utils.class.getSimpleName();
    private static final String FAVORITES_KEY = "FAVORITES_KEY";
    private static final String VERSION_CODE_KEY = "VERSION_CODE";
    public static final String BACKUP_PREFIX = "BACKUP_";
    private static final String TAG_ROOT_DIALOG = "RootDialog";
    private static final String PREF_SHOW_SYSTEM_APPS = "SHOW_SYSTEM_APPS";
    public static final String CMD_FIND_XML_FILES = "find /data/data/%s -type f -name \\*.xml";
    public static final String CMD_CHOWN = "chown %s.%s \"%s\"";
    public static final String CMD_CAT_FILE = "cat \"%s\"";
    public static final String CMD_CP = "cp \"%s\" \"%s\"";
    public static final String TMP_FILE = ".temp";
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String PACKAGE_NAME_PATTERN = "^[a-zA-Z_$][\\w$]*(?:\\.[a-zA-Z_$][\\w$]*)*$";
    
    public static List <String> findXmlFiles(final String packageName) throws Shell.ShellDiedException {
        Log.d(TAG, String.format("findXmlFiles(%s)", packageName));
        List <String> files = Shell.Pool.SU.run(String.format(CMD_FIND_XML_FILES, packageName));
        Log.d(TAG, "files: " + Arrays.toString(files.toArray()));
        return files;
    }
}

package app.simple.inure.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.regex.Pattern;

import androidx.core.content.ContextCompat;

public class SDCard {
    private static final String TAG = "SDCard";
    
    /**
     * In some scenarios we can expect to find a specified file or folder on SD cards designed
     * to work with this app. If so, set KNOWNFILE to that filename. It will make our job easier.
     * Set it to null otherwise.
     */
    private static final String KNOWNFILE = null;
    
    /**
     * Common paths for microSD card.
     **/
    @SuppressLint ("SdCardPath")
    private static final String[] commonPaths = {
            // Some of these taken from
            // https://stackoverflow.com/questions/13976982/removable-storage-external-sdcard-path-by-manufacturers
            // These are roughly in order such that the earlier ones, if they exist, are more sure
            // to be removable storage than the later ones.
            "/mnt/Removable/MicroSD",
            "/storage/removable/sdcard1", // !< Sony Xperia Z1
            "/Removable/MicroSD", // Asus ZenPad C
            "/removable/microsd",
            "/external_sd", // Samsung
            "/_ExternalSD", // some LGs
            "/storage/extSdCard", // later Samsung
            "/storage/extsdcard", // Main filesystem is case-sensitive; FAT isn't.
            "/mnt/extsd", // some Chinese tablets, e.g. Zeki
            "/storage/sdcard1", // If this exists it's more likely than sdcard0 to be removable.
            "/mnt/extSdCard",
            "/mnt/sdcard/external_sd",
            "/mnt/external_sd",
            "/storage/external_SD",
            "/storage/ext_sd", // HTC One Max
            "/mnt/sdcard/_ExternalSD",
            "/mnt/sdcard-ext",
            
            "/sdcard2", // HTC One M8s
            "/sdcard1", // Sony Xperia Z
            "/mnt/media_rw/sdcard1",   // 4.4.2 on CyanogenMod S3
            "/mnt/sdcard", // This can be built-in storage (non-removable).
            "/sdcard",
            "/storage/sdcard0",
            "/emmc",
            "/mnt/emmc",
            "/sdcard/sd",
            "/mnt/sdcard/bpemmctest",
            "/mnt/external1",
            "/data/sdext4",
            "/data/sdext3",
            "/data/sdext2",
            "/data/sdext",
            "/storage/microsd" //ASUS ZenFone 2
            
            // If we ever decide to support USB OTG storage, the following paths could be helpful:
            // An LG Nexus 5 apparently uses usb://1002/UsbStorage/ as a URI to access an SD
            // card over OTG cable. Other models, like Galaxy S5, use /storage/UsbDriveA
            //        "/mnt/usb_storage",
            //        "/mnt/UsbDriveA",
            //        "/mnt/UsbDriveB",
    };
    private static final String ANDROID_DIR = File.separator + "Android";
    /* Pattern that SD card device should match */
    private static final Pattern devicePattern = Pattern.compile("/dev/(block/.*vold.*|fuse)|/mnt/.*");
    /**
     * Pattern that SD card mount path should match
     */
    private static final Pattern
            pathPattern = Pattern.compile("/(mnt|storage|external_sd|extsd|_ExternalSD|Removable|.*MicroSD).*",
            Pattern.CASE_INSENSITIVE);
    /**
     * Pattern that the mount path should not match.
     * 'emulated' indicates an internal storage location, so skip it.
     * 'asec' is an encrypted package file, decrypted and mounted as a directory.
     */
    private static final Pattern pathAntiPattern = Pattern.compile(".*(/secure|/asec|/emulated).*");
    /*
     * These are expected fs types, including vfat. tmpfs is not OK.
     * fuse can be removable SD card (as on Moto E or Asus ZenPad), or can be internal (Huawei G610).
     */
    private static final Pattern fsTypePattern = Pattern.compile(".*(fat|msdos|ntfs|ext[34]|fuse|sdcard|esdfs).*");
    
    /**
     * Find path to removable SD card.
     */
    public static File findSdCardPath(Context context) {
        String[] mountFields;
        BufferedReader bufferedReader = null;
        String lineRead = null;
        
        /* Possible SD card paths */
        LinkedHashSet <File> candidatePaths = new LinkedHashSet <>();
        
        /* Build a list of candidate paths, roughly in order of preference. That way if
         * we can't definitively detect removable storage, we at least can pick a more likely
         * candidate. */
        
        // Could do: use getExternalStorageState(File path), with and without an argument, when
        // available. With an argument is available since API level 21.
        // This may not be necessary, since we also check whether a directory exists and has contents,
        // which would fail if the external storage state is neither MOUNTED nor MOUNTED_READ_ONLY.
        
        // I moved hard-coded paths toward the end, but we need to make sure we put the ones in
        // backwards order that are returned by the OS. And make sure the iterators respect
        // the order!
        // This is because when multiple "external" storage paths are returned, it's always (in
        // experience, but not guaranteed by documentation) with internal/emulated storage
        // first, removable storage second.
        
        // Add value of environment variables as candidates, if set:
        // EXTERNAL_STORAGE, SECONDARY_STORAGE, EXTERNAL_SDCARD_STORAGE
        // But note they are *not* necessarily *removable* storage! Especially EXTERNAL_STORAGE.
        // And they are not documented (API) features. Typically useful only for old versions of Android.
        
        String val = System.getenv("SECONDARY_STORAGE");
        if (!TextUtils.isEmpty(val)) {
            addPath(val, null, candidatePaths);
        }
        val = System.getenv("EXTERNAL_SDCARD_STORAGE");
        if (!TextUtils.isEmpty(val)) {
            addPath(val, null, candidatePaths);
        }
        
        // Get listing of mounted devices with their properties.
        ArrayList <File> mountedPaths = new ArrayList <>();
        try {
            // Note: Despite restricting some access to /proc (http://stackoverflow.com/a/38728738/423105),
            // Android 7.0 does *not* block access to /proc/mounts, according to our test on George's Alcatel A30 GSM.
            bufferedReader = new BufferedReader(new FileReader("/proc/mounts"));
            
            // Iterate over each line of the mounts listing.
            while ((lineRead = bufferedReader.readLine()) != null) {
                Log.d(TAG, "\nMounts line: " + lineRead);
                mountFields = lineRead.split(" ");
                
                // columns: device, mountpoint, fs type, options... Example:
                // /dev/block/vold/179:97 /storage/sdcard1 vfat rw,dirsync,nosuid,nodev,noexec,relatime,uid=1000,gid=1015,fmask=0002,dmask=0002,allow_utime=0020,codepage=cp437,iocharset=iso8859-1,shortname=mixed,utf8,errors=remount-ro 0 0
                String device = mountFields[0], path = mountFields[1], fsType = mountFields[2];
                
                // The device, path, and fs type must conform to expected patterns.
                if (!(devicePattern.matcher(device).matches() &&
                        pathPattern.matcher(path).matches() &&
                        fsTypePattern.matcher(fsType).matches()) ||
                        // mtdblock is internal, I'm told.
                        device.contains("mtdblock") ||
                        // Check for disqualifying patterns in the path.
                        pathAntiPattern.matcher(path).matches()) {
                    // If this mounts line fails our tests, skip it.
                    continue;
                }
                
                // TODO maybe: check options to make sure it's mounted RW?
                // The answer at http://stackoverflow.com/a/13648873/423105 does.
                // But it hasn't seemed to be necessary so far in my testing.
                
                // This line met the criteria so far, so add it to candidate list.
                addPath(path, null, mountedPaths);
            }
        } catch (IOException ignored) {
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ignored) {
                }
            }
        }
        
        // Append the paths from mount table to candidate list, in reverse order.
        if (!mountedPaths.isEmpty()) {
            // See https://stackoverflow.com/a/5374346/423105 on why the following is necessary.
            // Basically, .toArray() needs its parameter to know what type of array to return.
            File[] mountedPathsArray = mountedPaths.toArray(new File[mountedPaths.size()]);
            addAncestors(candidatePaths, mountedPathsArray);
        }
        
        // Add hard-coded known common paths to candidate list:
        addStrings(candidatePaths, commonPaths);
        
        // If the above doesn't work we could try the following other options, but in my experience they
        // haven't added anything helpful yet.
        
        // getExternalFilesDir() and getExternalStorageDirectory() typically something app-specific like
        //   /storage/sdcard1/Android/data/com.mybackuparchives.android/files
        // so we want the great-great-grandparent folder.
        
        // This may be non-removable.
        Log.d(TAG, "Environment.getExternalStorageDirectory():");
        addPath(null, ancestor(Environment.getExternalStorageDirectory()), candidatePaths);
        
        // Context.getExternalFilesDirs() is only available from API level 19. You can use
        // ContextCompat.getExternalFilesDirs() on earlier APIs, but it only returns one dir anyway.
        Log.d(TAG, "context.getExternalFilesDir(null):");
        addPath(null, ancestor(context.getExternalFilesDir(null)), candidatePaths);
        
        // "Returns absolute paths to application-specific directories on all external storage
        // devices where the application can place persistent files it owns."
        // We might be able to use these to deduce a higher-level folder that isn't app-specific.
        // Also, we apparently have to call getExternalFilesDir[s](), at least in KITKAT+, in order to ensure that the
        // "external files" directory exists and is available.
        Log.d(TAG, "ContextCompat.getExternalFilesDirs(context, null):");
        addAncestors(candidatePaths, ContextCompat.getExternalFilesDirs(context, null));
        // Very similar results:
        Log.d(TAG, "ContextCompat.getExternalCacheDirs(context):");
        addAncestors(candidatePaths, ContextCompat.getExternalCacheDirs(context));
        
        // TODO maybe: use getExternalStorageState(File path), with and without an argument, when
        // available. With an argument is available since API level 21.
        // This may not be necessary, since we also check whether a directory exists,
        // which would fail if the external storage state is neither MOUNTED nor MOUNTED_READ_ONLY.
        
        // A "public" external storage directory. But in my experience it doesn't add anything helpful.
        // Note that you can't pass null, or you'll get an NPE.
        final File publicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        // Take the parent, because we tend to get a path like /pathTo/sdCard/Music.
        addPath(null, publicDirectory.getParentFile(), candidatePaths);
        // EXTERNAL_STORAGE: may not be removable.
        val = System.getenv("EXTERNAL_STORAGE");
        if (!TextUtils.isEmpty(val)) {
            addPath(val, null, candidatePaths);
        }
        
        if (candidatePaths.isEmpty()) {
            Log.w(TAG, "No removable microSD card found.");
            return null;
        } else {
            Log.i(TAG, "\nFound potential removable storage locations: " + candidatePaths);
        }
        
        // Accept or eliminate candidate paths if we can determine whether they're removable storage.
        // In Lollipop and later, we can check isExternalStorageRemovable() status on each candidate.
        Iterator <File> itf = candidatePaths.iterator();
        while (itf.hasNext()) {
            File dir = itf.next();
            // handle illegalArgumentException if the path is not a valid storage device.
            try {
                if (Environment.isExternalStorageRemovable(dir)
                    // && containsKnownFile(dir)
                ) {
                    Log.i(TAG, dir.getPath() + " is removable external storage");
                    return dir;
                } else if (Environment.isExternalStorageEmulated(dir)) {
                    Log.d(TAG, "Removing emulated external storage dir " + dir);
                    itf.remove();
                }
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "isRemovable(" + dir.getPath() + "): not a valid storage device.", e);
            }
        }
        
        // Continue trying to accept or eliminate candidate paths based on whether they're removable storage.
        // On pre-Lollipop, we only have singular externalStorage. Check whether it's removable.
        File externalStorage = Environment.getExternalStorageDirectory();
        Log.d(TAG, String.format(Locale.ROOT, "findSDCardPath: getExternalStorageDirectory = %s", externalStorage.getPath()));
        if (Environment.isExternalStorageRemovable()) {
            // Make sure this is a candidate.
            // TODO: Does this contains() work? Should we be canonicalizing paths before comparing?
            if (candidatePaths.contains(externalStorage)
                // && containsKnownFile(externalStorage)
            ) {
                Log.d(TAG, "Using externalStorage dir " + externalStorage);
                return externalStorage;
            }
        } else if (Environment.isExternalStorageEmulated()) {
            Log.d(TAG, "Removing emulated external storage dir " + externalStorage);
            candidatePaths.remove(externalStorage);
        }
        
        // If any directory contains our special test file, consider that the microSD card.
        if (KNOWNFILE != null) {
            for (File dir : candidatePaths) {
                Log.d(TAG, String.format(Locale.ROOT, "findSdCardPath: Looking for known file in candidate path, %s", dir));
                if (containsKnownFile(dir)) {
                    return dir;
                }
            }
        }
        
        // If we don't find the known file, still try taking the first candidate.
        if (!candidatePaths.isEmpty()) {
            Log.d(TAG, "No definitive path to SD card; taking the first realistic candidate.");
            return candidatePaths.iterator().next();
        }
        
        // If no reasonable path was found, give up.
        return null;
    }
    
    /**
     * Add each path to the collection.
     */
    private static void addStrings(LinkedHashSet <File> candidatePaths, String[] newPaths) {
        for (String path : newPaths) {
            addPath(path, null, candidatePaths);
        }
    }
    
    /**
     * Add ancestor of each File to the collection.
     */
    private static void addAncestors(LinkedHashSet <File> candidatePaths, File[] files) {
        for (int i = files.length - 1; i >= 0; i--) {
            addPath(null, ancestor(files[i]), candidatePaths);
        }
    }
    
    /**
     * Add a new candidate directory path to our list, if it's not obviously wrong.
     * Supply path as either String or File object.
     *
     * @param strNew  path of directory to add (or null)
     * @param fileNew directory to add (or null)
     */
    private static void addPath(String strNew, File fileNew, Collection <File> paths) {
        // If one of the arguments is null, fill it in from the other.
        if (strNew == null) {
            if (fileNew == null) {
                return;
            }
            strNew = fileNew.getPath();
        } else if (fileNew == null) {
            fileNew = new File(strNew);
        }
        
        if (!paths.contains(fileNew) &&
                // Check for paths known not to be removable SD card.
                // The antipattern check can be redundant, depending on where this is called from.
                !pathAntiPattern.matcher(strNew).matches()) {
            
            // Eliminate candidate if not a directory or not fully accessible.
            if (fileNew.exists() && fileNew.isDirectory() && fileNew.canExecute()) {
                Log.d(TAG, "  Adding candidate path " + strNew);
                paths.add(fileNew);
            } else {
                Log.d(TAG, String.format(Locale.ROOT, "  Invalid path %s: exists: %b isDir: %b canExec: %b canRead: %b",
                        strNew, fileNew.exists(), fileNew.isDirectory(), fileNew.canExecute(), fileNew.canRead()));
            }
        }
    }
    
    private static File ancestor(File dir) {
        // getExternalFilesDir() and getExternalStorageDirectory() typically something app-specific like
        //   /storage/sdcard1/Android/data/com.mybackuparchives.android/files
        // so we want the great-great-grandparent folder.
        if (dir == null) {
            return null;
        } else {
            String path = dir.getAbsolutePath();
            int i = path.indexOf(ANDROID_DIR);
            if (i == -1) {
                return dir;
            } else {
                return new File(path.substring(0, i));
            }
        }
    }
    
    /**
     * Returns true iff dir contains the special test file.
     * Assumes that dir exists and is a directory. (Is this a necessary assumption?)
     */
    private static boolean containsKnownFile(File dir) {
        if (KNOWNFILE == null) {
            return false;
        }
        
        File knownFile = new File(dir, KNOWNFILE);
        return knownFile.exists();
    }
}
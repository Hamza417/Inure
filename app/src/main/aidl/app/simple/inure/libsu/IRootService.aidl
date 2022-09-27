// IRootService.aidl
package app.simple.inure.libsu;

// Declare any non-default types here with import statements

interface IRootService {
    int getPid();
    int getUid();
    String getUUID();
    IBinder getFileSystemService();
}
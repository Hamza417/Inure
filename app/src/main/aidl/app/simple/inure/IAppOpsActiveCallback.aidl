package app.simple.inure;

/**
 * Callback interface for AppOps active state changes.
 * This runs in the Shizuku service context with system privileges.
 */
interface IAppOpsActiveCallback {
    /**
     * Called when an app operation active state changes.
     * @param op The operation code that changed
     * @param uid The UID of the app
     * @param packageName The package name
     * @param attributionTag The attribution tag (can be null)
     * @param active Whether the operation is now active
     */
    void onOpActiveChanged(int op, int uid, String packageName, String attributionTag, boolean active);
}

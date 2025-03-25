package android.app;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArraySet;
import android.util.LongSparseArray;
import android.util.LongSparseLongArray;

import java.util.List;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import dev.rikka.tools.refine.RefineAs;

@RefineAs (AppOpsManager.class)
public class AppOpsManagerHidden {
    
    @RequiresApi (Build.VERSION_CODES.Q)
    public static String KEY_HISTORICAL_OPS;
    
    @RequiresApi (Build.VERSION_CODES.Q)
    public static int OP_FLAG_SELF;
    @RequiresApi (Build.VERSION_CODES.Q)
    public static int OP_FLAG_TRUSTED_PROXY;
    @RequiresApi (Build.VERSION_CODES.Q)
    public static int OP_FLAG_UNTRUSTED_PROXY;
    @RequiresApi (Build.VERSION_CODES.Q)
    public static int OP_FLAG_TRUSTED_PROXIED;
    @RequiresApi (Build.VERSION_CODES.Q)
    public static int OP_FLAG_UNTRUSTED_PROXIED;
    @RequiresApi (Build.VERSION_CODES.Q)
    public static int OP_FLAGS_ALL;
    
    @RequiresApi (Build.VERSION_CODES.S)
    public static int HISTORY_FLAG_AGGREGATE;
    @RequiresApi (Build.VERSION_CODES.S)
    public static int HISTORY_FLAG_DISCRETE;
    @RequiresApi (Build.VERSION_CODES.S)
    public static int HISTORY_FLAG_GET_ATTRIBUTION_CHAINS;
    @RequiresApi (Build.VERSION_CODES.S)
    public static int HISTORY_FLAGS_ALL;
    
    @RequiresApi (Build.VERSION_CODES.R)
    public static int FILTER_BY_UID;
    @RequiresApi (Build.VERSION_CODES.R)
    public static int FILTER_BY_PACKAGE_NAME;
    @RequiresApi (Build.VERSION_CODES.R)
    public static int FILTER_BY_ATTRIBUTION_TAG;
    @RequiresApi (Build.VERSION_CODES.R)
    public static int FILTER_BY_OP_NAMES;
    
    @RequiresApi (Build.VERSION_CODES.Q)
    public static int UID_STATE_PERSISTENT;
    @RequiresApi (Build.VERSION_CODES.Q)
    public static int UID_STATE_TOP;
    @RequiresApi (Build.VERSION_CODES.Q)
    public static int UID_STATE_FOREGROUND_SERVICE_LOCATION;
    @RequiresApi (Build.VERSION_CODES.Q)
    public static int UID_STATE_FOREGROUND_SERVICE;
    @RequiresApi (Build.VERSION_CODES.Q)
    public static int UID_STATE_FOREGROUND;
    @RequiresApi (Build.VERSION_CODES.Q)
    public static int UID_STATE_MAX_LAST_NON_RESTRICTED;
    @RequiresApi (Build.VERSION_CODES.Q)
    public static int UID_STATE_BACKGROUND;
    @RequiresApi (Build.VERSION_CODES.Q)
    public static int UID_STATE_CACHED;
    @RequiresApi (Build.VERSION_CODES.Q)
    public static int MAX_PRIORITY_UID_STATE;
    @RequiresApi (Build.VERSION_CODES.Q)
    public static int MIN_PRIORITY_UID_STATE;
    
    public static int strOpToOp(@NonNull String op) {
        throw new RuntimeException("STUB");
    }
    
    public static int strDebugOpToOp(String op) {
        throw new RuntimeException("STUB");
    }
    
    public static int opToSwitch(int op) {
        throw new RuntimeException("STUB");
    }
    
    public static String opToName(int op) {
        throw new RuntimeException("STUB");
    }
    
    @RequiresApi (api = 34)
    public static String opToPublicName(int op) {
        throw new RuntimeException("STUB");
    }
    
    public static String opToPermission(int op) {
        throw new RuntimeException("STUB");
    }
    
    @RequiresApi (api = Build.VERSION_CODES.Q)
    public static String opToPermission(@NonNull String op) {
        throw new RuntimeException("STUB");
    }
    
    public static String opToRestriction(int op) {
        throw new RuntimeException("STUB");
    }
    
    public static int opToDefaultMode(int op) {
        throw new RuntimeException("STUB");
    }
    
    @RequiresApi (api = Build.VERSION_CODES.Q)
    public static int opToDefaultMode(@NonNull String appOp) {
        throw new RuntimeException("STUB");
    }
    
    public static int permissionToOpCode(String permission) {
        throw new RuntimeException("STUB");
    }
    
    @RequiresApi (api = 34)
    public static RestrictionBypass opAllowSystemBypassRestriction(int op) {
        throw new RuntimeException("STUB");
    }
    
    @RequiresApi (api = Build.VERSION_CODES.TIRAMISU)
    public static boolean opRestrictsRead(int op) {
        throw new RuntimeException("STUB");
    }
    
    public static boolean opAllowsReset(int op) {
        throw new RuntimeException("STUB");
    }
    
    @RequiresApi (api = Build.VERSION_CODES.Q)
    public static int getNumOps() {
        throw new RuntimeException("STUB");
        
    }
    
    public static final class PackageOps {
        
        public String getPackageName() {
            throw new RuntimeException("STUB");
        }
        
        public int getUid() {
            throw new RuntimeException("STUB");
        }
        
        public List <OpEntry> getOps() {
            throw new RuntimeException("STUB");
        }
    }
    
    public static final class OpEntry {
        
        public int getOp() {
            throw new RuntimeException("STUB");
        }
        
        public int getMode() {
            throw new RuntimeException("STUB");
        }
        
        public long getTime() {
            throw new RuntimeException("STUB");
        }
        
        @RequiresApi (Build.VERSION_CODES.Q)
        public long getLastAccessTime(int flags) {
            throw new RuntimeException("STUB");
        }
        
        @RequiresApi (Build.VERSION_CODES.Q)
        public long getLastAccessForegroundTime(int flags) {
            throw new RuntimeException("STUB");
        }
        
        @RequiresApi (Build.VERSION_CODES.Q)
        public long getLastAccessBackgroundTime(int flags) {
            throw new RuntimeException("STUB");
        }
        
        @RequiresApi (Build.VERSION_CODES.Q)
        public long getLastAccessTime(int fromUidState, int toUidState, int flags) {
            throw new RuntimeException("STUB");
        }
        
        public long getRejectTime() {
            throw new RuntimeException("STUB");
        }
        
        @RequiresApi (Build.VERSION_CODES.Q)
        public long getLastRejectTime(int flags) {
            throw new RuntimeException("STUB");
        }
        
        @RequiresApi (Build.VERSION_CODES.Q)
        public long getLastRejectForegroundTime(int flags) {
            throw new RuntimeException("STUB");
        }
        
        @RequiresApi (Build.VERSION_CODES.Q)
        public long getLastRejectBackgroundTime(int flags) {
            throw new RuntimeException("STUB");
        }
        
        @RequiresApi (Build.VERSION_CODES.Q)
        public long getLastRejectTime(int fromUidState, int toUidState, int flags) {
            throw new RuntimeException("STUB");
        }
        
        public boolean isRunning() {
            throw new RuntimeException("STUB");
        }
        
        public int getDuration() {
            throw new RuntimeException("STUB");
        }
        
        @RequiresApi (Build.VERSION_CODES.Q)
        public long getLastDuration(int flags) {
            throw new RuntimeException("STUB");
        }
        
        @RequiresApi (Build.VERSION_CODES.Q)
        public long getLastForegroundDuration(int flags) {
            throw new RuntimeException("STUB");
            
        }
        
        @RequiresApi (Build.VERSION_CODES.Q)
        public long getLastBackgroundDuration(int flags) {
            throw new RuntimeException("STUB");
        }
        
        @RequiresApi (Build.VERSION_CODES.Q)
        public long getLastDuration(int fromUidState, int toUidState, int flags) {
            throw new RuntimeException("STUB");
        }
        
        public int getProxyUid() {
            throw new RuntimeException("STUB");
        }
        
        public String getProxyPackageName() {
            throw new RuntimeException("STUB");
        }
        
        @RequiresApi (Build.VERSION_CODES.Q)
        public @Nullable
        OpEventProxyInfo getLastProxyInfo(int flags) {
            throw new RuntimeException("STUB");
        }
        
        @RequiresApi (Build.VERSION_CODES.Q)
        public @Nullable
        OpEventProxyInfo getLastForegroundProxyInfo(int flags) {
            throw new RuntimeException("STUB");
        }
        
        @RequiresApi (Build.VERSION_CODES.Q)
        public @Nullable
        OpEventProxyInfo getLastBackgroundProxyInfo(int flags) {
            throw new RuntimeException("STUB");
        }
        
        @RequiresApi (Build.VERSION_CODES.Q)
        public @Nullable
        OpEventProxyInfo getLastProxyInfo(int fromUidState, int toUidState, int flags) {
            throw new RuntimeException("STUB");
        }
    }
    
    @RequiresApi (Build.VERSION_CODES.Q)
    public interface HistoricalOpsVisitor {
        void visitHistoricalOps(@NonNull HistoricalOps ops);
        
        void visitHistoricalUidOps(@NonNull HistoricalUidOps ops);
        
        void visitHistoricalPackageOps(@NonNull HistoricalPackageOps ops);
        
        @RequiresApi (Build.VERSION_CODES.R)
        void visitHistoricalAttributionOps(@NonNull AttributedHistoricalOps ops);
        
        void visitHistoricalOp(@NonNull HistoricalOp ops);
    }
    
    @RequiresApi (Build.VERSION_CODES.Q)
    public static final class HistoricalOps implements Parcelable {
        
        public static final Creator <HistoricalOps> CREATOR = new Creator <HistoricalOps>() {
            @Override
            public HistoricalOps createFromParcel(Parcel in) {
                throw new RuntimeException("STUB");
            }
            
            @Override
            public HistoricalOps[] newArray(int size) {
                throw new RuntimeException("STUB");
            }
        };
        
        public HistoricalOps(long beginTimeMillis, long endTimeMillis) {
            throw new RuntimeException("STUB");
        }
        
        public HistoricalOps(@NonNull HistoricalOps other) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Splice a piece from the beginning of these ops.
         *
         * @param splicePoint The fraction of the data to be spliced off.
         */
        @NonNull
        public HistoricalOps spliceFromBeginning(double splicePoint) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Splice a piece from the end of these ops.
         *
         * @param fractionToRemove The fraction of the data to be spliced off.
         */
        @NonNull
        public HistoricalOps spliceFromEnd(double fractionToRemove) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Merge the passed ops into the current ones. The time interval is a
         * union of the current and passed in one and the passed in data is
         * folded into the data of this instance.
         */
        public void merge(@NonNull HistoricalOps other) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * AppPermissionUsage the ops to leave only the data we filter for.
         *
         * @param uid             Uid to filter for or {@link android.os.Process#INCIDENTD_UID} for all.
         * @param packageName     Package to filter for or null for all.
         * @param opNames         Ops to filter for or null for all.
         * @param beginTimeMillis The begin time to filter for or {@link Long#MIN_VALUE} for all.
         * @param endTimeMillis   The end time to filter for or {@link Long#MAX_VALUE} for all.
         */
        public void filter(int uid, @Nullable String packageName, @Nullable String[] opNames,
                long beginTimeMillis, long endTimeMillis) {
            throw new RuntimeException("STUB");
        }
        
        public boolean isEmpty() {
            throw new RuntimeException("STUB");
        }
        
        public long getDurationMillis() {
            throw new RuntimeException("STUB");
        }
        
        /**
         * @return The beginning of the interval in milliseconds since
         * epoch start (January 1, 1970 00:00:00.000 GMT - Gregorian).
         */
        public long getBeginTimeMillis() {
            throw new RuntimeException("STUB");
        }
        
        /**
         * @return The end of the interval in milliseconds since
         * epoch start (January 1, 1970 00:00:00.000 GMT - Gregorian).
         */
        public long getEndTimeMillis() {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets number of UIDs with historical ops.
         *
         * @return The number of UIDs with historical ops.
         * @see #getUidOpsAt(int)
         */
        public @IntRange (from = 0)
        int getUidCount() {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the historical UID ops at a given index.
         *
         * @param index The index.
         * @return The historical UID ops at the given index.
         * @see #getUidCount()
         */
        public HistoricalUidOps getUidOpsAt(int index) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the historical UID ops for a given UID.
         *
         * @param uid The UID.
         * @return The historical ops for the UID.
         */
        @Nullable
        public HistoricalUidOps getUidOps(int uid) {
            throw new RuntimeException("STUB");
        }
        
        public void clearHistory(int uid, @NonNull String packageName) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Accepts a visitor to traverse the ops tree.
         *
         * @param visitor The visitor.
         */
        public void accept(@NonNull HistoricalOpsVisitor visitor) {
            throw new RuntimeException("STUB");
        }
        
        @Override
        public int describeContents() {
            throw new RuntimeException("STUB");
        }
        
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            throw new RuntimeException("STUB");
        }
    }
    
    @RequiresApi (Build.VERSION_CODES.Q)
    public static final class HistoricalUidOps {
        
        public int getUid() {
            throw new RuntimeException("STUB");
        }
        
        public int getPackageCount() {
            throw new RuntimeException("STUB");
        }
        
        public HistoricalPackageOps getPackageOpsAt(int index) {
            throw new RuntimeException("STUB");
        }
    }
    
    @RequiresApi (Build.VERSION_CODES.Q)
    public static final class HistoricalPackageOps {
        
        public String getPackageName() {
            throw new RuntimeException("STUB");
        }
        
        public int getOpCount() {
            throw new RuntimeException("STUB");
        }
        
        public HistoricalOp getOpAt(int index) {
            throw new RuntimeException("STUB");
        }
        
        @Nullable
        public HistoricalOp getOp(@NonNull String opName) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets number of attributed historical ops.
         *
         * @return The number of attribution with historical ops.
         * @see #getAttributedOpsAt(int)
         */
        @RequiresApi (Build.VERSION_CODES.R)
        @IntRange (from = 0)
        public int getAttributedOpsCount() {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the attributed historical ops at a given index.
         *
         * @param index The index.
         * @return The historical attribution ops at the given index.
         * @see #getAttributedOpsCount()
         */
        @RequiresApi (Build.VERSION_CODES.R)
        @NonNull
        public AttributedHistoricalOps getAttributedOpsAt(@IntRange (from = 0) int index) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the attributed historical ops for a given attribution tag.
         *
         * @param attributionTag The attribution tag.
         * @return The historical ops for the attribution.
         */
        @RequiresApi (Build.VERSION_CODES.R)
        @Nullable
        public AttributedHistoricalOps getAttributedOps(@Nullable String attributionTag) {
            throw new RuntimeException("STUB");
        }
    }
    
    @RequiresApi (Build.VERSION_CODES.R)
    public static final class AttributedHistoricalOps {
        
        /**
         * Gets number historical app ops.
         *
         * @return The number historical app ops.
         * @see #getOpAt(int)
         */
        @IntRange (from = 0)
        public int getOpCount() {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the historical op at a given index.
         *
         * @param index The index to lookup.
         * @return The op at the given index.
         * @see #getOpCount()
         */
        @NonNull
        public HistoricalOp getOpAt(@IntRange (from = 0) int index) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the historical entry for a given op name.
         *
         * @param opName The op name.
         * @return The historical entry for that op name.
         */
        @Nullable
        public HistoricalOp getOp(@NonNull String opName) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * {@link Context#createAttributionContext attribution} tag
         */
        @Nullable
        public String getTag() {
            throw new RuntimeException("STUB");
        }
    }
    
    @RequiresApi (Build.VERSION_CODES.Q)
    public static final class HistoricalOp {
        
        @NonNull
        public String getOpName() {
            throw new RuntimeException("STUB");
        }
        
        public int getOpCode() {
            throw new RuntimeException("STUB");
        }
        
        private LongSparseLongArray getOrCreateAccessCount() {
            throw new RuntimeException("STUB");
        }
        
        private LongSparseLongArray getOrCreateRejectCount() {
            throw new RuntimeException("STUB");
        }
        
        private LongSparseLongArray getOrCreateAccessDuration() {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets number of discrete historical app ops.
         *
         * @return The number historical app ops.
         * @see #getDiscreteAccessAt(int)
         */
        @RequiresApi (Build.VERSION_CODES.S)
        @IntRange (from = 0)
        public int getDiscreteAccessCount() {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the historical op at a given index.
         *
         * @param index The index to lookup.
         * @return The op at the given index.
         * @see #getDiscreteAccessCount()
         */
        @RequiresApi (Build.VERSION_CODES.S)
        @NonNull
        public AttributedOpEntry getDiscreteAccessAt(@IntRange (from = 0) int index) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the number times the op was accessed (performed) in the foreground.
         *
         * @param flags The flags which are any combination of
         *              {@link #OP_FLAG_SELF}, {@link #OP_FLAG_TRUSTED_PROXY},
         *              {@link #OP_FLAG_UNTRUSTED_PROXY}, {@link #OP_FLAG_TRUSTED_PROXIED},
         *              {@link #OP_FLAG_UNTRUSTED_PROXIED}. You can use {@link #OP_FLAGS_ALL}
         *              for any flag.
         * @return The times the op was accessed in the foreground.
         * @see #getBackgroundAccessCount(int)
         * @see #getAccessCount(int, int, int)
         */
        public long getForegroundAccessCount(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the discrete events the op was accessed (performed) in the foreground.
         *
         * @param flags The flags which are any combination of
         *              {@link #OP_FLAG_SELF}, {@link #OP_FLAG_TRUSTED_PROXY},
         *              {@link #OP_FLAG_UNTRUSTED_PROXY}, {@link #OP_FLAG_TRUSTED_PROXIED},
         *              {@link #OP_FLAG_UNTRUSTED_PROXIED}. You can use {@link #OP_FLAGS_ALL}
         *              for any flag.
         * @return The list of discrete ops accessed in the foreground.
         * @see #getBackgroundDiscreteAccesses(int)
         * @see #getDiscreteAccesses(int, int, int)
         */
        @RequiresApi (Build.VERSION_CODES.S)
        @NonNull
        public List <AttributedOpEntry> getForegroundDiscreteAccesses(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the number times the op was accessed (performed) in the background.
         *
         * @param flags The flags which are any combination of
         *              {@link #OP_FLAG_SELF}, {@link #OP_FLAG_TRUSTED_PROXY},
         *              {@link #OP_FLAG_UNTRUSTED_PROXY}, {@link #OP_FLAG_TRUSTED_PROXIED},
         *              {@link #OP_FLAG_UNTRUSTED_PROXIED}. You can use {@link #OP_FLAGS_ALL}
         *              for any flag.
         * @return The times the op was accessed in the background.
         * @see #getForegroundAccessCount(int)
         * @see #getAccessCount(int, int, int)
         */
        public long getBackgroundAccessCount(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the discrete events the op was accessed (performed) in the background.
         *
         * @param flags The flags which are any combination of
         *              {@link #OP_FLAG_SELF}, {@link #OP_FLAG_TRUSTED_PROXY},
         *              {@link #OP_FLAG_UNTRUSTED_PROXY}, {@link #OP_FLAG_TRUSTED_PROXIED},
         *              {@link #OP_FLAG_UNTRUSTED_PROXIED}. You can use {@link #OP_FLAGS_ALL}
         *              for any flag.
         * @return The list of discrete ops accessed in the background.
         * @see #getForegroundDiscreteAccesses(int)
         * @see #getDiscreteAccesses(int, int, int)
         */
        @RequiresApi (Build.VERSION_CODES.S)
        @NonNull
        public List <AttributedOpEntry> getBackgroundDiscreteAccesses(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the number times the op was accessed (performed) for a
         * range of uid states.
         *
         * @param fromUidState The UID state from which to query. Could be one of
         *                     {@link #UID_STATE_PERSISTENT}, {@link #UID_STATE_TOP},
         *                     {@link #UID_STATE_FOREGROUND_SERVICE}, {@link #UID_STATE_FOREGROUND},
         *                     {@link #UID_STATE_BACKGROUND}, {@link #UID_STATE_CACHED}.
         * @param toUidState   The UID state to which to query.
         * @param flags        The flags which are any combination of
         *                     {@link #OP_FLAG_SELF}, {@link #OP_FLAG_TRUSTED_PROXY},
         *                     {@link #OP_FLAG_UNTRUSTED_PROXY}, {@link #OP_FLAG_TRUSTED_PROXIED},
         *                     {@link #OP_FLAG_UNTRUSTED_PROXIED}. You can use {@link #OP_FLAGS_ALL}
         *                     for any flag.
         * @return The times the op was accessed for the given UID state.
         * @see #getForegroundAccessCount(int)
         * @see #getBackgroundAccessCount(int)
         */
        public long getAccessCount(int fromUidState, int toUidState, int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the discrete events the op was accessed (performed) for a
         * range of uid states.
         *
         * @param flags The flags which are any combination of
         *              {@link #OP_FLAG_SELF}, {@link #OP_FLAG_TRUSTED_PROXY},
         *              {@link #OP_FLAG_UNTRUSTED_PROXY}, {@link #OP_FLAG_TRUSTED_PROXIED},
         *              {@link #OP_FLAG_UNTRUSTED_PROXIED}. You can use {@link #OP_FLAGS_ALL}
         *              for any flag.
         * @return The discrete the op was accessed in the background.
         * @see #getBackgroundDiscreteAccesses(int)
         * @see #getForegroundDiscreteAccesses(int)
         */
        @RequiresApi (Build.VERSION_CODES.S)
        @NonNull
        public List <AttributedOpEntry> getDiscreteAccesses(int fromUidState,
                int toUidState, int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the number times the op was rejected in the foreground.
         *
         * @param flags The flags which are any combination of
         *              {@link #OP_FLAG_SELF}, {@link #OP_FLAG_TRUSTED_PROXY},
         *              {@link #OP_FLAG_UNTRUSTED_PROXY}, {@link #OP_FLAG_TRUSTED_PROXIED},
         *              {@link #OP_FLAG_UNTRUSTED_PROXIED}. You can use {@link #OP_FLAGS_ALL}
         *              for any flag.
         * @return The times the op was rejected in the foreground.
         * @see #getBackgroundRejectCount(int)
         * @see #getRejectCount(int, int, int)
         */
        public long getForegroundRejectCount(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the number times the op was rejected in the background.
         *
         * @param flags The flags which are any combination of
         *              {@link #OP_FLAG_SELF}, {@link #OP_FLAG_TRUSTED_PROXY},
         *              {@link #OP_FLAG_UNTRUSTED_PROXY}, {@link #OP_FLAG_TRUSTED_PROXIED},
         *              {@link #OP_FLAG_UNTRUSTED_PROXIED}. You can use {@link #OP_FLAGS_ALL}
         *              for any flag.
         * @return The times the op was rejected in the background.
         * @see #getForegroundRejectCount(int)
         * @see #getRejectCount(int, int, int)
         */
        public long getBackgroundRejectCount(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the number times the op was rejected for a given range of UID states.
         *
         * @param fromUidState The UID state from which to query. Could be one of
         *                     {@link #UID_STATE_PERSISTENT}, {@link #UID_STATE_TOP},
         *                     {@link #UID_STATE_FOREGROUND_SERVICE}, {@link #UID_STATE_FOREGROUND},
         *                     {@link #UID_STATE_BACKGROUND}, {@link #UID_STATE_CACHED}.
         * @param toUidState   The UID state to which to query.
         * @param flags        The flags which are any combination of
         *                     {@link #OP_FLAG_SELF}, {@link #OP_FLAG_TRUSTED_PROXY},
         *                     {@link #OP_FLAG_UNTRUSTED_PROXY}, {@link #OP_FLAG_TRUSTED_PROXIED},
         *                     {@link #OP_FLAG_UNTRUSTED_PROXIED}. You can use {@link #OP_FLAGS_ALL}
         *                     for any flag.
         * @return The times the op was rejected for the given UID state.
         * @see #getForegroundRejectCount(int)
         * @see #getBackgroundRejectCount(int)
         */
        public long getRejectCount(int fromUidState, int toUidState, int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the total duration the app op was accessed (performed) in the foreground.
         * The duration is in wall time.
         *
         * @param flags The flags which are any combination of
         *              {@link #OP_FLAG_SELF}, {@link #OP_FLAG_TRUSTED_PROXY},
         *              {@link #OP_FLAG_UNTRUSTED_PROXY}, {@link #OP_FLAG_TRUSTED_PROXIED},
         *              {@link #OP_FLAG_UNTRUSTED_PROXIED}. You can use {@link #OP_FLAGS_ALL}
         *              for any flag.
         * @return The total duration the app op was accessed in the foreground.
         * @see #getBackgroundAccessDuration(int)
         * @see #getAccessDuration(int, int, int)
         */
        public long getForegroundAccessDuration(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the total duration the app op was accessed (performed) in the background.
         * The duration is in wall time.
         *
         * @param flags The flags which are any combination of
         *              {@link #OP_FLAG_SELF}, {@link #OP_FLAG_TRUSTED_PROXY},
         *              {@link #OP_FLAG_UNTRUSTED_PROXY}, {@link #OP_FLAG_TRUSTED_PROXIED},
         *              {@link #OP_FLAG_UNTRUSTED_PROXIED}. You can use {@link #OP_FLAGS_ALL}
         *              for any flag.
         * @return The total duration the app op was accessed in the background.
         * @see #getForegroundAccessDuration(int)
         * @see #getAccessDuration(int, int, int)
         */
        public long getBackgroundAccessDuration(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the total duration the app op was accessed (performed) for a given
         * range of UID states. The duration is in wall time.
         *
         * @param fromUidState The UID state from which to query. Could be one of
         *                     {@link #UID_STATE_PERSISTENT}, {@link #UID_STATE_TOP},
         *                     {@link #UID_STATE_FOREGROUND_SERVICE}, {@link #UID_STATE_FOREGROUND},
         *                     {@link #UID_STATE_BACKGROUND}, {@link #UID_STATE_CACHED}.
         * @param toUidState   The UID state from which to query.
         * @param flags        The flags which are any combination of
         *                     {@link #OP_FLAG_SELF}, {@link #OP_FLAG_TRUSTED_PROXY},
         *                     {@link #OP_FLAG_UNTRUSTED_PROXY}, {@link #OP_FLAG_TRUSTED_PROXIED},
         *                     {@link #OP_FLAG_UNTRUSTED_PROXIED}. You can use {@link #OP_FLAGS_ALL}
         *                     for any flag.
         * @return The total duration the app op was accessed for the given UID state.
         * @see #getForegroundAccessDuration(int)
         * @see #getBackgroundAccessDuration(int)
         */
        public long getAccessDuration(int fromUidState, int toUidState, int flags) {
            throw new RuntimeException("STUB");
        }
    }
    
    @RequiresApi (Build.VERSION_CODES.R)
    public static final class AttributedOpEntry {
        
        /**
         * Returns all keys for which we have events.
         */
        @NonNull
        public ArraySet <Long> collectKeys() {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Return the last access time.
         *
         * @param flags The op flags
         * @return the last access time (in milliseconds since epoch start (January 1, 1970
         * 00:00:00.000 GMT - Gregorian)) or {@code -1} if there was no access
         * @see #getLastAccessForegroundTime(int)
         * @see #getLastAccessBackgroundTime(int)
         * @see #getLastAccessTime(int, int, int)
         * @see OpEntry#getLastAccessTime(int)
         */
        public long getLastAccessTime(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Return the last foreground access time.
         *
         * @param flags The op flags
         * @return the last access time (in milliseconds since epoch start (January 1, 1970
         * 00:00:00.000 GMT - Gregorian)) or {@code -1} if there was no foreground access
         * @see #getLastAccessTime(int)
         * @see #getLastAccessBackgroundTime(int)
         * @see #getLastAccessTime(int, int, int)
         * @see OpEntry#getLastAccessForegroundTime(int)
         */
        public long getLastAccessForegroundTime(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Return the last background access time.
         *
         * @param flags The op flags
         * @return the last access time (in milliseconds since epoch start (January 1, 1970
         * 00:00:00.000 GMT - Gregorian)) or {@code -1} if there was no background access
         * @see #getLastAccessTime(int)
         * @see #getLastAccessForegroundTime(int)
         * @see #getLastAccessTime(int, int, int)
         * @see OpEntry#getLastAccessBackgroundTime(int)
         */
        public long getLastAccessBackgroundTime(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Return the last access time.
         *
         * @param fromUidState The lowest UID state for which to query
         * @param toUidState   The highest UID state for which to query (inclusive)
         * @param flags        The op flags
         * @return the last access time (in milliseconds since epoch start (January 1, 1970
         * 00:00:00.000 GMT - Gregorian)) or {@code -1} if there was no access
         * @see #getLastAccessTime(int)
         * @see #getLastAccessForegroundTime(int)
         * @see #getLastAccessBackgroundTime(int)
         * @see OpEntry#getLastAccessTime(int, int, int)
         */
        public long getLastAccessTime(int fromUidState, int toUidState,
                int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Return the last rejection time.
         *
         * @param flags The op flags
         * @return the last rejection time (in milliseconds since epoch start (January 1, 1970
         * 00:00:00.000 GMT - Gregorian)) or {@code -1} if there was no rejection
         * @see #getLastRejectForegroundTime(int)
         * @see #getLastRejectBackgroundTime(int)
         * @see #getLastRejectTime(int, int, int)
         * @see OpEntry#getLastRejectTime(int)
         */
        public long getLastRejectTime(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Return the last foreground rejection time.
         *
         * @param flags The op flags
         * @return the last rejection time (in milliseconds since epoch start (January 1, 1970
         * 00:00:00.000 GMT - Gregorian)) or {@code -1} if there was no foreground rejection
         * @see #getLastRejectTime(int)
         * @see #getLastRejectBackgroundTime(int)
         * @see #getLastRejectTime(int, int, int)
         * @see OpEntry#getLastRejectForegroundTime(int)
         */
        public long getLastRejectForegroundTime(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Return the last background rejection time.
         *
         * @param flags The op flags
         * @return the last rejection time (in milliseconds since epoch start (January 1, 1970
         * 00:00:00.000 GMT - Gregorian)) or {@code -1} if there was no background rejection
         * @see #getLastRejectTime(int)
         * @see #getLastRejectForegroundTime(int)
         * @see #getLastRejectTime(int, int, int)
         * @see OpEntry#getLastRejectBackgroundTime(int)
         */
        public long getLastRejectBackgroundTime(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Return the last rejection time.
         *
         * @param fromUidState The lowest UID state for which to query
         * @param toUidState   The highest UID state for which to query (inclusive)
         * @param flags        The op flags
         * @return the last access time (in milliseconds since epoch) or {@code -1} if there was no
         * rejection
         * @see #getLastRejectTime(int)
         * @see #getLastRejectForegroundTime(int)
         * @see #getLastRejectForegroundTime(int)
         * @see #getLastRejectTime(int, int, int)
         * @see OpEntry#getLastRejectTime(int, int, int)
         */
        public long getLastRejectTime(int fromUidState, int toUidState,
                int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Return the duration in milliseconds of the last the access.
         *
         * @param flags The op flags
         * @return the duration in milliseconds or {@code -1} if there was no rejection
         * @see #getLastForegroundDuration(int)
         * @see #getLastBackgroundDuration(int)
         * @see #getLastDuration(int, int, int)
         * @see OpEntry#getLastDuration(int)
         */
        public long getLastDuration(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Return the duration in milliseconds of the last foreground access.
         *
         * @param flags The op flags
         * @return the duration in milliseconds or {@code -1} if there was no foreground rejection
         * @see #getLastDuration(int)
         * @see #getLastBackgroundDuration(int)
         * @see #getLastDuration(int, int, int)
         * @see OpEntry#getLastForegroundDuration(int)
         */
        public long getLastForegroundDuration(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Return the duration in milliseconds of the last background access.
         *
         * @param flags The op flags
         * @return the duration in milliseconds or {@code -1} if there was no background rejection
         * @see #getLastDuration(int)
         * @see #getLastForegroundDuration(int)
         * @see #getLastDuration(int, int, int)
         * @see OpEntry#getLastBackgroundDuration(int)
         */
        public long getLastBackgroundDuration(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Return the duration in milliseconds of the last access.
         *
         * @param fromUidState The lowest UID state for which to query
         * @param toUidState   The highest UID state for which to query (inclusive)
         * @param flags        The op flags
         * @return the duration in milliseconds or {@code -1} if there was no rejection
         * @see #getLastDuration(int)
         * @see #getLastForegroundDuration(int)
         * @see #getLastBackgroundDuration(int)
         * @see #getLastDuration(int, int, int)
         * @see OpEntry#getLastDuration(int, int, int)
         */
        public long getLastDuration(int fromUidState, int toUidState,
                int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the proxy info of the app that performed the last access on behalf of this
         * attribution and as a result blamed the op on this attribution.
         *
         * @param flags The op flags
         * @return The proxy info or {@code null} if there was no proxy access
         * @see #getLastForegroundProxyInfo(int)
         * @see #getLastBackgroundProxyInfo(int)
         * @see #getLastProxyInfo(int, int, int)
         * @see OpEntry#getLastProxyInfo(int)
         */
        @Nullable
        public OpEventProxyInfo getLastProxyInfo(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the proxy info of the app that performed the last foreground access on behalf of
         * this attribution and as a result blamed the op on this attribution.
         *
         * @param flags The op flags
         * @return The proxy info or {@code null} if there was no proxy access
         * @see #getLastProxyInfo(int)
         * @see #getLastBackgroundProxyInfo(int)
         * @see #getLastProxyInfo(int, int, int)
         * @see OpEntry#getLastForegroundProxyInfo(int)
         */
        @Nullable
        public OpEventProxyInfo getLastForegroundProxyInfo(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the proxy info of the app that performed the last background access on behalf of
         * this attribution and as a result blamed the op on this attribution.
         *
         * @param flags The op flags
         * @return The proxy info or {@code null} if there was no proxy background access
         * @see #getLastProxyInfo(int)
         * @see #getLastForegroundProxyInfo(int)
         * @see #getLastProxyInfo(int, int, int)
         * @see OpEntry#getLastBackgroundProxyInfo(int)
         */
        @Nullable
        public OpEventProxyInfo getLastBackgroundProxyInfo(int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Gets the proxy info of the app that performed the last access on behalf of this
         * attribution and as a result blamed the op on this attribution.
         *
         * @param fromUidState The lowest UID state for which to query
         * @param toUidState   The highest UID state for which to query (inclusive)
         * @param flags        The op flags
         * @return The proxy info or {@code null} if there was no proxy foreground access
         * @see #getLastProxyInfo(int)
         * @see #getLastForegroundProxyInfo(int)
         * @see #getLastBackgroundProxyInfo(int)
         * @see OpEntry#getLastProxyInfo(int, int, int)
         */
        @Nullable
        public OpEventProxyInfo getLastProxyInfo(int fromUidState,
                int toUidState, int flags) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Creates a new OpAttributionEntry.
         *
         * @param op           The code of the op
         * @param running      Whether the op is running
         * @param accessEvents The access events
         * @param rejectEvents The rejection events
         */
        public AttributedOpEntry(int op,
                boolean running,
                @Nullable LongSparseArray <NoteOpEvent> accessEvents,
                @Nullable LongSparseArray <NoteOpEvent> rejectEvents) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Whether the op is running
         */
        public boolean isRunning() {
            throw new RuntimeException("STUB");
        }
    }
    
    @RequiresApi (Build.VERSION_CODES.Q)
    public static final class OpEventProxyInfo {
        
        /**
         * UID of the proxy app that noted the op
         */
        @IntRange (from = 0)
        public int getUid() {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Package of the proxy that noted the op
         */
        @Nullable
        public String getPackageName() {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Attribution tag of the proxy that noted the op
         */
        @Nullable
        public String getAttributionTag() {
            throw new RuntimeException("STUB");
        }
        
    }
    
    public static final class NoteOpEvent {
        
        /**
         * Creates a new NoteOpEvent.
         *
         * @param noteTime Time of noteOp event
         * @param duration The duration of this event (in case this is a startOp event, -1 otherwise).
         * @param proxy    Proxy information of the noteOp event
         */
        public NoteOpEvent(
                @IntRange (from = 0) long noteTime,
                @IntRange (from = -1) long duration,
                @Nullable OpEventProxyInfo proxy) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Time of noteOp event
         */
        @IntRange (from = 0)
        public long getNoteTime() {
            throw new RuntimeException("STUB");
        }
        
        /**
         * The duration of this event (in case this is a startOp event, -1 otherwise).
         */
        @IntRange (from = -1)
        public long getDuration() {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Proxy information of the noteOp event
         */
        @Nullable
        public OpEventProxyInfo getProxy() {
            throw new RuntimeException("STUB");
        }
    }
    
    @RequiresApi (api = 34)
    public static class RestrictionBypass {
        public static RestrictionBypass UNRESTRICTED;
        public boolean isPrivileged;
        public boolean isRecordAudioRestrictionExcept;
        public boolean isSystemUid;
        
        public RestrictionBypass(boolean isSystemUid, boolean isPrivileged, boolean isRecordAudioRestrictionExcept) {
            throw new RuntimeException("STUB");
        }
    }
    
}

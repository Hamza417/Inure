package android.os;

import androidx.annotation.RequiresApi;

@RequiresApi (Build.VERSION_CODES.R)
public interface IDeviceIdleController extends IInterface {
    
    void addPowerSaveTempWhitelistApp(String name, long duration, int userId, String reason);
    
    @RequiresApi (Build.VERSION_CODES.S)
    void addPowerSaveTempWhitelistApp(String name, long duration, int userId, int reasonCode, String reason);
    
    abstract class Stub extends Binder implements IDeviceIdleController {
        
        public static IDeviceIdleController asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
    }
}

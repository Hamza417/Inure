package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class BatchUninstallerProgressStateModel implements Parcelable {
    private int count;
    private int done;
    private int failed;
    private int queued;
    
    public BatchUninstallerProgressStateModel(int count, int done, int failed, int queued) {
        this.count = count;
        this.done = done;
        this.failed = failed;
        this.queued = queued;
    }
    
    protected BatchUninstallerProgressStateModel(Parcel in) {
        count = in.readInt();
        done = in.readInt();
        failed = in.readInt();
        queued = in.readInt();
    }
    
    public static final Creator <BatchUninstallerProgressStateModel> CREATOR = new Creator <>() {
        @Override
        public BatchUninstallerProgressStateModel createFromParcel(Parcel in) {
            return new BatchUninstallerProgressStateModel(in);
        }
        
        @Override
        public BatchUninstallerProgressStateModel[] newArray(int size) {
            return new BatchUninstallerProgressStateModel[size];
        }
    };
    
    public BatchUninstallerProgressStateModel() {
    }
    
    public int getCount() {
        return count;
    }
    
    public int getDone() {
        return done;
    }
    
    public int getFailed() {
        return failed;
    }
    
    public int getQueued() {
        return queued;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public void setDone(int done) {
        this.done = done;
    }
    
    public void setFailed(int failed) {
        this.failed = failed;
    }
    
    public void setQueued(int queued) {
        this.queued = queued;
    }
    
    public void incrementCount() {
        this.count++;
    }
    
    public void incrementDone() {
        this.done++;
    }
    
    public void incrementFailed() {
        this.failed++;
    }
    
    public void incrementQueued() {
        this.queued++;
    }
    
    public void decrementCount() {
        this.count--;
    }
    
    public void decrementDone() {
        this.done--;
    }
    
    public void decrementFailed() {
        this.failed--;
    }
    
    public void decrementQueued() {
        this.queued--;
    }
    
    public void reset() {
        this.count = 0;
        this.done = 0;
        this.failed = 0;
        this.queued = 0;
    }
    
    public void resetCount() {
        this.count = 0;
    }
    
    public void resetDone() {
        this.done = 0;
    }
    
    public void resetFailed() {
        this.failed = 0;
    }
    
    public void resetQueued() {
        this.queued = 0;
    }
    
    public void resetAll() {
        this.count = 0;
        this.done = 0;
        this.failed = 0;
        this.queued = 0;
    }
    
    public void resetAllExceptCount() {
        this.done = 0;
        this.failed = 0;
        this.queued = 0;
    }
    
    public void resetAllExceptDone() {
        this.count = 0;
        this.failed = 0;
        this.queued = 0;
    }
    
    public void resetAllExceptFailed() {
        this.count = 0;
        this.done = 0;
        this.queued = 0;
    }
    
    public void resetAllExceptQueued() {
        this.count = 0;
        this.done = 0;
        this.failed = 0;
    }
    
    public void resetAllExceptCountAndDone() {
        this.failed = 0;
        this.queued = 0;
    }
    
    public void resetAllExceptCountAndFailed() {
        this.done = 0;
        this.queued = 0;
    }
    
    public void resetAllExceptCountAndQueued() {
        this.done = 0;
        this.failed = 0;
    }
    
    public void resetAllExceptDoneAndFailed() {
        this.count = 0;
        this.queued = 0;
    }
    
    public void resetAllExceptDoneAndQueued() {
        this.count = 0;
        this.failed = 0;
    }
    
    public void resetAllExceptFailedAndQueued() {
        this.count = 0;
        this.done = 0;
    }
    
    public void resetAllExceptCountAndDoneAndFailed() {
        this.queued = 0;
    }
    
    public void resetAllExceptCountAndDoneAndQueued() {
        this.failed = 0;
    }
    
    public void resetAllExceptCountAndFailedAndQueued() {
        this.done = 0;
    }
    
    public void resetAllExceptDoneAndFailedAndQueued() {
        this.count = 0;
    }
    
    public void resetAllExceptCountAndDoneAndFailedAndQueued() {
        // do nothing
    }
    
    public void incrementAll() {
        this.count++;
        this.done++;
        this.failed++;
        this.queued++;
    }
    
    public void decrementAll() {
        this.count--;
        this.done--;
        this.failed--;
        this.queued--;
    }
    
    public void incrementAllExceptCount() {
        this.done++;
        this.failed++;
        this.queued++;
    }
    
    public void incrementAllExceptDone() {
        this.count++;
        this.failed++;
        this.queued++;
    }
    
    public void incrementAllExceptFailed() {
        this.count++;
        this.done++;
        this.queued++;
    }
    
    public void incrementAllExceptQueued() {
        this.count++;
        this.done++;
        this.failed++;
    }
    
    public void incrementAllExceptCountAndDone() {
        this.failed++;
        this.queued++;
    }
    
    public void incrementAllExceptCountAndFailed() {
        this.done++;
        this.queued++;
    }
    
    public void incrementAllExceptCountAndQueued() {
        this.done++;
        this.failed++;
    }
    
    public void incrementAllExceptDoneAndFailed() {
        this.count++;
        this.queued++;
    }
    
    public void incrementAllExceptDoneAndQueued() {
        this.count++;
        this.failed++;
    }
    
    public void incrementAllExceptFailedAndQueued() {
        this.count++;
        this.done++;
    }
    
    public void incrementAllExceptCountAndDoneAndFailed() {
        this.queued++;
    }
    
    public void incrementAllExceptCountAndDoneAndQueued() {
        this.failed++;
    }
    
    public void incrementAllExceptCountAndFailedAndQueued() {
        this.done++;
    }
    
    public void incrementAllExceptDoneAndFailedAndQueued() {
        this.count++;
    }
    
    public void incrementAllExceptCountAndDoneAndFailedAndQueued() {
        // do nothing
    }
    
    public void decrementAllExceptCount() {
        this.done--;
        this.failed--;
        this.queued--;
    }
    
    public void decrementAllExceptDone() {
        this.count--;
        this.failed--;
        this.queued--;
    }
    
    public void decrementAllExceptFailed() {
        this.count--;
        this.done--;
        this.queued--;
    }
    
    public void decrementAllExceptQueued() {
        this.count--;
        this.done--;
        this.failed--;
    }
    
    public void decrementAllExceptCountAndDone() {
        this.failed--;
        this.queued--;
    }
    
    public void decrementAllExceptCountAndFailed() {
        this.done--;
        this.queued--;
    }
    
    public void decrementAllExceptCountAndQueued() {
        this.done--;
        this.failed--;
    }
    
    public void decrementAllExceptDoneAndFailed() {
        this.count--;
        this.queued--;
    }
    
    public void decrementAllExceptDoneAndQueued() {
        this.count--;
        this.failed--;
    }
    
    public void decrementAllExceptFailedAndQueued() {
        this.count--;
        this.done--;
    }
    
    public void decrementAllExceptCountAndDoneAndFailed() {
        this.queued--;
    }
    
    public void decrementAllExceptCountAndDoneAndQueued() {
        this.failed--;
    }
    
    public void decrementAllExceptCountAndFailedAndQueued() {
        this.done--;
    }
    
    public void decrementAllExceptDoneAndFailedAndQueued() {
        this.count--;
    }
    
    public void decrementAllExceptCountAndDoneAndFailedAndQueued() {
        // do nothing
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(count);
        dest.writeInt(done);
        dest.writeInt(failed);
        dest.writeInt(queued);
    }
}

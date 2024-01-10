package app.simple.inure.database.migration;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class FOSSMigration extends Migration {
    
    public FOSSMigration(int startVersion, int endVersion) {
        super(startVersion, endVersion);
    }
    
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase supportSQLiteDatabase) {
        /*
         * I've renamed version_code to license
         */
        supportSQLiteDatabase.execSQL("ALTER TABLE foss RENAME COLUMN version_code TO license");
        
        // Also empty all the license entries
        supportSQLiteDatabase.execSQL("UPDATE foss SET license = ''");
    }
}
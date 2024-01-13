package app.simple.inure.database.migration;

import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class FOSSMigration extends Migration {
    
    public FOSSMigration(int startVersion, int endVersion) {
        super(startVersion, endVersion);
    }
    
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase supportSQLiteDatabase) {
        try {
            /*
             * I've renamed version_code to license
             */
            supportSQLiteDatabase.execSQL("ALTER TABLE foss RENAME COLUMN version_code TO license");
            
            // Also empty all the license entries
            supportSQLiteDatabase.execSQL("UPDATE foss SET license = ''");
        } catch (SQLiteException e) {
            try {
                // Device is below Android 10 and doesn't support RENAME COLUMN
                // Create a new table with the new schema
                supportSQLiteDatabase.execSQL("CREATE TABLE foss_new (package_name TEXT NOT NULL, is_foss INTEGER NOT NULL, license TEXT, PRIMARY KEY(package_name))");
                supportSQLiteDatabase.execSQL("INSERT INTO foss_new (package_name, is_foss, license) SELECT package_name, is_foss, '' FROM foss");
                supportSQLiteDatabase.execSQL("DROP TABLE foss");
                supportSQLiteDatabase.execSQL("ALTER TABLE foss_new RENAME TO foss");
            } catch (SQLiteException e1) {
                try {
                    // It crashed again, so we'll just delete the table and recreate it
                    supportSQLiteDatabase.execSQL("DROP TABLE foss");
                    supportSQLiteDatabase.execSQL("CREATE TABLE foss (package_name TEXT NOT NULL, is_foss INTEGER NOT NULL, license TEXT, PRIMARY KEY(package_name))");
                } catch (IllegalStateException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }
}

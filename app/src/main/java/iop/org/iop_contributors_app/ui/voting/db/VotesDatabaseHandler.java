package iop.org.iop_contributors_app.ui.voting.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mati on 21/12/16.
 */

public class VotesDatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "VOTES";
    private static final int DATABASE_VERSION = 1;

    public VotesDatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

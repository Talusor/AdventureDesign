package kr.cnu.ai.lth.adventuredesign;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import kr.cnu.ai.lth.adventuredesign.History.History;
import kr.cnu.ai.lth.adventuredesign.Shelter.Shelter;

public class HistoryDBHelper extends SQLiteOpenHelper {

    public HistoryDBHelper(Context context, String name) {
        super(context, name, null, 1);
    }

    public HistoryDBHelper(Context context) {
        super(context, context.getFilesDir().getAbsolutePath() + "/history.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS HISTORY (ID INTEGER PRIMARY KEY AUTOINCREMENT, REGISTER_DATE TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), DETECT_COUNT INTEGER, DURATION INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS HISTORY");
        onCreate(db);
    }

    public long insertHistory(int detectCnt, long duration) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("DETECT_COUNT", detectCnt);
        values.put("DURATION", duration);
        return db.insert("HISTORY", null, values);
    }

    public List<History> getHistories(int year, int month) {
        List<History> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Cursor cursor = db.query(
                "HISTORY",
                new String[]{"REGISTER_DATE", "DETECT_COUNT", "DURATION"},
                String.format("strftime('%%Y', REGISTER_DATE) = '%d' AND strftime('%%m', REGISTER_DATE) = '%d'", year, month),
                null, null, null, null);

        while (cursor.moveToNext()) {
            String date = cursor.getString(0);
            int detectCnt = cursor.getInt(1);
            int duration = cursor.getInt(2);
            try {
                result.add(new History(
                        format.parse(date),
                        detectCnt,
                        duration
                ));
            } catch (Exception ignored) {
            }
        }

        cursor.close();
        return result;
    }
}

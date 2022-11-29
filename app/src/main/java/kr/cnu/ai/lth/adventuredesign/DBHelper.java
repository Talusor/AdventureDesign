package kr.cnu.ai.lth.adventuredesign;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import kr.cnu.ai.lth.adventuredesign.Shelter.Shelter;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context, String name) {
        super(context, name, null, 1);
    }

    public DBHelper(Context context) {
        super(context, context.getFilesDir().getAbsolutePath() + "/shelter.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 기존에 DB를 사용할 것
        // 따라서 TABLE 을 CREATE 하지 않음
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 기존에 DB를 사용할 것
    }

    public List<Shelter> getShelters() {
        List<Shelter> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                "전국졸음쉼터표준데이터",
                new String[] { "졸음쉼터명", "도로노선명", "도로노선방향", "주차면수", "화장실유무", "위도", "경도" },
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            String roadName = cursor.getString(1);
            String roadDirection = cursor.getString(2);
            int parkingSpace = cursor.getInt(3);
            String hasToilet = cursor.getString(4);
            double lat = cursor.getDouble(5);
            double lng = cursor.getDouble(6);

            result.add(new Shelter(
                    name,
                    roadName,
                    roadDirection,
                    parkingSpace,
                    hasToilet.equals("Y"),
                    lat,
                    lng
            ));
        }

        cursor.close();
        return result;
    }
}

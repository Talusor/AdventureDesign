package kr.cnu.ai.lth.adventuredesign;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import kr.cnu.ai.lth.adventuredesign.Shelter.Shelter;

public class Manager {
    public final String TAG = "[ADV]";

    private static Manager manager;
    private Manager() {}

    private DBHelper dbHelper;
    private List<Shelter> shelters = new ArrayList<>();

    public static Manager getInstance()
    {
        if (manager == null)
            manager = new Manager();
        return manager;
    }

    public synchronized void LoadDB(Context context) throws Exception {
        File file = new File(context.getFilesDir().getAbsolutePath() + "/shelter.db");

        if (!file.exists()) {
            Log.d(TAG, "Make file");
            try (
                    InputStream is = context.getResources().openRawResource(R.raw.shelter);
                    OutputStream output = new FileOutputStream(context.getFilesDir().getAbsolutePath() + "/shelter.db")
            ) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                output.flush();
            }
        }

        dbHelper = new DBHelper(context);
        shelters.clear();
        shelters = dbHelper.getShelters();
    }

    public synchronized void RefreshData() {
        shelters.clear();
        shelters = dbHelper.getShelters();
    }

    public synchronized List<Shelter> getClosestShelters(double lat, double lng, int limit) {
        shelters.sort((o1, o2) -> {
            double d1 = o1.getDistance(lat, lng);
            double d2 = o2.getDistance(lat, lng);
            if (d1 == d2)
                return 0;
            if (d1 - d2 > 0)
                return 1;
            else
                return -1;
        });

        List<Shelter> sortedData = shelters.stream().limit(limit).collect(Collectors.toList());
        sortedData.forEach(s -> s.setDistanceFromLatLonInKm(lat, lng));
        return sortedData;
    }
}

package kr.cnu.ai.lth.adventuredesign;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import kr.cnu.ai.lth.adventuredesign.History.History;
import kr.cnu.ai.lth.adventuredesign.History.HistoryDBHelper;
import kr.cnu.ai.lth.adventuredesign.Shelter.Shelter;
import kr.cnu.ai.lth.adventuredesign.Shelter.ShelterDBHelper;

public class Manager {
    public final String TAG = "[ADV]";
    public final String ChannelID = "DoNotSleep";

    private static Manager manager;

    private Manager() {
    }

    private ShelterDBHelper shelterDbHelper;
    private List<Shelter> shelters = new ArrayList<>();
    private HistoryDBHelper historyDbHelper;

    private final Settings settings = new Settings();

    public static Manager getInstance() {
        if (manager == null)
            manager = new Manager();
        return manager;
    }

    public synchronized Settings getSettings() { return settings; }

    public synchronized boolean isDriveServiceRunning(Context context, Class<?> cls) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (service.service.getClassName().equals(cls.getName()))
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
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

        shelterDbHelper = new ShelterDBHelper(context);
        historyDbHelper = new HistoryDBHelper(context);
        shelters.clear();
        shelters = shelterDbHelper.getShelters();
    }

    public synchronized void RefreshData() {
        shelters.clear();
        shelters = shelterDbHelper.getShelters();
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

    public synchronized List<History> getHistories(int year, int month) {
        return historyDbHelper.getHistories(year, month);
    }

    public synchronized long insertHistory(int detectCnt, long duration) {
        return historyDbHelper.insertHistory(detectCnt, duration);
    }

    public synchronized boolean checkNavi(Context context) {
        String url = settings.getUrlScheme(0, 0, "");

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);

        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list != null && !list.isEmpty();
    }
}

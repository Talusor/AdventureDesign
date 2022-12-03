package kr.cnu.ai.lth.adventuredesign;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class DuringDrive extends Service {
    public DuringDrive() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null && intent.getAction().equals("STOP"))
            stopService();
        else {

            Manager.getInstance().startService();
            // PendingIntent를 이용하면 포그라운드 서비스 상태에서 알림을 누르면 앱의 MainActivity를 다시 열게 된다.
            Intent testIntent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pendingIntent
                    = PendingIntent.getActivity(this, 0, testIntent, PendingIntent.FLAG_IMMUTABLE);

            NotificationManager manager = getBaseContext().getSystemService(NotificationManager.class);

            NotificationChannel serviceChannel = new NotificationChannel(
                    Manager.getInstance().ChannelID,
                    "Detector",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager.createNotificationChannel(serviceChannel);

            Notification notification =
                    new Notification.Builder(this, Manager.getInstance().ChannelID)
                            .setContentTitle("Don't Sleep!")
                            .setContentText("운전자의 졸음을 감지하는 중입니다.")
                            .setSmallIcon(R.drawable.ic_letter_p)
                            .setContentIntent(pendingIntent)
                            .build();

            startForeground(1, notification);
        }

        return START_STICKY;
    }

    private void stopService() {
        Manager.getInstance().stopService();
        stopForeground(true);
        stopSelf();
    }
}
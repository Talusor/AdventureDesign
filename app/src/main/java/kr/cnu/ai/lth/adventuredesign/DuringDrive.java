package kr.cnu.ai.lth.adventuredesign;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.camera2.interop.Camera2Interop;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.facemesh.FaceMeshDetection;
import com.google.mlkit.vision.facemesh.FaceMeshDetector;
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions;

import java.util.concurrent.Executors;

public class DuringDrive extends LifecycleService {
    private Thread background;
    private Handler handler;
    private final Manager manager = Manager.getInstance();
    private final FaceManager faceManager = FaceManager.getInstance();

    private MediaPlayer ventAlarm, sleepAlarm;

    public DuringDrive() {
    }

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        super.onBind(intent);
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null && intent.getAction() != null && intent.getAction().equals("STOP"))
            stopService();
        else {
            Manager.getInstance().startService();
            // PendingIntent를 이용하면 포그라운드 서비스 상태에서 알림을 누르면 앱의 MainActivity를 다시 열게 된다.
            Intent testIntent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pendingIntent
                    = PendingIntent.getActivity(this, 0, testIntent, PendingIntent.FLAG_IMMUTABLE);

            NotificationManager notimanager = getBaseContext().getSystemService(NotificationManager.class);

            NotificationChannel serviceChannel = new NotificationChannel(
                    manager.ChannelID,
                    "Detector",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notimanager.createNotificationChannel(serviceChannel);

            Notification notification =
                    new Notification.Builder(this, manager.ChannelID)
                            .setContentTitle("Don't Sleep!")
                            .setContentText("운전자의 졸음을 감지하는 중입니다.")
                            .setSmallIcon(R.drawable.ic_letter_p)
                            .setContentIntent(pendingIntent)
                            .build();

            sleepAlarm = MediaPlayer.create(this, R.raw.warn_tts);
            sleepAlarm.setLooping(false);
            ventAlarm = MediaPlayer.create(this, R.raw.vent_tts);
            ventAlarm.setLooping(false);
            handler = new Handler();
            background = new Thread(() -> {
                while (true) {
                    try {
                        //Thread.sleep(1000L * 60 * Manager.getInstance().getSettings().getVentTime());
                        Thread.sleep(15000L);
                    } catch (Exception ignored) {
                        return;
                    }
                    handler.post(() -> {
                                String Msg = manager.getSettings().getVentMsg();
                                Toast.makeText(this,
                                                Msg,
                                                Toast.LENGTH_LONG)
                                        .show();

                                float vol = manager.getSettings().getAlarmVolume() / 100.f;

                                if (manager.getSettings().getVentType() == VentType.WITH_TTS) {
                                    ventAlarm.setVolume(vol, vol);
                                    ventAlarm.seekTo(0);
                                    ventAlarm.start();
                                }
                            }
                    );
                }
            });
            background.start();

            InitCamera(new Size(480, 640), 20);

            startForeground(1, notification);
        }

        return START_STICKY;
    }

    private void Detector(boolean closed) {
        Log.d(manager.TAG, "눈 감김");
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void InitCamera(Size targetRes, int targetFPS) {
        faceManager.StartDetect(this::Detector);
        ListenableFuture<ProcessCameraProvider> provider = ProcessCameraProvider.getInstance(this);
        provider.addListener(() -> {
            try {
                ProcessCameraProvider pro = provider.get();

                ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
                Camera2Interop.Extender<ImageAnalysis> ext = new Camera2Interop.Extender<>(builder);
                ext.setCaptureRequestOption(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range<>(targetFPS, targetFPS));
                ImageAnalysis imageAnalysis =
                        builder
                                .setTargetResolution(targetRes)
                                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                FaceMeshDetector detector = FaceMeshDetection.getClient(
                        new FaceMeshDetectorOptions.Builder()
                                .setUseCase(FaceMeshDetectorOptions.FACE_MESH)
                                .build()
                );

                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), image -> {
                    @SuppressLint("UnsafeOptInUsageError") Image mediaImage = image.getImage();
                    if (mediaImage != null) {
                        InputImage inputimage =
                                InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
                        detector.process(inputimage)
                                .addOnSuccessListener(faces -> {
                                    //Log.d(Manager.getInstance().TAG, "Detect: " + faces.size());
                                    if (!faces.isEmpty())
                                        faceManager.ProcessData(faces.get(0));
                                    mediaImage.close();
                                    image.close();
                                })
                                .addOnFailureListener(e -> {
                                    mediaImage.close();
                                    image.close();
                                });
                    }
                });

                pro.unbindAll();
                pro.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, imageAnalysis);
            } catch (Exception e) {
                Log.d(Manager.getInstance().TAG, "onStartCommand: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void stopService() {
        manager.stopService();
        faceManager.StopDetect();
        background.interrupt();
        if (sleepAlarm != null) {
            sleepAlarm.stop();
            sleepAlarm.release();
            sleepAlarm = null;
        }
        if (ventAlarm != null) {
            ventAlarm.stop();
            ventAlarm.release();
            ventAlarm = null;
        }
        stopForeground(true);
        stopSelf();
    }
}
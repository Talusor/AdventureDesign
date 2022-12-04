package kr.cnu.ai.lth.adventuredesign;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.camera2.interop.Camera2Interop;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleService;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.facemesh.FaceMeshDetection;
import com.google.mlkit.vision.facemesh.FaceMeshDetector;
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions;

import org.w3c.dom.Text;

import java.util.Locale;
import java.util.concurrent.Executors;

public class DuringDrive extends LifecycleService {
    private Thread background;
    private Handler handler;
    private TextToSpeech tts;
    private Manager manager = Manager.getInstance();

    public DuringDrive() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
    }


    @SuppressLint("UnsafeOptInUsageError")
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

            ListenableFuture<ProcessCameraProvider> provider = ProcessCameraProvider.getInstance(this);
            provider.addListener(() -> {
                try {
                    ProcessCameraProvider pro = provider.get();

                    ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
                    @SuppressLint("UnsafeOptInUsageError") Camera2Interop.Extender ext = new Camera2Interop.Extender<>(builder);
                    ext.setCaptureRequestOption(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range<>(15, 20));
                    ImageAnalysis imageAnalysis =
                            builder
                                    .setTargetResolution(new Size(480, 640))
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
                                            FaceManager.getInstance().ProcessData(faces.get(0));
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

            handler = new Handler();

            tts = new TextToSpeech(this, status -> {
                if (status != TextToSpeech.ERROR) {
                    int result = tts.setLanguage(Locale.KOREAN);
                    if (result == TextToSpeech.LANG_MISSING_DATA) {
                        Log.d(manager.TAG, "TTS KOREAN MISSING");
                        tts.setLanguage(Locale.ENGLISH);
                    }
                    Log.d(manager.TAG, "TTS INIT");
                } else {
                    Log.d(manager.TAG, "TTS INIT FAIL");
                }
            });

            background = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(1000 * 60 * Manager.getInstance().getSettings().getVentTime());
                    } catch (Exception ignored) {
                        return;
                    }
                    handler.post(() -> {
                                String Msg = manager.getSettings().getVentMsg()
                                        .replace("%N", String.valueOf(Manager.getInstance().getSettings().getVentTime()));
                                Toast.makeText(this,
                                                Msg,
                                                Toast.LENGTH_LONG)
                                        .show();

                                if (manager.getSettings().getVentType() == VentType.WITH_TTS) {
                                    tts.setPitch(1);
                                    tts.setSpeechRate(1);
                                    tts.speak(Msg, TextToSpeech.QUEUE_FLUSH, null, "DONTSLEEP");
                                }
                            }
                    );
                }
            });
            background.start();

            startForeground(1, notification);
        }

        return START_STICKY;
    }

    private void stopService() {
        Manager.getInstance().stopService();
        background.interrupt();
        tts.stop();
        tts.shutdown();
        stopForeground(true);
        stopSelf();
    }
}
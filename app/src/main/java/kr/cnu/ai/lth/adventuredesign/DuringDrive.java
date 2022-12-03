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
import android.os.IBinder;
import android.util.Log;
import android.util.Range;
import android.util.Size;

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

import java.util.concurrent.Executors;

public class DuringDrive extends LifecycleService {
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
                                        Log.d(Manager.getInstance().TAG, "Detect: " + faces.size());
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
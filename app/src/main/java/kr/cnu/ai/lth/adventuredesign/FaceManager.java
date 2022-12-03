package kr.cnu.ai.lth.adventuredesign;

import android.util.Log;

import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.facemesh.FaceMesh;
import com.google.mlkit.vision.facemesh.FaceMeshPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FaceManager {
    private static FaceManager manager;

    int[] mouseIndexes = new int[] {
            62, 96, 89, 179, 86, 15, 316, 403, 319, 325, 292,
            407, 272, 271, 268, 12, 38, 41, 42, 183
    };

    int[] rightEyeIndexes = new int[] {
            33, 246, 161, 160, 159, 158, 157, 173, 133,
            155, 154, 153, 145, 144, 163, 7
    };

    int[] leftEyeIndexes = new int[] {
            362, 398, 384, 385, 386, 387, 388, 466, 263,
            249, 390, 373, 374, 380, 381, 382
    };

    List<PointF3D> mousePoints = new ArrayList<>(mouseIndexes.length);
    List<PointF3D> leftEyePoints = new ArrayList<>(leftEyeIndexes.length);
    List<PointF3D> rightEyePoints = new ArrayList<>(rightEyeIndexes.length);

    private double rawM, rawLE, rawRE;

    private FaceManager() {
    }

    public static FaceManager getInstance() {
        if (manager == null)
            manager = new FaceManager();
        return manager;
    }

    public synchronized void ProcessData(FaceMesh mesh) {
        mousePoints.clear();
        leftEyePoints.clear();
        rightEyePoints.clear();
        for (FaceMeshPoint meshPoint : mesh.getAllPoints()) {
            PointF3D tempPoint = meshPoint.getPosition();
            if (isInArr(meshPoint.getIndex(), mouseIndexes)) {
                mousePoints.add(tempPoint);
            }
            else if (isInArr(meshPoint.getIndex(), leftEyeIndexes)) {
                leftEyePoints.add(tempPoint);
            }
            else if (isInArr(meshPoint.getIndex(), rightEyeIndexes)) {
                rightEyePoints.add(tempPoint);
            }
        }

        Log.d("[ADV]", "Mouse size : " + getSizeOfPoints(mousePoints));
        Log.d("[ADV]", "Left Eye size : " + getSizeOfPoints(leftEyePoints));
        Log.d("[ADV]", "Right Eye size : " + getSizeOfPoints(rightEyePoints));
    }

    private boolean isInArr(int i, int[] arr) {
        for (int index : arr)
            if (index == i)
                return true;
        return false;
    }

    private double getSizeOfPoints(List<PointF3D> list) {
        PointF3D p0 = list.get(0);
        PointF3D p1 = list.get(1);
        PointF3D pN = list.get(list.size() - 1);
        double Size = p0.getX() * p1.getY() - p0.getY() * p1.getX();
        Size += pN.getX() * p0.getY() - pN.getY() * p0.getX();
        for (int i = 1; i < list.size() - 1; i++) {
            p0 = list.get(i);
            p1 = list.get(i + 1);
            Size += p0.getX() * p1.getY() - p0.getY() * p1.getX();
        }
        return Math.abs(Size / 2);
    }
}

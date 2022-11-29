package kr.cnu.ai.lth.adventuredesign.Shelter;

public class Shelter {
    final private String mName;
    final private String mRoadName;
    final private String mEndRoad;
    final private String mRoadDirection;
    final private int mParkingSpace;
    final private boolean mHasToilet;
    final private double mLat;
    final private double mLng;
    private double mDistance;

    public Shelter(String mName, String mRoadName, String mRoadDirection, int mParkingSpace, boolean mHasToilet, double mLat, double mLng) {
        this.mName = mName;
        this.mRoadName = mRoadName;
        this.mEndRoad = mRoadDirection.split(" \\+ ")[1];
        this.mRoadDirection = mRoadDirection;
        this.mParkingSpace = mParkingSpace;
        this.mHasToilet = mHasToilet;
        this.mLat = mLat;
        this.mLng = mLng;
    }

    public double getDistance(double lat, double lng) {
        return (mLat - lat) * (mLat - lat) + (mLng - lng) * (mLng - lng);
    }

    public double getDistanceFromLatLonInKm() {
        return mDistance;
    }

    public void setDistanceFromLatLonInKm(double lat, double lng) {
        double p = 0.017453292519943295;  // Math.PI / 180
        double a = 0.5 - Math.cos((mLat - lat) * p)/2 +
                Math.cos(mLat * p) * Math.cos(lat * p) *
                        (1 - Math.cos((lng - mLng) * p))/2;

        mDistance = 12742 * Math.asin(Math.sqrt(a));
    }

    public String getName() {
        return mName;
    }

    public String getRoadName() {
        return mRoadName;
    }

    public String getRoadDirection() {
        return mRoadDirection;
    }

    public String getEndRoad() { return mEndRoad; }

    public int getParkingSpace() {
        return mParkingSpace;
    }

    public boolean getHasToilet() {
        return mHasToilet;
    }

    public double getLat() {
        return mLat;
    }

    public double getLng() {
        return mLng;
    }
}

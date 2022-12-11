package kr.cnu.ai.lth.adventuredesign;

import java.net.URLEncoder;

enum VentType {NO_SOUND, WITH_SOUND, WITH_TTS}

enum NaviType { NAVER, KAKAO, TMAP, NONE }

public class Settings {
    private String mVentMsg;
    private VentType mVentType;
    private NaviType mNaviType;
    private int mVentTime = 30;

    private int mAlarmUri;
    private int mAlarmVolume;

    public Settings() {
        mVentMsg = "30분마다 환기를 권장합니다.";
        mVentType = VentType.WITH_TTS;
        mAlarmUri = R.raw.shelter;
        mAlarmVolume = 100;
        mNaviType = NaviType.KAKAO;
    }

    public synchronized boolean setVentMsg(String msg) {
        if (msg.length() < 60)
            return false;
        mVentMsg = msg;
        return true;
    }

    public int getVentTime() {
        return mVentTime;
    }

    public synchronized void setVentType(VentType type) {
        mVentType = type;
    }

    public synchronized void setAlarmId(int id) {
        mAlarmUri = id;
    }

    public synchronized void setAlarmVolume(int vol) {
        mAlarmVolume = vol;
    }

    public synchronized void setNaviType(NaviType type) {
        mNaviType = type;
    }

    public synchronized String getVentMsg() {
        return mVentMsg;
    }

    public synchronized VentType getVentType() {
        return mVentType;
    }

    public synchronized int getAlarmId() {
        return mAlarmUri;
    }

    public synchronized int getAlarmVolume() {
        return mAlarmVolume;
    }

    public synchronized NaviType getNaviType() {
        return mNaviType;
    }

    public synchronized String getUrlScheme(double lat, double lng, String name) {
        String result = null;
        try {
            switch (mNaviType) {
                case NAVER:
                    result = "nmap://navigation?dlat=" + lat +
                            "&dlng=" + lng +
                            "&dname=" + URLEncoder.encode(name, "UTF-8") +
                            "&appname=ADV";
                    break;
                case KAKAO:
                    result = "kakaomap://route?ep=" + lat + "," + lng + "&by=CAR";
                    break;
                case TMAP:
                    break;
                default:
                    return null;
            }
        } catch (Exception ignored) {
            return null;
        }
        return result;
    }
}

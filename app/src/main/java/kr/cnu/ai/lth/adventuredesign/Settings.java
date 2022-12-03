package kr.cnu.ai.lth.adventuredesign;

enum VentType { NO_SOUND, WITH_SOUND, WITH_TTS }

public class Settings {
    private String mVentMsg;
    private VentType mVentType;

    private int mAlarmUri;
    private int mAlarmVolume;

    public Settings() {
        mVentMsg = "%N분마다 환기를 권장합니다.";
        mVentType = VentType.WITH_SOUND;
        mAlarmUri = R.raw.shelter;
        mAlarmVolume = 100;
    }

    public synchronized boolean setVentMsg(String msg) {
        if (msg.length() < 60)
            return false;
        mVentMsg = msg;
        return true;
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
}

package kr.cnu.ai.lth.adventuredesign.History;

import java.util.Date;

public class History {
    final private Date mDate;
    final private int mCntOfDetect;
    final private int mDuration;

    public Date getDate() {
        return mDate;
    }

    public int getCntOfDetect() {
        return mCntOfDetect;
    }

    public int getDuration() {
        return mDuration;
    }

    public History(Date mDate, int mCntOfDetect, int mDuration) {
        this.mDate = mDate;
        this.mCntOfDetect = mCntOfDetect;
        this.mDuration = mDuration;
    }
}

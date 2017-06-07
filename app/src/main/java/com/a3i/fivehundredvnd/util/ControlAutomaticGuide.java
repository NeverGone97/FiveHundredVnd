package com.a3i.fivehundredvnd.util;

import android.util.Log;

import java.util.Calendar;

/**
 * Created by Anubis on 3/23/2017.
 */

public class ControlAutomaticGuide {

    public static int TIME_SPACE_ENABLE = 10;


    //  HashMap<String, Integer> idNodeViewed;
    // HashMap<String, Integer> neverAskNodeAgain;
    boolean isMarkerOnMap;
    boolean allowAutoGuide;
    long lastSecond;

    public ControlAutomaticGuide() {
        //   idNodeViewed = new HashMap<>();
        //   neverAskNodeAgain = new HashMap<>();


        allowAutoGuide = false;
        lastSecond = -1;
    }

//    public boolean isRewatchPopupShow() {
//        return isRewatchPopupShow;
//    }
//
//    public void setRewatchPopupShow(boolean rewatchPopupShow) {
//        isRewatchPopupShow = rewatchPopupShow;
//    }

//    public HashMap<String, Integer> getIdNodeViewed() {
//        return idNodeViewed;
//    }
//
//    public void setIdNodeViewed(HashMap<String, Integer> idNodeViewed) {
//        this.idNodeViewed = idNodeViewed;
//    }

    public boolean isMarkerOnMap() {
        return isMarkerOnMap;
    }

    public void setMarkerOnMap(boolean markerOnMap) {
        isMarkerOnMap = markerOnMap;
    }

    public boolean isAllowAutoGuide() {
        return allowAutoGuide;
    }

//    public HashMap<String, Integer> getNeverAskNodeAgain() {
//        return neverAskNodeAgain;
//    }
//
//    public void setNeverAskNodeAgain(HashMap<String, Integer> neverAskNodeAgain) {
//        this.neverAskNodeAgain = neverAskNodeAgain;
//    }

    public void setAllowAutoGuide(boolean allowAutoGuide) {
        this.allowAutoGuide = allowAutoGuide;
    }

    String TAG = "xxx";

    public boolean timerEnabble() {
        Calendar c = Calendar.getInstance();
        long currentSecond = c.getTimeInMillis();
        if (lastSecond == -1) {
            lastSecond = currentSecond;
            return true;
        } else {

            Log.d(TAG, "timerEnabble:last time " + lastSecond);
            long timePeriod = currentSecond - lastSecond;
            Log.d(TAG, "timerEnabble:space time " + timePeriod / 1000);
            lastSecond = currentSecond;
            if (timePeriod / 1000 < TIME_SPACE_ENABLE) {
                return false;
            } else return true;
        }


    }


}

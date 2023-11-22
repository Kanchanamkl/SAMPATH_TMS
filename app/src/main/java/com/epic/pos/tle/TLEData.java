package com.epic.pos.tle;


public class TLEData  {

    private int hostId;
    private int issuerId;
    private String track2;
    private String pan;
    private int chipStatus = 0;
    private boolean isTleEnable;

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    public int getIssuerId() {
        return issuerId;
    }

    public void setIssuerId(int issuerId) {
        this.issuerId = issuerId;
    }

    public int getChipStatus() {
        return chipStatus;
    }

    public void setChipStatus(int chipStatus) {
        this.chipStatus = chipStatus;
    }


    public int getTrack2Length() {
        if(track2!=null)
            return track2.length();
        else
            return 0;
    }

    public void setTrack2(String track2) {
        this.track2 = track2;
    }

    public int getPanLength() {
        if(pan!=null)
            return pan.length();
        else
            return 0;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public boolean isTleEnable() {
        return isTleEnable;
    }

    public void setTleEnable(boolean tleEnable) {
        isTleEnable = tleEnable;
    }
}

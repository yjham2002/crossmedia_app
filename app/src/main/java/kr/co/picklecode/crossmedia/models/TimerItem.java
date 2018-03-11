package kr.co.picklecode.crossmedia.models;

public class TimerItem {

    private int timeInMins;
    private String displayName;
    private boolean isCancel;

    public TimerItem(int timeInMins, String displayName, boolean isCancel) {
        this.timeInMins = timeInMins;
        this.displayName = displayName;
        this.isCancel = isCancel;
    }

    public boolean isCancel() {
        return isCancel;
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }

    public int getTimeInMins() {
        return timeInMins;
    }

    public void setTimeInMins(int timeInMins) {
        this.timeInMins = timeInMins;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}

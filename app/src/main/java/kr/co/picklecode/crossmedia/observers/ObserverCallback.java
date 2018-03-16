package kr.co.picklecode.crossmedia.observers;

/**
 * Created by HP on 2018-03-16.
 */

public interface ObserverCallback {
    void onNegativeCall(int extra);
    void onPositiveCall(int extra);
}

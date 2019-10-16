package id.co.tornado.billiton.module.listener;

import android.view.View;

import id.co.tornado.billiton.common.NsiccsData;

/**
 * Created by imome on 1/9/2019.
 */

public interface InputListener {

    public void onInputCompleted(View v, String result, String additional, NsiccsData cardData);
    public void onStateChanged(String p1, int p2);
}

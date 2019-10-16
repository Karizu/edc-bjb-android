package id.co.tornado.billiton.module.listener;

import android.view.View;

import com.wizarpos.apidemo.printer.PrintSize;

import java.util.List;

/**
 * Created by indra on 13/01/16.
 */
public interface TapListener {
    public void onTap();

    public interface PrintListener extends View.OnClickListener{
        public void onPrintListener(View v,List<PrintSize> dataPrints);
    };
}

package id.co.tornado.billiton.module;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import id.co.tornado.billiton.R;

/**
 * Created by indra on 26/11/15.
 */
public class TextView extends LinearLayout {
    private JSONObject comp;
    private com.rey.material.widget.TextView txtLabel,txtValues;

    public TextView(Context context) {
        super(context);
    }

    public TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void init(JSONObject comp){
        txtLabel = (com.rey.material.widget.TextView) findViewById(R.id.lbl);
        txtValues = (com.rey.material.widget.TextView) findViewById(R.id.lbl_values);
        this.comp = comp;
        try {
            this.setTag(comp.getString("comp_id"));
            String tag = "";
            String value = "";
            value = comp.getString("comp_lbl");
            boolean subtitle = false;
            if (value.startsWith("--S")) {
                subtitle = true;
            }
            if (value.startsWith("--")) {
                value = value.substring(3);
            }
            String lbl = value;

            txtLabel.setText(value);
            if (subtitle) {
                txtLabel.setTextSize(txtLabel.getTextSize() + 4);
                txtLabel.setTextAppearance(getContext(), R.style.TextAppearance_AppCompat_Large);
            }

//            Log.d("COMP_VALUES",comp.get("comp_values").toString());
            if (comp.has("comp_values")) {

                JSONObject val = comp.getJSONObject("comp_values").getJSONArray("comp_value").getJSONObject(0);
                value = val.getString("value");
                if(value.startsWith("[")){
                    value = value.substring(value.indexOf("]")+1);
                }

                if (comp.getString("comp_id").equals("M1012") && lbl.contains("NTPN") && (value == null || value.equals("null"))){
                    value = "-";
                }
//                Log.d("TEXT_VIEW",val.toString());
//                Log.d("TEXT_VIEW", value);
                String tmp = value; //val.getString("value");

                txtValues.setText(tmp);
            }
            if (comp.getBoolean("visible")
                    &&(!lbl.startsWith("START"))
                    &&(!lbl.startsWith("STOP"))
                    &&(!(lbl.startsWith("Nomor Penerbang")&&txtValues.getText().length()<1))) {
                this.setVisibility(View.VISIBLE);
            } else {
                this.setVisibility(View.GONE);
            }
            if (lbl.startsWith("SWAP")) {
                txtLabel.setText(value);
                txtValues.setText("");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

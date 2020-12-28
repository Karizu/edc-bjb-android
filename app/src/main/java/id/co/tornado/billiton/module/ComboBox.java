package id.co.tornado.billiton.module;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;

import com.rey.material.drawable.ThemeDrawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import id.co.tornado.billiton.R;

//import android.support.v7.widget.Tint;

/**
 * Created by indra on 26/11/15.
 */
public class ComboBox extends com.rey.material.widget.Spinner {
    public JSONObject comp;
    public JSONArray compValues;
    public HashMap<Integer, String> compValuesHashMap;
    public List<String> list;
    public List<String> compValuePrints;
    public String compId;

    public ComboBox(Context context) {
        super(context);
    }

    public ComboBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ComboBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ComboBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init(JSONObject comp){
        this.comp = comp;
//        getLabelView().setTextSize(50);
        try {
//            this.setTag(comp.getString("comp_id"));

            try {
                JSONObject comp_values = (comp.has("comp_values") ? comp.getJSONObject("comp_values") : null);
                if (comp_values != null){
                    compValues = (comp_values.has("comp_value") ? comp_values.getJSONArray("comp_value") : null);
                    compValuePrints = new ArrayList<>();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

            try {
                if (comp.getString("comp_id").equals("MA015") || comp.getString("comp_id").equals("MA021")
                        || comp.getString("comp_id").equals("EP010") || comp.getString("comp_id").equals("EP025")
                        || comp.getString("comp_id").equals("EP060") || comp.getString("comp_id").equals("EP075")
                        || comp.getString("comp_id").equals("EP130") || comp.getString("comp_id").equals("EP346")
                        ){
                    compId = comp.getString("comp_id");
                    JSONArray jsonArray = comp.getJSONObject("comp_values").getJSONArray("comp_value");
                    String predefined = jsonArray.getJSONObject(0).getString("value");
                    String[] pvalues = predefined.split("\\|");
                    list = Arrays.asList(pvalues);
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),R.layout.spinner_item, list);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    this.setAdapter(dataAdapter);
                }
            } catch (Exception e){
                e.printStackTrace();
            }

            setLabel(comp.getString("comp_lbl"));
            if (comp.getString("comp_act") != null && !comp.getString("comp_act").equalsIgnoreCase("null")) {
                String predefined = comp.getString("comp_act");
                String[] pvalues = predefined.split("\\|");
                List<String> list = Arrays.asList(pvalues);
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),R.layout.spinner_item, list);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                this.setAdapter(dataAdapter);
            }

            else if (compValues != null && compValues.length() > 0){
                compValuesHashMap = new HashMap<>();
                for (int i = 0; i < compValues.length(); i++){
                    JSONObject compValue = compValues.getJSONObject(i);
                    String key = compValue.getString("value");
                    if (key.contains("[OI]")){
                        key = key.substring(4);
                    }
                    compValuesHashMap.put(i, key);
                    compValuePrints.add(compValue.getString("print"));
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),R.layout.spinner_item, compValuePrints);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                this.setAdapter(dataAdapter);
            }

//            // TEST
//            String predefined = "SATU|DUA|TIGA|EMPAT";
//            String[] pvalues = predefined.split("\\|");
//            List<String> list = Arrays.asList(pvalues);
//            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),R.layout.spinner_item, list);
//            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            this.setAdapter(dataAdapter);

            if (comp.getBoolean("visible")) {
                this.setVisibility(View.VISIBLE);
            } else {
                this.setVisibility(View.INVISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

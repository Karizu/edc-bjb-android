package id.co.tornado.billiton;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import id.co.tornado.billiton.handler.DataBaseHelper;
import id.co.tornado.billiton.handler.JsonCompHandler;

public class DatabaseViewer extends AppCompatActivity implements View.OnClickListener {

    EditText inputText;
    Button runButton;
    TextView resultView;
    private DataBaseHelper helperDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_viewer);
        inputText = (EditText) findViewById(R.id.query_text);
        runButton = (Button) findViewById(R.id.run_query);
        resultView = (TextView) findViewById(R.id.txt_result);
        resultView.setMovementMethod(new ScrollingMovementMethod());
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(DatabaseViewer.this, "Clicked", Toast.LENGTH_LONG).show();
                String qry = inputText.getText().toString();
                Toast.makeText(DatabaseViewer.this, qry, Toast.LENGTH_LONG).show();
                if (qry!=null) {
                    if (qry.startsWith("select")) {
                        JSONArray rs = runSelectQuery(qry);
                        resultView.setText(rs.toString());
                    } else {
                        resultView.setText(runExecQuery(qry));
                    }
                } else {
                    resultView.setText("Query is null");
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(this, "Clicked", Toast.LENGTH_LONG).show();
        String qry = inputText.getText().toString();
        Toast.makeText(this, qry, Toast.LENGTH_LONG).show();
        if (qry!=null) {
            if (qry.startsWith("select")) {
                JSONArray rs = runSelectQuery(qry);
                resultView.setText(rs.toString());
            } else {
                resultView.setText(runExecQuery(qry));
            }
        } else {
            resultView.setText("Query is null");
        }
    }

    private JSONArray runSelectQuery(String selectQuery) {
        JSONArray result = new JSONArray();
        if (helperDb == null) {
            helperDb = new DataBaseHelper(this);
        }
        SQLiteDatabase clientDB = null;
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
            Cursor cLog = clientDB.rawQuery(selectQuery, null);
            if (cLog.moveToFirst()) {
                while (cLog.moveToNext()) {
                    JSONObject row = new JSONObject();
                    for (int i=0;i<cLog.getColumnCount();i++) {
                        String cName = cLog.getColumnName(i);
                        String cVal = "";
                        if (cLog.getString(i) != null) {
                            cVal = cLog.getString(i);
                        }
                        row.put(cName,cVal);
                    }
                    result.put(row);
                }
                if (cLog!=null) {
                    cLog.close();
                }
                if (clientDB.isOpen()) {
                    clientDB.close();
                }
            } else {
                if (cLog!=null) {
                    cLog.close();
                }
                if (clientDB.isOpen()) {
                    clientDB.close();
                }
            }
            clientDB = null;
        } catch (Exception ex) {
            Log.i("TX", "DB error");
            if (clientDB!=null) {
                if (clientDB.isOpen()) {
                    clientDB.close();
                }
                clientDB = null;
            }
        }
        return result;
    }

    private String runExecQuery(String execQuery) {
        String status = "";
        if (helperDb == null) {
            helperDb = new DataBaseHelper(this);
        }
        SQLiteDatabase clientDB = null;
        try {
            helperDb.openDataBase();
            clientDB = helperDb.getActiveDatabase();
            clientDB.execSQL(execQuery);
            if (clientDB.isOpen()) {
                clientDB.close();
            }
            clientDB = null;
            status = "OK";
        } catch (Exception ex) {
            Log.i("TX", "DB error");
            status = "ERROR";
            if (clientDB!=null) {
                if (clientDB.isOpen()) {
                    clientDB.close();
                }
                clientDB = null;
            }
        }
        return status;
    }
}

package com.swufe.test;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView out;
    TextView input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        out=findViewById(R.id.tran_show);
        input=findViewById(R.id.tran_input);

        Button btn =findViewById(R.id.tran_bot1);
        btn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Log.i("click","onClick...");
        Double tem= Double.valueOf(input.getText().toString());
        out.setText(tem+"°C所对应华氏度为："+convert(tem)+"°F");
    }

    public double  convert(double tem){
        return tem*1.8+32;
    }
}
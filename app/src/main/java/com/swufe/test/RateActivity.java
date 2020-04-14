package com.swufe.test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DecimalFormat;

public class RateActivity extends AppCompatActivity {
    EditText rmb;
    TextView show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        rmb=findViewById(R.id.rate_input);
        show=findViewById(R.id.rate_output);

    }


    public void convert(View btn){

        String str=rmb.getText().toString();
        float value=0;
        float output=0;

        if(str.length()>0){
            value=Float.parseFloat(str);
        }else{
            Toast.makeText(this,"请输入数额",Toast.LENGTH_SHORT).show();
        }

        if(btn.getId()==R.id.rate_dol){
            output=value*(1/7f);
        }else if(btn.getId()==R.id.rate_euro){
            output=value*(1/11f);
        }else{
            output=value*500;
        }
        DecimalFormat  df  = new DecimalFormat("######0.00");
        show.setText(df.format(output));
    }

}

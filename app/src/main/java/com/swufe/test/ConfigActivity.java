package com.swufe.test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class ConfigActivity extends AppCompatActivity {

    EditText dollarText;
    EditText euroText;
    EditText wonText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        Intent intent=getIntent();
        float dollar=intent.getFloatExtra("cfg_dol_rate",0.0f);
        float euro=intent.getFloatExtra("cfg_euro_rate",0.0f);
        float won=intent.getFloatExtra("cfg_won_rate",0.0f);

        Log.i("show","dollar rate="+dollar);
        Log.i("show","euro rate="+euro);
        Log.i("show","won rate="+won);

        dollarText=findViewById(R.id.cfg_dol);
        euroText=findViewById(R.id.cfg_euro);
        wonText=findViewById(R.id.cfg_won);

        dollarText.setText(""+dollar);
        euroText.setText(""+euro);
        wonText.setText(""+won);
    }

    public void save(View btn){

        float newDollar=Float.parseFloat(dollarText.getText().toString());
        float newEuro=Float.parseFloat(euroText.getText().toString());
        float newWon=Float.parseFloat(wonText.getText().toString());

        Bundle bdn=new Bundle();
        bdn.putFloat("newDollar",newDollar);
        bdn.putFloat("newEuro",newEuro);
        bdn.putFloat("newWon",newWon);

        Intent intent =getIntent();
        intent.putExtras(bdn);

        setResult(2,intent);
        finish();
    }
}

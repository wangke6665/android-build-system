package com.shenma.tvlauncher;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.app.Activity;


import android.widget.TextView;

import android.view.View;




public class MainActivity3 extends Activity {
    private TextView more;
    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wellcom);
        more=findViewById(R.id.textViewDetails);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent b=new Intent(MainActivity3.this,MainActivity4.class);
                startActivity(b);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("userchoose", Context.MODE_PRIVATE);
        String state=sharedPreferences.getString("text", "");
        if (state.equals("OK")){
            Intent ok = new Intent(this,  com.shenma.tvlauncher.SplashActivity.class);
            startActivity(ok);
            finish();
        }


    }
    public  void agree(View view){
        SharedPreferences sharedPreferences = getSharedPreferences("userchoose", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("text", "OK");
        editor.apply();
        Intent ok = new Intent(this, com.shenma.tvlauncher.SplashActivity.class);
        startActivity(ok);
        finish();
    }
    public  void  unegree(View view){
        finish();
    }
}
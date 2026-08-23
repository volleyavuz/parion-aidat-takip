package com.parion.aidat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

public class SplashActivity extends Activity {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER);root.setPadding(dp(28),dp(28),dp(28),dp(28));root.setBackgroundColor(Color.WHITE);
        ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.parion_brand_mark);logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);root.addView(logo,new LinearLayout.LayoutParams(dp(150),dp(150)));
        TextView title=new TextView(this);title.setText("PARİON SPOR KULÜBÜ");title.setTextColor(Color.BLACK);title.setTextSize(24);title.setGravity(Gravity.CENTER);title.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);tp.setMargins(0,dp(16),0,0);root.addView(title,tp);
        TextView sub=new TextView(this);sub.setText("SPORCU TAKİP SİSTEMİ");sub.setTextColor(Color.DKGRAY);sub.setTextSize(14);sub.setGravity(Gravity.CENTER);sub.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);root.addView(sub,new LinearLayout.LayoutParams(-1,-2));
        setContentView(root);
        root.postDelayed(()->{startActivity(new Intent(this,MainActivityV646.class));finish();},320);
    }
    private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
}

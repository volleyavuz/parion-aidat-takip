package com.parion.aidat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashActivity extends Activity {
    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(36,36,36,36);
        box.setBackgroundColor(Color.BLACK);

        ImageView logo=new ImageView(this);
        logo.setImageBitmap(ClubLogoAsset.bitmap());
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        box.addView(logo,new LinearLayout.LayoutParams(180,180));

        TextView title=new TextView(this);
        title.setText("PARİON SPOR KULÜBÜ");
        title.setTextColor(Color.rgb(245,197,24));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);tp.setMargins(0,18,0,0);box.addView(title,tp);

        TextView sub=new TextView(this);
        sub.setText("AİDAT TAKİP SİSTEMİ");
        sub.setTextColor(Color.LTGRAY);
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        box.addView(sub,new LinearLayout.LayoutParams(-1,-2));
        setContentView(box);

        box.postDelayed(()->{
            Intent i=new Intent(this,MainActivityV434.class);
            startActivity(i);
            overridePendingTransition(0,0);
            finish();
        },120);
    }
}

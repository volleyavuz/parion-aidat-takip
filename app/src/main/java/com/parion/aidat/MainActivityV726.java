package com.parion.aidat;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

/** v4.2.1 - safe first-install bootstrap for additional Android devices. */
public class MainActivityV726 extends MainActivityV725 {
    private boolean ready726=false;
    private boolean bootstrapRequested726=false;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        ready726=true;
        enforceBootstrap726();
    }

    private boolean localEmpty726(){
        try{return db!=null&&db.count(null)==0;}catch(Exception e){return true;}
    }

    private boolean hasSession726(){
        try{return cloudPrefs!=null&&!cloudPrefs.getString("access_token","").isEmpty();}catch(Exception e){return false;}
    }

    private void enforceBootstrap726(){
        if(!ready726||db==null||!localEmpty726())return;
        if(!hasSession726()){
            bootstrapRequested726=false;
            showLogin();
            return;
        }
        showBootstrap726();
        requestBootstrap726();
    }

    private void showBootstrap726(){
        page="BOOTSTRAP_726";currentAthlete=-1;
        base("PARION • İLK SENKRONİZASYON",false);
        LinearLayout wrap=new LinearLayout(this);wrap.setOrientation(LinearLayout.VERTICAL);wrap.setGravity(Gravity.CENTER);wrap.setPadding(dp(24),dp(36),dp(24),dp(36));
        TextView icon=tv("☁",52,Color.rgb(205,156,34),true);icon.setGravity(Gravity.CENTER);wrap.addView(icon,new LinearLayout.LayoutParams(-1,-2));
        TextView title=tv("BULUTTAKİ VERİLER İNDİRİLİYOR",18,BLACK,true);title.setGravity(Gravity.CENTER);wrap.addView(title,new LinearLayout.LayoutParams(-1,-2));
        TextView info=tv("Bu cihazda henüz yerel sporcu verisi yok. İlk kurulumda yalnızca Bulut → Cihaz aktarımı yapılır. İndirme tamamlanmadan bu cihaz buluta veri göndermez.",13,Color.DKGRAY,false);info.setGravity(Gravity.CENTER);info.setPadding(dp(8),dp(14),dp(8),dp(18));wrap.addView(info,new LinearLayout.LayoutParams(-1,-2));
        ProgressBar p=new ProgressBar(this);wrap.addView(p,new LinearLayout.LayoutParams(-2,-2));
        Button retry=btn("TEKRAR DENE");LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(54));rp.setMargins(0,dp(22),0,0);wrap.addView(retry,rp);retry.setOnClickListener(v->{bootstrapRequested726=false;requestBootstrap726();});
        root.addView(wrap,new LinearLayout.LayoutParams(-1,0,1));
    }

    private void requestBootstrap726(){
        if(bootstrapRequested726||!hasSession726()||!localEmpty726())return;
        bootstrapRequested726=true;
        syncFromCloud(true);
        root.postDelayed(()->bootstrapRequested726=false,4000);
    }

    @Override void showHome(){
        if(!ready726){super.showHome();return;}
        if(db!=null&&localEmpty726()){
            if(!hasSession726()){
                bootstrapRequested726=false;
                showLogin();
            }else{
                showBootstrap726();
                requestBootstrap726();
            }
            return;
        }
        bootstrapRequested726=false;
        super.showHome();
    }
}

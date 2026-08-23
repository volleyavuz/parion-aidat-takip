package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.*;
import androidx.work.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** v4.0.39 - move sync actions into Settings; add logout and app exit. */
public class MainActivityV639 extends MainActivityV638 {
    private PopupWindow popup639;

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(()->{patchSettings639(root);disableTopCloudActions639(root);});
    }

    @Override void showHome(){
        super.showHome();
        if(root!=null)root.post(()->disableTopCloudActions639(root));
    }

    private void patchSettings639(View v){
        CharSequence d=v.getContentDescription();
        if(d!=null&&"Ayarlar".equalsIgnoreCase(d.toString())){v.setOnClickListener(this::showSettings639);return;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchSettings639(g.getChildAt(i));}
    }

    private void disableTopCloudActions639(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;String s=String.valueOf(t.getText()).toUpperCase(new Locale("tr","TR"));
            if(s.contains("ONLINE")||s.contains("BULUT GÜNCEL")||s.contains("DEĞİŞİKLİK BEKLİYOR")){
                t.setOnClickListener(null);t.setClickable(false);t.setLongClickable(false);
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)disableTopCloudActions639(g.getChildAt(i));}
    }

    private void showSettings639(View anchor){
        dismiss639();
        LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(dp(8),dp(8),dp(8),dp(8));
        android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(14));bg.setStroke(dp(1),Color.rgb(215,205,175));p.setBackground(bg);

        addItem639(p,"GRUPLARI DÜZENLE",()->invokeNoArg639(MainActivityV625.class,"showGroups625"));
        addItem639(p,"YOKLAMA AYARLARI",()->invokeNoArg639(MainActivityV630.class,"showAttendanceSettings630"));
        addItem639(p,"GÜNCELLEMELER",this::showUpdates639);
        addDivider639(p);
        addItem639(p,"OTURUMU KAPAT",this::confirmLogout639);
        addItem639(p,"ÇIKIŞ",this::exitApp639);

        popup639=new PopupWindow(p,dp(250),WindowManager.LayoutParams.WRAP_CONTENT,true);popup639.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));popup639.setOutsideTouchable(true);popup639.setElevation(dp(8));
        int[] loc=new int[2];anchor.getLocationOnScreen(loc);p.measure(View.MeasureSpec.makeMeasureSpec(dp(250),View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
        int x=Math.max(dp(8),getResources().getDisplayMetrics().widthPixels-dp(262));int y=Math.max(dp(8),loc[1]-p.getMeasuredHeight()-dp(8));popup639.showAtLocation(root,Gravity.TOP|Gravity.START,x,y);
    }

    private void addItem639(LinearLayout p,String text,Runnable r){TextView t=new TextView(this);t.setText(text);t.setTextSize(13);t.setTextColor(Color.rgb(28,28,28));t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(dp(14),0,dp(14),0);t.setOnClickListener(v->{dismiss639();r.run();});p.addView(t,new LinearLayout.LayoutParams(dp(234),dp(48)));}
    private void addDivider639(LinearLayout p){View d=new View(this);d.setBackgroundColor(Color.rgb(232,228,216));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(1));lp.setMargins(dp(8),dp(3),dp(8),dp(3));p.addView(d,lp);}

    private void showUpdates639(){
        int pending=0;try{Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM pending_sync",null);if(c.moveToFirst())pending=c.getInt(0);c.close();}catch(Exception ignored){}
        long last=getSharedPreferences("parion_cloud_session",MODE_PRIVATE).getLong("last_background_sync",0L);
        String lastText=last<=0?"Henüz başarılı arka plan senkronizasyonu yok":new SimpleDateFormat("dd.MM.yyyy HH:mm",new Locale("tr","TR")).format(new Date(last));
        String[] items={"ŞİMDİ TAM SENKRONİZE ET","BEKLEYEN DEĞİŞİKLİKLERİ GÖNDER"};
        new AlertDialog.Builder(this).setTitle("GÜNCELLEMELER").setMessage("Bekleyen değişiklik: "+pending+"\nSon arka plan senkronizasyonu: "+lastText).setItems(items,(d,w)->{
            if(w==0)syncFromCloud(true);else{enqueueBackground639();toast("Bekleyen değişiklikler için bulut eşitlemesi sıraya alındı.");}
        }).setNegativeButton("KAPAT",null).show();
    }

    private void enqueueBackground639(){
        try{db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS pending_sync(id INTEGER PRIMARY KEY AUTOINCREMENT,kind TEXT NOT NULL,entity_key TEXT NOT NULL DEFAULT '',created_at INTEGER NOT NULL,UNIQUE(kind,entity_key))");db.getWritableDatabase().execSQL("INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('LOCAL_SNAPSHOT','ALL',?)",new Object[]{System.currentTimeMillis()});}catch(Exception ignored){}
        try{Constraints c=new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();OneTimeWorkRequest w=new OneTimeWorkRequest.Builder(ParionSyncWorker.class).setConstraints(c).setBackoffCriteria(BackoffPolicy.EXPONENTIAL,30,TimeUnit.SECONDS).build();WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork("parion-pending-sync",ExistingWorkPolicy.REPLACE,w);}catch(Exception ignored){}
    }

    private void confirmLogout639(){
        new AlertDialog.Builder(this).setTitle("OTURUMU KAPAT").setMessage("Online hesabın oturumu kapatılsın mı? Yerel veriler cihazda kalır; yeniden bulut bağlantısı için tekrar giriş yapman gerekir.").setPositiveButton("OTURUMU KAPAT",(d,w)->{if(cloudPrefs!=null)cloudPrefs.edit().clear().apply();showLogin();}).setNegativeButton("DEVAM ET",null).show();
    }

    private void exitApp639(){finishAffinity();}

    private void invokeNoArg639(Class<?> cls,String name){try{Method m=cls.getDeclaredMethod(name);m.setAccessible(true);m.invoke(this);}catch(Exception e){toast("İşlem açılamadı.");}}
    private void dismiss639(){if(popup639!=null){try{popup639.dismiss();}catch(Exception ignored){}popup639=null;}}
    @Override protected void onDestroy(){dismiss639();super.onDestroy();}
}

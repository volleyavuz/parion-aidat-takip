package com.parion.aidat;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.*;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivityV436 extends MainActivityV435 {
    private final Handler safeSync436=new Handler(Looper.getMainLooper());
    private final AtomicBoolean auditRunning436=new AtomicBoolean(false);
    private boolean alive436=true;
    private final Runnable tick436=new Runnable(){@Override public void run(){
        if(!alive436)return;
        cancelInheritedTimers436();
        if(pending436()>0)kickSafePush436();
        safeSync436.postDelayed(this,15000L);
    }};

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        cancelInheritedTimers436();
        quarantineAuditBacklog436();
        safeSync436.removeCallbacks(tick436);
        safeSync436.postDelayed(tick436,1200L);
    }

    @Override protected void onDestroy(){
        alive436=false;
        safeSync436.removeCallbacksAndMessages(null);
        cancelInheritedTimers436();
        super.onDestroy();
    }

    @Override void base(String title, boolean back){
        super.base(title,back);
        applySelectedLogo436();
    }

    @Override void showHome(){
        super.showHome();
        cancelInheritedTimers436();
    }

    @Override void showProfile(long id){
        super.showProfile(id);
        cancelInheritedTimers436();
    }

    @Override void form(long id){
        super.form(id);
        cancelInheritedTimers436();
    }

    private void applySelectedLogo436(){
        try{
            if(root==null||root.getChildCount()==0||!(root.getChildAt(0) instanceof LinearLayout))return;
            LinearLayout bar=(LinearLayout)root.getChildAt(0);
            for(int i=bar.getChildCount()-1;i>=0;i--){
                View v=bar.getChildAt(i);Object tag=v.getTag();
                if("PARION_HEADER_LOGO_431".equals(tag)||"PARION_HOME_LOGO_435".equals(tag)||"PARION_SELECTED_LOGO_436".equals(tag))bar.removeViewAt(i);
            }
            ImageView logo=new ImageView(this);
            logo.setTag("PARION_SELECTED_LOGO_436");
            logo.setImageResource(R.drawable.parion_app_icon);
            logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            logo.setContentDescription("Ana Sayfa");
            logo.setClickable(true);logo.setFocusable(true);
            logo.setOnClickListener(v->showHome());
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(44),dp(44));
            lp.setMargins(dp(2),0,dp(7),0);
            bar.addView(logo,0,lp);
        }catch(Throwable ignored){}
    }

    private void cancelInheritedTimers436(){
        try{
            Field f=MainActivityV421.class.getDeclaredField("syncHandler421");
            f.setAccessible(true);
            Object x=f.get(this);
            if(x instanceof Handler)((Handler)x).removeCallbacksAndMessages(null);
        }catch(Throwable ignored){}
    }

    private int pending436(){
        try{
            Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM sync_queue",null);
            int n=c.moveToFirst()?c.getInt(0):0;c.close();return n;
        }catch(Throwable e){return 0;}
    }

    private void quarantineAuditBacklog436(){
        try{
            db.getWritableDatabase().execSQL("UPDATE activity_log_local SET cloudSynced=2 WHERE cloudSynced=0");
        }catch(Throwable ignored){}
    }

    private void kickSafePush436(){
        if(!auditRunning436.compareAndSet(false,true))return;
        new Thread(()->{
            try{
                quarantineAuditBacklog436();
                pushAuditBatch436();
            }catch(Throwable ignored){}
            runOnUiThread(()->{
                try{invokeCorePush436();}finally{auditRunning436.set(false);}
            });
        },"safe-sync-436").start();
    }

    private void invokeCorePush436(){
        try{
            Method m=MainActivityV421.class.getDeclaredMethod("autoPush421",boolean.class,Runnable.class);
            m.setAccessible(true);
            m.invoke(this,false,null);
        }catch(Throwable ignored){}
    }

    private void pushAuditBatch436(){
        Cursor c=null;
        try{
            SQLiteDatabase rdb=db.getReadableDatabase();
            c=rdb.rawQuery("SELECT id,action,entityType,entityId,detail,createdAt FROM activity_log_local WHERE cloudSynced=2 ORDER BY id LIMIT 200",null);
            JSONArray batch=new JSONArray();ArrayList<Long> ids=new ArrayList<>();String email=currentEmail436();
            while(c.moveToNext()){
                long id=c.getLong(0);ids.add(id);
                JSONObject o=new JSONObject();
                o.put("user_email",email);
                o.put("action",c.getString(1));
                o.put("entity_type",c.getString(2));
                o.put("entity_id",c.getString(3));
                o.put("detail",c.getString(4));
                o.put("client_created_at",sqliteTime436(c.getString(5)));
                batch.put(o);
            }
            c.close();c=null;
            if(ids.isEmpty())return;
            String token=cloudPrefs==null?"":cloudPrefs.getString("access_token","");
            if(token.isEmpty())return;
            HttpResult hr=request("POST",SUPABASE_URL+"/rest/v1/app_activity_log",batch.toString(),token);
            if(hr.code==401&&refreshSession())hr=request("POST",SUPABASE_URL+"/rest/v1/app_activity_log",batch.toString(),cloudPrefs.getString("access_token",""));
            if(hr.code/100!=2)return;
            SQLiteDatabase w=db.getWritableDatabase();w.beginTransaction();
            try{
                ContentValues v=new ContentValues();v.put("cloudSynced",1);v.put("userEmail",email);
                for(Long id:ids)w.update("activity_log_local",v,"id=?",new String[]{String.valueOf(id)});
                w.setTransactionSuccessful();
            }finally{w.endTransaction();}
        }catch(Throwable ignored){
        }finally{if(c!=null)try{c.close();}catch(Throwable ignored){}}
    }

    private String currentEmail436(){
        try{
            String t=cloudPrefs==null?"":cloudPrefs.getString("access_token","");String[] p=t.split("\\.");
            if(p.length>1){byte[] b=Base64.decode(p[1],Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING);return new JSONObject(new String(b,StandardCharsets.UTF_8)).optString("email","");}
        }catch(Throwable ignored){}
        return "";
    }

    private String sqliteTime436(String x){
        if(x==null||x.trim().isEmpty())return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX",Locale.US).format(new Date());
        String s=x.trim();return s.contains("T")?s:s.replace(' ','T')+"+00:00";
    }
}

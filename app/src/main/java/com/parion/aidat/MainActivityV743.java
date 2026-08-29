package com.parion.aidat;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import org.json.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** v4.2.22 - one canonical sync path for manual, realtime and pending triggers. */
public class MainActivityV743 extends MainActivityV742 {
    private final ExecutorService direct743=Executors.newSingleThreadExecutor();
    private final AtomicBoolean running743=new AtomicBoolean(false);

    @Override protected boolean useDirectSync741(){return true;}

    @Override void syncFromCloud(boolean announce){
        if(db==null||cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty())return;
        direct743.execute(()->directPass743(announce));
    }

    private void directPass743(boolean announce){
        if(!running743.compareAndSet(false,true))return;
        try{
            int pushed=invokeInt740("flushPending740");
            if(pendingCount741()>0)throw new Exception("YEREL DEĞİŞİKLİKLER BULUTA AKTARILAMADI");

            JSONObject s=fetchSnapshot743();
            JSONArray aa=s.getJSONArray("athletes"),pp=s.getJSONArray("payments"),ff=s.getJSONArray("fees"),dd=s.getJSONArray("deleted");
            int ac=s.optInt("athlete_count",-1),pc=s.optInt("payment_count",-1),fc=s.optInt("fee_count",-1);
            if(ac<200||pc<500||fc<300||aa.length()!=ac||pp.length()!=pc||ff.length()!=fc)
                throw new Exception("SNAPSHOT EKSİK: "+aa.length()+"/"+pp.length()+"/"+ff.length());

            HashMap<Long,JSONArray> pay=new HashMap<>(),fee=new HashMap<>();
            for(int i=0;i<pp.length();i++){JSONObject o=pp.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id>0)pay.computeIfAbsent(id,k->new JSONArray()).put(o);}
            for(int i=0;i<ff.length();i++){JSONObject o=ff.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id>0)fee.computeIfAbsent(id,k->new JSONArray()).put(o);}

            SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();
            try{
                invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,true);
                for(int i=0;i<aa.length();i++){
                    JSONObject a=aa.getJSONObject(i);long id=a.optLong("legacy_id",-1);if(id<=0)continue;
                    invoke740("mergeAthlete740",new Class<?>[]{SQLiteDatabase.class,JSONObject.class,JSONObject.class},d,a,null);
                    d.delete("payments","athleteId=?",new String[]{String.valueOf(id)});
                    JSONArray px=pay.get(id);if(px!=null)for(int j=0;j<px.length();j++)invoke740("insertPayment740",new Class<?>[]{SQLiteDatabase.class,JSONObject.class},d,px.getJSONObject(j));
                    d.delete("fee_history","athleteId=?",new String[]{String.valueOf(id)});
                    JSONArray fx=fee.get(id);if(fx!=null)for(int j=0;j<fx.length();j++)invoke740("insertFee740",new Class<?>[]{SQLiteDatabase.class,JSONObject.class},d,fx.getJSONObject(j));
                    invoke740("mark740",new Class<?>[]{SQLiteDatabase.class,long.class,String.class},d,id,"");
                }
                for(int i=0;i<dd.length();i++){
                    JSONObject o=dd.getJSONObject(i);long id=o.optLong("legacy_id",-1);String at=o.optString("deleted_at","");
                    if(id>0&&!at.isEmpty()){ContentValues v=new ContentValues();v.put("deletedAt",at);d.update("athletes",v,"id=?",new String[]{String.valueOf(id)});}
                }
                d.delete("pending_sync",null,null);
                invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,false);
                d.setTransactionSuccessful();
            }finally{
                try{invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,false);}catch(Exception ignored){}
                d.endTransaction();
            }

            if(announce){final int n=pushed;runOnUiThread(()->{toast("EŞZAMANLAMA TAMAM • "+n+" YEREL DEĞİŞİKLİK GÖNDERİLDİ • BULUT VERİSİ ALINDI");showHome();});}
            else runOnUiThread(this::showHome);
        }catch(Exception e){
            if(announce){String m=rootCause743(e);runOnUiThread(()->toast("EŞZAMANLAMA DURDU • YEREL VERİ KORUNDU • "+m));}
        }finally{running743.set(false);}
    }

    private JSONObject fetchSnapshot743()throws Exception{
        String token=cloudPrefs.getString("access_token","");
        HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_recovery_snapshot_v1","{}",token);
        if(r.code==401&&refreshSession()){
            token=cloudPrefs.getString("access_token","");
            r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_recovery_snapshot_v1","{}",token);
        }
        if(r.code<200||r.code>=300)throw new Exception("BULUT HTTP "+r.code);
        return new JSONObject(r.body);
    }

    private String rootCause743(Throwable t){
        Throwable x=t;while(x.getCause()!=null)x=x.getCause();String m=x.getMessage();return m==null?x.getClass().getSimpleName():m;
    }

    @Override protected void onDestroy(){direct743.shutdownNow();super.onDestroy();}
}

package com.parion.aidat;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import org.json.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * v4.2.34 - stable visual V750 + true delta synchronization.
 * HOME/finance atomic UI behavior is preserved. Realtime/manual/pending triggers now pull only
 * athletes changed since the last server cursor; pending_sync is never bulk-cleared by pull.
 */
public class MainActivityV750 extends MainActivityV749 {
    private int financeGeneration750=0;
    private final ExecutorService delta750=Executors.newSingleThreadExecutor();
    private final AtomicBoolean running750=new AtomicBoolean(false);

    @Override void base(String title, boolean back){
        super.base(title, back);
        if(root==null || title==null || !"FİNANS".equalsIgnoreCase(title.trim())) return;
        final int gen=++financeGeneration750;
        final View builtRoot=root;
        builtRoot.setAlpha(0f);
        builtRoot.post(() -> builtRoot.post(() -> builtRoot.postOnAnimation(() -> {
            if(gen!=financeGeneration750 || root!=builtRoot) return;
            builtRoot.setAlpha(1f);
        })));
    }

    @Override void syncFromCloud(boolean announce){
        if(db==null||cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty())return;
        delta750.execute(()->deltaPass750(announce));
    }

    private void deltaPass750(boolean announce){
        if(!running750.compareAndSet(false,true))return;
        try{
            int pushed=invokeInt740("flushPending740");
            if(pendingCount741()>0)throw new Exception("YEREL DEĞİŞİKLİKLER BULUTA AKTARILAMADI");

            String since=cloudPrefs.getString("delta_cursor_750","");
            JSONObject s=fetchDelta750(since);
            JSONArray aa=s.optJSONArray("athletes"),pp=s.optJSONArray("payments"),ff=s.optJSONArray("fees"),dd=s.optJSONArray("deleted"),ids=s.optJSONArray("changed_ids");
            if(aa==null)aa=new JSONArray();if(pp==null)pp=new JSONArray();if(ff==null)ff=new JSONArray();if(dd==null)dd=new JSONArray();if(ids==null)ids=new JSONArray();
            String cursor=s.optString("cursor_at","");
            if(cursor.isEmpty())throw new Exception("DELTA CURSOR YOK");

            HashMap<Long,JSONObject> athletes=new HashMap<>();
            HashMap<Long,JSONArray> pay=new HashMap<>(),fee=new HashMap<>();
            HashMap<Long,String> deleted=new HashMap<>();
            for(int i=0;i<aa.length();i++){JSONObject o=aa.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id>0)athletes.put(id,o);}
            for(int i=0;i<pp.length();i++){JSONObject o=pp.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id>0)pay.computeIfAbsent(id,k->new JSONArray()).put(o);}
            for(int i=0;i<ff.length();i++){JSONObject o=ff.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id>0)fee.computeIfAbsent(id,k->new JSONArray()).put(o);}
            for(int i=0;i<dd.length();i++){JSONObject o=dd.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id>0)deleted.put(id,o.optString("deleted_at",""));}

            SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();
            try{
                invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,true);
                for(int i=0;i<ids.length();i++){
                    long id=ids.optLong(i,-1);if(id<=0)continue;
                    JSONObject a=athletes.get(id);
                    if(a!=null){
                        invoke740("mergeAthlete740",new Class<?>[]{SQLiteDatabase.class,JSONObject.class,JSONObject.class},d,a,null);
                        d.delete("payments","athleteId=?",new String[]{String.valueOf(id)});
                        JSONArray px=pay.get(id);if(px!=null)for(int j=0;j<px.length();j++)invoke740("insertPayment740",new Class<?>[]{SQLiteDatabase.class,JSONObject.class},d,px.getJSONObject(j));
                        d.delete("fee_history","athleteId=?",new String[]{String.valueOf(id)});
                        JSONArray fx=fee.get(id);if(fx!=null)for(int j=0;j<fx.length();j++)invoke740("insertFee740",new Class<?>[]{SQLiteDatabase.class,JSONObject.class},d,fx.getJSONObject(j));
                        invoke740("mark740",new Class<?>[]{SQLiteDatabase.class,long.class,String.class},d,id,"");
                    }
                    String at=deleted.get(id);
                    if(at!=null&&!at.isEmpty()){ContentValues v=new ContentValues();v.put("deletedAt",at);d.update("athletes",v,"id=?",new String[]{String.valueOf(id)});}
                }
                invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,false);
                d.setTransactionSuccessful();
            }finally{
                try{invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,false);}catch(Exception ignored){}
                d.endTransaction();
            }

            cloudPrefs.edit().putString("delta_cursor_750",cursor).apply();
            int changed=s.optInt("changed_count",ids.length());
            if(announce){final int n=pushed,c=changed;runOnUiThread(()->{toast("EŞZAMANLAMA TAMAM • "+n+" GÖNDERİLDİ • "+c+" SPORCU GÜNCELLENDİ");showHome();});}
            else if(changed>0)runOnUiThread(this::showHome);
        }catch(Exception e){
            if(announce){String m=rootCause750(e);runOnUiThread(()->toast("EŞZAMANLAMA DURDU • YEREL VERİ KORUNDU • "+m));}
        }finally{running750.set(false);}
    }

    private JSONObject fetchDelta750(String since)throws Exception{
        String token=cloudPrefs.getString("access_token","");
        String body=(since==null||since.isEmpty())?"{\"p_since\":null}":new JSONObject().put("p_since",since).toString();
        HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_delta_snapshot_v1",body,token);
        if(r.code==401&&refreshSession()){
            token=cloudPrefs.getString("access_token","");
            r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_delta_snapshot_v1",body,token);
        }
        if(r.code<200||r.code>=300)throw new Exception("BULUT HTTP "+r.code);
        return new JSONObject(r.body);
    }

    private String rootCause750(Throwable t){Throwable x=t;while(x.getCause()!=null)x=x.getCause();String m=x.getMessage();return m==null?x.getClass().getSimpleName():m;}

    @Override protected void onDestroy(){delta750.shutdownNow();super.onDestroy();}
}

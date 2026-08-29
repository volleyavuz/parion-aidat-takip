package com.parion.aidat;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import org.json.*;
import java.lang.reflect.Method;
import java.util.concurrent.*;

/** v4.2.21 - atomic cloud recovery + post-recovery sync-state seeding. */
public class MainActivityV742 extends MainActivityV741 {
    private final ExecutorService recovery742=Executors.newSingleThreadExecutor();
    private volatile boolean recovering742=false;

    @Override void showCloudMenu(){
        String[] items={"ŞİMDİ EŞZAMANLA","BULUTTAN GÜVENLİ KURTAR","OTURUMU KAPAT"};
        new AlertDialog.Builder(this).setTitle("GÜVENLİ SENKRONİZASYON").setItems(items,(d,w)->{
            if(w==0)syncFromCloud(true);
            else if(w==1)new AlertDialog.Builder(this).setTitle("BULUTTAN GÜVENLİ KURTAR")
                    .setMessage("Bulut snapshot'ı önce tamamen indirilip doğrulanacak. Doğrulama başarısız olursa cihazdaki hiçbir veri silinmeyecek. Devam edilsin mi?")
                    .setNegativeButton("VAZGEÇ",null).setPositiveButton("KURTAR",(a,b)->safeRecover742()).show();
            else{cloudPrefs.edit().clear().apply();showLogin();}
        }).show();
    }

    private void safeRecover742(){
        if(recovering742)return; recovering742=true; toast("BULUT SNAPSHOT'I DOĞRULANIYOR...");
        recovery742.execute(()->{
            try{
                String token=cloudPrefs.getString("access_token","");
                HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_recovery_snapshot_v1","{}",token);
                if(r.code==401&&refreshSession()){
                    token=cloudPrefs.getString("access_token","");
                    r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_recovery_snapshot_v1","{}",token);
                }
                if(r.code<200||r.code>=300)throw new Exception("SNAPSHOT HTTP "+r.code);
                JSONObject s=new JSONObject(r.body);
                JSONArray aa=s.getJSONArray("athletes"),pp=s.getJSONArray("payments"),ff=s.getJSONArray("fees"),dd=s.getJSONArray("deleted");
                int ac=s.optInt("athlete_count",-1),pc=s.optInt("payment_count",-1),fc=s.optInt("fee_count",-1);
                if(ac<200||pc<500||fc<300||aa.length()!=ac||pp.length()!=pc||ff.length()!=fc)
                    throw new Exception("SNAPSHOT EKSİK: "+aa.length()+"/"+pp.length()+"/"+ff.length());

                SQLiteDatabase d=db.getWritableDatabase(); d.beginTransaction();
                try{
                    ContentValues gv=new ContentValues();gv.put("applying_remote",1);d.update("sync_guard",gv,"id=1",null);
                    d.delete("payment_recent",null,null);d.delete("sync_state",null,null);d.delete("athlete_restart_periods",null,null);
                    d.delete("fee_history",null,null);d.delete("payments",null,null);d.delete("athletes",null,null);d.delete("pending_sync",null,null);
                    for(int i=0;i<aa.length();i++)insertAthlete742(d,aa.getJSONObject(i));
                    for(int i=0;i<pp.length();i++)insertPayment742(d,pp.getJSONObject(i));
                    for(int i=0;i<ff.length();i++)insertFee742(d,ff.getJSONObject(i));
                    for(int i=0;i<dd.length();i++)markDeleted742(d,dd.getJSONObject(i));
                    seedSyncState742(d,aa);
                    gv.put("applying_remote",0);d.update("sync_guard",gv,"id=1",null);
                    d.setTransactionSuccessful();
                }finally{try{ContentValues gv=new ContentValues();gv.put("applying_remote",0);d.update("sync_guard",gv,"id=1",null);}catch(Exception ignored){}d.endTransaction();}
                runOnUiThread(()->{toast("KURTARMA TAMAMLANDI • "+ac+" SPORCU • "+pc+" ÖDEME");showHome();});
            }catch(Exception e){String m=e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();runOnUiThread(()->toast("KURTARMA İPTAL • YEREL VERİYE DOKUNULMADI • "+m));}
            finally{recovering742=false;}
        });
    }

    private void seedSyncState742(SQLiteDatabase d,JSONArray aa){
        long now=System.currentTimeMillis();
        for(int i=0;i<aa.length();i++)try{
            JSONObject o=aa.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id<=0)continue;
            ContentValues v=new ContentValues();v.put("entity","ATHLETE");v.put("entityKey",String.valueOf(id));v.put("localHash",hash742(id));v.put("cloudUpdatedAt","");v.put("lastSyncedAt",now);
            d.insertWithOnConflict("sync_state",null,v,SQLiteDatabase.CONFLICT_REPLACE);
        }catch(Exception ignored){}
    }
    private String hash742(long id){try{Method m=MainActivityV735.class.getDeclaredMethod("hash735",long.class);m.setAccessible(true);return String.valueOf(m.invoke(this,id));}catch(Exception e){return "";}}

    private void insertAthlete742(SQLiteDatabase d,JSONObject o){
        long id=o.optLong("legacy_id",-1);if(id<=0)return;ContentValues v=new ContentValues();v.put("id",id);
        putI(v,"seq",o,"seq");putI(v,"birthYear",o,"birth_year");putS(v,"birthDate",o,"birth_date");putS(v,"name",o,"name");putS(v,"category",o,"category");putS(v,"status",o,"status");putI(v,"monthlyFee",o,"monthly_fee");putS(v,"sibling",o,"sibling");putS(v,"notes",o,"notes");putS(v,"phone",o,"phone");putS(v,"motherName",o,"mother_name");putS(v,"motherPhone",o,"mother_phone");putS(v,"fatherName",o,"father_name");putS(v,"fatherPhone",o,"father_phone");putS(v,"startDate",o,"start_date");putS(v,"endDate",o,"end_date");putS(v,"restartDate",o,"restart_date");putS(v,"tckn",o,"tckn");
        if(o.has("photo")&&!o.isNull("photo")){String p=o.optString("photo","");if(!p.isEmpty())v.put("photo",p);}if(o.has("deleted_at")&&!o.isNull("deleted_at"))v.put("deletedAt",o.optString("deleted_at",""));else v.put("deletedAt","");
        d.insertWithOnConflict("athletes",null,v,SQLiteDatabase.CONFLICT_REPLACE);
    }
    private void insertPayment742(SQLiteDatabase d,JSONObject o){long id=o.optLong("legacy_id",-1);int m=o.optInt("month",0);if(id<=0||m<1||m>12)return;ContentValues v=new ContentValues();v.put("athleteId",id);v.put("year",o.optInt("year",2026));v.put("month",m);v.put("marker",o.optString("marker",""));v.put("amount",o.optInt("amount",0));d.insertWithOnConflict("payments",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
    private void insertFee742(SQLiteDatabase d,JSONObject o){long id=o.optLong("legacy_id",-1);int m=o.optInt("month",0);if(id<=0||m<1||m>12)return;ContentValues v=new ContentValues();v.put("athleteId",id);v.put("year",o.optInt("year",2026));v.put("effectiveMonth",m);v.put("fee",o.optInt("fee",0));d.insertWithOnConflict("fee_history",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
    private void markDeleted742(SQLiteDatabase d,JSONObject o){long id=o.optLong("legacy_id",-1);if(id<=0)return;String x=o.optString("deleted_at","");if(x.isEmpty())return;ContentValues v=new ContentValues();v.put("deletedAt",x);d.update("athletes",v,"id=?",new String[]{String.valueOf(id)});}
    private void putS(ContentValues v,String c,JSONObject o,String k){if(o.has(k)&&!o.isNull(k))v.put(c,o.optString(k,""));}
    private void putI(ContentValues v,String c,JSONObject o,String k){if(o.has(k)&&!o.isNull(k))v.put(c,o.optInt(k,0));}
    @Override protected void onDestroy(){recovery742.shutdownNow();super.onDestroy();}
}

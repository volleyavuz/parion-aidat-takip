package com.parion.aidat;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.json.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/** v4.2.2 - conflict-checked per-athlete multi-device sync; all bulk cloud writes blocked. */
public class MainActivityV727 extends MainActivityV726 {
    private final ExecutorService safe727=Executors.newSingleThreadExecutor();
    private volatile boolean running727=false;
    private volatile Thread allowedDeltaThread727=null;
    private volatile long restartPull727=0L;

    @Override HttpResult request(String method,String url,String body,String bearer)throws Exception{
        if(url!=null&&url.contains("/rpc/parion_sync_mobile_snapshot"))return new HttpResult(423,"{\"error\":\"SAFE_MULTI_DEVICE_SNAPSHOT_BLOCKED\"}");
        if(url!=null&&url.contains("/rpc/parion_sync_attendance_snapshot"))return new HttpResult(423,"{\"error\":\"SAFE_MULTI_DEVICE_ATTENDANCE_SNAPSHOT_BLOCKED\"}");
        if(url!=null&&url.contains("/rpc/parion_sync_one_athlete_delta_v4")&&Thread.currentThread()!=allowedDeltaThread727)return new HttpResult(423,"{\"error\":\"DELTA_REQUIRES_SAFE_PREFLIGHT\"}");
        return super.request(method,url,body,bearer);
    }

    @Override void syncFromCloud(boolean announce){
        if(running727||cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty())return;
        if(db==null||db.count(null)==0){super.syncFromCloud(announce);return;}
        running727=true;if(announce)toast("GÜVENLİ SENKRONİZASYON • KONTROL EDİLİYOR...");
        safe727.execute(()->reconcile727(announce));
    }

    private void reconcile727(boolean announce){
        try{
            final ArrayList<Long> dirty=findDirty727();ArrayList<Long> conflicts=new ArrayList<>();
            for(long id:dirty){
                Stamp727 cloud=cloudStamp727(id);long baseline=lastSynced727(id);boolean hasBaseline=!savedHash727(id).isEmpty();
                if(cloud.exists&&(!hasBaseline||(baseline>0&&cloud.time>baseline+1500L))){conflicts.add(id);continue;}
                if(!safePush727(id))throw new IllegalStateException("Sporcu "+id+" doğrulanamadı");
            }
            if(!conflicts.isEmpty()){final String names=conflictNames727(conflicts);runOnUiThread(()->{running727=false;new AlertDialog.Builder(this).setTitle("SENKRONİZASYON ÇAKIŞMASI").setMessage("Bu cihazda ve bulutta aynı kayıt üzerinde farklı değişiklikler bulundu. Veri kaybını önlemek için hiçbir taraf ezilmedi.\n\n"+names).setPositiveButton("TAMAM",null).show();});return;}
            if(!pushRestartEnds727())throw new IllegalStateException("Yeniden bırakma tarihleri çakıştı veya gönderilemedi");
            final int pushed=dirty.size();runOnUiThread(()->{running727=false;pullAfter727(announce);if(pushed>0)toast(pushed+" YEREL DEĞİŞİKLİK GÜVENLE BULUTA AKTARILDI • BULUT VERİLERİ ALINIYOR");});
        }catch(Exception e){final String m=e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();runOnUiThread(()->{running727=false;toast("GÜVENLİ SENKRONİZASYON DURDU • "+m);});}
    }
    private void pullAfter727(boolean announce){super.syncFromCloud(announce);}

    @Override void showHome(){super.showHome();if(db==null||db.count(null)==0||cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty())return;long now=System.currentTimeMillis();if(now-restartPull727>60000L){restartPull727=now;safe727.execute(this::pullRestartEnds727);}}

    @Override void showCloudMenu(){int n=0;try{n=findDirty727().size();}catch(Exception ignored){}final int pending=n;String[] items={"ŞİMDİ GÜVENLİ SENKRONİZE ET","BEKLEYEN YEREL DEĞİŞİKLİK: "+pending,"OTURUMU KAPAT"};new AlertDialog.Builder(this).setTitle("GÜVENLİ ÇOKLU CİHAZ SENKRONİZASYONU").setItems(items,(d,w)->{if(w==0)syncFromCloud(true);else if(w==1)toast(pending==0?"BEKLEYEN DEĞİŞİKLİK YOK.":pending+" DEĞİŞİKLİK BEKLİYOR.");else{cloudPrefs.edit().clear().apply();showLogin();}}).show();}

    private ArrayList<Long> findDirty727(){ArrayList<Long> out=new ArrayList<>();Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes",null);while(c.moveToNext()){long id=c.getLong(0);if(!hash727(id).equals(savedHash727(id)))out.add(id);}c.close();return out;}
    private String hash727(long id){StringBuilder b=new StringBuilder();SQLiteDatabase d=db.getReadableDatabase();Cursor a=d.rawQuery("SELECT * FROM athletes WHERE id=?",new String[]{String.valueOf(id)});if(a.moveToFirst())for(int i=0;i<a.getColumnCount();i++){String n=a.getColumnName(i);if("photo".equalsIgnoreCase(n))continue;b.append(n).append('=').append(a.isNull(i)?"":a.getString(i)).append('|');}a.close();Cursor p=d.rawQuery("SELECT year,month,marker,amount FROM payments WHERE athleteId=? ORDER BY year,month",new String[]{String.valueOf(id)});while(p.moveToNext())b.append("P:").append(p.getInt(0)).append(':').append(p.getInt(1)).append(':').append(p.getString(2)).append(':').append(p.getInt(3)).append('|');p.close();Cursor f=d.rawQuery("SELECT year,effectiveMonth,fee FROM fee_history WHERE athleteId=? ORDER BY year,effectiveMonth",new String[]{String.valueOf(id)});while(f.moveToNext())b.append("F:").append(f.getInt(0)).append(':').append(f.getInt(1)).append(':').append(f.getInt(2)).append('|');f.close();return Integer.toHexString(b.toString().hashCode());}
    private String savedHash727(long id){Cursor c=db.getReadableDatabase().rawQuery("SELECT localHash FROM sync_state WHERE entity='ATHLETE' AND entityKey=?",new String[]{String.valueOf(id)});String x="";if(c.moveToFirst()&&!c.isNull(0))x=c.getString(0);c.close();return x==null?"":x;}
    private long lastSynced727(long id){Cursor c=db.getReadableDatabase().rawQuery("SELECT lastSyncedAt FROM sync_state WHERE entity='ATHLETE' AND entityKey=?",new String[]{String.valueOf(id)});long x=0;if(c.moveToFirst())x=c.getLong(0);c.close();return x;}
    private void mark727(long id,String cloudAt){ContentValues v=new ContentValues();v.put("entity","ATHLETE");v.put("entityKey",String.valueOf(id));v.put("localHash",hash727(id));v.put("cloudUpdatedAt",cloudAt);v.put("lastSyncedAt",System.currentTimeMillis());db.getWritableDatabase().insertWithOnConflict("sync_state",null,v,SQLiteDatabase.CONFLICT_REPLACE);}

    private Stamp727 cloudStamp727(long id)throws Exception{HttpResult r=getAuthed("/rest/v1/athletes?legacy_id=eq."+id+"&select=legacy_id,updated_at&limit=1");if(r.code==401&&refreshSession())r=getAuthed("/rest/v1/athletes?legacy_id=eq."+id+"&select=legacy_id,updated_at&limit=1");if(r.code<200||r.code>=300)throw new IllegalStateException("Bulut kontrolü HTTP "+r.code);JSONArray a=new JSONArray(r.body);if(a.length()==0)return new Stamp727(false,0,"");String raw=a.getJSONObject(0).optString("updated_at","");return new Stamp727(true,parseTime727(raw),raw);}
    private boolean safePush727(long id)throws Exception{JSONObject body=body727(id);String token=cloudPrefs.getString("access_token","");HttpResult r;allowedDeltaThread727=Thread.currentThread();try{r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_one_athlete_delta_v4",body.toString(),token);if(r.code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_one_athlete_delta_v4",body.toString(),token);}}finally{allowedDeltaThread727=null;}if(r.code<200||r.code>=300)return false;Stamp727 s=cloudStamp727(id);if(!s.exists)return false;mark727(id,s.raw);return true;}

    private JSONObject body727(long id)throws Exception{SQLiteDatabase d=db.getReadableDatabase();Cursor a=d.rawQuery("SELECT * FROM athletes WHERE id=?",new String[]{String.valueOf(id)});if(!a.moveToFirst()){a.close();throw new IllegalStateException("Yerel sporcu yok: "+id);}JSONObject x=new JSONObject().put("legacy_id",id);String[][] m={{"seq","seq"},{"birth_year","birthYear"},{"birth_date","birthDate"},{"name","name"},{"category","category"},{"status","status"},{"monthly_fee","monthlyFee"},{"sibling","sibling"},{"tshirt_qty","tshirtQty"},{"tshirt_paid","tshirtPaid"},{"tracksuit_qty","tracksuitQty"},{"tracksuit_paid","tracksuitPaid"},{"notes","notes"},{"phone","phone"},{"mother_name","motherName"},{"mother_phone","motherPhone"},{"father_name","fatherName"},{"father_phone","fatherPhone"},{"start_date","startDate"},{"end_date","endDate"},{"restart_date","restartDate"},{"tckn","tckn"}};for(String[] z:m)pc727(x,z[0],a,z[1]);int si=a.getColumnIndex("summerCall"),wi=a.getColumnIndex("winterCall");x.put("summer_call",si>=0&&a.getInt(si)==1);x.put("winter_call",wi>=0&&a.getInt(wi)==1);a.close();JSONArray ps=new JSONArray();Cursor p=d.rawQuery("SELECT year,month,marker,amount FROM payments WHERE athleteId=? ORDER BY year,month",new String[]{String.valueOf(id)});while(p.moveToNext())ps.put(new JSONObject().put("legacy_id",id).put("year",p.getInt(0)).put("month",p.getInt(1)).put("marker",p.getString(2)==null?"":p.getString(2)).put("amount",p.getInt(3)));p.close();JSONArray fs=new JSONArray();Cursor f=d.rawQuery("SELECT year,effectiveMonth,fee FROM fee_history WHERE athleteId=? ORDER BY year,effectiveMonth",new String[]{String.valueOf(id)});while(f.moveToNext())fs.put(new JSONObject().put("legacy_id",id).put("year",f.getInt(0)).put("month",f.getInt(1)).put("fee",f.getInt(2)));f.close();return new JSONObject().put("p_legacy_id",id).put("p_athlete",x).put("p_payments",ps).put("p_fees",fs);}
    private void pc727(JSONObject o,String k,Cursor c,String col)throws Exception{int i=c.getColumnIndex(col);if(i<0||c.isNull(i)){o.put(k,"");return;}if(c.getType(i)==Cursor.FIELD_TYPE_INTEGER)o.put(k,c.getLong(i));else o.put(k,c.getString(i)==null?"":c.getString(i));}

    private boolean pushRestartEnds727(){try{Cursor c=db.getReadableDatabase().rawQuery("SELECT athleteId,restartEndDate FROM athlete_restart_periods WHERE TRIM(COALESCE(restartEndDate,''))<>''",null);while(c.moveToNext()){long id=c.getLong(0);String date=c.getString(1);HttpResult g=getAuthed("/rest/v1/athlete_membership_events?legacy_id=eq."+id+"&event_type=eq.RESTART_END&select=id,event_date&limit=1");if(g.code<200||g.code>=300){c.close();return false;}JSONArray a=new JSONArray(g.body);if(a.length()>0){if(!date.equals(a.getJSONObject(0).optString("event_date",""))){c.close();return false;}continue;}JSONObject j=new JSONObject().put("legacy_id",id).put("event_date",date).put("event_type","RESTART_END").put("note","SAFE MULTI DEVICE");HttpResult p=request("POST",SUPABASE_URL+"/rest/v1/athlete_membership_events",j.toString(),cloudPrefs.getString("access_token",""));if(p.code<200||p.code>=300){c.close();return false;}}c.close();return true;}catch(Exception e){return false;}}
    private void pullRestartEnds727(){try{HttpResult r=getAuthed("/rest/v1/athlete_membership_events?event_type=eq.RESTART_END&select=legacy_id,event_date,updated_at&order=updated_at.asc");if(r.code<200||r.code>=300)return;JSONArray a=new JSONArray(r.body);SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();try{d.delete("athlete_restart_periods",null,null);for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);long id=o.optLong("legacy_id",-1);String date=o.optString("event_date","");if(id<=0||date.isEmpty())continue;ContentValues v=new ContentValues();v.put("athleteId",id);v.put("restartEndDate",date);d.insertWithOnConflict("athlete_restart_periods",null,v,SQLiteDatabase.CONFLICT_REPLACE);}d.setTransactionSuccessful();}finally{d.endTransaction();}}catch(Exception ignored){}}
    private String conflictNames727(List<Long> ids){StringBuilder b=new StringBuilder();for(long id:ids){Cursor c=db.athlete(id);String n=String.valueOf(id);if(c.moveToFirst()){int i=c.getColumnIndex("name");if(i>=0&&!c.isNull(i))n=c.getString(i);}c.close();if(b.length()>0)b.append("\n");b.append("• ").append(n);}return b.toString();}
    private long parseTime727(String raw){if(raw==null||raw.isEmpty())return 0;String[] p={"yyyy-MM-dd'T'HH:mm:ss.SSSXXX","yyyy-MM-dd'T'HH:mm:ssXXX","yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX"};for(String x:p)try{return new SimpleDateFormat(x,Locale.US).parse(raw).getTime();}catch(Exception ignored){}return 0;}
    private static class Stamp727{final boolean exists;final long time;final String raw;Stamp727(boolean e,long t,String r){exists=e;time=t;raw=r;}}
    @Override protected void onDestroy(){safe727.shutdownNow();super.onDestroy();}
}

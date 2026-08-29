package com.parion.aidat;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import org.json.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import okhttp3.*;

/**
 * v4.2.17 - canonical multi-device sync.
 *
 * Architecture:
 *   LOCAL-FIRST -> SQLite pending queue -> ONE LWW RPC -> Supabase Realtime invalidation
 *   -> targeted cloud pull -> SQLite -> UI.
 *
 * Legacy athlete snapshot writes, V600 delta sender, V727 conflict/delta chain,
 * V735 stacked sync and V737 four-second payment polling are not used by this class.
 * WorkManager remains only as a safety/fallback mechanism.
 */
public class MainActivityV740 extends MainActivityV739 {
    private static final String RT_PREF="parion_realtime_v4217";
    private static final String RT_TOPIC="realtime:parion-v4217";
    private static final long LOCAL_SCAN_MS=350L;
    private final ExecutorService canonical740=Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService timers740=Executors.newSingleThreadScheduledExecutor();
    private final Set<Long> remoteAthletes740=Collections.newSetFromMap(new ConcurrentHashMap<Long,Boolean>());
    private final AtomicLong ref740=new AtomicLong(1L);
    private final OkHttpClient wsClient740=new OkHttpClient.Builder().pingInterval(20,TimeUnit.SECONDS).build();
    private volatile WebSocket socket740;
    private volatile boolean resumed740=false;
    private volatile boolean syncRunning740=false;
    private volatile boolean destroyed740=false;
    private volatile int reconnectAttempt740=0;

    @Override public void onCreate(Bundle b){
        disableLegacySync740();
        super.onCreate(b);
        ensureQueue740();
        disableLegacySync740();
        // Do not run the legacy all-athlete hash sweep on every startup. Safe recovery seeds
        // sync_state and all subsequent edits are captured by the SQLite triggers below.
        // The old sweep performed multiple SQLite scans per athlete and delayed first paint.
        scheduleLocalPump740();
    }

    /** Stop inherited writers/pollers while preserving all UI/media behavior. */
    private void disableLegacySync740(){
        try{Field f=MainActivityV600.class.getDeclaredField("delta600");f.setAccessible(true);f.setBoolean(this,true);}catch(Exception ignored){}
        try{Field f=MainActivityV737.class.getDeclaredField("liveEnabled737");f.setAccessible(true);f.setBoolean(this,false);}catch(Exception ignored){}
    }

    @Override protected void onResume(){
        super.onResume();
        disableLegacySync740();
        resumed740=true;
        connectRealtime740();
        requestCanonical740(false,true);
    }

    @Override protected void onPause(){
        resumed740=false;
        closeRealtime740();
        super.onPause();
    }

    @Override void syncFromCloud(boolean announce){
        requestCanonical740(announce,true);
    }

    @Override void showCloudMenu(){
        int pending=pendingCount740();
        String state=socket740!=null?"REALTIME BAĞLI":"REALTIME YENİDEN BAĞLANIYOR";
        String[] items={"ŞİMDİ EŞZAMANLA","DURUM • "+state+" • BEKLEYEN: "+pending,"BULUTTAN TEMİZ GERİ YÜKLE","OTURUMU KAPAT"};
        new AlertDialog.Builder(this).setTitle("ANLIK ÇOKLU CİHAZ SENKRONİZASYONU").setItems(items,(d,w)->{
            if(w==0)requestCanonical740(true,true);
            else if(w==1)toast(state+" • "+pending+" YEREL DEĞİŞİKLİK BEKLİYOR");
            else if(w==2)invokeNoArg740(MainActivityV730.class,"confirmRestore730");
            else{closeRealtime740();cloudPrefs.edit().clear().apply();showLogin();}
        }).show();
    }

    /** Never permit old bulk writers. Delta RPC is also blocked because V740 has one canonical writer. */
    @Override HttpResult request(String method,String url,String body,String bearer)throws Exception{
        if(url!=null&&(url.contains("/rpc/parion_sync_mobile_snapshot")||url.contains("/rpc/parion_sync_attendance_snapshot")))
            return new HttpResult(423,"{\"error\":\"V4217_LEGACY_SNAPSHOT_DISABLED\"}");
        if(url!=null&&url.contains("/rpc/parion_sync_one_athlete_delta_v4"))
            return new HttpResult(423,"{\"error\":\"V4217_USE_CANONICAL_LWW\"}");
        return super.request(method,url,body,bearer);
    }

    private void ensureQueue740(){
        if(db==null)return;SQLiteDatabase d=db.getWritableDatabase();
        d.execSQL("CREATE TABLE IF NOT EXISTS pending_sync(kind TEXT NOT NULL,entity_key TEXT NOT NULL,created_at INTEGER NOT NULL,PRIMARY KEY(kind,entity_key))");
        d.execSQL("CREATE TABLE IF NOT EXISTS sync_guard(id INTEGER PRIMARY KEY,applying_remote INTEGER NOT NULL DEFAULT 0)");
        d.execSQL("INSERT OR IGNORE INTO sync_guard(id,applying_remote) VALUES(1,0)");
        d.execSQL("DROP TRIGGER IF EXISTS rt740_athlete_insert");d.execSQL("DROP TRIGGER IF EXISTS rt740_athlete_update");
        d.execSQL("DROP TRIGGER IF EXISTS rt740_payment_insert");d.execSQL("DROP TRIGGER IF EXISTS rt740_payment_update");d.execSQL("DROP TRIGGER IF EXISTS rt740_payment_delete");
        d.execSQL("DROP TRIGGER IF EXISTS rt740_fee_insert");d.execSQL("DROP TRIGGER IF EXISTS rt740_fee_update");d.execSQL("DROP TRIGGER IF EXISTS rt740_fee_delete");
        String when=" WHEN (SELECT applying_remote FROM sync_guard WHERE id=1)=0 ";
        String now="CAST(strftime('%s','now') AS INTEGER)*1000";
        d.execSQL("CREATE TRIGGER rt740_athlete_insert AFTER INSERT ON athletes"+when+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('ATHLETE',NEW.id,"+now+"); END");
        d.execSQL("CREATE TRIGGER rt740_athlete_update AFTER UPDATE ON athletes"+when+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('ATHLETE',NEW.id,"+now+"); END");
        d.execSQL("CREATE TRIGGER rt740_payment_insert AFTER INSERT ON payments"+when+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('ATHLETE',NEW.athleteId,"+now+"); END");
        d.execSQL("CREATE TRIGGER rt740_payment_update AFTER UPDATE ON payments"+when+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('ATHLETE',NEW.athleteId,"+now+"); END");
        d.execSQL("CREATE TRIGGER rt740_payment_delete AFTER DELETE ON payments"+when+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('ATHLETE',OLD.athleteId,"+now+"); END");
        d.execSQL("CREATE TRIGGER rt740_fee_insert AFTER INSERT ON fee_history"+when+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('ATHLETE',NEW.athleteId,"+now+"); END");
        d.execSQL("CREATE TRIGGER rt740_fee_update AFTER UPDATE ON fee_history"+when+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('ATHLETE',NEW.athleteId,"+now+"); END");
        d.execSQL("CREATE TRIGGER rt740_fee_delete AFTER DELETE ON fee_history"+when+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('ATHLETE',OLD.athleteId,"+now+"); END");
    }

    /** Kept only for explicit migration/debug use; no longer invoked during Activity startup. */
    private void seedDirty740(){
        if(db==null)return;
        try{Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes",null);while(c.moveToNext()){long id=c.getLong(0);String h=hash740(id),s=savedHash740(id);if(!h.equals(s))enqueue740(id);}c.close();}catch(Exception ignored){}
    }

    private void scheduleLocalPump740(){
        timers740.scheduleWithFixedDelay(()->{
            if(destroyed740||!resumed740||!hasSession740())return;
            if(pendingCount740()>0)requestCanonical740(false,false);
        },250L,LOCAL_SCAN_MS,TimeUnit.MILLISECONDS);
    }

    private boolean hasSession740(){return cloudPrefs!=null&&!cloudPrefs.getString("access_token","").isEmpty();}
    private int pendingCount740(){if(db==null)return 0;try{Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM pending_sync",null);int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;}catch(Exception e){return 0;}}
    private boolean pending740(long id){try{Cursor c=db.getReadableDatabase().rawQuery("SELECT 1 FROM pending_sync WHERE kind='ATHLETE' AND entity_key=? LIMIT 1",new String[]{String.valueOf(id)});boolean x=c.moveToFirst();c.close();return x;}catch(Exception e){return false;}}
    private void enqueue740(long id){if(id<=0||db==null)return;ContentValues v=new ContentValues();v.put("kind","ATHLETE");v.put("entity_key",String.valueOf(id));v.put("created_at",System.currentTimeMillis());db.getWritableDatabase().insertWithOnConflict("pending_sync",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
    private void dequeue740(long id){if(db!=null)db.getWritableDatabase().delete("pending_sync","kind='ATHLETE' AND entity_key=?",new String[]{String.valueOf(id)});}

    private void requestCanonical740(boolean announce,boolean catchup){
        if(!hasSession740()||db==null)return;
        canonical740.execute(()->canonicalPass740(announce,catchup));
    }

    private void canonicalPass740(boolean announce,boolean catchup){
        if(syncRunning740)return;syncRunning740=true;
        try{
            int pushed=flushPending740();
            drainRemoteAthletes740();
            if(catchup&&pendingCount740()==0)bulkCatchup740();
            invokeAttendanceSync740(announce);
            try{reconcileRecentPayments737();}catch(Exception ignored){}
            if(announce){final int n=pushed;runOnUiThread(()->toast("EŞZAMANLAMA TAMAM • "+n+" YEREL DEĞİŞİKLİK BULUTA AKTARILDI"));}
        }catch(Exception e){if(announce){String m=short740(e);runOnUiThread(()->toast("EŞZAMANLAMA DURDU • "+m));}}
        finally{syncRunning740=false;}
    }

    private int flushPending740()throws Exception{
        int pushed=0;
        for(int round=0;round<50;round++){
            ArrayList<Long> ids=new ArrayList<>();Cursor c=db.getReadableDatabase().rawQuery("SELECT entity_key FROM pending_sync WHERE kind='ATHLETE' ORDER BY created_at ASC LIMIT 20",null);while(c.moveToNext())try{ids.add(Long.parseLong(c.getString(0)));}catch(Exception ignored){}c.close();
            if(ids.isEmpty())break;
            boolean progress=false;
            for(long id:ids){
                String now=hash740(id),saved=savedHash740(id);
                if(now.equals(saved)){dequeue740(id);progress=true;continue;}
                if(pushOne740(id,now)){pushed++;progress=true;}
                else return pushed;
            }
            if(!progress)break;
        }
        return pushed;
    }

    private boolean pushOne740(long id,String before)throws Exception{
        JSONObject body=body740(id);String token=cloudPrefs.getString("access_token","");
        HttpResult r=super.request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_one_athlete_lww_v411",body.toString(),token);
        if(r.code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");r=super.request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_one_athlete_lww_v411",body.toString(),token);}
        if(r.code<200||r.code>=300)throw new IOException("SPORCU "+id+" HTTP "+r.code);
        if(!before.equals(hash740(id))){enqueue740(id);return true;}
        if(!pullOneAthlete740(id,before)){enqueue740(id);return true;}
        dequeue740(id);return true;
    }

    private void drainRemoteAthletes740()throws Exception{
        ArrayList<Long> ids=new ArrayList<>(remoteAthletes740);remoteAthletes740.removeAll(ids);
        for(long id:ids){
            if(pending740(id)){String h=hash740(id);if(!h.equals(savedHash740(id)))pushOne740(id,h);}
            if(!pending740(id))pullOneAthlete740(id,hash740(id));
        }
    }

    private boolean pullOneAthlete740(long id,String expectedLocal)throws Exception{
        HttpResult ar=getAuthed("/rest/v1/mobile_athletes?legacy_id=eq."+id+"&select=*&limit=1");
        HttpResult pr=getAuthed("/rest/v1/mobile_payments_legacy?legacy_id=eq."+id+"&select=*&order=year.asc,month.asc");
        HttpResult fr=getAuthed("/rest/v1/mobile_fee_history?legacy_id=eq."+id+"&select=*&order=year.asc,month.asc");
        HttpResult mr=getAuthed("/rest/v1/athletes?legacy_id=eq."+id+"&select=legacy_id,photo_path,registration_form_path,deleted_at,updated_at&limit=1");
        need740(ar);need740(pr);need740(fr);need740(mr);
        JSONArray aa=new JSONArray(ar.body),pp=new JSONArray(pr.body),ff=new JSONArray(fr.body),mm=new JSONArray(mr.body);
        SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();
        try{
            if(pending740(id)&&!expectedLocal.equals(hash740(id)))return false;
            guard740(d,true);
            JSONObject raw=mm.length()>0?mm.getJSONObject(0):null;
            if(aa.length()>0)mergeAthlete740(d,aa.getJSONObject(0),raw);
            else if(raw!=null&&raw.has("deleted_at")&&!raw.isNull("deleted_at")){ContentValues x=new ContentValues();x.put("deletedAt",raw.optString("deleted_at",""));d.update("athletes",x,"id=?",new String[]{String.valueOf(id)});}
            d.delete("payments","athleteId=?",new String[]{String.valueOf(id)});for(int i=0;i<pp.length();i++)insertPayment740(d,pp.getJSONObject(i));
            d.delete("fee_history","athleteId=?",new String[]{String.valueOf(id)});for(int i=0;i<ff.length();i++)insertFee740(d,ff.getJSONObject(i));
            mark740(d,id,raw==null?"":raw.optString("updated_at",""));
            guard740(d,false);d.setTransactionSuccessful();
        }finally{try{guard740(d,false);}catch(Exception ignored){}d.endTransaction();}
        return true;
    }

    private void bulkCatchup740()throws Exception{
        HttpResult ar=getAuthed("/rest/v1/mobile_athletes?select=*&order=legacy_id.asc");
        HttpResult pr=getAuthed("/rest/v1/mobile_payments_legacy?select=*&order=legacy_id.asc,year.asc,month.asc");
        HttpResult fr=getAuthed("/rest/v1/mobile_fee_history?select=*&order=legacy_id.asc,year.asc,month.asc");
        HttpResult mr=getAuthed("/rest/v1/athletes?select=legacy_id,photo_path,registration_form_path,deleted_at,updated_at&order=legacy_id.asc");
        need740(ar);need740(pr);need740(fr);need740(mr);
        JSONArray aa=new JSONArray(ar.body),pp=new JSONArray(pr.body),ff=new JSONArray(fr.body),mm=new JSONArray(mr.body);
        HashMap<Long,JSONObject> raw=new HashMap<>();for(int i=0;i<mm.length();i++)raw.put(mm.getJSONObject(i).optLong("legacy_id",-1),mm.getJSONObject(i));
        SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();
        try{
            if(pendingCount740()>0)return;
            guard740(d,true);
            HashSet<Long> cloudIds=new HashSet<>();
            for(int i=0;i<aa.length();i++){JSONObject o=aa.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id<=0)continue;cloudIds.add(id);mergeAthlete740(d,o,raw.get(id));}
            d.delete("payments",null,null);for(int i=0;i<pp.length();i++)insertPayment740(d,pp.getJSONObject(i));
            d.delete("fee_history",null,null);for(int i=0;i<ff.length();i++)insertFee740(d,ff.getJSONObject(i));
            for(Map.Entry<Long,JSONObject> e:raw.entrySet()){
                long id=e.getKey();JSONObject o=e.getValue();String del=o.isNull("deleted_at")?"":o.optString("deleted_at","");
                if(!del.isEmpty()){ContentValues v=new ContentValues();v.put("deletedAt",del);d.update("athletes",v,"id=?",new String[]{String.valueOf(id)});}
            }
            for(long id:cloudIds){JSONObject o=raw.get(id);mark740(d,id,o==null?"":o.optString("updated_at",""));}
            guard740(d,false);d.setTransactionSuccessful();
        }finally{try{guard740(d,false);}catch(Exception ignored){}d.endTransaction();}
        pullMaterials740();pullRestartEnds740();
    }

    private void mergeAthlete740(SQLiteDatabase d,JSONObject o,JSONObject raw)throws Exception{
        long id=o.optLong("legacy_id",-1);if(id<=0)return;ContentValues v=new ContentValues();v.put("id",id);
        putInt740(v,"seq",o,"seq");putInt740(v,"birthYear",o,"birth_year");putText740(v,"birthDate",o,"birth_date");putText740(v,"name",o,"name");putText740(v,"category",o,"category");putText740(v,"status",o,"status");putInt740(v,"monthlyFee",o,"monthly_fee");putText740(v,"sibling",o,"sibling");
        putInt740(v,"tshirtQty",o,"tshirt_qty");putInt740(v,"tshirtPaid",o,"tshirt_paid");putInt740(v,"tracksuitQty",o,"tracksuit_qty");putInt740(v,"tracksuitPaid",o,"tracksuit_paid");putText740(v,"notes",o,"notes");putText740(v,"phone",o,"phone");putText740(v,"motherName",o,"mother_name");putText740(v,"motherPhone",o,"mother_phone");putText740(v,"fatherName",o,"father_name");putText740(v,"fatherPhone",o,"father_phone");putText740(v,"startDate",o,"start_date");putText740(v,"endDate",o,"end_date");putText740(v,"restartDate",o,"restart_date");putText740(v,"tckn",o,"tckn");
        v.put("summerCall",o.optBoolean("summer_call",false)?1:0);v.put("winterCall",o.optBoolean("winter_call",false)?1:0);
        String p="";if(raw!=null&&!raw.isNull("photo_path"))p=raw.optString("photo_path","").trim();if(p.isEmpty())p=o.optString("photo","").trim();if(!p.isEmpty()&&!"null".equalsIgnoreCase(p)){v.put("photo","CLOUD:"+p);photoMap413().put(id,p);}if(raw!=null&&!raw.isNull("deleted_at"))v.put("deletedAt",raw.optString("deleted_at",""));else v.put("deletedAt","");
        Cursor ex=d.rawQuery("SELECT id FROM athletes WHERE id=?",new String[]{String.valueOf(id)});boolean exists=ex.moveToFirst();ex.close();if(exists)d.update("athletes",v,"id=?",new String[]{String.valueOf(id)});else d.insertOrThrow("athletes",null,v);
        if(raw!=null&&!raw.isNull("registration_form_path")){String f=raw.optString("registration_form_path","").trim();if(!f.isEmpty())formMap413().put(id,f);}
    }
    private void insertPayment740(SQLiteDatabase d,JSONObject p){long id=p.optLong("legacy_id",-1);int m=p.optInt("month",0);if(id<=0||m<1||m>12)return;ContentValues v=new ContentValues();v.put("athleteId",id);v.put("year",p.optInt("year",2026));v.put("month",m);v.put("marker",p.optString("marker",""));v.put("amount",p.optInt("amount",0));d.insertWithOnConflict("payments",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
    private void insertFee740(SQLiteDatabase d,JSONObject f){long id=f.optLong("legacy_id",-1);int m=f.optInt("month",0);if(id<=0||m<1||m>12)return;ContentValues v=new ContentValues();v.put("athleteId",id);v.put("year",f.optInt("year",2026));v.put("effectiveMonth",m);v.put("fee",f.optInt("fee",0));d.insertWithOnConflict("fee_history",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
    private void putText740(ContentValues v,String c,JSONObject o,String k){if(o.has(k)&&!o.isNull(k))v.put(c,o.optString(k,""));}
    private void putInt740(ContentValues v,String c,JSONObject o,String k){if(o.has(k)&&!o.isNull(k))v.put(c,o.optInt(k,0));}
    private void guard740(SQLiteDatabase d,boolean on){ContentValues v=new ContentValues();v.put("applying_remote",on?1:0);d.update("sync_guard",v,"id=1",null);}
    private void mark740(SQLiteDatabase d,long id,String cloudAt){ContentValues v=new ContentValues();v.put("entity","ATHLETE");v.put("entityKey",String.valueOf(id));v.put("localHash",hash740(id));v.put("cloudUpdatedAt",cloudAt);v.put("lastSyncedAt",System.currentTimeMillis());d.insertWithOnConflict("sync_state",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
    private void need740(HttpResult r)throws IOException{if(r==null||r.code<200||r.code>=300)throw new IOException("BULUT HTTP "+(r==null?"?":r.code));}

    private void pullMaterials740(){
        try{HttpResult a=getAuthed("/rest/v1/material_products?select=*&order=name.asc"),b=getAuthed("/rest/v1/material_transactions?select=*&order=issued_at.asc,created_at.asc");need740(a);need740(b);JSONArray pp=new JSONArray(a.body),tt=new JSONArray(b.body);SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();try{guard740(d,true);d.delete("material_transactions",null,null);d.delete("material_products",null,null);for(int i=0;i<pp.length();i++){JSONObject o=pp.getJSONObject(i);ContentValues v=new ContentValues();v.put("name",o.optString("name",""));v.put("currentPrice",o.optInt("current_price",0));v.put("active",o.optBoolean("active",true)?1:0);v.put("cloudId",o.optString("id",""));d.insertWithOnConflict("material_products",null,v,SQLiteDatabase.CONFLICT_REPLACE);}for(int i=0;i<tt.length();i++){JSONObject o=tt.getJSONObject(i);long id=o.optLong("athlete_legacy_id",-1);if(id<=0)continue;ContentValues v=new ContentValues();v.put("cloudId",o.optString("id",""));v.put("athleteId",id);v.put("product",o.optString("product_name",""));v.put("qty",o.optInt("quantity",0));v.put("unitPrice",o.optInt("unit_price",0));v.put("total",o.optInt("total_amount",0));v.put("paidAmount",o.optInt("paid_amount",0));v.put("issuedDate",o.optString("issued_at",""));v.put("paymentDate",o.optString("payment_date",""));v.put("note",o.optString("note",""));d.insertWithOnConflict("material_transactions",null,v,SQLiteDatabase.CONFLICT_REPLACE);}guard740(d,false);d.setTransactionSuccessful();}finally{try{guard740(d,false);}catch(Exception ignored){}d.endTransaction();}}catch(Exception ignored){}
    }

    private String hash740(long id){try{Method m=MainActivityV735.class.getDeclaredMethod("hash735",long.class);m.setAccessible(true);return String.valueOf(m.invoke(this,id));}catch(Exception e){return "";}}
    private String savedHash740(long id){try{Cursor c=db.getReadableDatabase().rawQuery("SELECT localHash FROM sync_state WHERE entity='ATHLETE' AND entityKey=?",new String[]{String.valueOf(id)});String x="";if(c.moveToFirst()&&!c.isNull(0))x=c.getString(0);c.close();return x==null?"":x;}catch(Exception e){return "";}}
    private JSONObject body740(long id)throws Exception{Method m=MainActivityV727.class.getDeclaredMethod("body727",long.class);m.setAccessible(true);return (JSONObject)m.invoke(this,id);}
    private String short740(Exception e){String s=e.getMessage();if(s==null||s.trim().isEmpty())s=e.getClass().getSimpleName();return s.length()>90?s.substring(0,90):s;}

    private void invokeAttendanceSync740(boolean announce){try{Method m=MainActivityV731.class.getDeclaredMethod("syncAttendanceDelta731",boolean.class);m.setAccessible(true);m.invoke(this,announce);}catch(Exception ignored){}}
    private void pullAttendance740(){try{Method m=MainActivityV731.class.getDeclaredMethod("pullAttendance731",boolean.class);m.setAccessible(true);m.invoke(this,false);runOnUiThread(this::showHome);}catch(Exception ignored){}}
    private void pullRestartEnds740(){try{Method m=MainActivityV727.class.getDeclaredMethod("pullRestartEnds727");m.setAccessible(true);m.invoke(this);}catch(Exception ignored){}}
    private void invokeNoArg740(Class<?> owner,String name){try{Method m=owner.getDeclaredMethod(name);m.setAccessible(true);m.invoke(this);}catch(Exception e){toast("İŞLEM AÇILAMADI.");}}

    private void connectRealtime740(){
        if(!resumed740||destroyed740||!hasSession740()||socket740!=null)return;
        try{
            String base=SUPABASE_URL.replace("https://","wss://").replace("http://","ws://");
            String url=base+"/realtime/v1/websocket?apikey="+URLEncoder.encode(SUPABASE_KEY,"UTF-8")+"&vsn=1.0.0";
            Request req=new Request.Builder().url(url).build();socket740=wsClient740.newWebSocket(req,new WebSocketListener(){
                @Override public void onOpen(WebSocket ws,Response response){reconnectAttempt740=0;sendJoin740(ws);requestCanonical740(false,true);}
                @Override public void onMessage(WebSocket ws,String text){handleRealtime740(text);}
                @Override public void onClosed(WebSocket ws,int code,String reason){if(socket740==ws)socket740=null;scheduleReconnect740();}
                @Override public void onFailure(WebSocket ws,Throwable t,Response response){if(socket740==ws)socket740=null;scheduleReconnect740();}
            });
        }catch(Exception e){socket740=null;scheduleReconnect740();}
    }

    private void sendJoin740(WebSocket ws){
        try{
            JSONArray changes=new JSONArray();
            String[] tables={"athletes","payments","fee_periods","mobile_attendance_records","mobile_attendance_sessions","mobile_attendance_schedule","athlete_membership_events","material_products","material_transactions"};
            for(String t:tables)changes.put(new JSONObject().put("event","*").put("schema","public").put("table",t));
            JSONObject config=new JSONObject().put("broadcast",new JSONObject().put("ack",false).put("self",false)).put("presence",new JSONObject().put("enabled",false)).put("postgres_changes",changes).put("private",false);
            String ref=String.valueOf(ref740.getAndIncrement());JSONObject payload=new JSONObject().put("config",config).put("access_token",cloudPrefs.getString("access_token",""));JSONObject msg=new JSONObject().put("topic",RT_TOPIC).put("event","phx_join").put("payload",payload).put("ref",ref).put("join_ref",ref);ws.send(msg.toString());
        }catch(Exception ignored){}
    }

    private void handleRealtime740(String text){
        try{JSONObject x=new JSONObject(text);if(!"postgres_changes".equals(x.optString("event")))return;JSONObject p=x.optJSONObject("payload");JSONObject data=p==null?null:p.optJSONObject("data");if(data==null)return;String table=data.optString("table","");JSONObject r=data.optJSONObject("record");if(r==null||r.length()==0)r=data.optJSONObject("old_record");
            if("athletes".equals(table)){long id=r==null?-1:r.optLong("legacy_id",-1);if(id>0){remoteAthletes740.add(id);requestCanonical740(false,false);}}
            else if("payments".equals(table)||"fee_periods".equals(table)){String uuid=r==null?"":r.optString("athlete_id","");if(!uuid.isEmpty())canonical740.execute(()->resolveUuidAndPull740(uuid));}
            else if(table.startsWith("mobile_attendance_"))canonical740.execute(this::pullAttendance740);
            else if("athlete_membership_events".equals(table))canonical740.execute(this::pullRestartEnds740);
            else if(table.startsWith("material_"))canonical740.execute(this::pullMaterials740);
        }catch(Exception ignored){}
    }

    private void resolveUuidAndPull740(String uuid){
        try{HttpResult r=getAuthed("/rest/v1/athletes?id=eq."+uuid+"&select=legacy_id&limit=1");if(r.code<200||r.code>=300)return;JSONArray a=new JSONArray(r.body);if(a.length()==0)return;long id=a.getJSONObject(0).optLong("legacy_id",-1);if(id>0){remoteAthletes740.add(id);canonicalPass740(false,false);}}catch(Exception ignored){}
    }

    private void scheduleReconnect740(){
        if(!resumed740||destroyed740)return;int n=Math.min(++reconnectAttempt740,5);long delay=Math.min(30000L,1000L*(1L<<n));timers740.schedule(this::connectRealtime740,delay,TimeUnit.MILLISECONDS);
    }
    private void closeRealtime740(){WebSocket s=socket740;socket740=null;if(s!=null)try{s.close(1000,"pause");}catch(Exception ignored){}
    }

    @Override protected void onDestroy(){
        destroyed740=true;resumed740=false;closeRealtime740();timers740.shutdownNow();canonical740.shutdownNow();wsClient740.dispatcher().executorService().shutdown();wsClient740.connectionPool().evictAll();super.onDestroy();
    }
}

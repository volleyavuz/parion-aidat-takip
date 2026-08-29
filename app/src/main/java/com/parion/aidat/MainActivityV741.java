package com.parion.aidat;

import android.app.AlertDialog;
import android.os.Bundle;
import org.json.*;
import java.lang.reflect.*;
import java.net.URLEncoder;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.*;

/**
 * v4.2.18 - loop-safe realtime architecture.
 *
 * Realtime is INVALIDATION ONLY: many postgres events are coalesced into one catch-up.
 * The v4.2.17 canonical engine/socket are permanently suppressed to prevent event storms.
 * Local SQLite changes still enter pending_sync and are flushed through the single LWW RPC.
 */
public class MainActivityV741 extends MainActivityV740 {
    private final ExecutorService safe741=Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService timer741=Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean pass741=new AtomicBoolean(false);
    private final AtomicBoolean catchupQueued741=new AtomicBoolean(false);
    private final OkHttpClient wsClient741=new OkHttpClient.Builder().pingInterval(20,TimeUnit.SECONDS).build();
    private volatile WebSocket socket741;
    private volatile boolean resumed741=false;
    private volatile boolean booting741=true;
    private volatile int reconnect741=0;

    @Override public void onCreate(Bundle b){
        suppress740();
        super.onCreate(b);
        suppress740();
        booting741=false;
        timer741.scheduleWithFixedDelay(()->{
            if(!resumed741||!hasSession741())return;
            if(pendingCount741()>0)requestSafe741(false,false);
        },300,450,TimeUnit.MILLISECONDS);
    }

    /** V740 calls syncFromCloud from inherited lifecycle code; swallow during bootstrap. */
    @Override void syncFromCloud(boolean announce){
        if(booting741)return;
        requestSafe741(announce,true);
    }

    @Override protected void onResume(){
        suppress740();
        super.onResume();
        suppress740();
        resumed741=true;
        connect741();
        requestSafe741(false,true);
    }

    @Override protected void onPause(){
        resumed741=false;
        close741();
        suppress740();
        super.onPause();
        suppress740();
    }

    /** Permanently disable V740 socket + canonical pass; inherited scheduled tasks become no-ops. */
    private void suppress740(){
        try{Field f=MainActivityV740.class.getDeclaredField("destroyed740");f.setAccessible(true);f.setBoolean(this,true);}catch(Exception ignored){}
        try{Field f=MainActivityV740.class.getDeclaredField("syncRunning740");f.setAccessible(true);f.setBoolean(this,true);}catch(Exception ignored){}
        try{Field f=MainActivityV740.class.getDeclaredField("resumed740");f.setAccessible(true);f.setBoolean(this,false);}catch(Exception ignored){}
        try{Field f=MainActivityV737.class.getDeclaredField("liveEnabled737");f.setAccessible(true);f.setBoolean(this,false);}catch(Exception ignored){}
        try{Field f=MainActivityV600.class.getDeclaredField("delta600");f.setAccessible(true);f.setBoolean(this,true);}catch(Exception ignored){}
    }

    @Override void showCloudMenu(){
        int pending=pendingCount741();
        String state=socket741!=null?"REALTIME BAĞLI":"REALTIME YENİDEN BAĞLANIYOR";
        String[] items={"ŞİMDİ EŞZAMANLA","DURUM • "+state+" • BEKLEYEN: "+pending,"BULUTTAN TEMİZ GERİ YÜKLE","OTURUMU KAPAT"};
        new AlertDialog.Builder(this).setTitle("ANLIK SENKRONİZASYON • DÖNGÜ KORUMALI").setItems(items,(d,w)->{
            if(w==0)requestSafe741(true,true);
            else if(w==1)toast(state+" • "+pending+" YEREL DEĞİŞİKLİK BEKLİYOR");
            else if(w==2)invokeNoArg741(MainActivityV730.class,"confirmRestore730");
            else{close741();cloudPrefs.edit().clear().apply();showLogin();}
        }).show();
    }

    private void requestSafe741(boolean announce,boolean catchup){
        if(!hasSession741()||db==null)return;
        safe741.execute(()->safePass741(announce,catchup));
    }

    private void safePass741(boolean announce,boolean catchup){
        if(!pass741.compareAndSet(false,true))return;
        try{
            int pushed=invokeInt741("flushPending740");
            if(catchup&&pendingCount741()==0)invokeVoid741("bulkCatchup740");
            if(announce){final int n=pushed;runOnUiThread(()->toast("EŞZAMANLAMA TAMAM • "+n+" YEREL DEĞİŞİKLİK AKTARILDI"));}
        }catch(Exception e){if(announce){String m=e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();runOnUiThread(()->toast("EŞZAMANLAMA DURDU • "+m));}}
        finally{pass741.set(false);}
    }

    /** Coalesce a burst of postgres events into ONE catch-up after 750 ms quiet time. */
    private void queueCatchup741(){
        if(!catchupQueued741.compareAndSet(false,true))return;
        timer741.schedule(()->{
            catchupQueued741.set(false);
            if(resumed741&&hasSession741())requestSafe741(false,true);
        },750,TimeUnit.MILLISECONDS);
    }

    private boolean hasSession741(){return cloudPrefs!=null&&!cloudPrefs.getString("access_token","").isEmpty();}
    private int pendingCount741(){try{android.database.Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM pending_sync",null);int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;}catch(Exception e){return 0;}}

    private int invokeInt741(String name)throws Exception{Method m=MainActivityV740.class.getDeclaredMethod(name);m.setAccessible(true);Object x=m.invoke(this);return x instanceof Number?((Number)x).intValue():0;}
    private void invokeVoid741(String name)throws Exception{Method m=MainActivityV740.class.getDeclaredMethod(name);m.setAccessible(true);m.invoke(this);}
    private void invokeNoArg741(Class<?> owner,String name){try{Method m=owner.getDeclaredMethod(name);m.setAccessible(true);m.invoke(this);}catch(Exception e){toast("İŞLEM AÇILAMADI.");}}

    private void connect741(){
        if(!resumed741||!hasSession741()||socket741!=null)return;
        try{
            String base=SUPABASE_URL.replace("https://","wss://").replace("http://","ws://");
            String url=base+"/realtime/v1/websocket?apikey="+URLEncoder.encode(SUPABASE_KEY,"UTF-8")+"&vsn=1.0.0";
            Request req=new Request.Builder().url(url).build();
            socket741=wsClient741.newWebSocket(req,new WebSocketListener(){
                @Override public void onOpen(WebSocket ws,Response response){reconnect741=0;join741(ws);queueCatchup741();}
                @Override public void onMessage(WebSocket ws,String text){
                    try{JSONObject x=new JSONObject(text);if("postgres_changes".equals(x.optString("event")))queueCatchup741();}catch(Exception ignored){}
                }
                @Override public void onClosed(WebSocket ws,int code,String reason){if(socket741==ws)socket741=null;scheduleReconnect741();}
                @Override public void onFailure(WebSocket ws,Throwable t,Response response){if(socket741==ws)socket741=null;scheduleReconnect741();}
            });
        }catch(Exception e){socket741=null;scheduleReconnect741();}
    }

    private void join741(WebSocket ws){
        try{
            JSONArray changes=new JSONArray();
            String[] tables={"athletes","payments","fee_periods","mobile_attendance_records","mobile_attendance_sessions","mobile_attendance_schedule","athlete_membership_events","material_products","material_transactions"};
            for(String t:tables)changes.put(new JSONObject().put("event","*").put("schema","public").put("table",t));
            JSONObject config=new JSONObject().put("broadcast",new JSONObject().put("ack",false).put("self",false)).put("presence",new JSONObject().put("enabled",false)).put("postgres_changes",changes).put("private",false);
            String ref="741"+System.currentTimeMillis();
            JSONObject payload=new JSONObject().put("config",config).put("access_token",cloudPrefs.getString("access_token",""));
            ws.send(new JSONObject().put("topic","realtime:parion-v4218").put("event","phx_join").put("payload",payload).put("ref",ref).put("join_ref",ref).toString());
        }catch(Exception ignored){}
    }

    private void scheduleReconnect741(){
        if(!resumed741)return;
        long delay=Math.min(15000L,800L*(1L<<Math.min(4,reconnect741++)));
        timer741.schedule(this::connect741,delay,TimeUnit.MILLISECONDS);
    }
    private void close741(){WebSocket s=socket741;socket741=null;if(s!=null)try{s.close(1000,"pause");}catch(Exception ignored){}}

    @Override protected void onDestroy(){
        resumed741=false;close741();timer741.shutdownNow();safe741.shutdownNow();wsClient741.dispatcher().executorService().shutdown();suppress740();super.onDestroy();
    }
}

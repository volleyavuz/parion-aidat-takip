package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ParionSyncWorker extends Worker {
    private static final String URL="https://ujjtsemybslznmzadzvk.supabase.co";
    private static final String KEY="sb_publishable_tYGPzcWkdcxwjbBr3hnitg_ce7mVdfM";
    private static final String PREF="parion_cloud_session";
    private final Context ctx;
    private final DbHelper db;

    public ParionSyncWorker(@NonNull Context c,@NonNull WorkerParameters p){super(c,p);ctx=c.getApplicationContext();db=new DbHelper(ctx);}

    @NonNull @Override public Result doWork(){
        ensureQueue();
        SharedPreferences prefs=ctx.getSharedPreferences(PREF,Context.MODE_PRIVATE);
        String token=prefs.getString("access_token","");
        if(token.isEmpty())return Result.success();
        try{
            if(!pushCore(token,prefs))return Result.retry();
            if(!pushAttendance(token,prefs))return Result.retry();
            SQLiteDatabase d=db.getWritableDatabase();
            d.execSQL("DELETE FROM pending_sync");
            prefs.edit().putLong("last_background_sync",System.currentTimeMillis()).apply();
            return Result.success();
        }catch(Exception e){return Result.retry();}
    }

    private void ensureQueue(){db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS pending_sync(id INTEGER PRIMARY KEY AUTOINCREMENT,kind TEXT NOT NULL,entity_key TEXT NOT NULL DEFAULT '',created_at INTEGER NOT NULL,UNIQUE(kind,entity_key))");}

    private boolean pushCore(String token,SharedPreferences prefs)throws Exception{
        JSONArray athletes=new JSONArray(),payments=new JSONArray(),fees=new JSONArray();SQLiteDatabase d=db.getReadableDatabase();
        Cursor a=d.rawQuery("SELECT * FROM athletes",null);while(a.moveToNext()){JSONObject o=new JSONObject();long id=a.getLong(a.getColumnIndexOrThrow("id"));o.put("legacy_id",id);pc(o,"seq",a,"seq");pc(o,"birth_year",a,"birthYear");pc(o,"birth_date",a,"birthDate");pc(o,"name",a,"name");pc(o,"category",a,"category");pc(o,"status",a,"status");pc(o,"monthly_fee",a,"monthlyFee");pc(o,"sibling",a,"sibling");pc(o,"tshirt_qty",a,"tshirtQty");pc(o,"tshirt_paid",a,"tshirtPaid");pc(o,"tracksuit_qty",a,"tracksuitQty");pc(o,"tracksuit_paid",a,"tracksuitPaid");pc(o,"notes",a,"notes");pc(o,"phone",a,"phone");pc(o,"mother_name",a,"motherName");pc(o,"mother_phone",a,"motherPhone");pc(o,"father_name",a,"fatherName");pc(o,"father_phone",a,"fatherPhone");pc(o,"start_date",a,"startDate");pc(o,"end_date",a,"endDate");pc(o,"restart_date",a,"restartDate");pc(o,"tckn",a,"tckn");athletes.put(o);}a.close();
        Cursor p=d.rawQuery("SELECT athleteId,year,month,marker,amount FROM payments",null);while(p.moveToNext()){JSONObject o=new JSONObject();o.put("legacy_id",p.getLong(0));o.put("year",p.getInt(1));o.put("month",p.getInt(2));o.put("marker",p.getString(3)==null?"":p.getString(3));o.put("amount",p.getInt(4));payments.put(o);}p.close();
        try{Cursor f=d.rawQuery("SELECT athleteId,year,effectiveMonth,fee FROM fee_history",null);while(f.moveToNext()){JSONObject o=new JSONObject();o.put("legacy_id",f.getLong(0));o.put("year",f.getInt(1));o.put("month",f.getInt(2));o.put("fee",f.getInt(3));fees.put(o);}f.close();}catch(Exception ignored){}
        JSONObject body=new JSONObject().put("p_athletes",athletes).put("p_payments",payments).put("p_fees",fees);
        HttpResult r=request("POST",URL+"/rest/v1/rpc/parion_sync_mobile_snapshot",body.toString(),token);
        if(r.code==401&&refresh(prefs)){token=prefs.getString("access_token","");r=request("POST",URL+"/rest/v1/rpc/parion_sync_mobile_snapshot",body.toString(),token);}return r.code>=200&&r.code<300;
    }

    private boolean pushAttendance(String token,SharedPreferences prefs)throws Exception{
        JSONArray sessions=new JSONArray(),records=new JSONArray();SQLiteDatabase d=db.getReadableDatabase();
        Cursor s=d.rawQuery("SELECT id,groupName,sessionDate,cancelled FROM attendance_sessions",null);while(s.moveToNext()){long sid=s.getLong(0);String g=s.getString(1),date=s.getString(2);JSONObject o=new JSONObject();o.put("group_name",g);o.put("session_date",date);o.put("cancelled",s.getInt(3)==1);sessions.put(o);Cursor r=d.rawQuery("SELECT athleteId,present FROM attendance_records WHERE sessionId=?",new String[]{String.valueOf(sid)});while(r.moveToNext()){JSONObject q=new JSONObject();q.put("group_name",g);q.put("session_date",date);q.put("athlete_id",r.getLong(0));q.put("present",r.getInt(1)==1);records.put(q);}r.close();}s.close();
        JSONObject body=new JSONObject().put("p_sessions",sessions).put("p_records",records);
        HttpResult r=request("POST",URL+"/rest/v1/rpc/parion_sync_attendance_snapshot",body.toString(),token);
        if(r.code==401&&refresh(prefs)){token=prefs.getString("access_token","");r=request("POST",URL+"/rest/v1/rpc/parion_sync_attendance_snapshot",body.toString(),token);}return r.code>=200&&r.code<300;
    }

    private boolean refresh(SharedPreferences prefs){try{String rt=prefs.getString("refresh_token","");if(rt.isEmpty())return false;JSONObject b=new JSONObject().put("refresh_token",rt);HttpResult r=request("POST",URL+"/auth/v1/token?grant_type=refresh_token",b.toString(),null);if(r.code<200||r.code>=300)return false;JSONObject j=new JSONObject(r.body);String at=j.optString("access_token","");if(at.isEmpty())return false;prefs.edit().putString("access_token",at).putString("refresh_token",j.optString("refresh_token",rt)).apply();return true;}catch(Exception e){return false;}}
    private void pc(JSONObject o,String key,Cursor c,String col)throws Exception{int i=c.getColumnIndex(col);if(i<0||c.isNull(i)){o.put(key,"");return;}if(c.getType(i)==Cursor.FIELD_TYPE_INTEGER)o.put(key,c.getLong(i));else o.put(key,c.getString(i)==null?"":c.getString(i));}
    private HttpResult request(String method,String url,String body,String bearer)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setRequestMethod(method);c.setConnectTimeout(15000);c.setReadTimeout(25000);c.setRequestProperty("apikey",KEY);c.setRequestProperty("Accept","application/json");if(bearer!=null&&!bearer.isEmpty())c.setRequestProperty("Authorization","Bearer "+bearer);if(body!=null){c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");try(OutputStream o=c.getOutputStream()){o.write(body.getBytes(StandardCharsets.UTF_8));}}int code=c.getResponseCode();InputStream in=code>=400?c.getErrorStream():c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while(in!=null&&(n=in.read(b))>0)out.write(b,0,n);if(in!=null)in.close();c.disconnect();return new HttpResult(code,out.toString("UTF-8"));}
    private static class HttpResult{int code;String body;HttpResult(int c,String b){code=c;body=b;}}
}

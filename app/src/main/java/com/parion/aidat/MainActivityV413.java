package com.parion.aidat;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import java.io.*;
import java.lang.reflect.*;
import java.util.concurrent.*;
import org.json.*;

public class MainActivityV413 extends MainActivityV412 {
    private final ExecutorService fix413=Executors.newSingleThreadExecutor();

    @Override public void onCreate(Bundle b){super.onCreate(b);}

    @Override void showProfile(long id){
        super.showProfile(id);
        // New records must exist in public.athletes before media_path can be attached.
        // V412 started media promotion and athlete push concurrently; Storage could receive
        // the object first while set_athlete_media_path returned false because the athlete row
        // did not exist yet. Serialize the repair path: push athlete -> attach any orphan media.
        if(id>0)repairNewAthleteMedia413(id);
    }

    private void repairNewAthleteMedia413(long id){
        fix413.execute(()->{
            try{
                String token=cloudPrefs==null?"":cloudPrefs.getString("access_token","");
                if(token.isEmpty())return;
                ensureAthleteCloud413(id,token);
                attachLatestOrphan413(id,"photo",token);
                attachLatestOrphan413(id,"form",token);
            }catch(Exception ignored){}
        });
    }

    private void ensureAthleteCloud413(long id,String token)throws Exception{
        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return;}
        JSONObject o=new JSONObject();o.put("legacy_id",id);
        put413(o,"seq",a,"seq");put413(o,"birth_year",a,"birthYear");put413(o,"birth_date",a,"birthDate");put413(o,"name",a,"name");put413(o,"category",a,"category");put413(o,"status",a,"status");put413(o,"monthly_fee",a,"monthlyFee");put413(o,"sibling",a,"sibling");put413(o,"tshirt_qty",a,"tshirtQty");put413(o,"tshirt_paid",a,"tshirtPaid");put413(o,"tracksuit_qty",a,"tracksuitQty");put413(o,"tracksuit_paid",a,"tracksuitPaid");put413(o,"notes",a,"notes");put413(o,"phone",a,"phone");put413(o,"mother_name",a,"motherName");put413(o,"mother_phone",a,"motherPhone");put413(o,"father_name",a,"fatherName");put413(o,"father_phone",a,"fatherPhone");put413(o,"start_date",a,"startDate");put413(o,"end_date",a,"endDate");put413(o,"restart_date",a,"restartDate");put413(o,"tckn",a,"tckn");a.close();
        HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_upsert_mobile_athlete",new JSONObject().put("p",o).toString(),token);
        if(r.code==401&&refreshSession()){
            token=cloudPrefs.getString("access_token","");
            r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_upsert_mobile_athlete",new JSONObject().put("p",o).toString(),token);
        }
        if(r.code<200||r.code>=300||!r.body.contains("true"))throw new IOException("athlete cloud");
    }

    private void put413(JSONObject o,String key,Cursor c,String col)throws Exception{
        int i=c.getColumnIndex(col);if(i<0||c.isNull(i)){o.put(key,"");return;}if(c.getType(i)==Cursor.FIELD_TYPE_INTEGER)o.put(key,c.getLong(i));else o.put(key,c.getString(i)==null?"":c.getString(i));
    }

    private void attachLatestOrphan413(long id,String kind,String token)throws Exception{
        String bucket="photo".equals(kind)?"athlete-photos":"registration-forms";
        String prefix="user/"+id+"/";
        HttpResult list=request("POST",SUPABASE_URL+"/storage/v1/object/list/"+bucket,new JSONObject().put("prefix",prefix).put("limit",100).put("sortBy",new JSONObject().put("column","created_at").put("order","desc")).toString(),token);
        if(list.code<200||list.code>=300)return;
        JSONArray arr=new JSONArray(list.body);String wanted="photo".equals(kind)?"photo_":"form_",path="";
        for(int i=0;i<arr.length();i++){String n=arr.getJSONObject(i).optString("name","");if(n.startsWith(wanted)){path=prefix+n;break;}}
        if(path.isEmpty())return;
        JSONObject body=new JSONObject().put("p_legacy_id",id).put("p_kind",kind).put("p_path",path);
        HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/set_athlete_media_path",body.toString(),token);
        if(r.code<200||r.code>=300||!r.body.contains("true"))return;
        if("photo".equals(kind)){
            photoMap413().put(id,path);db.getWritableDatabase().execSQL("UPDATE athletes SET photo=? WHERE id=?",new Object[]{"CLOUD:"+path,id});
        }else formMap413().put(id,path);
        runOnUiThread(()->{if("PROFILE".equals(page)&&currentAthlete==id)showProfile(id);});
    }

    @SuppressWarnings("unchecked") private ConcurrentHashMap<Long,String> photoMap413(){try{Field f=MainActivityV405.class.getDeclaredField("photoPath");f.setAccessible(true);return (ConcurrentHashMap<Long,String>)f.get(this);}catch(Exception e){return new ConcurrentHashMap<>();}}
    @SuppressWarnings("unchecked") private ConcurrentHashMap<Long,String> formMap413(){try{Field f=MainActivityV405.class.getDeclaredField("formPath");f.setAccessible(true);return (ConcurrentHashMap<Long,String>)f.get(this);}catch(Exception e){return new ConcurrentHashMap<>();}}

    @Override protected void onDestroy(){fix413.shutdownNow();super.onDestroy();}
}

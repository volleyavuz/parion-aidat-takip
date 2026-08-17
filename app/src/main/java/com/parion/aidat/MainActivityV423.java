package com.parion.aidat;

import android.database.Cursor;
import org.json.JSONObject;
import java.io.IOException;

public class MainActivityV423 extends MainActivityV422 {
    @Override protected void ensureAthleteCloud413(long id,String token)throws Exception{
        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();throw new IOException("SPORCU YEREL KAYDI BULUNAMADI");}
        JSONObject o=new JSONObject();o.put("legacy_id",id);
        put423(o,"seq",a,"seq",false);put423(o,"birth_year",a,"birthYear",false);putDate423(o,"birth_date",a,"birthDate");put423(o,"name",a,"name",false);put423(o,"category",a,"category",false);put423(o,"status",a,"status",false);put423(o,"monthly_fee",a,"monthlyFee",false);put423(o,"sibling",a,"sibling",false);put423(o,"tshirt_qty",a,"tshirtQty",false);put423(o,"tshirt_paid",a,"tshirtPaid",false);put423(o,"tracksuit_qty",a,"tracksuitQty",false);put423(o,"tracksuit_paid",a,"tracksuitPaid",false);put423(o,"notes",a,"notes",false);put423(o,"phone",a,"phone",false);put423(o,"mother_name",a,"motherName",false);put423(o,"mother_phone",a,"motherPhone",false);put423(o,"father_name",a,"fatherName",false);put423(o,"father_phone",a,"fatherPhone",false);putDate423(o,"start_date",a,"startDate");putDate423(o,"end_date",a,"endDate");putDate423(o,"restart_date",a,"restartDate");put423(o,"tckn",a,"tckn",false);a.close();
        String use=token;HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_upsert_mobile_athlete",new JSONObject().put("p",o).toString(),use);
        if(r.code==401&&refreshSession()){use=cloudPrefs.getString("access_token","");r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_upsert_mobile_athlete",new JSONObject().put("p",o).toString(),use);}
        if(r.code<200||r.code>=300||r.body==null||!r.body.contains("true"))throw new IOException("SPORCU BULUT KAYDI HAZIRLANAMADI ("+r.code+")"+mediaErr423(r.body));
    }
    private void put423(JSONObject o,String key,Cursor c,String col,boolean date)throws Exception{int i=c.getColumnIndex(col);if(i<0||c.isNull(i)){o.put(key,"");return;}if(c.getType(i)==Cursor.FIELD_TYPE_INTEGER)o.put(key,c.getLong(i));else o.put(key,c.getString(i)==null?"":c.getString(i));}
    private void putDate423(JSONObject o,String key,Cursor c,String col)throws Exception{int i=c.getColumnIndex(col);String s=i<0||c.isNull(i)?"":c.getString(i);s=s==null?"":s.trim();if("DEVAM".equalsIgnoreCase(s)||"-".equals(s)||"—".equals(s))s="";if(s.matches("\\d{2}\\.\\d{2}\\.\\d{4}")){String[] p=s.split("\\.");s=p[2]+"-"+p[1]+"-"+p[0];}if(!s.isEmpty()&&!s.matches("\\d{4}-\\d{2}-\\d{2}"))s="";o.put(key,s);}
    private String mediaErr423(String s){if(s==null||s.trim().isEmpty())return "";try{JSONObject x=new JSONObject(s);String m=x.optString("message",x.optString("hint",x.optString("error_description","")));return m.isEmpty()?"":" • "+m;}catch(Exception e){String x=s.replace('\n',' ').trim();return x.isEmpty()?"":" • "+(x.length()>180?x.substring(0,180):x);}}
    @Override void goBack(){if("FORM".equals(page)){if(currentAthlete>0)showProfile(currentAthlete);else showAthletes();return;}super.goBack();}
}
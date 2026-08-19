package com.parion.aidat;

import android.database.Cursor;
import android.os.Bundle;
import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import org.json.*;

public class MainActivityV406 extends MainActivityV405 {
    private static final String SB_URL_406="https://ujjtsemybslznmzadzvk.supabase.co";
    private static final String SB_KEY_406="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVqanRzZW15YnNsem5temFkenZrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY3MzIyMjIsImV4cCI6MjEwMjMwODIyMn0.qZPcYZwAjMJpc2yBB1bdTjA8YguFqr3UY85VuQGQRLE";

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().getDecorView().postDelayed(this::loadSafeMediaIndex406,400);
    }

    @SuppressWarnings("unchecked")
    private void loadSafeMediaIndex406(){
        new Thread(()->{
            try{
                URL u=new URL(SB_URL_406+"/rest/v1/athlete_media_index?select=legacy_id,photo_path,registration_form_path");
                HttpURLConnection h=(HttpURLConnection)u.openConnection();
                h.setRequestProperty("apikey",SB_KEY_406);
                h.setRequestProperty("Authorization","Bearer "+SB_KEY_406);
                h.setRequestProperty("Accept","application/json");
                h.setConnectTimeout(12000);h.setReadTimeout(20000);
                int code=h.getResponseCode();
                InputStream in=(code>=200&&code<300)?h.getInputStream():h.getErrorStream();
                ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;while(in!=null&&(n=in.read(buf))>0)out.write(buf,0,n);if(in!=null)in.close();h.disconnect();
                if(code<200||code>=300)throw new IOException("HTTP "+code);
                JSONArray a=new JSONArray(out.toString(StandardCharsets.UTF_8.name()));

                Field pf=MainActivityV405.class.getDeclaredField("photoPath");pf.setAccessible(true);
                Field ff=MainActivityV405.class.getDeclaredField("formPath");ff.setAccessible(true);
                Field af=MainActivityV405.class.getDeclaredField("photoAlias");af.setAccessible(true);
                Field rf=MainActivityV405.class.getDeclaredField("cloudIndexReady");rf.setAccessible(true);
                ConcurrentHashMap<Long,String> photos=(ConcurrentHashMap<Long,String>)pf.get(this);
                ConcurrentHashMap<Long,String> forms=(ConcurrentHashMap<Long,String>)ff.get(this);
                ConcurrentHashMap<String,String> aliases=(ConcurrentHashMap<String,String>)af.get(this);
                photos.clear();forms.clear();aliases.clear();
                for(int i=0;i<a.length();i++){
                    JSONObject o=a.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id<=0)continue;
                    String pp=o.optString("photo_path","");String fp=o.optString("registration_form_path","");
                    if(pp!=null&&!pp.isEmpty()&&!"null".equals(pp)){
                        photos.put(id,pp);
                        String base=pp.substring(pp.lastIndexOf('/')+1);aliases.put(base,pp);
                        Cursor c=db.athlete(id);if(c.moveToFirst()){String local=v(c,"photo");if(local!=null&&!local.trim().isEmpty())aliases.put(local,pp);}c.close();
                    }
                    if(fp!=null&&!fp.isEmpty()&&!"null".equals(fp))forms.put(id,fp);
                }
                rf.setBoolean(this,true);
                runOnUiThread(()->{if("HOME".equals(page))showHome(); else if("PROFILE".equals(page)&&currentAthlete>0)showProfile(currentAthlete);});
            }catch(Exception e){runOnUiThread(()->toast("Bulut medya listesi alınamadı."));}
        }).start();
    }
}

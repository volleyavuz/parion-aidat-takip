package com.parion.aidat;

import android.view.*;
import org.json.JSONObject;
import java.lang.reflect.Method;

/** v4.2.8 - serialize refresh-token rotation and keep Settings on the current menu. */
public class MainActivityV732 extends MainActivityV731 {
    private static final Object AUTH_REFRESH_LOCK_732=new Object();

    @Override HttpResult request(String method,String url,String body,String bearer)throws Exception{
        if(url!=null&&url.contains("/auth/v1/token")&&url.contains("grant_type=refresh_token")){
            synchronized(AUTH_REFRESH_LOCK_732){
                String adjusted=body;
                try{
                    JSONObject j=new JSONObject(body==null||body.trim().isEmpty()?"{}":body);
                    String latest=cloudPrefs==null?"":cloudPrefs.getString("refresh_token","");
                    if(!latest.isEmpty())j.put("refresh_token",latest);
                    adjusted=j.toString();
                }catch(Exception ignored){}
                HttpResult r=super.request(method,url,adjusted,bearer);
                if(r.code>=200&&r.code<300&&cloudPrefs!=null){
                    try{
                        JSONObject x=new JSONObject(r.body==null?"{}":r.body);
                        android.content.SharedPreferences.Editor e=cloudPrefs.edit();
                        String access=x.optString("access_token","");
                        String refresh=x.optString("refresh_token","");
                        if(!access.isEmpty())e.putString("access_token",access);
                        if(!refresh.isEmpty())e.putString("refresh_token",refresh);
                        e.apply();
                    }catch(Exception ignored){}
                }
                return r;
            }
        }
        return super.request(method,url,body,bearer);
    }

    @Override void base(String title,boolean back){
        super.base(title,back);
        reinforceSettings732();
    }

    @Override void showHome(){
        super.showHome();
        reinforceSettings732();
    }

    private void reinforceSettings732(){
        if(root==null)return;
        root.post(()->patchSettings732(root));
        root.postDelayed(()->patchSettings732(root),350L);
        root.postDelayed(()->patchSettings732(root),950L);
    }

    private void patchSettings732(View v){
        if(v==null)return;
        CharSequence d=v.getContentDescription();
        if(d!=null&&"Ayarlar".equalsIgnoreCase(d.toString())){
            v.setOnClickListener(this::openSettings732);
            return;
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++)patchSettings732(g.getChildAt(i));
        }
    }

    private void openSettings732(View anchor){
        try{
            Method m=MainActivityV729.class.getDeclaredMethod("showSettings729",View.class);
            m.setAccessible(true);
            m.invoke(this,anchor);
        }catch(Exception e){toast("Ayarlar açılamadı.");}
    }
}

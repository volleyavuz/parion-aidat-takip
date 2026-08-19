package com.parion.aidat;

import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;
import java.util.Locale;

/**
 * v4 cloud safety layer.
 * Normal authenticated delta writes are allowed again.
 * The legacy whole-database snapshot RPC stays permanently blocked in-app.
 */
public class MainActivityV500 extends MainActivityV416 {
    @Override HttpResult request(String method,String url,String body,String bearer)throws Exception{
        if(url!=null && url.contains("/rpc/parion_sync_mobile_snapshot")){
            return new HttpResult(403,"{\"error\":\"WHOLE_SNAPSHOT_DISABLED_V4\"}");
        }
        return super.request(method,url,body,bearer);
    }

    @Override void showCloudMenu(){
        String[] items={"ŞİMDİ SENKRONİZE ET (BULUT → YEREL)","OTURUMU KAPAT"};
        new android.app.AlertDialog.Builder(this).setTitle("ONLINE HESAP • DELTA SENKRONİZASYON").setItems(items,(d,w)->{
            if(w==0)syncFromCloud(true);
            else{cloudPrefs.edit().clear().apply();showLogin();}
        }).show();
    }

    @Override void showHome(){
        super.showHome();
        TextView mode=tv("☁ ÇİFT YÖNLÜ DELTA SENKRONİZASYON • TOPLU SNAPSHOT KAPALI",11,Color.rgb(70,90,70),true);
        mode.setGravity(Gravity.CENTER);mode.setPadding(dp(6),dp(4),dp(6),dp(6));
        root.addView(mode,Math.min(1,root.getChildCount()));
    }
}

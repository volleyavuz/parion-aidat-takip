package com.parion.aidat;

/** v4.2.9 - legacy immediate athlete pushes are queued into the conflict-checked safe delta engine. */
public class MainActivityV733 extends MainActivityV732 {
    private volatile long lastLegacyRedirect733=0L;

    @Override HttpResult request(String method,String url,String body,String bearer)throws Exception{
        if(url!=null && url.contains("/rpc/parion_sync_one_athlete_delta_v4") && !insideSafePush733()){
            long now=System.currentTimeMillis();
            if(now-lastLegacyRedirect733>250L){
                lastLegacyRedirect733=now;
                runOnUiThread(()->syncFromCloud(false));
            }
            // Keep the old caller from marking the record synced. The safe engine will mark it only after
            // cloud preflight + RPC + read-back verification all succeed.
            return new HttpResult(425,"{\"error\":\"ROUTED_TO_SAFE_DELTA\"}");
        }
        return super.request(method,url,body,bearer);
    }

    private boolean insideSafePush733(){
        StackTraceElement[] st=Thread.currentThread().getStackTrace();
        for(StackTraceElement e:st){
            if("safePush727".equals(e.getMethodName()) && "com.parion.aidat.MainActivityV727".equals(e.getClassName()))return true;
        }
        return false;
    }

    @Override void toast(String s){
        if(s!=null && s.startsWith("DEĞİŞİKLİK BULUTA GÖNDERİLEMEDİ • HTTP 425"))return;
        super.toast(s);
    }
}

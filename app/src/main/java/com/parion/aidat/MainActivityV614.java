package com.parion.aidat;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * v4.0.14 - ANR hotfix 2.
 * V613 already disables the automatic full CLOUD -> LOCAL restore, but the older
 * V393 layer still starts its own private material-products/material-transactions
 * pull from onCreate(). That background pull writes SQLite while the home screen
 * reads it and can block the UI thread. Suppress only the two startup material GETs.
 * Normal material writes and later user-triggered/manual synchronization remain enabled.
 */
public class MainActivityV614 extends MainActivityV613 {
    private final AtomicInteger startupMaterialGets614 = new AtomicInteger(2);

    @Override HttpResult request(String method,String url,String body,String bearer)throws Exception {
        String m=method==null?"":method.toUpperCase(Locale.ROOT);
        String u=url==null?"":url;
        if("GET".equals(m) && startupMaterialGets614.get()>0 &&
                (u.contains("/rest/v1/material_products?select=") ||
                 u.contains("/rest/v1/material_transactions?select="))) {
            startupMaterialGets614.decrementAndGet();
            return new HttpResult(409,"{\"error\":\"STARTUP_MATERIAL_PULL_DISABLED_V614\"}");
        }
        return super.request(method,url,body,bearer);
    }
}

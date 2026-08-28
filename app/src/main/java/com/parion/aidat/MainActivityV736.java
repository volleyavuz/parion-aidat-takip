package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.lang.reflect.*;
import java.util.concurrent.*;

/** v4.2.12 - isolate missing-photo selection target and verify cloud athlete mirror after pull. */
public class MainActivityV736 extends MainActivityV735 {
    private static final int REQ_MISSING_PHOTO_736=7362;
    private volatile long missingPhotoTarget736=-1L;
    private final ScheduledExecutorService verify736=Executors.newSingleThreadScheduledExecutor();
    private volatile long syncGeneration736=0L;

    @Override void showHome(){
        super.showHome();
        patchMissingPhotoCard736(root);
    }

    private void patchMissingPhotoCard736(View v){
        if(!(v instanceof ViewGroup))return;
        ViewGroup g=(ViewGroup)v;
        for(int i=0;i<g.getChildCount();i++){
            View c=g.getChildAt(i);
            if(c instanceof TextView){
                String s=String.valueOf(((TextView)c).getText()).toUpperCase(new java.util.Locale("tr","TR"));
                if(s.contains("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR")||s.contains("FOTOĞRAF EKSİK")){
                    View p=(View)c.getParent();
                    if(p!=null)p.setOnClickListener(x->showMissingPhotos736());
                }
            }
            patchMissingPhotoCard736(c);
        }
    }

    private void showMissingPhotos736(){
        page="MISSING_PHOTOS_736";
        base("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR",true);
        ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;
        Cursor c=db.athletes("","AKTİF");
        while(c.moveToNext()){
            long id=c.getLong(c.getColumnIndexOrThrow("id"));
            if(photoMap413().containsKey(id))continue;
            String name=c.getString(c.getColumnIndexOrThrow("name"));
            int by=c.getInt(c.getColumnIndexOrThrow("birthYear"));
            String cat=c.getString(c.getColumnIndexOrThrow("category"));
            LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(dp(12),dp(10),dp(12),dp(10));r.setBackground(round(android.graphics.Color.WHITE,10));
            r.addView(tv((by>0?by+" • ":"")+(name==null?"":name),15,BLACK,true));
            r.addView(tv((cat==null?"":cat)+" • FOTOĞRAF EKLE",12,android.graphics.Color.DKGRAY,true));
            final long target=id;
            r.setOnClickListener(x->{missingPhotoTarget736=target;pickMissingPhoto736();});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(r,lp);n++;
        }
        c.close();
        if(n==0)b.addView(tv("FOTOĞRAFI OLMAYAN AKTİF SPORCU BULUNMUYOR.",14,GREEN,true));
    }

    private void pickMissingPhoto736(){
        Intent i;
        if(Build.VERSION.SDK_INT>=33){i=new Intent(MediaStore.ACTION_PICK_IMAGES);i.setType("image/*");}
        else{i=new Intent(Intent.ACTION_PICK);i.setType("image/*");}
        startActivityForResult(i,REQ_MISSING_PHOTO_736);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==REQ_MISSING_PHOTO_736){
            long id=missingPhotoTarget736;missingPhotoTarget736=-1L;
            if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null&&id>0){
                Uri uri=data.getData();invokeNormalizedPhoto736(id,uri);
            }else showMissingPhotos736();
            return;
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    private void invokeNormalizedPhoto736(long id,Uri uri){
        verify736.execute(()->{
            try{
                Method m=MainActivityV735.class.getDeclaredMethod("uploadNormalizedPhoto735",long.class,Uri.class);m.setAccessible(true);m.invoke(this,id,uri);
                // V735 returns to profile; restore this dedicated list after upload settles.
                verify736.schedule(()->runOnUiThread(this::showMissingPhotos736),900,TimeUnit.MILLISECONDS);
            }catch(Exception e){runOnUiThread(()->{toast("FOTOĞRAF YÜKLENEMEDİ.");showMissingPhotos736();});}
        });
    }

    @Override void syncFromCloud(boolean announce){
        long gen=++syncGeneration736;
        super.syncFromCloud(announce);
        waitForPull736(gen,0,false,announce);
    }

    private void waitForPull736(long gen,int tries,boolean seenBusy,boolean announce){
        if(gen!=syncGeneration736)return;
        boolean busy=false;try{busy=syncing;}catch(Exception ignored){}
        boolean seen=seenBusy||busy;
        if(seen&&!busy){
            verify736.execute(()->{
                try{
                    int n=reconcileAthletes736();
                    runOnUiThread(()->{if(announce)toast("BULUT SPORCULARI DOĞRULANDI • "+n+" KAYIT");showHome();});
                }catch(Exception e){runOnUiThread(()->toast("SPORCU SENKRONİZASYONU DOĞRULANAMADI."));}
            });
            return;
        }
        if(tries>=120)return;
        final boolean nextSeen=seen;
        verify736.schedule(()->waitForPull736(gen,tries+1,nextSeen,announce),500,TimeUnit.MILLISECONDS);
    }

    private int reconcileAthletes736()throws Exception{
        HttpResult r=getAuthed("/rest/v1/mobile_athletes?select=*&order=legacy_id.asc");
        if(r.code==401&&refreshSession())r=getAuthed("/rest/v1/mobile_athletes?select=*&order=legacy_id.asc");
        if(r.code<200||r.code>=300)throw new IllegalStateException("HTTP "+r.code);
        JSONArray a=new JSONArray(r.body);SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();int n=0;
        try{
            for(int i=0;i<a.length();i++){
                JSONObject o=a.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id<=0)continue;
                ContentValues v=new ContentValues();v.put("id",id);
                putInt736(v,"seq",o,"seq");putInt736(v,"birthYear",o,"birth_year");putText736(v,"birthDate",o,"birth_date");putText736(v,"name",o,"name");putText736(v,"category",o,"category");putText736(v,"status",o,"status");putInt736(v,"monthlyFee",o,"monthly_fee");putText736(v,"sibling",o,"sibling");
                putInt736(v,"tshirtQty",o,"tshirt_qty");putInt736(v,"tshirtPaid",o,"tshirt_paid");putInt736(v,"tracksuitQty",o,"tracksuit_qty");putInt736(v,"tracksuitPaid",o,"tracksuit_paid");putText736(v,"notes",o,"notes");putText736(v,"phone",o,"phone");putText736(v,"motherName",o,"mother_name");putText736(v,"motherPhone",o,"mother_phone");putText736(v,"fatherName",o,"father_name");putText736(v,"fatherPhone",o,"father_phone");putText736(v,"startDate",o,"start_date");putText736(v,"endDate",o,"end_date");putText736(v,"restartDate",o,"restart_date");putText736(v,"tckn",o,"tckn");
                v.put("summerCall",o.optBoolean("summer_call",false)?1:0);v.put("winterCall",o.optBoolean("winter_call",false)?1:0);
                String photo=o.optString("photo","").trim();if(!photo.isEmpty()&&!"null".equalsIgnoreCase(photo)){v.put("photo","CLOUD:"+photo);photoMap413().put(id,photo);}
                Cursor ex=d.rawQuery("SELECT id FROM athletes WHERE id=?",new String[]{String.valueOf(id)});boolean exists=ex.moveToFirst();ex.close();
                if(exists)d.update("athletes",v,"id=?",new String[]{String.valueOf(id)});else d.insertOrThrow("athletes",null,v);
                n++;
            }
            d.setTransactionSuccessful();
        }finally{d.endTransaction();}
        // Baseline after cloud mirror so untouched cloud rows never become dirty on this device.
        for(int i=0;i<a.length();i++){
            long id=a.getJSONObject(i).optLong("legacy_id",-1);if(id<=0)continue;
            String h=hashViaReflection736(id);ContentValues s=new ContentValues();s.put("entity","ATHLETE");s.put("entityKey",String.valueOf(id));s.put("localHash",h);s.put("lastSyncedAt",System.currentTimeMillis());d.insertWithOnConflict("sync_state",null,s,SQLiteDatabase.CONFLICT_REPLACE);
        }
        return n;
    }

    private String hashViaReflection736(long id)throws Exception{Method m=MainActivityV735.class.getDeclaredMethod("hash735",long.class);m.setAccessible(true);return String.valueOf(m.invoke(this,id));}
    private void putText736(ContentValues v,String col,JSONObject o,String key){if(o.has(key)&&!o.isNull(key))v.put(col,o.optString(key,""));}
    private void putInt736(ContentValues v,String col,JSONObject o,String key){if(o.has(key)&&!o.isNull(key))v.put(col,o.optInt(key,0));}

    @Override protected void onDestroy(){verify736.shutdownNow();super.onDestroy();}
}

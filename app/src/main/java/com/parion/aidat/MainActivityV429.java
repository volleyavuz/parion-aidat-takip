package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivityV429 extends MainActivityV428 {
    private final Handler seasonHandler429=new Handler(Looper.getMainLooper());
    private final AtomicBoolean seasonPush429=new AtomicBoolean(false);
    private boolean pendingNew429=false,pendingSummer429=false,pendingWinter429=false;
    private String seasonOrigin429="";

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        ensureSeason429();
        migrateLocalSeasonNotes429();
        scheduleSeason429(1500);
    }

    private void ensureSeason429(){
        SQLiteDatabase d=db.getWritableDatabase();
        try{d.execSQL("ALTER TABLE athletes ADD COLUMN summerCall INTEGER NOT NULL DEFAULT 0");}catch(Exception ignored){}
        try{d.execSQL("ALTER TABLE athletes ADD COLUMN winterCall INTEGER NOT NULL DEFAULT 0");}catch(Exception ignored){}
        d.execSQL("CREATE TABLE IF NOT EXISTS season_sync_queue(athleteId INTEGER PRIMARY KEY,createdAt TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
    }

    private void migrateLocalSeasonNotes429(){
        try{
            ensureSeason429();SQLiteDatabase d=db.getWritableDatabase();Cursor c=d.rawQuery("SELECT id,notes FROM athletes WHERE upper(COALESCE(notes,'')) LIKE '%KIŞIN ARANACAK%' OR upper(COALESCE(notes,'')) LIKE '%EYLÜLDE ARANACAK%'",null);
            ArrayList<Long> ids=new ArrayList<>();ArrayList<String> notes=new ArrayList<>();
            while(c.moveToNext()){ids.add(c.getLong(0));notes.add(cleanSeasonNote429(c.getString(1)));}c.close();if(ids.isEmpty())return;
            try{d.execSQL("UPDATE sync_control SET suppress=1 WHERE id=1");}catch(Exception ignored){}
            d.beginTransaction();for(int i=0;i<ids.size();i++){ContentValues v=new ContentValues();v.put("winterCall",1);v.put("notes",notes.get(i));d.update("athletes",v,"id=?",new String[]{String.valueOf(ids.get(i))});}d.setTransactionSuccessful();d.endTransaction();
            try{d.execSQL("UPDATE sync_control SET suppress=0 WHERE id=1");}catch(Exception ignored){}
        }catch(Exception e){try{db.getWritableDatabase().execSQL("UPDATE sync_control SET suppress=0 WHERE id=1");}catch(Exception ignored){}}
    }
    private String cleanSeasonNote429(String s){
        String x=s==null?"":s.toUpperCase(TR);x=x.replaceAll("KIŞIN\\s+ARANACAK","").replaceAll("EYLÜLDE\\s+ARANACAK","");x=x.replaceAll("\\s*[,;]+\\s*"," ").replaceAll("\\s{2,}"," ").trim();return x.replaceAll("^[ .,:;-]+|[ .,:;-]+$","").trim();
    }

    @Override int[] applyCloudCache(JSONArray athletes,JSONArray payments)throws Exception{
        ensureSeason429();int[] r=super.applyCloudCache(athletes,payments);SQLiteDatabase d=db.getWritableDatabase();
        for(int i=0;i<athletes.length();i++){JSONObject a=athletes.getJSONObject(i);long id=a.optLong("legacy_id",-1);if(id<=0)continue;ContentValues v=new ContentValues();v.put("summerCall",a.optBoolean("summer_call",false)?1:0);v.put("winterCall",a.optBoolean("winter_call",false)?1:0);d.update("athletes",v,"id=?",new String[]{String.valueOf(id)});}return r;
    }

    @Override void form(long id){
        ensureSeason429();super.form(id);patchSeasonForm429(id);
    }
    private void patchSeasonForm429(long id){
        EditText notes=findEdit429(root,"ÖZEL NOTLAR");Button save=findSave429(root);if(notes==null||save==null)return;ViewParent p=notes.getParent();if(!(p instanceof LinearLayout))return;LinearLayout box=(LinearLayout)p;
        boolean summer=false,winter=false;if(id>0){Cursor c=db.athlete(id);if(c.moveToFirst()){int si=c.getColumnIndex("summerCall"),wi=c.getColumnIndex("winterCall");summer=si>=0&&c.getInt(si)!=0;winter=wi>=0&&c.getInt(wi)!=0;}c.close();}
        LinearLayout checks=new LinearLayout(this);checks.setOrientation(LinearLayout.HORIZONTAL);checks.setGravity(Gravity.CENTER_VERTICAL);checks.setPadding(dp(4),dp(3),dp(4),dp(5));
        CheckBox su=new CheckBox(this);su.setText("YAZIN ARANACAK");su.setChecked(summer);CheckBox wi=new CheckBox(this);wi.setText("KIŞIN ARANACAK");wi.setChecked(winter);checks.addView(su,new LinearLayout.LayoutParams(0,dp(52),1));checks.addView(wi,new LinearLayout.LayoutParams(0,dp(52),1));
        int at=box.indexOfChild(notes);box.addView(checks,Math.max(0,at));
        save.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_UP){if(id>0)saveSeasonFlags429(id,su.isChecked(),wi.isChecked(),true);else{pendingNew429=true;pendingSummer429=su.isChecked();pendingWinter429=wi.isChecked();}}return false;});
    }
    private EditText findEdit429(View v,String term){if(v instanceof EditText){EditText e=(EditText)v;String h=e.getHint()==null?"":e.getHint().toString().toUpperCase(TR);if(h.contains(term))return e;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){EditText e=findEdit429(g.getChildAt(i),term);if(e!=null)return e;}}return null;}
    private Button findSave429(View v){if(v instanceof Button){String s=String.valueOf(((Button)v).getText()).toUpperCase(TR);if(s.contains("KAYDI OLUŞTUR")||s.contains("DEĞİŞİKLİKLERİ KAYDET"))return (Button)v;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){Button b=findSave429(g.getChildAt(i));if(b!=null)return b;}}return null;}

    private void saveSeasonFlags429(long id,boolean summer,boolean winter,boolean log){
        ensureSeason429();Cursor c=db.athlete(id);String name="SPORCU";if(c.moveToFirst())name=s(c,"name");c.close();ContentValues v=new ContentValues();v.put("summerCall",summer?1:0);v.put("winterCall",winter?1:0);db.getWritableDatabase().update("athletes",v,"id=?",new String[]{String.valueOf(id)});ContentValues q=new ContentValues();q.put("athleteId",id);db.getWritableDatabase().insertWithOnConflict("season_sync_queue",null,q,SQLiteDatabase.CONFLICT_REPLACE);
        if(log){ContentValues a=new ContentValues();a.put("action","ARAMA DURUMU GÜNCELLENDİ");a.put("entityType","SPORCU");a.put("entityId",String.valueOf(id));a.put("detail",name+" • YAZ: "+(summer?"EVET":"HAYIR")+" • KIŞ: "+(winter?"EVET":"HAYIR"));db.getWritableDatabase().insert("activity_log_local",null,a);}scheduleSeason429(350);
    }

    @Override void showProfile(long id){
        if(pendingNew429&&id>0){boolean su=pendingSummer429,wi=pendingWinter429;pendingNew429=false;saveSeasonFlags429(id,su,wi,true);}super.showProfile(id);
    }

    private void scheduleSeason429(long ms){seasonHandler429.removeCallbacks(seasonRun429);seasonHandler429.postDelayed(seasonRun429,ms);}
    private final Runnable seasonRun429=new Runnable(){public void run(){pushSeasonQueue429();seasonHandler429.postDelayed(this,12000);}};
    private void pushSeasonQueue429(){
        if(seasonPush429.get()||cloudPrefs==null)return;String token=cloudPrefs.getString("access_token","");if(token.isEmpty())return;seasonPush429.set(true);
        new Thread(()->{try{Cursor c=db.getReadableDatabase().rawQuery("SELECT q.athleteId,a.name,a.summerCall,a.winterCall FROM season_sync_queue q JOIN athletes a ON a.id=q.athleteId ORDER BY q.createdAt",null);ArrayList<Long> done=new ArrayList<>();while(c.moveToNext()){long id=c.getLong(0);JSONObject p=new JSONObject().put("legacy_id",id).put("name",c.getString(1)).put("summer_call",c.getInt(2)!=0).put("winter_call",c.getInt(3)!=0);String tok=cloudPrefs.getString("access_token","");HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_upsert_mobile_athlete",new JSONObject().put("p",p).toString(),tok);if(r.code==401&&refreshSession())r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_upsert_mobile_athlete",new JSONObject().put("p",p).toString(),cloudPrefs.getString("access_token",""));if(r.code>=200&&r.code<300&&r.body!=null&&r.body.contains("true"))done.add(id);}c.close();for(long id:done)db.getWritableDatabase().delete("season_sync_queue","athleteId=?",new String[]{String.valueOf(id)});}catch(Exception ignored){}finally{seasonPush429.set(false);}},"season-sync-429").start();
    }

    @Override void showHome(){
        ensureSeason429();super.showHome();removeLegacyHomeLogo429();addSeasonCards429();
    }
    private void removeLegacyHomeLogo429(){
        ScrollView sv=findScroll429(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;LinearLayout b=(LinearLayout)sv.getChildAt(0);
        for(int i=0;i<Math.min(4,b.getChildCount());i++){View v=b.getChildAt(i);if(v instanceof ImageView){b.removeViewAt(i);return;}}
    }
    private void addSeasonCards429(){
        ScrollView sv=findScroll429(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;LinearLayout b=(LinearLayout)sv.getChildAt(0);if(findText429(b,"KIŞIN ARANACAK")!=null)return;
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);int wc=countFlag429("winterCall"),sc=countFlag429("summerCall");row.addView(seasonCard429("KIŞIN ARANACAK",wc,Color.rgb(70,110,170),"winterCall"),new LinearLayout.LayoutParams(0,dp(94),1));row.addView(seasonCard429("YAZIN ARANACAK",sc,Color.rgb(220,145,20),"summerCall"),new LinearLayout.LayoutParams(0,dp(94),1));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(100));lp.setMargins(0,dp(4),0,dp(5));int at=Math.min(6,b.getChildCount());for(int i=0;i<b.getChildCount();i++){if(findText429(b.getChildAt(i),"ARA VERENLER")!=null){at=Math.min(i+1,b.getChildCount());break;}}b.addView(row,at,lp);
    }
    private View seasonCard429(String title,int count,int color,String col){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setGravity(Gravity.CENTER);x.setBackground(round(Color.WHITE,10));TextView n=tv(String.valueOf(count),22,color,true);n.setGravity(Gravity.CENTER);x.addView(n);TextView t=tv(title,11,Color.DKGRAY,true);t.setGravity(Gravity.CENTER);x.addView(t);x.setOnClickListener(v->showSeasonList429(title,col));return x;}
    private int countFlag429(String col){Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE "+col+"=1 AND TRIM(COALESCE(deletedAt,''))=''",null);c.moveToFirst();int n=c.getInt(0);c.close();return n;}
    private void showSeasonList429(String title,String col){seasonOrigin429=col;page="SEASON429_"+col;base(title,true);ScrollView sv=scroll();LinearLayout b=box(sv);Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE "+col+"=1 AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name",null);int n=0;while(c.moveToNext()){long id=c.getLong(c.getColumnIndexOrThrow("id"));String name=s(c,"name"),cat=s(c,"category");int by=c.getInt(c.getColumnIndexOrThrow("birthYear"));LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(dp(10),dp(9),dp(10),dp(9));r.setBackground(round(Color.WHITE,10));r.addView(tv((by>0?by+" • ":"")+name,15,BLACK,true));r.addView(tv(cat+" • "+title,12,Color.DKGRAY,false));r.setOnClickListener(v->{seasonOrigin429=col;showProfile(id);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(6));b.addView(r,lp);n++;}c.close();if(n==0)b.addView(tv("BU LİSTEDE SPORCU YOK.",13,Color.DKGRAY,true));}

    @Override void goBack(){if(page!=null&&page.startsWith("SEASON429_")){seasonOrigin429="";showHome();return;}if("PROFILE".equals(page)&&seasonOrigin429!=null&&!seasonOrigin429.isEmpty()){String c=seasonOrigin429;showSeasonList429("winterCall".equals(c)?"KIŞIN ARANACAK":"YAZIN ARANACAK",c);return;}super.goBack();}

    private ScrollView findScroll429(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll429(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private TextView findText429(View v,String term){if(v instanceof TextView&&String.valueOf(((TextView)v).getText()).toUpperCase(TR).contains(term))return (TextView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView t=findText429(g.getChildAt(i),term);if(t!=null)return t;}}return null;}

    @Override protected void onDestroy(){seasonHandler429.removeCallbacksAndMessages(null);super.onDestroy();}
}

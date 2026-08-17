package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

public class MainActivityV419 extends MainActivityV418 {
    private static final String LEGACY_NOTE="LEGACY";
    private boolean dashList419=false, returnDash419=false, restoringDash419=false;
    private String dashTitle419="";
    private int dashSort419=0, dashScroll419=0;

    @Override public void onCreate(android.os.Bundle b){
        super.onCreate(b);
        ensureMembership419();
        seedLegacyMembership419();
        pullMembership419();
        pushPendingMembership419();
    }

    private void ensureMembership419(){
        try{
            db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS membership_events(id INTEGER PRIMARY KEY AUTOINCREMENT,cloudId TEXT,athleteId INTEGER NOT NULL,eventDate TEXT NOT NULL,eventType TEXT NOT NULL,note TEXT NOT NULL DEFAULT '',UNIQUE(athleteId,eventDate,eventType))");
        }catch(Exception ignored){}
    }

    private void seedLegacyMembership419(){
        ensureMembership419();
        try{
            SQLiteDatabase d=db.getWritableDatabase();Cursor c=d.rawQuery("SELECT id,startDate,endDate,restartDate FROM athletes",null);d.beginTransaction();
            while(c.moveToNext()){
                long id=c.getLong(0);String start=safe419(c.getString(1)),end=safe419(c.getString(2)),restart=safe419(c.getString(3));
                if(isIso419(start))insertMembershipLocal419(d,id,start,"START",LEGACY_NOTE,null);
                if(isIso419(end))insertMembershipLocal419(d,id,end,"LEAVE",LEGACY_NOTE,null);
                if(isIso419(restart))insertMembershipLocal419(d,id,restart,"RESTART",LEGACY_NOTE,null);
            }
            c.close();d.setTransactionSuccessful();d.endTransaction();
        }catch(Exception ignored){}
    }

    private void insertMembershipLocal419(SQLiteDatabase d,long id,String date,String type,String note,String cloudId){
        ContentValues v=new ContentValues();if(cloudId!=null)v.put("cloudId",cloudId);v.put("athleteId",id);v.put("eventDate",date);v.put("eventType",type);v.put("note",note==null?"":note);
        d.insertWithOnConflict("membership_events",null,v,SQLiteDatabase.CONFLICT_IGNORE);
    }

    private String safe419(String s){return s==null?"":s.trim();}
    private boolean isIso419(String s){return s!=null&&s.matches("\\d{4}-\\d{2}-\\d{2}");}

    private void pullMembership419(){
        if(cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty())return;
        new Thread(()->{try{
            String token=cloudPrefs.getString("access_token","");HttpResult r=request("GET",SUPABASE_URL+"/rest/v1/athlete_membership_events?select=id,legacy_id,event_date,event_type,note&order=event_date.asc",null,token);
            if(r.code==401&&refreshSession())r=request("GET",SUPABASE_URL+"/rest/v1/athlete_membership_events?select=id,legacy_id,event_date,event_type,note&order=event_date.asc",null,cloudPrefs.getString("access_token",""));
            if(r.code<200||r.code>=300)return;JSONArray a=new JSONArray(r.body);SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();
            for(int i=0;i<a.length();i++){JSONObject x=a.getJSONObject(i);long id=x.optLong("legacy_id",-1);String date=x.optString("event_date",""),type=x.optString("event_type","");if(id<=0||!isIso419(date)||type.isEmpty())continue;ContentValues v=new ContentValues();v.put("cloudId",x.optString("id",""));v.put("athleteId",id);v.put("eventDate",date);v.put("eventType",type);v.put("note",x.optString("note",""));d.insertWithOnConflict("membership_events",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
            d.setTransactionSuccessful();d.endTransaction();
        }catch(Exception ignored){}},"membership-pull-419").start();
    }

    private void pushPendingMembership419(){
        if(cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty())return;
        new Thread(()->{try{Cursor c=db.getReadableDatabase().rawQuery("SELECT id,athleteId,eventDate,eventType,note FROM membership_events WHERE (cloudId IS NULL OR TRIM(cloudId)='') AND note<>?",new String[]{LEGACY_NOTE});while(c.moveToNext())pushMembershipRow419(c.getLong(0),c.getLong(1),c.getString(2),c.getString(3),c.getString(4));c.close();}catch(Exception ignored){}},"membership-push-419").start();
    }

    private void pushMembershipRow419(long localId,long athleteId,String date,String type,String note){
        try{
            String token=cloudPrefs.getString("access_token","");if(token.isEmpty())return;
            JSONObject o=new JSONObject().put("legacy_id",athleteId).put("event_date",date).put("event_type",type).put("note",note==null?"":note);
            HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/athlete_membership_events",o.toString(),token);
            if(r.code==401&&refreshSession())r=request("POST",SUPABASE_URL+"/rest/v1/athlete_membership_events",o.toString(),cloudPrefs.getString("access_token",""));
            if((r.code>=200&&r.code<300)||r.code==409){ContentValues v=new ContentValues();v.put("cloudId","SYNCED:"+athleteId+":"+date+":"+type);db.getWritableDatabase().update("membership_events",v,"id=?",new String[]{String.valueOf(localId)});}
        }catch(Exception ignored){}
    }

    @Override void syncFromCloud(boolean announce){super.syncFromCloud(announce);pullMembership419();}

    @Override void form(long id){
        super.form(id);
        if(id<=0)return;
        ensureMembership419();seedLegacyMembership419();
        addRestartClear419(root,id);
        LinearLayout b=findBox(root);if(b!=null)addMembershipEditor419(b,id);
    }

    private void addRestartClear419(View root,long id){
        EditText restart=findEdit419(root,"YENİDEN BAŞLAMA");if(restart==null)return;ViewParent p=restart.getParent();if(!(p instanceof LinearLayout))return;LinearLayout box=(LinearLayout)p;
        Button clear=btn("YENİDEN BAŞLAMA TARİHİNİ TEMİZLE");clear.setBackground(round(Color.rgb(235,235,235),10));clear.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("YENİDEN BAŞLAMA TARİHİNİ TEMİZLE").setMessage("Bu tarih temizlensin mi? Değişiklikleri kaydettiğinizde uygulanacaktır.").setPositiveButton("EVET",(d,w)->{restart.setText("");toast("YENİDEN BAŞLAMA TARİHİ TEMİZLENDİ. DEĞİŞİKLİKLERİ KAYDEDİN.");}).setNegativeButton("VAZGEÇ",null).show());
        int pos=box.indexOfChild(restart);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(46));lp.setMargins(0,dp(3),0,dp(5));box.addView(clear,Math.min(pos+1,box.getChildCount()),lp);
    }

    private EditText findEdit419(View v,String term){if(v instanceof EditText){EditText e=(EditText)v;String h=e.getHint()==null?"":e.getHint().toString().toUpperCase(new Locale("tr","TR"));if(h.contains(term))return e;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){EditText e=findEdit419(g.getChildAt(i),term);if(e!=null)return e;}}return null;}

    private void addMembershipEditor419(LinearLayout b,long id){
        TextView h=tv("ÜYELİK HAREKETLERİ",16,BLACK,true);h.setPadding(dp(8),dp(18),dp(8),dp(6));b.addView(h);
        LinearLayout actions=new LinearLayout(this);Button leave=btn("ARA VER / BIRAK");Button restart=btn("YENİDEN BAŞLAT");actions.addView(leave,new LinearLayout.LayoutParams(0,dp(52),1));actions.addView(restart,new LinearLayout.LayoutParams(0,dp(52),1));b.addView(actions);
        leave.setOnClickListener(v->chooseLeave419(id));restart.setOnClickListener(v->pickEventDate419(id,"RESTART","YENİDEN BAŞLADI"));
        Button last=btn("SON HAREKETİ DÜZENLE / SİL");last.setBackground(round(Color.rgb(235,235,235),10));last.setOnClickListener(v->lastEventMenu419(id));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(48));lp.setMargins(0,dp(5),0,dp(8));b.addView(last,lp);
        Cursor c=db.getReadableDatabase().rawQuery("SELECT eventDate,eventType,note FROM membership_events WHERE athleteId=? ORDER BY eventDate ASC,id ASC",new String[]{String.valueOf(id)});while(c.moveToNext()){String date=c.getString(0),type=c.getString(1),note=c.getString(2);String label="START".equals(type)?"BAŞLANGIÇ":"RESTART".equals(type)?"YENİDEN BAŞLADI":(LEGACY_NOTE.equals(note)?"ARA VERDİ / BIRAKTI":note);b.addView(tv(dateTr(date)+" • "+label,12,Color.DKGRAY,false));}c.close();
    }

    private void chooseLeave419(long id){new AlertDialog.Builder(this).setTitle("ÜYELİK HAREKETİ").setItems(new String[]{"ARA VERDİ","BIRAKTI"},(d,w)->pickEventDate419(id,"LEAVE",w==0?"ARA VERDİ":"BIRAKTI")).show();}

    private void pickEventDate419(long id,String type,String note){
        Calendar c=Calendar.getInstance();new DatePickerDialog(this,(v,y,m,d)->saveEvent419(id,String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d),type,note),c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveEvent419(long id,String date,String type,String note){
        ensureMembership419();SQLiteDatabase d=db.getWritableDatabase();ContentValues v=new ContentValues();v.put("athleteId",id);v.put("eventDate",date);v.put("eventType",type);v.put("note",note);long row=d.insertWithOnConflict("membership_events",null,v,SQLiteDatabase.CONFLICT_IGNORE);
        if(row<0){toast("BU TARİHTE AYNI ÜYELİK HAREKETİ ZATEN KAYITLI.");return;}
        ContentValues a=new ContentValues();if("LEAVE".equals(type))a.put("status","BIRAKTI".equals(note)?"BIRAKTI":"ARA VERDİ");else a.put("status","AKTİF");d.update("athletes",a,"id=?",new String[]{String.valueOf(id)});
        final long localId=row;new Thread(()->pushMembershipRow419(localId,id,date,type,note),"membership-one-419").start();toast("ÜYELİK HAREKETİ KAYDEDİLDİ.");form(id);
    }

    private void lastEventMenu419(long id){
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,eventDate,eventType,note FROM membership_events WHERE athleteId=? AND eventType<>'START' ORDER BY eventDate DESC,id DESC LIMIT 1",new String[]{String.valueOf(id)});if(!c.moveToFirst()){c.close();toast("DÜZENLENECEK ÜYELİK HAREKETİ YOK.");return;}long row=c.getLong(0);String oldDate=c.getString(1),type=c.getString(2),note=c.getString(3);c.close();
        new AlertDialog.Builder(this).setTitle(dateTr(oldDate)+" • "+("RESTART".equals(type)?"YENİDEN BAŞLADI":note)).setItems(new String[]{"TARİHİ DEĞİŞTİR","HAREKETİ SİL"},(d,w)->{if(w==0)editEventDate419(id,row,oldDate,type,note);else deleteEvent419(id,row,oldDate,type,note);}).show();
    }

    private void editEventDate419(long id,long row,String oldDate,String type,String note){Calendar c=Calendar.getInstance();try{c.set(Integer.parseInt(oldDate.substring(0,4)),Integer.parseInt(oldDate.substring(5,7))-1,Integer.parseInt(oldDate.substring(8,10)));}catch(Exception ignored){}new DatePickerDialog(this,(v,y,m,d)->{String nd=String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d);deleteCloudEvent419(id,oldDate,type);ContentValues cv=new ContentValues();cv.put("eventDate",nd);cv.putNull("cloudId");db.getWritableDatabase().update("membership_events",cv,"id=?",new String[]{String.valueOf(row)});new Thread(()->pushMembershipRow419(row,id,nd,type,note)).start();form(id);},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}

    private void deleteEvent419(long id,long row,String date,String type,String note){new AlertDialog.Builder(this).setTitle("ÜYELİK HAREKETİNİ SİL").setMessage("Seçili hareket silinsin mi? Geçmiş aidat kayıtlarına dokunulmaz.").setPositiveButton("SİL",(d,w)->{db.getWritableDatabase().delete("membership_events","id=?",new String[]{String.valueOf(row)});if(!LEGACY_NOTE.equals(note))deleteCloudEvent419(id,date,type);recalcStatus419(id);form(id);}).setNegativeButton("VAZGEÇ",null).show();}

    private void deleteCloudEvent419(long id,String date,String type){new Thread(()->{try{String token=cloudPrefs.getString("access_token","");String url=SUPABASE_URL+"/rest/v1/athlete_membership_events?legacy_id=eq."+id+"&event_date=eq."+URLEncoder.encode(date,"UTF-8")+"&event_type=eq."+URLEncoder.encode(type,"UTF-8");request("DELETE",url,null,token);}catch(Exception ignored){}},"membership-delete-419").start();}

    private void recalcStatus419(long id){Cursor c=db.getReadableDatabase().rawQuery("SELECT eventType,note FROM membership_events WHERE athleteId=? ORDER BY eventDate DESC,id DESC LIMIT 1",new String[]{String.valueOf(id)});String status="AKTİF";if(c.moveToFirst()){String type=c.getString(0),note=c.getString(1);if("LEAVE".equals(type))status="BIRAKTI".equals(note)?"BIRAKTI":"ARA VERDİ";}c.close();ContentValues v=new ContentValues();v.put("status",status);db.getWritableDatabase().update("athletes",v,"id=?",new String[]{String.valueOf(id)});}

    private boolean activeByEvents419(long id,int year,int month,String start,String end,String restart){
        try{
            ensureMembership419();int anchor=anchorDay(start);Calendar due=cycleDate(year*100+month,anchor);String ds=String.format(Locale.US,"%04d-%02d-%02d",due.get(Calendar.YEAR),due.get(Calendar.MONTH)+1,due.get(Calendar.DAY_OF_MONTH));Cursor c=db.getReadableDatabase().rawQuery("SELECT eventType FROM membership_events WHERE athleteId=? AND eventDate<=? ORDER BY eventDate ASC,id ASC",new String[]{String.valueOf(id),ds});boolean any=false,active=false;while(c.moveToNext()){any=true;String t=c.getString(0);if("START".equals(t)||"RESTART".equals(t))active=true;else if("LEAVE".equals(t))active=false;}c.close();if(any)return active;
        }catch(Exception ignored){}
        return activeAt(year,month,start,end,restart);
    }

    @Override DashData dashboardData(){
        DashData d=new DashData();Calendar now=Calendar.getInstance();int cy=now.get(Calendar.YEAR),cm=now.get(Calendar.MONTH)+1,currentCalendarKey=cy*100+cm;Calendar monthStart=(Calendar)now.clone();monthStart.set(Calendar.DAY_OF_MONTH,1);zeroTime(monthStart);
        Cursor c=db.athletes("","TÜMÜ");while(c.moveToNext()){
            long id=c.getLong(c.getColumnIndexOrThrow("id"));String name=s(c,"name"),cat=s(c,"category"),photo=s(c,"photo"),start=s(c,"startDate"),end=s(c,"endDate"),restart=s(c,"restartDate"),sib=s(c,"sibling");int by=c.getInt(c.getColumnIndexOrThrow("birthYear"));int anchor=anchorDay(start);HashMap<Integer,PayRec> pays=paymentMap(id);
            int paidMonth=0;for(Map.Entry<Integer,PayRec> e:pays.entrySet()){PayRec pr=e.getValue();if(pr.amount<=0)continue;if(isDate(pr.marker)){Calendar pd=parseIsoCal(pr.marker);if(pd!=null&&pd.get(Calendar.YEAR)==cy&&pd.get(Calendar.MONTH)+1==cm&&!pd.after(now))paidMonth+=pr.amount;}else if(e.getKey()==currentCalendarKey)paidMonth+=pr.amount;}if(paidMonth>0){DashItem z=item(id,name,cat,photo,by);z.amount=paidMonth;z.detail="Bu ay tahsil edilen: "+money(paidMonth);d.collected.put(id,z);d.collectedTotal+=paidMonth;}
            Calendar due=cycleDate(currentCalendarKey,anchor);boolean active=activeByEvents419(id,cy,cm,start,end,restart);PayRec cur=pays.containsKey(currentCalendarKey)?pays.get(currentCalendarKey):new PayRec("",0);int exp=expectedFeeAt(id,cy,cm,cur);if("BURSLU".equalsIgnoreCase(sib))exp=0;int remainCurrent=Math.max(0,exp-cur.amount);
            if(active&&remainCurrent>0&&!due.before(monthStart)){if(!due.after(now)){DashItem z=item(id,name,cat,photo,by);z.amount=remainCurrent;z.detail="Vade: "+cycleDateLabel(currentCalendarKey,anchor)+" • Beklenen: "+money(remainCurrent);d.expected.put(id,z);d.expectedTotal+=remainCurrent;}else{DashItem z=item(id,name,cat,photo,by);z.amount=remainCurrent;z.detail="Vade: "+cycleDateLabel(currentCalendarKey,anchor)+" • Gelecek: "+money(remainCurrent);d.upcoming.put(id,z);d.upcomingTotal+=remainCurrent;}}
            int reg=parseMonthKey(start);if(reg==0)reg=202601;int lastCompleted=shiftMonth(currentCycleKey(now,anchor),-1);int late=0;for(int key=Math.max(reg,202601);key<=lastCompleted;key=shiftMonth(key,1)){if(key/100!=2026)continue;int yy=key/100,mm=key%100;if(!activeByEvents419(id,yy,mm,start,end,restart))continue;PayRec pr=pays.get(key);if(pr==null)pr=new PayRec("",0);if("X".equals(pr.marker))continue;int ex=expectedFeeAt(id,yy,mm,pr);if(ex<=0||"BURSLU".equalsIgnoreCase(sib))continue;late+=Math.max(0,ex-pr.amount);}
            if(late>0){DashItem z=item(id,name,cat,photo,by);z.amount=late;z.detail="Önceki dönemlerden borç: "+money(late);d.overdue.put(id,z);d.overdueTotal+=late;}
        }c.close();return d;
    }

    @Override void addCycleProfileRow(LinearLayout b,long id,int key,int anchor,String start,String end,String restart,String sibling,HashMap<Integer,PayRec> pays,boolean future){
        int yr=key/100,mo=key%100;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);int expected=expectedFeeAt(id,yr,mo,r);boolean active=activeByEvents419(id,yr,mo,start,end,restart);String status,detail;int color;String period=cycleDateLabel(key,anchor)+" – "+cycleDateLabel(shiftMonth(key,1),anchor);
        if(future){status="BEKLİYOR";color=Color.WHITE;detail=period+" • "+(expected>0?money(expected):"—")+(active&&expected>0?" • ERKEN ÖDEME GİRİLEBİLİR":"");}
        else if("X".equals(r.marker)){status="ARA VERDİ";color=GRAY;detail=period+" • ARA VERDİ";}
        else if("!".equals(r.marker)||"!!".equals(r.marker)){status=expected>0?(r.amount<expected?"EKSİK ÖDEME":r.amount>expected?"FAZLA ÖDEME":"ÖDENDİ"):"FARKLI TUTAR";color=status.equals("ÖDENDİ")?Color.rgb(9,242,153):ORANGE;detail=period+" • "+money(r.amount)+" • "+status;}
        else if(isDate(r.marker)){status=expected>0&&r.amount!=expected?(r.amount<expected?"EKSİK ÖDEME":r.amount>expected?"FAZLA ÖDEME":"ÖDENDİ"):"ÖDENDİ";color=status.equals("ÖDENDİ")?Color.rgb(9,242,153):ORANGE;detail=period+" • "+dateTr(r.marker)+" • "+money(r.amount)+" • "+status;}
        else if(!active){status="AKTİF DEĞİL";color=Color.rgb(225,225,225);detail=period+" • AKTİF DEĞİL";expected=0;}
        else if("BURSLU".equalsIgnoreCase(sibling)||expected==0&&yr>=2026&&currentMonthlyFee(id)==0){status="BURSLU";color=Color.rgb(225,225,225);detail=period+" • BURSLU";expected=0;}
        else if(expected==0&&yr<2026){status="VERİ YOK";color=Color.rgb(225,225,225);detail=period+" • VERİ YOK";}
        else{status="ÖDEME DÖNEMİ";color=YELLOW;detail=period+" • "+money(expected)+" • ÖDEME DÖNEMİ";}
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(10),dp(8),dp(10),dp(8));row.setBackground(round(color,9));row.addView(tv(monthName(mo)+" "+yr,14,BLACK,true));row.addView(tv(detail,12,Color.DKGRAY,false));
        if(yr==2026&&active&&expected>0){final int mm=mo,fee=expected,amt=r.amount;final String mk=r.marker;row.setOnClickListener(v->editPayment(id,mm,fee,mk,amt));}
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(6));b.addView(row,lp);
    }

    @Override void showDashList(String title,LinkedHashMap<Long,DashItem> map){
        if(!restoringDash419){dashTitle419=title;dashSort419=0;dashScroll419=0;}dashList419=true;super.showDashList(title,map);Spinner s=findSpinner419(root);ScrollView sv=findScroll419(root);if(restoringDash419){restoringDash419=false;if(s!=null)s.setSelection(dashSort419);if(sv!=null)sv.post(()->sv.scrollTo(0,dashScroll419));}
    }

    @Override void showAthletes(){dashList419=false;returnDash419=false;super.showAthletes();}

    @Override void showProfile(long id){
        if("LIST".equals(page)&&dashList419){Spinner s=findSpinner419(root);ScrollView sv=findScroll419(root);dashSort419=s==null?0:s.getSelectedItemPosition();dashScroll419=sv==null?0:sv.getScrollY();returnDash419=true;}
        super.showProfile(id);
    }

    private Spinner findSpinner419(View v){if(v instanceof Spinner)return (Spinner)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){Spinner s=findSpinner419(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private ScrollView findScroll419(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll419(g.getChildAt(i));if(s!=null)return s;}}return null;}

    private LinkedHashMap<Long,DashItem> dashMap419(String title){DashData d=dashboardData();if(title.startsWith("TAHSİL"))return d.collected;if(title.startsWith("BEKLENEN"))return d.expected;if(title.startsWith("GECİKMİŞ"))return d.overdue;return d.upcoming;}
    private void restoreDash419(){restoringDash419=true;returnDash419=false;showDashList(dashTitle419,dashMap419(dashTitle419));}

    @Override void goBack(){if("PROFILE".equals(page)&&returnDash419){restoreDash419();return;}super.goBack();}
    @Override public void onBackPressed(){if("PROFILE".equals(page)&&returnDash419){restoreDash419();return;}super.onBackPressed();}
}

package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivityV426 extends MainActivityV424 {
    private static final String[] UNDO_TABLES={"athletes","payments","fee_history","membership_events","material_transactions","material_products"};
    private final TimeZone IST426=TimeZone.getTimeZone("Europe/Istanbul");

    @Override public void onCreate(android.os.Bundle b){
        super.onCreate(b);
        try{db.close();}catch(Exception ignored){}
        db=new DbHelper426(this);
        ensureUndo426();
        showHome();
    }

    @Override void base(String title,boolean back){
        super.base(title,back);
        try{
            if(root==null||root.getChildCount()==0||!(root.getChildAt(0) instanceof LinearLayout))return;
            LinearLayout bar=(LinearLayout)root.getChildAt(0);int n=undoCount426();
            TextView u=tv("↶ GERİ AL"+(n>0?" ("+n+")":""),11,Color.WHITE,true);u.setGravity(Gravity.CENTER);u.setAlpha(n>0?1f:.45f);
            u.setOnClickListener(v->{if(undoCount426()==0)toast("GERİ ALINACAK İŞLEM YOK.");else showUndo426();});
            bar.addView(u,new LinearLayout.LayoutParams(dp(92),dp(48)));
        }catch(Exception ignored){}
    }

    private void ensureUndo426(){
        SQLiteDatabase d=db.getWritableDatabase();
        d.execSQL("CREATE TABLE IF NOT EXISTS undo_history(id INTEGER PRIMARY KEY AUTOINCREMENT,tableName TEXT NOT NULL,operation TEXT NOT NULL,athleteId INTEGER NOT NULL DEFAULT 0,aux1 INTEGER NOT NULL DEFAULT 0,aux2 INTEGER NOT NULL DEFAULT 0,undoSql TEXT NOT NULL,detail TEXT NOT NULL DEFAULT '',createdAt TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        for(String t:UNDO_TABLES)if(tableExists426(d,t)){rebuildAudit426(d,t);rebuildUndo426(d,t);}
    }

    private boolean tableExists426(SQLiteDatabase d,String t){Cursor c=d.rawQuery("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?",new String[]{t});boolean ok=c.moveToFirst();c.close();return ok;}
    private ArrayList<String> columns426(SQLiteDatabase d,String t){ArrayList<String>x=new ArrayList<>();Cursor c=d.rawQuery("PRAGMA table_info("+t+")",null);while(c.moveToNext())x.add(c.getString(1));c.close();return x;}
    private String pk426(SQLiteDatabase d,String t){String pk="rowid";Cursor c=d.rawQuery("PRAGMA table_info("+t+")",null);while(c.moveToNext())if(c.getInt(5)>0){pk=c.getString(1);break;}c.close();return pk;}
    private boolean hasCol426(SQLiteDatabase d,String t,String col){Cursor c=d.rawQuery("PRAGMA table_info("+t+")",null);boolean ok=false;while(c.moveToNext())if(col.equals(c.getString(1))){ok=true;break;}c.close();return ok;}

    private String aidExpr426(SQLiteDatabase d,String t,String ref){if("athletes".equals(t))return ref+".id";if(hasCol426(d,t,"athleteId"))return ref+".athleteId";return "0";}
    private int labelColor426(String t){return "payments".equals(t)?ORANGE:"athletes".equals(t)?GREEN:GOLD;}
    private String label426(String t){if("athletes".equals(t))return "SPORCU";if("payments".equals(t))return "ÖDEME";if("fee_history".equals(t))return "AİDAT";if("membership_events".equals(t))return "ÜYELİK";if("material_transactions".equals(t))return "MALZEME HAREKETİ";if("material_products".equals(t))return "MALZEME";return t.toUpperCase(TR);}
    private boolean queue426(String t){return "athletes".equals(t)||"payments".equals(t)||"fee_history".equals(t)||"membership_events".equals(t);}
    private String detailExpr426(SQLiteDatabase d,String t,String ref){
        if("athletes".equals(t))return "COALESCE("+ref+".name,'SPORCU')";
        if(hasCol426(d,t,"athleteId")){
            String base="COALESCE((SELECT name FROM athletes WHERE id="+ref+".athleteId),'SPORCU')";
            if("payments".equals(t))return base+" || ' • ' || "+ref+".month || '/' || "+ref+".year";
            if("fee_history".equals(t))return base+" || ' • ' || "+ref+".effectiveMonth || '/' || "+ref+".year";
            if("membership_events".equals(t))return base+" || ' • ' || COALESCE("+ref+".note,"+ref+".eventType) || ' • ' || "+ref+".eventDate";
            if("material_transactions".equals(t)&&hasCol426(d,t,"product"))return base+" || ' • ' || COALESCE("+ref+".product,'MALZEME')";
            return base;
        }
        if("material_products".equals(t)&&hasCol426(d,t,"name"))return "COALESCE("+ref+".name,'MALZEME')";
        return "'"+label426(t)+"'";
    }
    private String aux1426(String t,String ref){if("payments".equals(t))return ref+".year";if("fee_history".equals(t))return ref+".year";return "0";}
    private String aux2426(String t,String ref){if("payments".equals(t))return ref+".month";if("fee_history".equals(t))return ref+".effectiveMonth";return "0";}

    private void rebuildAudit426(SQLiteDatabase d,String t){
        String safe=t.replaceAll("[^A-Za-z0-9_]","");for(String s:new String[]{"ai","au","ad"})d.execSQL("DROP TRIGGER IF EXISTS q_"+safe+"_"+s);
        String cond="(SELECT suppress FROM sync_control WHERE id=1)=0",lab=label426(t);
        for(String op:new String[]{"INSERT","UPDATE","DELETE"}){
            String suf=op.equals("INSERT")?"ai":op.equals("UPDATE")?"au":"ad",ref=op.equals("DELETE")?"OLD":"NEW",act=op.equals("INSERT")?"EKLENDİ":op.equals("UPDATE")?"GÜNCELLENDİ":"SİLİNDİ";
            String aid=aidExpr426(d,t,ref),detail=detailExpr426(d,t,ref),q=queue426(t)?"INSERT INTO sync_queue(kind,entityId) VALUES('"+lab+"',CAST("+aid+" AS TEXT)); ":"";
            d.execSQL("CREATE TRIGGER q_"+safe+"_"+suf+" AFTER "+op+" ON "+safe+" WHEN "+cond+" BEGIN "+q+"INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('"+act+"','"+lab+"',CAST("+aid+" AS TEXT),"+detail+"); END");
        }
    }

    private void rebuildUndo426(SQLiteDatabase d,String t){
        String safe=t.replaceAll("[^A-Za-z0-9_]","");for(String s:new String[]{"ai","au","ad"})d.execSQL("DROP TRIGGER IF EXISTS u_"+safe+"_"+s);
        ArrayList<String> cols=columns426(d,t);if(cols.isEmpty())return;String pk=pk426(d,t),cond="(SELECT suppress FROM sync_control WHERE id=1)=0";
        String delExpr="'DELETE FROM "+safe+" WHERE "+pk+"=' || quote(NEW."+pk+")";
        StringBuilder upd=new StringBuilder("'UPDATE "+safe+" SET ");for(int i=0;i<cols.size();i++){String c=cols.get(i);if(i==0)upd.append(c).append("=' || quote(OLD.").append(c).append(")");else upd.append(" || ',").append(c).append("=' || quote(OLD.").append(c).append(")");}upd.append(" || ' WHERE ").append(pk).append("=' || quote(OLD.").append(pk).append(")");
        StringBuilder ins=new StringBuilder("'INSERT OR REPLACE INTO "+safe+"(");for(int i=0;i<cols.size();i++){if(i>0)ins.append(",");ins.append(cols.get(i));}ins.append(") VALUES(' || quote(OLD.").append(cols.get(0)).append(")");for(int i=1;i<cols.size();i++)ins.append(" || ',' || quote(OLD.").append(cols.get(i)).append(")");ins.append(" || ')'");
        createUndoTrigger426(d,safe,"INSERT","ai","NEW",delExpr,cond);createUndoTrigger426(d,safe,"UPDATE","au","NEW",upd.toString(),cond);createUndoTrigger426(d,safe,"DELETE","ad","OLD",ins.toString(),cond);
    }
    private void createUndoTrigger426(SQLiteDatabase d,String t,String op,String suf,String ref,String undoExpr,String cond){String aid=aidExpr426(d,t,ref),det=detailExpr426(d,t,ref),a1=aux1426(t,ref),a2=aux2426(t,ref);d.execSQL("CREATE TRIGGER u_"+t+"_"+suf+" AFTER "+op+" ON "+t+" WHEN "+cond+" BEGIN INSERT INTO undo_history(tableName,operation,athleteId,aux1,aux2,undoSql,detail) VALUES('"+t+"','"+op+"',"+aid+","+a1+","+a2+","+undoExpr+","+det+"); DELETE FROM undo_history WHERE id NOT IN (SELECT id FROM undo_history ORDER BY id DESC LIMIT 20); END");}

    private int undoCount426(){try{Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM undo_history",null);c.moveToFirst();int n=c.getInt(0);c.close();return n;}catch(Exception e){return 0;}}
    private void showUndo426(){
        ArrayList<String> rows=new ArrayList<>();Cursor c=db.getReadableDatabase().rawQuery("SELECT operation,tableName,detail,createdAt FROM undo_history ORDER BY id DESC LIMIT 20",null);while(c.moveToNext())rows.add(istanbul426(c.getString(3))+" • "+opText426(c.getString(0))+" • "+c.getString(2));c.close();
        new AlertDialog.Builder(this).setTitle("GERİ ALMA GEÇMİŞİ • "+rows.size()+"/20").setMessage(rows.isEmpty()?"Kayıt yok.":join426(rows)).setPositiveButton("SON İŞLEMİ GERİ AL",(d,w)->confirmUndo426()).setNegativeButton("KAPAT",null).show();
    }
    private String join426(ArrayList<String>a){StringBuilder b=new StringBuilder();for(int i=0;i<a.size();i++)b.append(i+1).append(". ").append(a.get(i)).append('\n');return b.toString().trim();}
    private String opText426(String o){return "INSERT".equals(o)?"EKLEME":"DELETE".equals(o)?"SİLME":"DEĞİŞİKLİK";}
    private void confirmUndo426(){Cursor c=db.getReadableDatabase().rawQuery("SELECT id,tableName,operation,athleteId,aux1,aux2,undoSql,detail FROM undo_history ORDER BY id DESC LIMIT 1",null);if(!c.moveToFirst()){c.close();return;}long uid=c.getLong(0),aid=c.getLong(3);String t=c.getString(1),op=c.getString(2),sql=c.getString(6),det=c.getString(7);int a1=c.getInt(4),a2=c.getInt(5);c.close();new AlertDialog.Builder(this).setTitle("SON İŞLEMİ GERİ AL").setMessage(det+"\n\nBu işlem geri alınsın mı?").setPositiveButton("EVET, GERİ AL",(d,w)->undo426(uid,t,op,aid,a1,a2,sql,det)).setNegativeButton("VAZGEÇ",null).show();}
    private void undo426(long uid,String t,String op,long aid,int a1,int a2,String sql,String det){
        SQLiteDatabase d=db.getWritableDatabase();try{d.execSQL("UPDATE sync_control SET suppress=1 WHERE id=1");d.beginTransaction();d.execSQL(sql);d.delete("undo_history","id=?",new String[]{String.valueOf(uid)});d.setTransactionSuccessful();d.endTransaction();}catch(Exception e){try{if(d.inTransaction())d.endTransaction();}catch(Exception ignored){}toast("GERİ ALINAMADI: "+e.getMessage());return;}finally{try{d.execSQL("UPDATE sync_control SET suppress=0 WHERE id=1");}catch(Exception ignored){}}
        ContentValues l=new ContentValues();l.put("userEmail",email426());l.put("action","GERİ ALINDI");l.put("entityType",label426(t));l.put("entityId",String.valueOf(aid));l.put("detail",det);d.insert("activity_log_local",null,l);
        if(queue426(t)){ContentValues q=new ContentValues();q.put("kind",label426(t));q.put("entityId",String.valueOf(aid));d.insert("sync_queue",null,q);}syncUndoCloud426(t,op,aid,a1,a2);
        toast("SON İŞLEM GERİ ALINDI.");if(currentAthlete>0)showProfile(currentAthlete);else showHome();
    }

    private void syncUndoCloud426(String t,String op,long aid,int a1,int a2){new Thread(()->{try{String token=cloudPrefs==null?"":cloudPrefs.getString("access_token","");if(token.isEmpty())return;if("payments".equals(t)&&"INSERT".equals(op)){request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_delete_mobile_payment",new JSONObject().put("p_legacy_id",aid).put("p_year",a1).put("p_month",a2).toString(),token);return;}if("fee_history".equals(t)&&"INSERT".equals(op)){request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_delete_mobile_fee",new JSONObject().put("p_legacy_id",aid).put("p_year",a1).put("p_month",a2).toString(),token);return;}if("athletes".equals(t)&&"INSERT".equals(op)){request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_delete_mobile_athlete",new JSONObject().put("p_legacy_id",aid).toString(),token);return;}if("membership_events".equals(t)){syncMembershipState426(aid,token);return;}if("material_transactions".equals(t)){syncMaterialState426(aid,token);return;}if("material_products".equals(t)){syncProducts426(token);return;}}catch(Exception ignored){}},"undo-cloud-426").start();}
    private void syncMembershipState426(long aid,String token)throws Exception{request("DELETE",SUPABASE_URL+"/rest/v1/athlete_membership_events?legacy_id=eq."+aid,null,token);Cursor c=db.getReadableDatabase().rawQuery("SELECT eventDate,eventType,note FROM membership_events WHERE athleteId=? AND note<>'LEGACY' ORDER BY eventDate,id",new String[]{String.valueOf(aid)});while(c.moveToNext()){JSONObject o=new JSONObject().put("legacy_id",aid).put("event_date",c.getString(0)).put("event_type",c.getString(1)).put("note",c.getString(2)==null?"":c.getString(2));request("POST",SUPABASE_URL+"/rest/v1/athlete_membership_events",o.toString(),token);}c.close();}
    private void syncMaterialState426(long aid,String token)throws Exception{request("DELETE",SUPABASE_URL+"/rest/v1/material_transactions?athlete_legacy_id=eq."+aid,null,token);Cursor c=db.getReadableDatabase().rawQuery("SELECT product,qty,unitPrice,total,paidAmount,issuedDate,paymentDate,note FROM material_transactions WHERE athleteId=? ORDER BY id",new String[]{String.valueOf(aid)});while(c.moveToNext()){JSONObject o=new JSONObject().put("athlete_legacy_id",aid).put("product_name",c.getString(0)).put("quantity",c.getInt(1)).put("unit_price",c.getInt(2)).put("total_amount",c.getInt(3)).put("paid_amount",c.getInt(4)).put("issued_at",c.getString(5)).put("note",c.getString(7)==null?"":c.getString(7));String pd=c.getString(6);if(pd!=null&&!pd.isEmpty())o.put("payment_date",pd);request("POST",SUPABASE_URL+"/rest/v1/material_transactions",o.toString(),token);}c.close();}
    private void syncProducts426(String token)throws Exception{request("DELETE",SUPABASE_URL+"/rest/v1/material_products?id=not.is.null",null,token);Cursor c=db.getReadableDatabase().rawQuery("SELECT name,currentPrice,active FROM material_products ORDER BY name",null);while(c.moveToNext()){JSONObject o=new JSONObject().put("name",c.getString(0)).put("current_price",c.getInt(1)).put("active",c.getInt(2)!=0);request("POST",SUPABASE_URL+"/rest/v1/material_products",o.toString(),token);}c.close();}

    @Override void showHome(){super.showHome();regroupHomeCards426();replaceActivity426();}
    private void regroupHomeCards426(){ScrollView sv=findScroll426(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;LinearLayout box=(LinearLayout)sv.getChildAt(0);View material=findCard426(root,"ÖDENMEMİŞ MALZEME"),photo=findCard426(root,"FOTOĞRAFI OLMAYAN AKTİF SPORCULAR"),form=findCard426(root,"KAYIT FORMU OLMAYAN AKTİF SPORCULAR");ArrayList<View> cards=new ArrayList<>();for(View c:new View[]{material,photo,form})if(c!=null&&!cards.contains(c))cards.add(c);if(cards.isEmpty())return;for(View c:cards){ViewParent p=c.getParent();if(p instanceof ViewGroup)((ViewGroup)p).removeView(c);}LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);for(View c:cards){shrink426(c);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(118),1);lp.setMargins(dp(3),dp(4),dp(3),dp(4));row.addView(c,lp);}int at=findDashEnd426(box);box.addView(row,Math.min(at,box.getChildCount()),new LinearLayout.LayoutParams(-1,dp(126)));}
    private int findDashEnd426(LinearLayout b){for(int i=0;i<b.getChildCount();i++){View v=b.getChildAt(i);if(contains426(v,"GECİKMİŞ")||contains426(v,"AY SONUNA KADAR"))return i+1;}return Math.min(3,b.getChildCount());}
    private void shrink426(View v){if(v instanceof TextView){TextView t=(TextView)v;t.setTextSize(Math.min(t.getTextSize()/getResources().getDisplayMetrics().scaledDensity,12f));t.setGravity(Gravity.CENTER);t.setPadding(dp(3),dp(3),dp(3),dp(3));}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)shrink426(g.getChildAt(i));}}
    private View findCard426(View v,String term){if(v instanceof TextView&&String.valueOf(((TextView)v).getText()).toUpperCase(TR).contains(term)){View p=(View)v.getParent();return p==null?v:p;}if(v instanceof Button&&String.valueOf(((Button)v).getText()).toUpperCase(TR).contains(term)){View p=(View)v.getParent();return p==null?v:p;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View x=findCard426(g.getChildAt(i),term);if(x!=null)return x;}}return null;}
    private boolean contains426(View v,String term){if(v instanceof TextView&&String.valueOf(((TextView)v).getText()).contains(term))return true;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(contains426(g.getChildAt(i),term))return true;}return false;}
    private ScrollView findScroll426(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll426(g.getChildAt(i));if(s!=null)return s;}}return null;}

    private void replaceActivity426(){ScrollView s=findScroll426(root);if(s==null||s.getChildCount()==0||!(s.getChildAt(0) instanceof LinearLayout))return;LinearLayout b=(LinearLayout)s.getChildAt(0);int start=-1,end=-1;for(int i=0;i<b.getChildCount();i++){View v=b.getChildAt(i);if(start<0&&contains426(v,"SON İŞLEMLER"))start=i;if(start>=0&&contains426(v,"TÜM İŞLEM GÜNLÜĞÜ")){end=i;break;}}if(start>=0){if(end<start)end=b.getChildCount()-1;for(int i=end;i>=start;i--)b.removeViewAt(i);}TextView h=tv("SON İŞLEMLER • İSTANBUL SAATİ",15,BLACK,true);h.setPadding(dp(8),dp(18),dp(8),dp(6));b.addView(h);Cursor c=db.getReadableDatabase().rawQuery("SELECT action,entityType,detail,createdAt,cloudSynced FROM activity_log_local ORDER BY id DESC LIMIT 6",null);int n=0;while(c.moveToNext()){String text=istanbul426(c.getString(3))+" • "+c.getString(0)+" • "+c.getString(1);String det=c.getString(2);if(det!=null&&!det.isEmpty())text+="\n"+det;text+=" • "+(c.getInt(4)==1?"BULUTTA":"BEKLİYOR");b.addView(tv(text,11,Color.DKGRAY,false));n++;}c.close();if(n==0)b.addView(tv("Henüz kullanıcı işlemi yok.",11,Color.DKGRAY,false));Button all=btn("TÜM İŞLEM GÜNLÜĞÜ");all.setOnClickListener(v->showActivity426());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(48));lp.setMargins(0,dp(5),0,dp(10));b.addView(all,lp);}
    private void showActivity426(){page="ACTIVITY426";base("İŞLEM GÜNLÜĞÜ",true);ScrollView s=scroll();LinearLayout b=box(s);Cursor c=db.getReadableDatabase().rawQuery("SELECT userEmail,action,entityType,detail,createdAt,cloudSynced FROM activity_log_local ORDER BY id DESC LIMIT 300",null);while(c.moveToNext()){String who=c.getString(0);if(who==null||who.isEmpty())who=email426();String x=istanbul426(c.getString(4))+"\n"+who+"\n"+c.getString(1)+" • "+c.getString(2)+(c.getString(3)==null||c.getString(3).isEmpty()?"":"\n"+c.getString(3))+"\n"+(c.getInt(5)==1?"BULUTA AKTARILDI":"BULUTA AKTARILMAYI BEKLİYOR");TextView t=tv(x,12,Color.DKGRAY,false);t.setBackground(round(Color.WHITE,9));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(6));b.addView(t,lp);}c.close();}
    private String istanbul426(String x){try{SimpleDateFormat in=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);in.setTimeZone(TimeZone.getTimeZone("UTC"));Date d=in.parse(x);SimpleDateFormat out=new SimpleDateFormat("dd.MM.yyyy HH:mm:ss",new Locale("tr","TR"));out.setTimeZone(IST426);return out.format(d);}catch(Exception e){return x==null?"":x;}}
    private String email426(){try{String t=cloudPrefs==null?"":cloudPrefs.getString("access_token","");String[] p=t.split("\\.");if(p.length>1){byte[] b=Base64.decode(p[1],Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING);return new JSONObject(new String(b,StandardCharsets.UTF_8)).optString("email","");}}catch(Exception ignored){}return "";}

    @Override void showCloudMenu(){int pending=0;try{Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM sync_queue",null);c.moveToFirst();pending=c.getInt(0);c.close();}catch(Exception ignored){}String pull=cloudPrefs.getString("sync_last_pull_at","HENÜZ YOK"),push=cloudPrefs.getString("sync_last_push_at","HENÜZ YOK"),auto=cloudPrefs.getString("sync_last_auto_at","HENÜZ YOK");String msg="OTOMATİK SENKRONİZASYON: AÇIK\nBEKLEYEN KAYIT: "+pending+"\n\nSON OTOMATİK GÖNDERME\n"+push+"\n\nSON BULUTTAN GÜNCELLEME\n"+pull+"\n\nSON BULUTA GÖNDERME\n"+push;String[] items={"TÜMÜNÜ ŞİMDİ SENKRONİZE ET","BULUTTAN GÜNCELLE","YEREL DEĞİŞİKLİKLERİ BULUTA GÖNDER","İŞLEM GÜNLÜĞÜ","OTURUMU KAPAT"};new AlertDialog.Builder(this).setTitle("ONLINE HESAP").setMessage(msg).setItems(items,(d,w)->{if(w==0)invokeAuto426(true,()->syncFromCloud(true));else if(w==1)syncFromCloud(true);else if(w==2)invokeAuto426(true,null);else if(w==3)showActivity426();else{cloudPrefs.edit().clear().apply();showLogin();}}).show();}
    private void invokeAuto426(boolean announce,Runnable after){try{Method m=MainActivityV421.class.getDeclaredMethod("autoPush421",boolean.class,Runnable.class);m.setAccessible(true);m.invoke(this,announce,after);}catch(Exception e){toast("SENKRONİZASYON BAŞLATILAMADI.");}}

    @Override void goBack(){if("ACTIVITY426".equals(page)){showHome();return;}super.goBack();}
}

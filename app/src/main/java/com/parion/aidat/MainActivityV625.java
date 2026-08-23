package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

/** v4.0.25 - fixed bottom navigation, editable athlete groups, payment-share green. */
public class MainActivityV625 extends MainActivityV624 {
    private static final int PAID_GREEN_625=Color.rgb(9,242,153); // matches ÖDEME BİLGİSİ share output
    private static final int GOLD_625=Color.rgb(205,156,34);
    private static final int TEXT_625=Color.rgb(28,28,28);
    private final ExecutorService groupSync625=Executors.newSingleThreadExecutor();

    @Override public void onCreate(android.os.Bundle b){super.onCreate(b);ensureGroups625();}

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(this::installBottomNav625);
    }

    @Override int paymentColor(int m,int fee,String sibling,String start,String end,String restart,String marker,int amount){
        int base=super.paymentColor(m,fee,sibling,start,end,restart,marker,amount);
        boolean normalPaid=isDate(marker)&&!"!".equals(marker)&&!"!!".equals(marker)&&!(amount>0&&fee>0&&amount!=fee);
        return normalPaid?PAID_GREEN_625:base;
    }

    private void installBottomNav625(){
        if(root==null||"LOGIN".equals(page))return;
        for(int i=root.getChildCount()-1;i>=0;i--){View v=root.getChildAt(i);if("v625-bottom-nav".equals(v.getTag()))root.removeViewAt(i);}
        LinearLayout nav=new LinearLayout(this);nav.setTag("v625-bottom-nav");nav.setGravity(Gravity.CENTER_VERTICAL);nav.setPadding(dp(6),dp(5),dp(6),dp(5));nav.setBackground(bottomBg625());
        Button settings=bottomButton625("⚙  AYARLAR");Button home=bottomButton625("⌂  ANASAYFA");
        settings.setOnClickListener(v->showSettings625());home.setOnClickListener(v->showHome());
        nav.addView(settings,new LinearLayout.LayoutParams(0,dp(48),1));
        View gap=new View(this);nav.addView(gap,new LinearLayout.LayoutParams(dp(10),dp(1)));
        nav.addView(home,new LinearLayout.LayoutParams(0,dp(48),1));
        root.addView(nav,new LinearLayout.LayoutParams(-1,dp(60)));
    }
    private Button bottomButton625(String text){Button b=new Button(this);b.setText(text);b.setTextSize(12.5f);b.setTextColor(TEXT_625);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setAllCaps(false);b.setBackground(roundStroke625(Color.WHITE,GOLD_625,12,1));b.setPadding(dp(6),0,dp(6),0);return b;}
    private GradientDrawable bottomBg625(){GradientDrawable d=new GradientDrawable();d.setColor(Color.rgb(250,248,241));d.setStroke(dp(1),Color.rgb(224,214,184));return d;}

    private void showSettings625(){
        page="SETTINGS_625";currentAthlete=-1;base("AYARLAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);b.setPadding(dp(14),dp(18),dp(14),dp(24));
        TextView title=tv("UYGULAMA AYARLARI",16,TEXT_625,true);b.addView(title);
        TextView info=tv("Sporcu gruplarını buradan yönetin. Grup listesi, sporcu kayıt ve düzenleme ekranındaki Grup / Takım alanında kullanılır.",12,Color.DKGRAY,false);info.setPadding(dp(8),dp(4),dp(8),dp(14));b.addView(info);
        Button groups=btn("GRUPLARI DÜZENLE");groups.setOnClickListener(v->showGroups625());b.addView(groups,new LinearLayout.LayoutParams(-1,dp(58)));
    }

    private void ensureGroups625(){
        if(db==null)return;SQLiteDatabase d=db.getWritableDatabase();
        d.execSQL("CREATE TABLE IF NOT EXISTS app_groups (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE COLLATE NOCASE, sortOrder INTEGER NOT NULL DEFAULT 0)");
        Cursor c=d.rawQuery("SELECT DISTINCT TRIM(category) FROM athletes WHERE category IS NOT NULL AND TRIM(category)<>''",null);while(c.moveToNext()){String g=c.getString(0);if(g!=null&&!g.trim().isEmpty()){ContentValues v=new ContentValues();v.put("name",g.trim().toUpperCase(new Locale("tr","TR")));d.insertWithOnConflict("app_groups",null,v,SQLiteDatabase.CONFLICT_IGNORE);}}c.close();
    }
    private ArrayList<String> groups625(){ensureGroups625();ArrayList<String> out=new ArrayList<>();Cursor c=db.getReadableDatabase().rawQuery("SELECT name FROM app_groups ORDER BY sortOrder,id,name COLLATE NOCASE",null);while(c.moveToNext())out.add(c.getString(0));c.close();return out;}

    private void showGroups625(){
        ensureGroups625();page="GROUPS_625";currentAthlete=-1;base("GRUPLARI DÜZENLE",true);ScrollView sv=scroll();LinearLayout b=box(sv);b.setPadding(dp(12),dp(12),dp(12),dp(22));
        Button add=btn("+ YENİ GRUP EKLE");add.setOnClickListener(v->editGroup625(null));LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,dp(56));ap.setMargins(0,0,0,dp(12));b.addView(add,ap);
        ArrayList<String> gs=groups625();if(gs.isEmpty()){b.addView(tv("Henüz grup tanımlanmamış.",14,Color.DKGRAY,true));return;}
        for(String g:gs)addGroupRow625(b,g);
    }
    private void addGroupRow625(LinearLayout b,String name){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(10),dp(7),dp(6),dp(7));row.setBackground(roundStroke625(Color.WHITE,Color.rgb(225,225,225),12,1));
        int count=groupAthleteCount625(name);LinearLayout txt=new LinearLayout(this);txt.setOrientation(LinearLayout.VERTICAL);txt.addView(tv(name,14,TEXT_625,true));txt.addView(tv(count+" sporcu",11,Color.DKGRAY,false));row.addView(txt,new LinearLayout.LayoutParams(0,-2,1));
        Button edit=miniButton625("DÜZENLE");Button del=miniButton625("SİL");del.setTextColor(Color.rgb(165,35,35));edit.setOnClickListener(v->editGroup625(name));del.setOnClickListener(v->deleteGroup625(name,count));row.addView(edit,new LinearLayout.LayoutParams(dp(94),dp(44)));row.addView(del,new LinearLayout.LayoutParams(dp(68),dp(44)));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(row,lp);
    }
    private Button miniButton625(String s){Button b=new Button(this);b.setText(s);b.setTextSize(10.5f);b.setAllCaps(false);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setBackground(roundStroke625(Color.WHITE,GOLD_625,10,1));return b;}
    private int groupAthleteCount625(String g){Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE category=? COLLATE NOCASE",new String[]{g});int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;}

    private void editGroup625(String oldName){
        EditText e=new EditText(this);e.setSingleLine(true);e.setHint("Grup adı");e.setText(oldName==null?"":oldName);e.setSelectAllOnFocus(true);int pad=dp(18);FrameLayout wrap=new FrameLayout(this);wrap.setPadding(pad,0,pad,0);wrap.addView(e,new FrameLayout.LayoutParams(-1,-2));
        new AlertDialog.Builder(this).setTitle(oldName==null?"YENİ GRUP":"GRUBU DÜZENLE").setView(wrap).setPositiveButton("KAYDET",(d,w)->saveGroup625(oldName,e.getText().toString())).setNegativeButton("VAZGEÇ",null).show();
    }
    private void saveGroup625(String oldName,String raw){
        String neo=raw==null?"":raw.trim().replaceAll("\\s+"," ").toUpperCase(new Locale("tr","TR"));if(neo.isEmpty()){toast("Grup adı boş olamaz.");return;}ensureGroups625();SQLiteDatabase d=db.getWritableDatabase();
        Cursor ex=d.rawQuery("SELECT COUNT(*) FROM app_groups WHERE name=? COLLATE NOCASE"+(oldName==null?"":" AND name<>? COLLATE NOCASE"),oldName==null?new String[]{neo}:new String[]{neo,oldName});boolean exists=ex.moveToFirst()&&ex.getInt(0)>0;ex.close();if(exists){toast("Bu grup zaten var.");return;}
        if(oldName==null){ContentValues v=new ContentValues();v.put("name",neo);d.insert("app_groups",null,v);toast("Grup eklendi.");showGroups625();return;}
        ArrayList<Long> affected=new ArrayList<>();Cursor ids=d.rawQuery("SELECT id FROM athletes WHERE category=? COLLATE NOCASE",new String[]{oldName});while(ids.moveToNext())affected.add(ids.getLong(0));ids.close();
        d.beginTransaction();try{ContentValues gv=new ContentValues();gv.put("name",neo);d.update("app_groups",gv,"name=? COLLATE NOCASE",new String[]{oldName});ContentValues av=new ContentValues();av.put("category",neo);d.update("athletes",av,"category=? COLLATE NOCASE",new String[]{oldName});d.setTransactionSuccessful();}finally{d.endTransaction();}
        syncAthletes625(affected);toast("Grup adı güncellendi.");showGroups625();
    }
    private void deleteGroup625(String name,int count){
        if(count>0){new AlertDialog.Builder(this).setTitle("GRUP SİLİNEMEDİ").setMessage(name+" grubunda "+count+" sporcu bulunuyor. Önce bu sporcuların Grup / Takım bilgisini başka bir gruba taşıyın; ardından grubu silebilirsiniz.").setPositiveButton("TAMAM",null).show();return;}
        new AlertDialog.Builder(this).setTitle("GRUBU SİL").setMessage(name+" grubu silinsin mi?").setPositiveButton("EVET, SİL",(d,w)->{db.getWritableDatabase().delete("app_groups","name=? COLLATE NOCASE",new String[]{name});toast("Grup silindi.");showGroups625();}).setNegativeButton("VAZGEÇ",null).show();
    }

    @Override void form(long id){super.form(id);if(root!=null)root.post(this::patchGroupField625);}
    private void patchGroupField625(){EditText cat=findCategoryField625(root);if(cat==null)return;cat.setFocusable(false);cat.setClickable(true);cat.setLongClickable(false);cat.setHint("GRUP / TAKIM • SEÇMEK İÇİN DOKUN");cat.setOnClickListener(v->chooseGroup625(cat));}
    private EditText findCategoryField625(View v){if(v instanceof EditText){EditText e=(EditText)v;CharSequence h=e.getHint();String s=h==null?"":h.toString().toUpperCase(new Locale("tr","TR"));if(s.contains("GRUP / TAKIM"))return e;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){EditText r=findCategoryField625(g.getChildAt(i));if(r!=null)return r;}}return null;}
    private void chooseGroup625(EditText target){ArrayList<String> gs=groups625();if(gs.isEmpty()){new AlertDialog.Builder(this).setTitle("GRUP YOK").setMessage("Önce Ayarlar > Grupları Düzenle bölümünden bir grup ekleyin.").setPositiveButton("AYARLARA GİT",(d,w)->showGroups625()).setNegativeButton("KAPAT",null).show();return;}String[] a=gs.toArray(new String[0]);new AlertDialog.Builder(this).setTitle("GRUP / TAKIM SEÇ").setItems(a,(d,w)->target.setText(a[w])).setNegativeButton("İPTAL",null).show();}

    private void syncAthletes625(ArrayList<Long> ids){if(ids==null||ids.isEmpty())return;groupSync625.execute(()->{for(long id:ids){try{String token=cloudPrefs==null?"":cloudPrefs.getString("access_token","");if(token.isEmpty())continue;ensureAthleteCloud413(id,token);}catch(Exception ignored){}}});}

    @Override void goBack(){if("GROUPS_625".equals(page)){showSettings625();return;}if("SETTINGS_625".equals(page)){showHome();return;}super.goBack();}
    @Override protected void onDestroy(){groupSync625.shutdownNow();super.onDestroy();}
    private GradientDrawable roundStroke625(int fill,int stroke,int radius,int width){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));d.setStroke(dp(width),stroke);return d;}
}

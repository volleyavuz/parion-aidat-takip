package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.*;
import android.net.Uri;
import android.text.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivityV393 extends MainActivityV392 {
    private final HashMap<String,Boolean> placeholderCache=new HashMap<>();
    private final SimpleDateFormat isoDate=new SimpleDateFormat("yyyy-MM-dd",Locale.US);

    @Override public void onCreate(android.os.Bundle b){
        super.onCreate(b);
        ensureMaterialTables();
        syncMaterialsFromCloud(false);
    }

    private void ensureMaterialTables(){
        SQLiteDatabase d=db.getWritableDatabase();
        d.execSQL("CREATE TABLE IF NOT EXISTS material_products(name TEXT PRIMARY KEY,currentPrice INTEGER NOT NULL DEFAULT 0,active INTEGER NOT NULL DEFAULT 1,cloudId TEXT)");
        d.execSQL("CREATE TABLE IF NOT EXISTS material_transactions(id INTEGER PRIMARY KEY AUTOINCREMENT,cloudId TEXT UNIQUE,athleteId INTEGER NOT NULL,product TEXT NOT NULL,qty INTEGER NOT NULL,unitPrice INTEGER NOT NULL,total INTEGER NOT NULL,paidAmount INTEGER NOT NULL DEFAULT 0,issuedDate TEXT NOT NULL,paymentDate TEXT,note TEXT)");
        d.execSQL("INSERT OR IGNORE INTO material_products(name,currentPrice,active) VALUES('TİŞÖRT',500,1)");
    }

    // FOTOĞRAF VAR/YOK kriteri artık dosya adı değil, gerçek görsel içeriğidir.
    private boolean isActuallyMissingPhoto(String photo){
        String p=photo==null?"":photo.trim();
        String up=p.toUpperCase(new Locale("tr","TR"));
        if(p.isEmpty()||"NONE".equalsIgnoreCase(p)||up.contains("0000 BOS"))return true;
        if(p.startsWith("USER:")){
            File f=new File(new File(getFilesDir(),"athlete_photos"),p.substring(5));
            return !f.isFile();
        }
        Boolean cached=placeholderCache.get(p);if(cached!=null)return cached;
        boolean missing=true;
        try(InputStream in=getAssets().open("photos/"+p)){
            Bitmap bm=BitmapFactory.decodeStream(in);
            missing=bm==null||looksLikeGrayPlaceholder(bm);
            if(bm!=null)bm.recycle();
        }catch(Exception ignored){missing=true;}
        placeholderCache.put(p,missing);return missing;
    }

    private boolean looksLikeGrayPlaceholder(Bitmap src){
        Bitmap b=Bitmap.createScaledBitmap(src,32,32,true);int lowSat=0,lightGray=0,dark=0,n=32*32;
        for(int y=0;y<32;y++)for(int x=0;x<32;x++){
            int c=b.getPixel(x,y),r=Color.red(c),g=Color.green(c),bl=Color.blue(c);int mx=Math.max(r,Math.max(g,bl)),mn=Math.min(r,Math.min(g,bl));int lum=(r+g+bl)/3;
            if(mx-mn<14)lowSat++;
            if((lum>=188&&lum<=238)||lum>=246)lightGray++;
            if(lum<175)dark++;
        }
        b.recycle();
        // 0000 BOS ve AZRA/AYPERİ örneklerindeki gibi neredeyse tamamen beyaz-açık gri silüet.
        return lowSat>n*0.985 && lightGray>n*0.94 && dark<n*0.015;
    }

    @Override void showHome(){
        super.showHome();
        replaceMissingPhotoCard(root);
        addMaterialDebtCard();
    }

    private void replaceMissingPhotoCard(View v){
        if(!(v instanceof ViewGroup))return;ViewGroup g=(ViewGroup)v;
        for(int i=0;i<g.getChildCount();i++){
            View ch=g.getChildAt(i);
            if(ch instanceof Button&&String.valueOf(((Button)ch).getText()).contains("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR")){
                if(i>0&&g.getChildAt(i-1) instanceof TextView)((TextView)g.getChildAt(i-1)).setText(String.valueOf(countActuallyMissing()));
                ch.setOnClickListener(x->showActuallyMissing());return;
            }
            replaceMissingPhotoCard(ch);
        }
    }

    private int countActuallyMissing(){int n=0;Cursor c=db.athletes("","AKTİF");while(c.moveToNext()){A x=a(c);if(isActuallyMissingPhoto(x.photo))n++;}c.close();return n;}
    private void showActuallyMissing(){
        page="LIST";base("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;
        Cursor c=db.athletes("","AKTİF");while(c.moveToNext()){A x=a(c);if(!isActuallyMissingPhoto(x.photo))continue;row(b,x,null,0);n++;}c.close();
        if(n==0)b.addView(tv("Gerçek fotoğrafı olmayan aktif sporcu bulunmuyor.",14,Color.DKGRAY,true));
    }

    private ScrollView firstScroll(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=firstScroll(g.getChildAt(i));if(s!=null)return s;}}return null;}

    private void addMaterialDebtCard(){
        ensureMaterialTables();ScrollView sv=firstScroll(root);if(sv==null)return;LinearLayout b=box(sv);
        Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(DISTINCT athleteId),COALESCE(SUM(total-paidAmount),0) FROM material_transactions WHERE paidAmount<total",null);c.moveToFirst();int people=c.getInt(0),due=c.getInt(1);c.close();
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(8),dp(8),dp(8),dp(8));card.setBackground(round(due>0?Color.rgb(255,232,232):Color.WHITE,12));
        TextView amount=tv(money(due),22,due>0?RED:GREEN,true);amount.setGravity(Gravity.CENTER);card.addView(amount);
        TextView label=tv("ÖDENMEMİŞ MALZEME • "+people+" SPORCU",12,Color.DKGRAY,true);label.setGravity(Gravity.CENTER);card.addView(label);
        card.setOnClickListener(v->showMaterialDebts());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(8),0,dp(8));b.addView(card,lp);
    }

    @Override void showProfile(long id){
        super.showProfile(id);ensureMaterialTables();ScrollView sv=firstScroll(root);if(sv==null)return;LinearLayout b=box(sv);addMaterialSection(b,id);
    }

    private void addMaterialSection(LinearLayout b,long athleteId){
        TextView h=tv("MALZEME TAKİBİ",16,BLACK,true);h.setPadding(dp(10),dp(20),dp(10),dp(6));b.addView(h);
        int due=materialDue(athleteId);if(due>0){TextView warn=tv("⚠ TAHSİL EDİLMEMİŞ MALZEME BORCU: "+money(due),13,Color.WHITE,true);warn.setBackground(round(RED,10));warn.setGravity(Gravity.CENTER);b.addView(warn,new LinearLayout.LayoutParams(-1,dp(48)));}
        LinearLayout actions=new LinearLayout(this);Button add=btn("+ MALZEME VER");Button prices=btn("GÜNCEL FİYATLAR");add.setOnClickListener(v->showAddMaterial(athleteId));prices.setOnClickListener(v->showMaterialPrices(athleteId));actions.addView(add,new LinearLayout.LayoutParams(0,dp(54),1));actions.addView(prices,new LinearLayout.LayoutParams(0,dp(54),1));b.addView(actions);
        loadMaterialRows(b,athleteId);
    }

    private int materialDue(long athleteId){Cursor c=db.getReadableDatabase().rawQuery("SELECT COALESCE(SUM(total-paidAmount),0) FROM material_transactions WHERE athleteId=?",new String[]{String.valueOf(athleteId)});c.moveToFirst();int x=c.getInt(0);c.close();return x;}

    private void loadMaterialRows(LinearLayout b,long athleteId){
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,product,qty,unitPrice,total,paidAmount,issuedDate,paymentDate,note FROM material_transactions WHERE athleteId=? ORDER BY issuedDate DESC,id DESC",new String[]{String.valueOf(athleteId)});
        if(c.getCount()==0){c.close();b.addView(tv("Henüz malzeme hareketi yok.",12,Color.DKGRAY,false));return;}
        while(c.moveToNext()){
            long id=c.getLong(0);String product=c.getString(1),date=c.getString(6);int qty=c.getInt(2),unit=c.getInt(3),total=c.getInt(4),paid=c.getInt(5),remain=total-paid;
            LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(dp(10),dp(8),dp(10),dp(8));r.setBackground(round(remain>0?Color.rgb(255,235,235):Color.rgb(232,248,236),10));
            r.addView(tv(product+" • "+qty+" ADET × "+money(unit)+" = "+money(total),13,BLACK,true));
            r.addView(tv(dateTr(date)+" • "+(remain>0?"KALAN: "+money(remain):"ÖDENDİ"),12,remain>0?RED:GREEN,true));
            r.setOnClickListener(v->showMaterialTransactionMenu(athleteId,id));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(5),0,0);b.addView(r,lp);
        }c.close();
    }

    private ArrayList<String> activeProducts(){ArrayList<String>x=new ArrayList<>();Cursor c=db.getReadableDatabase().rawQuery("SELECT name FROM material_products WHERE active=1 ORDER BY name COLLATE NOCASE",null);while(c.moveToNext())x.add(c.getString(0));c.close();return x;}
    private int productPrice(String name){Cursor c=db.getReadableDatabase().rawQuery("SELECT currentPrice FROM material_products WHERE name=?",new String[]{name});int p=0;if(c.moveToFirst())p=c.getInt(0);c.close();return p;}

    private void showAddMaterial(long athleteId){
        ArrayList<String> products=activeProducts();if(products.isEmpty()){toast("Önce güncel fiyatlardan bir malzeme ekleyin.");showMaterialPrices(athleteId);return;}
        LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(dp(20),dp(6),dp(20),0);
        Spinner prod=new Spinner(this);prod.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,products));
        EditText qty=new EditText(this);qty.setHint("ADET");qty.setInputType(2);qty.setText("1");
        EditText price=new EditText(this);price.setHint("BİRİM FİYAT ₺");price.setInputType(2);price.setText(String.valueOf(productPrice(products.get(0))));
        EditText date=new EditText(this);date.setHint("VERİLİŞ TARİHİ");date.setFocusable(false);date.setClickable(true);date.setText(isoDate.format(new Date()));date.setOnClickListener(v->pickIsoDate(date));
        CheckBox paid=new CheckBox(this);paid.setText("ÜCRETİ ŞİMDİ TAM OLARAK ALINDI");
        EditText note=new EditText(this);note.setHint("NOT (BEDEN, AÇIKLAMA VB.)");
        x.addView(prod);x.addView(qty);x.addView(price);x.addView(date);x.addView(paid);x.addView(note);
        prod.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?>a,View v,int p,long i){price.setText(String.valueOf(productPrice(String.valueOf(prod.getSelectedItem()))));}public void onNothingSelected(android.widget.AdapterView<?>a){}});
        new AlertDialog.Builder(this).setTitle("SPORCUYA MALZEME VER").setView(x).setPositiveButton("KAYDET",(d,w)->{
            String product=String.valueOf(prod.getSelectedItem());int q=Math.max(1,parseInt(qty.getText().toString())),u=parseInt(price.getText().toString());if(u<=0){toast("Geçerli bir birim fiyat girin.");return;}int total=q*u,pa=paid.isChecked()?total:0;String cloudId=UUID.randomUUID().toString();
            ContentValues cv=new ContentValues();cv.put("cloudId",cloudId);cv.put("athleteId",athleteId);cv.put("product",product);cv.put("qty",q);cv.put("unitPrice",u);cv.put("total",total);cv.put("paidAmount",pa);cv.put("issuedDate",date.getText().toString());cv.put("paymentDate",paid.isChecked()?date.getText().toString():"");cv.put("note",note.getText().toString().trim());db.getWritableDatabase().insert("material_transactions",null,cv);
            pushMaterialTransaction(cloudId,athleteId,product,q,u,total,pa,date.getText().toString(),paid.isChecked()?date.getText().toString():"",note.getText().toString().trim());showProfile(athleteId);
        }).setNegativeButton("VAZGEÇ",null).show();
    }

    private void pickIsoDate(EditText e){Calendar c=Calendar.getInstance();try{String[] p=e.getText().toString().split("-");if(p.length==3){c.set(Integer.parseInt(p[0]),Integer.parseInt(p[1])-1,Integer.parseInt(p[2]));}}catch(Exception ignored){}new DatePickerDialog(this,(v,y,m,d)->e.setText(String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d)),c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}

    private void showMaterialTransactionMenu(long athleteId,long transactionId){
        Cursor c=db.getReadableDatabase().rawQuery("SELECT product,total,paidAmount,cloudId FROM material_transactions WHERE id=?",new String[]{String.valueOf(transactionId)});if(!c.moveToFirst()){c.close();return;}String product=c.getString(0),cloudId=c.getString(3);int total=c.getInt(1),paid=c.getInt(2),remain=total-paid;c.close();
        ArrayList<String> opts=new ArrayList<>();if(remain>0){opts.add("KALANIN TAMAMINI TAHSİL ET");opts.add("KISMİ ÖDEME GİR");}opts.add("HAREKETİ SİL");
        new AlertDialog.Builder(this).setTitle(product+" • "+money(total)).setItems(opts.toArray(new String[0]),(d,w)->{
            String sel=opts.get(w);if(sel.startsWith("KALANIN"))applyMaterialPayment(athleteId,transactionId,cloudId,total);else if(sel.startsWith("KISMİ"))askPartialPayment(athleteId,transactionId,cloudId,total,paid);else confirmDeleteMaterial(athleteId,transactionId,cloudId);
        }).show();
    }

    private void askPartialPayment(long athleteId,long id,String cloudId,int total,int paid){EditText e=new EditText(this);e.setInputType(2);e.setHint("TAHSİL EDİLEN TUTAR");e.setText(String.valueOf(total-paid));new AlertDialog.Builder(this).setTitle("KISMİ ÖDEME").setView(e).setPositiveButton("KAYDET",(d,w)->{int add=parseInt(e.getText().toString());if(add<=0)return;applyMaterialPayment(athleteId,id,cloudId,Math.min(total,paid+add));}).setNegativeButton("VAZGEÇ",null).show();}
    private void applyMaterialPayment(long athleteId,long id,String cloudId,int newPaid){String day=isoDate.format(new Date());ContentValues cv=new ContentValues();cv.put("paidAmount",newPaid);cv.put("paymentDate",day);db.getWritableDatabase().update("material_transactions",cv,"id=?",new String[]{String.valueOf(id)});patchMaterialPayment(cloudId,newPaid,day);showProfile(athleteId);}
    private void confirmDeleteMaterial(long athleteId,long id,String cloudId){new AlertDialog.Builder(this).setTitle("MALZEME HAREKETİNİ SİL").setMessage("Bu malzeme hareketi silinsin mi?").setPositiveButton("SİL",(d,w)->{db.getWritableDatabase().delete("material_transactions","id=?",new String[]{String.valueOf(id)});deleteMaterialCloud(cloudId);showProfile(athleteId);}).setNegativeButton("VAZGEÇ",null).show();}

    private void showMaterialPrices(long returnAthlete){
        page="MATERIAL_PRICES";base("GÜNCEL MALZEME FİYATLARI",true);ScrollView sv=scroll();LinearLayout b=box(sv);Button add=btn("+ YENİ MALZEME / FİYAT");add.setOnClickListener(v->editProduct(null,0,returnAthlete));b.addView(add,new LinearLayout.LayoutParams(-1,dp(54)));
        Cursor c=db.getReadableDatabase().rawQuery("SELECT name,currentPrice FROM material_products WHERE active=1 ORDER BY name",null);while(c.moveToNext()){String name=c.getString(0);int p=c.getInt(1);Button r=btn(name+" • "+money(p));r.setOnClickListener(v->editProduct(name,p,returnAthlete));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(54));lp.setMargins(0,dp(6),0,0);b.addView(r,lp);}c.close();
    }
    private void editProduct(String oldName,int oldPrice,long returnAthlete){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);EditText n=new EditText(this);n.setHint("MALZEME ADI");n.setText(oldName==null?"":oldName);EditText p=new EditText(this);p.setHint("GÜNCEL FİYAT ₺");p.setInputType(2);if(oldPrice>0)p.setText(String.valueOf(oldPrice));x.addView(n);x.addView(p);new AlertDialog.Builder(this).setTitle(oldName==null?"YENİ MALZEME":"FİYATI GÜNCELLE").setView(x).setPositiveButton("KAYDET",(d,w)->{String name=n.getText().toString().trim().toUpperCase(new Locale("tr","TR"));int price=parseInt(p.getText().toString());if(name.isEmpty()||price<0)return;ContentValues cv=new ContentValues();cv.put("name",name);cv.put("currentPrice",price);cv.put("active",1);db.getWritableDatabase().insertWithOnConflict("material_products",null,cv,SQLiteDatabase.CONFLICT_REPLACE);upsertProductCloud(name,price);showMaterialPrices(returnAthlete);}).setNegativeButton("VAZGEÇ",null).show();}

    private void showMaterialDebts(){
        page="MATERIAL_DEBTS";base("ÖDENMEMİŞ MALZEMELER",true);ScrollView sv=scroll();LinearLayout b=box(sv);
        Cursor c=db.getReadableDatabase().rawQuery("SELECT athleteId,SUM(total-paidAmount) due FROM material_transactions WHERE paidAmount<total GROUP BY athleteId ORDER BY due DESC",null);int n=0;while(c.moveToNext()){long aid=c.getLong(0);int due=c.getInt(1);Cursor a=db.athlete(aid);if(a.moveToFirst()){row(b,a(a),"MALZEME BORCU",due);n++;}a.close();}c.close();if(n==0)b.addView(tv("Ödenmemiş malzeme borcu bulunmuyor.",14,GREEN,true));
    }

    // --- ONLINE MATERIAL SYNC ---
    private void syncMaterialsFromCloud(boolean announce){
        if(cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty())return;
        new Thread(()->{try{
            HttpResult pr=getAuthed("/rest/v1/material_products?select=*");HttpResult tr=getAuthed("/rest/v1/material_transactions?select=*");if(pr.code<300&&tr.code<300){JSONArray ps=new JSONArray(pr.body),ts=new JSONArray(tr.body);SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();try{for(int i=0;i<ps.length();i++){JSONObject o=ps.getJSONObject(i);ContentValues v=new ContentValues();v.put("name",o.optString("name"));v.put("currentPrice",o.optInt("current_price"));v.put("active",o.optBoolean("active",true)?1:0);v.put("cloudId",o.optString("id"));d.insertWithOnConflict("material_products",null,v,SQLiteDatabase.CONFLICT_REPLACE);}for(int i=0;i<ts.length();i++){JSONObject o=ts.getJSONObject(i);ContentValues v=new ContentValues();v.put("cloudId",o.optString("id"));v.put("athleteId",o.optLong("athlete_legacy_id"));v.put("product",o.optString("product_name"));v.put("qty",o.optInt("quantity"));v.put("unitPrice",o.optInt("unit_price"));v.put("total",o.optInt("total_amount"));v.put("paidAmount",o.optInt("paid_amount"));v.put("issuedDate",o.optString("issued_at"));v.put("paymentDate",o.optString("payment_date"));v.put("note",o.optString("note"));d.insertWithOnConflict("material_transactions",null,v,SQLiteDatabase.CONFLICT_REPLACE);}d.setTransactionSuccessful();}finally{d.endTransaction();}if(announce)runOnUiThread(()->toast("Malzeme kayıtları eşitlendi."));}}
            catch(Exception e){if(announce)runOnUiThread(()->toast("Malzeme eşitleme hatası: "+shortMsg(e)));}}).start();
    }

    private void pushMaterialTransaction(String id,long aid,String product,int qty,int unit,int total,int paid,String issued,String payment,String note){new Thread(()->{try{JSONObject o=new JSONObject();o.put("id",id);o.put("athlete_legacy_id",aid);o.put("product_name",product);o.put("quantity",qty);o.put("unit_price",unit);o.put("total_amount",total);o.put("paid_amount",paid);o.put("issued_at",issued);if(payment!=null&&!payment.isEmpty())o.put("payment_date",payment);o.put("note",note);request("POST",SUPABASE_URL+"/rest/v1/material_transactions",o.toString(),cloudPrefs.getString("access_token",""));}catch(Exception ignored){}}).start();}
    private void patchMaterialPayment(String cloudId,int paid,String day){if(cloudId==null||cloudId.isEmpty())return;new Thread(()->{try{JSONObject o=new JSONObject().put("paid_amount",paid).put("payment_date",day);request("PATCH",SUPABASE_URL+"/rest/v1/material_transactions?id=eq."+URLEncoder.encode(cloudId,"UTF-8"),o.toString(),cloudPrefs.getString("access_token",""));}catch(Exception ignored){}}).start();}
    private void deleteMaterialCloud(String cloudId){if(cloudId==null||cloudId.isEmpty())return;new Thread(()->{try{request("DELETE",SUPABASE_URL+"/rest/v1/material_transactions?id=eq."+URLEncoder.encode(cloudId,"UTF-8"),null,cloudPrefs.getString("access_token",""));}catch(Exception ignored){}}).start();}
    private void upsertProductCloud(String name,int price){new Thread(()->{try{JSONObject o=new JSONObject().put("name",name).put("current_price",price).put("active",true);requestPrefer("POST",SUPABASE_URL+"/rest/v1/material_products?on_conflict=name",o.toString(),cloudPrefs.getString("access_token",""),"resolution=merge-duplicates");syncMaterialsFromCloud(false);}catch(Exception ignored){}}).start();}
    private HttpResult requestPrefer(String method,String url,String body,String bearer,String prefer)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setRequestMethod(method);c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setRequestProperty("apikey",SUPABASE_KEY);c.setRequestProperty("Accept","application/json");if(bearer!=null&&!bearer.isEmpty())c.setRequestProperty("Authorization","Bearer "+bearer);if(prefer!=null)c.setRequestProperty("Prefer",prefer);if(body!=null){c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");try(OutputStream o=c.getOutputStream()){o.write(body.getBytes(StandardCharsets.UTF_8));}}int code=c.getResponseCode();InputStream in=code>=400?c.getErrorStream():c.getInputStream();String text=readAll(in);c.disconnect();return new HttpResult(code,text);}
}

package com.parion.aidat;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.1.27 - recent-payments back navigation fix + monthly collection dashboard card. */
public class MainActivityV705 extends MainActivityV704 {

    @Override void showHome(){
        super.showHome();
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ensureCollectionDateTable705();
        LinearLayout fresh=findFresh705(root);
        if(fresh==null)return;
        removeOldCollection705(fresh);
        View target=directChildContaining705(fresh,"AYLIK HEDEF");
        int index=target==null?Math.min(4,fresh.getChildCount()):fresh.indexOfChild(target)+1;
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(126));
        lp.setMargins(dp(4),0,dp(4),dp(9));
        fresh.addView(collectionCard705(),Math.min(index,fresh.getChildCount()),lp);
    }

    @Override void goBack(){
        if("RECENT_PAYMENTS".equals(page)||"COLLECTION_DETAIL".equals(page)){showHome();return;}
        super.goBack();
    }

    @Override void editPayment(long id,int month,int fee,String marker,int amount){
        final String[] opts={"ÖDEME GİR","ARA VERDİ (X)","FARKLI TUTAR (!)","KAYDI TEMİZLE"};
        new AlertDialog.Builder(this).setTitle(monthName(month)+" 2026").setItems(opts,(d,w)->{
            if(w==1){db.updatePayment(id,month,"X",0);deleteCollectionDate705(id,month);showProfile(id);}
            else if(w==3){db.updatePayment(id,month,"",0);deleteCollectionDate705(id,month);showProfile(id);}
            else{
                LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(dp(20),0,dp(20),0);
                EditText date=new EditText(this);date.setHint("Ödeme tarihi (gg.aa.yyyy)");date.setText(isDate(marker)?dateTr(marker):new SimpleDateFormat("dd.MM.yyyy",TR).format(new Date()));
                EditText am=new EditText(this);am.setHint("Tutar");am.setInputType(2);am.setText(String.valueOf(amount>0?amount:fee));x.addView(date);x.addView(am);
                new AlertDialog.Builder(this).setTitle(w==2?"Farklı tutar":"Aidat ödemesi").setView(x)
                    .setPositiveButton("KAYDET",(a,z)->{
                        String iso=toIso(date.getText().toString());int val=parseInt(am.getText().toString());
                        db.updatePayment(id,month,w==2?"!":iso,val);
                        if(val>0&&isDate(iso))saveCollectionDate705(id,month,iso);else deleteCollectionDate705(id,month);
                        showProfile(id);
                    }).setNegativeButton("İPTAL",null).show();
            }
        }).show();
    }

    private void ensureCollectionDateTable705(){
        try{db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS payment_collection_dates(athleteId INTEGER NOT NULL,year INTEGER NOT NULL,month INTEGER NOT NULL,paidDate TEXT NOT NULL,PRIMARY KEY(athleteId,year,month))");}catch(Exception ignored){}
    }
    private void saveCollectionDate705(long athleteId,int month,String paidDate){
        try{ensureCollectionDateTable705();ContentValues v=new ContentValues();v.put("athleteId",athleteId);v.put("year",2026);v.put("month",month);v.put("paidDate",paidDate);db.getWritableDatabase().insertWithOnConflict("payment_collection_dates",null,v,SQLiteDatabase.CONFLICT_REPLACE);}catch(Exception ignored){}
    }
    private void deleteCollectionDate705(long athleteId,int month){
        try{ensureCollectionDateTable705();db.getWritableDatabase().delete("payment_collection_dates","athleteId=? AND year=2026 AND month=?",new String[]{String.valueOf(athleteId),String.valueOf(month)});}catch(Exception ignored){}
    }

    private LinearLayout findFresh705(View v){
        if(v instanceof LinearLayout&&"v657-fresh".equals(v.getTag()))return(LinearLayout)v;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){LinearLayout r=findFresh705(g.getChildAt(i));if(r!=null)return r;}}
        return null;
    }
    private void removeOldCollection705(LinearLayout fresh){for(int i=fresh.getChildCount()-1;i>=0;i--){View v=fresh.getChildAt(i);if("v705-collection".equals(v.getTag()))fresh.removeViewAt(i);}}
    private View directChildContaining705(LinearLayout parent,String needle){
        for(int i=0;i<parent.getChildCount();i++)if(viewContains705(parent.getChildAt(i),needle))return parent.getChildAt(i);return null;
    }
    private boolean viewContains705(View v,String needle){
        if(v instanceof TextView&&String.valueOf(((TextView)v).getText()).toUpperCase(new Locale("tr","TR")).contains(needle.toUpperCase(new Locale("tr","TR"))))return true;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(viewContains705(g.getChildAt(i),needle))return true;}
        return false;
    }

    private View collectionCard705(){
        Calendar now=Calendar.getInstance();int y=now.get(Calendar.YEAR),m=now.get(Calendar.MONTH)+1;long total=collectionTotal705(y,m);
        LinearLayout c=new LinearLayout(this);c.setTag("v705-collection");c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER);c.setPadding(dp(10),dp(10),dp(10),dp(10));
        GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));c.setBackground(bg);c.setElevation(dp(2));
        ImageView icon=new ImageView(this);icon.setImageResource(android.R.drawable.ic_menu_save);icon.setColorFilter(Color.rgb(39,134,82));icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);c.addView(icon,new LinearLayout.LayoutParams(dp(24),dp(24)));
        TextView ttl=new TextView(this);ttl.setText("BU AYKİ TAHSİLAT");ttl.setTextSize(11.2f);ttl.setTextColor(Color.rgb(45,45,45));ttl.setTypeface(Typeface.DEFAULT,Typeface.BOLD);ttl.setGravity(Gravity.CENTER);c.addView(ttl,new LinearLayout.LayoutParams(-1,-2));
        TextView val=new TextView(this);val.setText(money705(total));val.setTextSize(27f);val.setTextColor(Color.rgb(39,134,82));val.setTypeface(Typeface.DEFAULT,Typeface.BOLD);val.setGravity(Gravity.CENTER);val.setSingleLine(true);c.addView(val,new LinearLayout.LayoutParams(-1,-2));
        TextView sub=new TextView(this);sub.setText(monthLabel705(y,m)+" ödeme tarihine göre");sub.setTextSize(9.3f);sub.setTextColor(Color.rgb(105,105,105));sub.setGravity(Gravity.CENTER);c.addView(sub,new LinearLayout.LayoutParams(-1,-2));
        c.setClickable(true);c.setOnClickListener(v->showCollections705(0));return c;
    }

    private void showCollections705(int selectedOffset){
        page="COLLECTION_DETAIL";base("TAHSİLATLAR",true);ensureCollectionDateTable705();
        LinearLayout filter=new LinearLayout(this);filter.setPadding(dp(12),dp(8),dp(12),dp(2));filter.setGravity(Gravity.CENTER_VERTICAL);
        TextView lab=new TextView(this);lab.setText("AY: ");lab.setTextSize(12f);lab.setTypeface(Typeface.DEFAULT,Typeface.BOLD);lab.setTextColor(Color.DKGRAY);filter.addView(lab);
        Spinner spinner=new Spinner(this);ArrayList<String> labels=new ArrayList<>();ArrayList<int[]> months=new ArrayList<>();
        Calendar c=Calendar.getInstance();for(int i=0;i<6;i++){int y=c.get(Calendar.YEAR),m=c.get(Calendar.MONTH)+1;labels.add(monthLabel705(y,m));months.add(new int[]{y,m});c.add(Calendar.MONTH,-1);}
        spinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,labels));spinner.setSelection(Math.max(0,Math.min(selectedOffset,5)));filter.addView(spinner,new LinearLayout.LayoutParams(0,dp(52),1));root.addView(filter);
        TextView totalView=new TextView(this);totalView.setGravity(Gravity.CENTER);totalView.setTextSize(24f);totalView.setTypeface(Typeface.DEFAULT,Typeface.BOLD);totalView.setTextColor(Color.rgb(39,134,82));totalView.setPadding(dp(10),dp(8),dp(10),dp(4));root.addView(totalView,new LinearLayout.LayoutParams(-1,-2));
        TextView totalSub=new TextView(this);totalSub.setGravity(Gravity.CENTER);totalSub.setTextSize(11f);totalSub.setTextColor(Color.DKGRAY);totalSub.setPadding(dp(10),0,dp(10),dp(8));root.addView(totalSub,new LinearLayout.LayoutParams(-1,-2));
        ScrollView sv=scroll();LinearLayout list=box(sv);
        Runnable load=()->{int pos=spinner.getSelectedItemPosition();if(pos<0)pos=0;int[] ym=months.get(pos);loadCollectionMonth705(list,totalView,totalSub,ym[0],ym[1]);};
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){load.run();}public void onNothingSelected(android.widget.AdapterView<?> p){}});load.run();
    }

    private void loadCollectionMonth705(LinearLayout list,TextView totalView,TextView totalSub,int year,int month){
        list.removeAllViews();long total=collectionTotal705(year,month);totalView.setText(money705(total));totalSub.setText(monthLabel705(year,month)+" TOPLAM TAHSİLAT");
        Cursor c=null;int n=0;try{c=collectionRows705(year,month);while(c.moveToNext()){
            long athleteId=c.getLong(c.getColumnIndexOrThrow("athleteId"));String name=c.getString(c.getColumnIndexOrThrow("name"));int amount=c.getInt(c.getColumnIndexOrThrow("amount"));String paidDate=c.getString(c.getColumnIndexOrThrow("paidDate"));
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(12),dp(10),dp(12),dp(10));GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(12));row.setBackground(bg);row.setElevation(dp(1));
            LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);TextView nm=new TextView(this);nm.setText(name);nm.setTextSize(14f);nm.setTextColor(Color.rgb(30,30,30));nm.setTypeface(Typeface.DEFAULT,Typeface.BOLD);top.addView(nm,new LinearLayout.LayoutParams(0,-2,1));TextView am=new TextView(this);am.setText(money705(amount));am.setTextSize(14f);am.setTextColor(Color.rgb(39,134,82));am.setTypeface(Typeface.DEFAULT,Typeface.BOLD);top.addView(am);row.addView(top);
            TextView d=new TextView(this);d.setText(dateTr(paidDate));d.setTextSize(10.5f);d.setTextColor(Color.GRAY);row.addView(d);row.setOnClickListener(v->showProfile(athleteId));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));list.addView(row,lp);n++;
        }}catch(Exception ignored){}finally{if(c!=null)c.close();}
        if(n==0){TextView e=new TextView(this);e.setText("Bu ay için ödeme tarihine bağlı tahsilat kaydı yok.");e.setTextSize(13f);e.setTextColor(Color.DKGRAY);e.setGravity(Gravity.CENTER);e.setPadding(dp(10),dp(28),dp(10),dp(28));list.addView(e);}
    }

    private Cursor collectionRows705(int year,int month){
        ensureCollectionDateTable705();String prefix=String.format(Locale.US,"%04d-%02d",year,month);
        String paidExpr="CASE WHEN p.marker GLOB '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]' THEN p.marker ELSE cd.paidDate END";
        String sql="SELECT p.athleteId,p.amount,a.name,"+paidExpr+" AS paidDate FROM payments p JOIN athletes a ON a.id=p.athleteId LEFT JOIN payment_collection_dates cd ON cd.athleteId=p.athleteId AND cd.year=p.year AND cd.month=p.month WHERE p.amount>0 AND "+paidExpr+" LIKE ? ORDER BY paidDate DESC,a.name COLLATE NOCASE";
        return db.getReadableDatabase().rawQuery(sql,new String[]{prefix+"%"});
    }
    private long collectionTotal705(int year,int month){Cursor c=null;long sum=0;try{c=collectionRows705(year,month);while(c.moveToNext())sum+=c.getLong(c.getColumnIndexOrThrow("amount"));}catch(Exception ignored){}finally{if(c!=null)c.close();}return sum;}
    private String monthLabel705(int year,int month){String[] names={"OCAK","ŞUBAT","MART","NİSAN","MAYIS","HAZİRAN","TEMMUZ","AĞUSTOS","EYLÜL","EKİM","KASIM","ARALIK"};return names[Math.max(1,Math.min(12,month))-1]+" "+year;}
    private String money705(long n){return String.format(new Locale("tr","TR"),"₺%,d",n).replace(',','.');}
}

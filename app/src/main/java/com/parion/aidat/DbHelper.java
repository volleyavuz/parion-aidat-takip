package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import android.util.Base64;
import java.util.*;

public class DbHelper extends SQLiteOpenHelper {
    static final String DB="parion_spor_okulu.db"; static final int VER=1;
    public DbHelper(Context c){super(c,DB,null,VER);ctx=c.getApplicationContext();}
    public void onCreate(SQLiteDatabase d){
        d.execSQL("CREATE TABLE athletes(id INTEGER PRIMARY KEY AUTOINCREMENT,seq INTEGER,birthYear INTEGER,name TEXT,category TEXT,status TEXT,monthlyFee INTEGER,sibling TEXT,tshirtQty INTEGER,tshirtPaid INTEGER,tracksuitQty INTEGER,tracksuitPaid INTEGER,notes TEXT,phone TEXT,motherName TEXT,motherPhone TEXT,fatherName TEXT,fatherPhone TEXT,startDate TEXT,endDate TEXT,restartDate TEXT,photo TEXT)");
        d.execSQL("CREATE TABLE payments(id INTEGER PRIMARY KEY AUTOINCREMENT,athleteId INTEGER,year INTEGER,month INTEGER,marker TEXT,amount INTEGER,UNIQUE(athleteId,year,month))");
        seed(d);
    }
    public void onUpgrade(SQLiteDatabase d,int o,int n){}
    private void seed(SQLiteDatabase d){
        try{
            byte[] packed=Base64.decode(SeedData1.S+SeedData2.S+SeedData3.S+SeedData4.S,Base64.DEFAULT); InputStream in=new GZIPInputStream(new ByteArrayInputStream(packed)); ByteArrayOutputStream b=new ByteArrayOutputStream(); byte[] buf=new byte[8192]; int n; while((n=in.read(buf))>0)b.write(buf,0,n); in.close();
            JSONObject root=new JSONObject(b.toString(StandardCharsets.UTF_8.name())); int year=root.optInt("year",2026); JSONArray arr=root.getJSONArray("athletes");
            d.beginTransaction();
            for(int i=0;i<arr.length();i++){
                JSONObject a=arr.getJSONObject(i); ContentValues v=new ContentValues();
                put(v,"seq",a,"seq");put(v,"birthYear",a,"birthYear");put(v,"name",a,"name");put(v,"category",a,"category");put(v,"status",a,"status");put(v,"monthlyFee",a,"monthlyFee");put(v,"sibling",a,"sibling");put(v,"tshirtQty",a,"tshirtQty");put(v,"tshirtPaid",a,"tshirtPaid");put(v,"tracksuitQty",a,"tracksuitQty");put(v,"tracksuitPaid",a,"tracksuitPaid");put(v,"notes",a,"notes");put(v,"phone",a,"phone");put(v,"motherName",a,"motherName");put(v,"motherPhone",a,"motherPhone");put(v,"fatherName",a,"fatherName");put(v,"fatherPhone",a,"fatherPhone");put(v,"startDate",a,"startDate");put(v,"endDate",a,"endDate");put(v,"restartDate",a,"restartDate");put(v,"photo",a,"photo");
                long id=d.insert("athletes",null,v); JSONArray ps=a.optJSONArray("payments"); if(ps!=null) for(int j=0;j<ps.length();j++){JSONObject p=ps.getJSONObject(j); ContentValues pv=new ContentValues();pv.put("athleteId",id);pv.put("year",year);pv.put("month",p.optInt("month"));pv.put("marker",p.optString("marker"));pv.put("amount",p.optInt("amount"));d.insert("payments",null,pv);}
            }
            d.setTransactionSuccessful(); d.endTransaction();
        }catch(Exception e){throw new RuntimeException(e);}
    }
    private Context getContext(){return ctx;} private Context ctx;
    @Override public SQLiteDatabase getWritableDatabase(){return super.getWritableDatabase();}
    private void put(ContentValues v,String col,JSONObject o,String key){Object x=o.opt(key);if(x instanceof Number)v.put(col,((Number)x).intValue());else v.put(col,o.optString(key,""));}
    public Cursor athletes(String q,String status){String sql="SELECT * FROM athletes WHERE 1=1";ArrayList<String> args=new ArrayList<>();if(q!=null&&!q.trim().isEmpty()){sql+=" AND name LIKE ?";args.add("%"+q.trim().toUpperCase(Locale.forLanguageTag("tr-TR"))+"%");}if(status!=null&&!status.equals("TÜMÜ")){sql+=" AND status=?";args.add(status);}sql+=" ORDER BY CASE status WHEN 'AKTİF' THEN 0 WHEN 'ARA VERDİ' THEN 1 ELSE 2 END,name COLLATE NOCASE";return getReadableDatabase().rawQuery(sql,args.toArray(new String[0]));}
    public Cursor athlete(long id){return getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE id=?",new String[]{String.valueOf(id)});}
    public Cursor payments(long id){return getReadableDatabase().rawQuery("SELECT * FROM payments WHERE athleteId=? ORDER BY month",new String[]{String.valueOf(id)});}
    public int count(String status){Cursor c=getReadableDatabase().rawQuery(status==null?"SELECT COUNT(*) FROM athletes":"SELECT COUNT(*) FROM athletes WHERE status=?",status==null?null:new String[]{status});c.moveToFirst();int x=c.getInt(0);c.close();return x;}
    public int paidThisMonth(int year,int month){Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(amount),0) FROM payments WHERE year=? AND month=?",new String[]{String.valueOf(year),String.valueOf(month)});c.moveToFirst();int x=c.getInt(0);c.close();return x;}
    public void updatePayment(long athleteId,int month,String marker,int amount){ContentValues v=new ContentValues();v.put("athleteId",athleteId);v.put("year",2026);v.put("month",month);v.put("marker",marker);v.put("amount",amount);getWritableDatabase().insertWithOnConflict("payments",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
}

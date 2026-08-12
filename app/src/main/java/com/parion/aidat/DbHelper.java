package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import java.util.*;

public class DbHelper extends SQLiteOpenHelper {
    public DbHelper(Context c) { super(c, "parion_aidat.db", null, 1); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE athletes(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,birthYear INTEGER,parent TEXT,phone TEXT,groupName TEXT,monthlyFee REAL NOT NULL DEFAULT 0,active INTEGER NOT NULL DEFAULT 1)");
        db.execSQL("CREATE TABLE payments(id INTEGER PRIMARY KEY AUTOINCREMENT,athleteId INTEGER NOT NULL,year INTEGER NOT NULL,month INTEGER NOT NULL,amount REAL NOT NULL,paymentDate TEXT,method TEXT,note TEXT,UNIQUE(athleteId,year,month),FOREIGN KEY(athleteId) REFERENCES athletes(id) ON DELETE CASCADE)");
    }
    @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) {}

    public long addAthlete(String name,int birthYear,String parent,String phone,String group,double fee) {
        ContentValues v=new ContentValues(); v.put("name",name); v.put("birthYear",birthYear); v.put("parent",parent); v.put("phone",phone); v.put("groupName",group); v.put("monthlyFee",fee); return getWritableDatabase().insert("athletes",null,v);
    }
    public Cursor athletes() { return getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE active=1 ORDER BY name COLLATE NOCASE",null); }
    public Cursor debtors(int year,int month) { return getReadableDatabase().rawQuery("SELECT a.* FROM athletes a LEFT JOIN payments p ON p.athleteId=a.id AND p.year=? AND p.month=? WHERE a.active=1 AND p.id IS NULL ORDER BY a.name COLLATE NOCASE",new String[]{String.valueOf(year),String.valueOf(month)}); }
    public boolean isPaid(long athleteId,int year,int month) { Cursor c=getReadableDatabase().rawQuery("SELECT id FROM payments WHERE athleteId=? AND year=? AND month=?",new String[]{String.valueOf(athleteId),String.valueOf(year),String.valueOf(month)}); boolean ok=c.moveToFirst(); c.close(); return ok; }
    public void markPaid(long athleteId,int year,int month,double amount,String method) {
        ContentValues v=new ContentValues();v.put("athleteId",athleteId);v.put("year",year);v.put("month",month);v.put("amount",amount);v.put("paymentDate",new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date()));v.put("method",method);getWritableDatabase().insertWithOnConflict("payments",null,v,SQLiteDatabase.CONFLICT_REPLACE);
    }
    public void removePayment(long athleteId,int year,int month) { getWritableDatabase().delete("payments","athleteId=? AND year=? AND month=?",new String[]{String.valueOf(athleteId),String.valueOf(year),String.valueOf(month)}); }
    public double expected() { Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(monthlyFee),0) FROM athletes WHERE active=1",null); c.moveToFirst(); double x=c.getDouble(0);c.close();return x; }
    public double collected(int year,int month) { Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(amount),0) FROM payments WHERE year=? AND month=?",new String[]{String.valueOf(year),String.valueOf(month)}); c.moveToFirst();double x=c.getDouble(0);c.close();return x; }
    public int activeCount() { Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE active=1",null);c.moveToFirst();int x=c.getInt(0);c.close();return x; }
    public int paidCount(int year,int month) { Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM payments p JOIN athletes a ON a.id=p.athleteId WHERE a.active=1 AND p.year=? AND p.month=?",new String[]{String.valueOf(year),String.valueOf(month)});c.moveToFirst();int x=c.getInt(0);c.close();return x; }
}

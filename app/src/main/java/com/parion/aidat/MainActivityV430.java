package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivityV430 extends MainActivityV429 {
    private boolean pendingNew430=false,pendingSummer430=false,pendingWinter430=false;

    @Override void form(long id){
        super.form(id);
        CheckBox summer=findCheck430(root,"YAZIN ARANACAK"),winter=findCheck430(root,"KIŞIN ARANACAK");Button save=findSave430(root);if(summer==null||winter==null||save==null)return;
        ArrayList<EditText> phones=new ArrayList<>();collectPhones430(root,phones);
        save.setOnTouchListener((v,e)->{
            if(e.getAction()==MotionEvent.ACTION_DOWN)for(EditText p:phones)p.setText(phoneDigits430(p.getText().toString()));
            if(e.getAction()==MotionEvent.ACTION_UP){if(id>0)saveFlags430(id,summer.isChecked(),winter.isChecked());else{pendingNew430=true;pendingSummer430=summer.isChecked();pendingWinter430=winter.isChecked();}}
            return false;
        });
    }

    @Override void showProfile(long id){if(pendingNew430&&id>0){boolean s=pendingSummer430,w=pendingWinter430;pendingNew430=false;saveFlags430(id,s,w);}super.showProfile(id);}

    private void saveFlags430(long id,boolean summer,boolean winter){
        SQLiteDatabase d=db.getWritableDatabase();ContentValues v=new ContentValues();v.put("summerCall",summer?1:0);v.put("winterCall",winter?1:0);d.update("athletes",v,"id=?",new String[]{String.valueOf(id)});ContentValues q=new ContentValues();q.put("athleteId",id);d.insertWithOnConflict("season_sync_queue",null,q,SQLiteDatabase.CONFLICT_REPLACE);
        Cursor c=db.athlete(id);String name="SPORCU";if(c.moveToFirst()){int i=c.getColumnIndex("name");if(i>=0&&!c.isNull(i))name=c.getString(i);}c.close();ContentValues a=new ContentValues();a.put("action","ARAMA DURUMU GÜNCELLENDİ");a.put("entityType","SPORCU");a.put("entityId",String.valueOf(id));a.put("detail",name+" • YAZ: "+(summer?"EVET":"HAYIR")+" • KIŞ: "+(winter?"EVET":"HAYIR"));d.insert("activity_log_local",null,a);
    }
    private CheckBox findCheck430(View v,String term){if(v instanceof CheckBox&&String.valueOf(((CheckBox)v).getText()).toUpperCase(TR).contains(term))return (CheckBox)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){CheckBox c=findCheck430(g.getChildAt(i),term);if(c!=null)return c;}}return null;}
    private Button findSave430(View v){if(v instanceof Button){String s=String.valueOf(((Button)v).getText()).toUpperCase(TR);if(s.contains("KAYDI OLUŞTUR")||s.contains("DEĞİŞİKLİKLERİ KAYDET"))return (Button)v;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){Button b=findSave430(g.getChildAt(i));if(b!=null)return b;}}return null;}
    private void collectPhones430(View v,ArrayList<EditText> out){if(v instanceof EditText){EditText e=(EditText)v;String h=e.getHint()==null?"":e.getHint().toString().toUpperCase(TR);if(h.contains("TELEFON"))out.add(e);return;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectPhones430(g.getChildAt(i),out);}}
    private String phoneDigits430(String raw){String x=raw==null?"":raw.replaceAll("[^0-9]","");if(x.startsWith("90")&&x.length()==12)x="0"+x.substring(2);if(x.length()==10&&x.startsWith("5"))x="0"+x;if(x.length()>11)x=x.substring(0,11);return x;}
}

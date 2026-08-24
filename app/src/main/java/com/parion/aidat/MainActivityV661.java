package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.*;
import java.lang.reflect.Method;

/** v4.0.61 - restore Deleted Athletes entry in Settings without touching dashboard/ANR-safe startup. */
public class MainActivityV661 extends MainActivityV660 {
    private PopupWindow popup661;

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(()->patchSettings661(root));
    }

    @Override void showHome(){
        super.showHome();
        if(root!=null)root.post(()->patchSettings661(root));
    }

    private void patchSettings661(View v){
        if(v==null)return;
        CharSequence d=v.getContentDescription();
        if(d!=null&&"Ayarlar".equalsIgnoreCase(d.toString())){v.setOnClickListener(this::showSettings661);return;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchSettings661(g.getChildAt(i));}
    }

    private void showSettings661(View anchor){
        dismiss661();
        LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(dp(8),dp(8),dp(8),dp(8));
        android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(14));bg.setStroke(dp(1),Color.rgb(215,205,175));p.setBackground(bg);
        addItem661(p,"GRUPLARI DÜZENLE",()->invoke661(MainActivityV625.class,"showGroups625"));
        addItem661(p,"YOKLAMA AYARLARI",()->invoke661(MainActivityV630.class,"showAttendanceSettings630"));
        addItem661(p,"SİLİNEN SPORCULAR",this::showDeleted661);
        addItem661(p,"GÜNCELLEMELER",()->invoke661(MainActivityV639.class,"showUpdates639"));
        addDivider661(p);
        addItem661(p,"OTURUMU KAPAT",()->invoke661(MainActivityV639.class,"confirmLogout639"));
        addItem661(p,"ÇIKIŞ",this::finishAffinity);
        popup661=new PopupWindow(p,dp(250),WindowManager.LayoutParams.WRAP_CONTENT,true);popup661.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));popup661.setOutsideTouchable(true);popup661.setElevation(dp(8));
        int[] loc=new int[2];anchor.getLocationOnScreen(loc);p.measure(View.MeasureSpec.makeMeasureSpec(dp(250),View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
        int x=Math.max(dp(8),getResources().getDisplayMetrics().widthPixels-dp(262));int y=Math.max(dp(8),loc[1]-p.getMeasuredHeight()-dp(8));popup661.showAtLocation(root,Gravity.TOP|Gravity.START,x,y);
    }

    private void showDeleted661(){
        page="DELETED_661";base("SİLİNEN SPORCULAR",true);
        ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name,birthYear,deletedAt,deletedPrevStatus,status FROM athletes WHERE TRIM(COALESCE(deletedAt,''))<>'' ORDER BY deletedAt DESC,name COLLATE NOCASE",null);
        while(c.moveToNext()){
            final long id=c.getLong(0);String name=c.getString(1)==null?"":c.getString(1);int year=c.getInt(2);String deleted=c.getString(3)==null?"":c.getString(3);String prev=c.getString(4)==null?"":c.getString(4);if(prev.trim().isEmpty())prev=c.getString(5)==null?"AKTİF":c.getString(5);
            LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(14),dp(12),dp(14),dp(12));android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(14));bg.setStroke(dp(1),Color.rgb(220,210,180));card.setBackground(bg);card.setElevation(dp(1));
            TextView title=new TextView(this);title.setText((year>0?year+" • ":"")+name);title.setTextSize(14);title.setTextColor(Color.rgb(28,28,28));title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);card.addView(title);
            TextView info=new TextView(this);info.setText("Önceki durum: "+prev+(deleted.trim().isEmpty()?"":"\nSilinme: "+deleted));info.setTextSize(11);info.setTextColor(Color.DKGRAY);info.setPadding(0,dp(4),0,dp(8));card.addView(info);
            final String restoreStatus=prev;
            Button restore=new Button(this);restore.setText("GERİ YÜKLE");restore.setAllCaps(false);restore.setOnClickListener(v->confirmRestore661(id,name,restoreStatus));card.addView(restore,new LinearLayout.LayoutParams(-1,dp(44)));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(dp(8),dp(5),dp(8),dp(5));b.addView(card,lp);n++;
        }
        c.close();
        if(n==0)b.addView(tv("SİLİNMİŞ SPORCU BULUNMUYOR.",14,Color.DKGRAY,true));
    }

    private void confirmRestore661(long id,String name,String status){
        new AlertDialog.Builder(this).setTitle("SPORCUYU GERİ YÜKLE").setMessage(name+" yeniden aktif kayıtlara alınsın mı?").setPositiveButton("GERİ YÜKLE",(d,w)->{
            String st=(status==null||status.trim().isEmpty())?"AKTİF":status;
            db.getWritableDatabase().execSQL("UPDATE athletes SET deletedAt='', status=?, deletedPrevStatus=NULL WHERE id=?",new Object[]{st,id});
            try{invoke661(MainActivityV639.class,"enqueueBackground639");}catch(Exception ignored){}
            toast("Sporcu geri yüklendi.");showDeleted661();
        }).setNegativeButton("VAZGEÇ",null).show();
    }

    @Override void goBack(){if("DELETED_661".equals(page)){showHome();return;}super.goBack();}

    private void addItem661(LinearLayout p,String text,Runnable r){TextView t=new TextView(this);t.setText(text);t.setTextSize(13);t.setTextColor(Color.rgb(28,28,28));t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(dp(14),0,dp(14),0);t.setOnClickListener(v->{dismiss661();r.run();});p.addView(t,new LinearLayout.LayoutParams(dp(234),dp(48)));}
    private void addDivider661(LinearLayout p){View d=new View(this);d.setBackgroundColor(Color.rgb(232,228,216));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(1));lp.setMargins(dp(8),dp(3),dp(8),dp(3));p.addView(d,lp);}
    private void invoke661(Class<?> cls,String name){try{Method m=cls.getDeclaredMethod(name);m.setAccessible(true);m.invoke(this);}catch(Exception e){toast("İşlem açılamadı.");}}
    private void dismiss661(){if(popup661!=null){try{popup661.dismiss();}catch(Exception ignored){}popup661=null;}}
    @Override protected void onDestroy(){dismiss661();super.onDestroy();}
}

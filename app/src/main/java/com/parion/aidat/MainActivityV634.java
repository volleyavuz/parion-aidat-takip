package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.0.34 - attendance eligibility by active dates, continuity bar/percentage, improved photos. */
public class MainActivityV634 extends MainActivityV633 {
    private static final int TEXT=Color.rgb(28,28,28), MUTED=Color.rgb(92,92,92), GRID=Color.rgb(228,228,228);
    private final SimpleDateFormat ISO=new SimpleDateFormat("yyyy-MM-dd",Locale.US);
    private final SimpleDateFormat DAY=new SimpleDateFormat("dd",new Locale("tr","TR"));
    private final SimpleDateFormat MON=new SimpleDateFormat("MMM",new Locale("tr","TR"));
    private final SimpleDateFormat MONTH=new SimpleDateFormat("MMMM yyyy",new Locale("tr","TR"));

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(()->patchGroupButtons634(root));
    }

    private void patchGroupButtons634(View v){
        if(!"ATTENDANCE_GROUPS_631".equals(page))return;
        if(v instanceof Button){
            Button b=(Button)v;String s=String.valueOf(b.getText()).trim();
            if(!s.isEmpty()&&!s.toUpperCase(new Locale("tr","TR")).contains("DIŞA AKTAR"))b.setOnClickListener(x->chooseMonth634(s));
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchGroupButtons634(g.getChildAt(i));}
    }

    private void chooseMonth634(String group){
        Calendar now=Calendar.getInstance();ArrayList<Calendar> months=new ArrayList<>();ArrayList<String> labels=new ArrayList<>();
        for(int i=0;i<10;i++){Calendar c=(Calendar)now.clone();c.set(Calendar.DAY_OF_MONTH,1);c.add(Calendar.MONTH,-i);months.add(c);labels.add(MONTH.format(c.getTime()));}
        new AlertDialog.Builder(this).setTitle(group+" • YOKLAMA AYI").setItems(labels.toArray(new String[0]),(d,w)->showMonth634(group,months.get(w))).show();
    }

    private void showMonth634(String group,Calendar month){
        ArrayList<Sess> sessions=sessions634(group,month);
        page="ATTENDANCE_MONTH_634:"+group;currentAthlete=-1;base(group+" • "+MONTH.format(month.getTime()).toUpperCase(new Locale("tr","TR")),true);
        if(sessions.isEmpty()){ScrollView sv=scroll();LinearLayout b=box(sv);b.addView(tv("Bu ay için yoklama günü bulunmuyor.",12,MUTED,true));return;}

        View content=findContent634();if(content!=null&&content.getParent() instanceof ViewGroup)((ViewGroup)content.getParent()).removeView(content);
        LinearLayout host=new LinearLayout(this);host.setOrientation(LinearLayout.VERTICAL);host.setPadding(dp(4),dp(4),dp(4),dp(6));
        host.addView(tv("Yalnızca sporcunun aktif olduğu günler değerlendirilir. Boş hücre = o tarihte grupta aktif değildi.",9,MUTED,false));

        final int leftW=dp(228), dateW=dp(54), rowH=dp(52), headerH=dp(46);
        LinearLayout header=new LinearLayout(this);header.setOrientation(LinearLayout.HORIZONTAL);
        TextView athleteHead=cell634("#   SPORCU                                      DEVAM",leftW,headerH,9.2f,true);header.addView(athleteHead);
        HorizontalScrollView headerHsv=new HorizontalScrollView(this);headerHsv.setHorizontalScrollBarEnabled(false);
        LinearLayout dateHead=new LinearLayout(this);dateHead.setOrientation(LinearLayout.HORIZONTAL);
        for(Sess s:sessions)dateHead.addView(dateCell634(s.date,dateW,headerH));
        headerHsv.addView(dateHead,new HorizontalScrollView.LayoutParams(-2,headerH));header.addView(headerHsv,new LinearLayout.LayoutParams(0,headerH,1));host.addView(header);

        ScrollView vertical=new ScrollView(this);vertical.setFillViewport(true);vertical.setVerticalScrollBarEnabled(true);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout names=new LinearLayout(this);names.setOrientation(LinearLayout.VERTICAL);names.setBackgroundColor(Color.WHITE);
        HorizontalScrollView gridHsv=new HorizontalScrollView(this);gridHsv.setHorizontalScrollBarEnabled(true);
        LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);

        Cursor a=db.getReadableDatabase().rawQuery("SELECT id,name,birthYear,photo,startDate,endDate,restartDate,status FROM athletes WHERE category=? COLLATE NOCASE AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",new String[]{group});
        int order=0;
        while(a.moveToNext()){
            long athleteId=a.getLong(0);String name=safe634(a.getString(1));int birthYear=a.getInt(2);String photo=safe634(a.getString(3));String start=safe634(a.getString(4));String end=safe634(a.getString(5));String restart=safe634(a.getString(6));
            boolean anyEligible=false;int eligible=0,present=0;
            for(Sess s:sessions){if(activeOn634(s.date,start,end,restart)){anyEligible=true;eligible++;ensureRecord634(s.id,athleteId);if(recordPresent634(s.id,athleteId))present++;}}
            if(!anyEligible)continue;
            order++;int pct=eligible==0?0:Math.round(present*100f/eligible);
            names.addView(athleteCell634(order,athleteId,name,birthYear,photo,pct,leftW,rowH));
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
            for(Sess s:sessions){
                if(!activeOn634(s.date,start,end,restart)){TextView blank=tv("",8,MUTED,false);blank.setBackground(stroke634(Color.rgb(248,248,248),GRID,2,1));row.addView(blank,new LinearLayout.LayoutParams(dateW,rowH));continue;}
                ensureRecord634(s.id,athleteId);CheckBox cb=new CheckBox(this);cb.setGravity(Gravity.CENTER);cb.setChecked(recordPresent634(s.id,athleteId));cb.setContentDescription(name+" • "+s.date);cb.setOnCheckedChangeListener((v,on)->{setRecord634(s.id,athleteId,on);showMonth634(group,month);});row.addView(cb,new LinearLayout.LayoutParams(dateW,rowH));
            }
            grid.addView(row,new LinearLayout.LayoutParams(-2,rowH));
        }
        a.close();

        body.addView(names,new LinearLayout.LayoutParams(leftW,-2));gridHsv.addView(grid,new HorizontalScrollView.LayoutParams(-2,-2));body.addView(gridHsv,new LinearLayout.LayoutParams(0,-2,1));vertical.addView(body,new ScrollView.LayoutParams(-1,-2));host.addView(vertical,new LinearLayout.LayoutParams(-1,0,1));
        if(android.os.Build.VERSION.SDK_INT>=23){gridHsv.setOnScrollChangeListener((v,x,y,ox,oy)->headerHsv.scrollTo(x,0));headerHsv.setOnScrollChangeListener((v,x,y,ox,oy)->gridHsv.scrollTo(x,0));}
        root.addView(host,Math.max(0,root.getChildCount()-1),new LinearLayout.LayoutParams(-1,0,1));
    }

    private boolean activeOn634(String date,String start,String end,String restart){
        if(date==null||date.isEmpty())return false;
        if(start!=null&&!start.trim().isEmpty()&&!"DEVAM".equalsIgnoreCase(start)&&date.compareTo(start.trim())<0)return false;
        if(end==null||end.trim().isEmpty()||"DEVAM".equalsIgnoreCase(end))return true;
        String e=end.trim();
        if(date.compareTo(e)<=0)return true;
        if(restart!=null&&!restart.trim().isEmpty()&&!"DEVAM".equalsIgnoreCase(restart)&&date.compareTo(restart.trim())>=0)return true;
        return false;
    }

    private View athleteCell634(int order,long athleteId,String name,int birthYear,String photo,int pct,int width,int height){
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(2),dp(2),dp(3),dp(2));row.setBackground(stroke634(Color.WHITE,GRID,3,1));
        TextView no=tv(String.valueOf(order),8.8f>0?8:8,TEXT,true);no.setTextSize(8.8f);no.setGravity(Gravity.CENTER);no.setPadding(0,0,0,0);row.addView(no,new LinearLayout.LayoutParams(dp(24),height));
        ImageView img=new ImageView(this);img.setScaleType(ImageView.ScaleType.CENTER_CROP);img.setImageResource(R.drawable.parion_logo);img.setContentDescription(name+" fotoğrafı");img.setOnClickListener(v->showProfile(athleteId));loadPhoto634(img,photo);
        LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(38),dp(38));ip.setMargins(0,0,dp(4),0);row.addView(img,ip);
        TextView nm=tv((birthYear>0?birthYear+" • ":"")+name,9,TEXT,true);nm.setMaxLines(2);nm.setPadding(dp(2),0,dp(2),0);nm.setOnClickListener(v->showProfile(athleteId));row.addView(nm,new LinearLayout.LayoutParams(0,height,1));
        LinearLayout meter=new LinearLayout(this);meter.setOrientation(LinearLayout.VERTICAL);meter.setGravity(Gravity.CENTER);TextView pctText=tv("%"+pct,8,TEXT,true);pctText.setGravity(Gravity.CENTER);pctText.setPadding(0,0,0,0);meter.addView(pctText,new LinearLayout.LayoutParams(dp(48),dp(20)));
        FrameLayout barBg=new FrameLayout(this);barBg.setBackground(stroke634(Color.rgb(235,235,235),Color.TRANSPARENT,4,0));View fill=new View(this);fill.setBackground(stroke634(attColor634(pct),Color.TRANSPARENT,4,0));FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(Math.max(dp(3),Math.round(dp(44)*pct/100f)),dp(8));fp.gravity=Gravity.START|Gravity.CENTER_VERTICAL;barBg.addView(fill,fp);meter.addView(barBg,new LinearLayout.LayoutParams(dp(44),dp(12)));row.addView(meter,new LinearLayout.LayoutParams(dp(50),height));
        row.setOnClickListener(v->showProfile(athleteId));row.setLayoutParams(new LinearLayout.LayoutParams(width,height));return row;
    }

    private int attColor634(int pct){float p=Math.max(0,Math.min(100,pct))/100f;float hue=120f*p;return Color.HSVToColor(new float[]{hue,0.88f,0.82f});}

    private void loadPhoto634(ImageView view,String photo){
        if(photo==null)return;String p=photo.trim();if(p.isEmpty())return;
        try{
            if(p.startsWith("http://")||p.startsWith("https://")){new Thread(()->{try{Bitmap bm=BitmapFactory.decodeStream(new URL(p).openStream());if(bm!=null)view.post(()->view.setImageBitmap(bm));}catch(Exception ignored){}}).start();return;}
            if(p.startsWith("content://")||p.startsWith("file://")){view.setImageURI(Uri.parse(p));return;}
            File f=new File(p);if(f.exists()){Bitmap bm=BitmapFactory.decodeFile(f.getAbsolutePath());if(bm!=null)view.setImageBitmap(bm);return;}
            Uri u=Uri.parse(p);if(u.getScheme()!=null)view.setImageURI(u);
        }catch(Exception ignored){}
    }

    private View dateCell634(String iso,int width,int height){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER);c.setBackground(stroke634(Color.WHITE,GRID,3,1));try{Date d=ISO.parse(iso);TextView day=tv(DAY.format(d),11,TEXT,true);day.setGravity(Gravity.CENTER);day.setPadding(0,0,0,0);day.setIncludeFontPadding(false);TextView mon=tv(MON.format(d).toUpperCase(new Locale("tr","TR")),6,MUTED,true);mon.setGravity(Gravity.CENTER);mon.setPadding(0,0,0,0);mon.setIncludeFontPadding(false);c.addView(day,new LinearLayout.LayoutParams(width,dp(27)));c.addView(mon,new LinearLayout.LayoutParams(width,dp(15)));}catch(Exception ignored){}c.setLayoutParams(new LinearLayout.LayoutParams(width,height));return c;}
    private TextView cell634(String text,int w,int h,float sp,boolean bold){TextView t=tv(text,9,TEXT,bold);t.setTextSize(sp);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(dp(5),0,dp(5),0);t.setBackground(stroke634(Color.WHITE,GRID,3,1));t.setLayoutParams(new LinearLayout.LayoutParams(w,h));return t;}

    private ArrayList<Sess> sessions634(String group,Calendar month){ArrayList<Sess> out=new ArrayList<>();String ym=new SimpleDateFormat("yyyy-MM",Locale.US).format(month.getTime()),today=ISO.format(new Date());Cursor c=db.getReadableDatabase().rawQuery("SELECT id,sessionDate FROM attendance_sessions WHERE groupName=? AND substr(sessionDate,1,7)=? AND sessionDate<=? AND cancelled=0 ORDER BY sessionDate",new String[]{group,ym,today});while(c.moveToNext())out.add(new Sess(c.getLong(0),c.getString(1)));c.close();return out;}
    private static class Sess{long id;String date;Sess(long i,String d){id=i;date=d;}}
    private void ensureRecord634(long session,long athlete){ContentValues v=new ContentValues();v.put("sessionId",session);v.put("athleteId",athlete);v.put("present",1);db.getWritableDatabase().insertWithOnConflict("attendance_records",null,v,android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE);}
    private boolean recordPresent634(long session,long athlete){Cursor c=db.getReadableDatabase().rawQuery("SELECT present FROM attendance_records WHERE sessionId=? AND athleteId=?",new String[]{String.valueOf(session),String.valueOf(athlete)});boolean yes=!c.moveToFirst()||c.getInt(0)==1;c.close();return yes;}
    private void setRecord634(long session,long athlete,boolean present){ensureRecord634(session,athlete);ContentValues v=new ContentValues();v.put("present",present?1:0);db.getWritableDatabase().update("attendance_records",v,"sessionId=? AND athleteId=?",new String[]{String.valueOf(session),String.valueOf(athlete)});}
    private View findContent634(){for(int i=0;i<root.getChildCount();i++){View v=root.getChildAt(i);if(v instanceof ScrollView)return v;}return null;}
    private GradientDrawable stroke634(int fill,int stroke,int radius,int width){GradientDrawable g=new GradientDrawable();g.setColor(fill);g.setCornerRadius(dp(radius));if(width>0)g.setStroke(dp(width),stroke);return g;}
    private String safe634(String s){return s==null?"":s;}
}

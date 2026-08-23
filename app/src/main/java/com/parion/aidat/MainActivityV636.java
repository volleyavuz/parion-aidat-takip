package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.0.36 - group-specific attendance export, share chooser, A4 list-style PDF. */
public class MainActivityV636 extends MainActivityV635 {
    private final SimpleDateFormat ISO636=new SimpleDateFormat("yyyy-MM-dd",Locale.US);
    private final SimpleDateFormat TR636=new SimpleDateFormat("dd.MM.yyyy",new Locale("tr","TR"));
    private final SimpleDateFormat SHORT636=new SimpleDateFormat("dd.MM",new Locale("tr","TR"));

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(()->patchExport636(root));
    }

    private void patchExport636(View v){
        if(!"ATTENDANCE_GROUPS_631".equals(page))return;
        if(v instanceof Button){
            Button b=(Button)v;String s=String.valueOf(b.getText()).trim().toUpperCase(new Locale("tr","TR"));
            if(s.contains("DIŞA AKTAR")){b.setOnClickListener(x->chooseExportGroup636());return;}
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchExport636(g.getChildAt(i));}
    }

    private ArrayList<String> groups636(){ArrayList<String> out=new ArrayList<>();Cursor c=db.getReadableDatabase().rawQuery("SELECT name FROM app_groups ORDER BY sortOrder,id,name COLLATE NOCASE",null);while(c.moveToNext())out.add(c.getString(0));c.close();return out;}
    private void chooseExportGroup636(){ArrayList<String> gs=groups636();if(gs.isEmpty()){toast("Dışa aktarılacak grup yok.");return;}new AlertDialog.Builder(this).setTitle("YOKLAMA DIŞA AKTAR • GRUP SEÇ").setItems(gs.toArray(new String[0]),(d,w)->chooseExportPeriod636(gs.get(w))).show();}
    private void chooseExportPeriod636(String group){String[] labels={"SON 1 AY","SON 3 AY","SON 6 AY","SON 10 AY"};int[] months={1,3,6,10};new AlertDialog.Builder(this).setTitle(group+" • ZAMAN ARALIĞI").setItems(labels,(d,w)->chooseExportFormat636(group,months[w],labels[w])).show();}
    private void chooseExportFormat636(String group,int months,String period){String[] f={"PDF • A4 YOKLAMA LİSTESİ","CSV • EXCEL İÇİN"};new AlertDialog.Builder(this).setTitle(group+" • "+period).setItems(f,(d,w)->{if(w==0)exportPdf636(group,months,period);else exportCsv636(group,months,period);}).show();}

    private static class S636{long id;String date;S636(long i,String d){id=i;date=d;}}
    private static class A636{long id;String name,start,end,restart;A636(long i,String n,String s,String e,String r){id=i;name=n;start=s;end=e;restart=r;}}
    private ArrayList<S636> sessions636(String group,int months){
        Calendar from=Calendar.getInstance();from.add(Calendar.MONTH,-months);String start=ISO636.format(from.getTime()),today=ISO636.format(new Date());ArrayList<S636> out=new ArrayList<>();
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,sessionDate FROM attendance_sessions WHERE groupName=? AND sessionDate>=? AND sessionDate<=? AND cancelled=0 ORDER BY sessionDate DESC",new String[]{group,start,today});while(c.moveToNext())out.add(new S636(c.getLong(0),c.getString(1)));c.close();return out;
    }
    private ArrayList<A636> athletes636(String group,ArrayList<S636> ss){ArrayList<A636> out=new ArrayList<>();Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name,startDate,endDate,restartDate FROM athletes WHERE category=? COLLATE NOCASE AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",new String[]{group});while(c.moveToNext()){A636 a=new A636(c.getLong(0),safe636(c.getString(1)),safe636(c.getString(2)),safe636(c.getString(3)),safe636(c.getString(4)));boolean any=false;for(S636 s:ss)if(activeOn636(s.date,a.start,a.end,a.restart)){any=true;break;}if(any)out.add(a);}c.close();return out;}
    private boolean activeOn636(String date,String start,String end,String restart){if(date==null||date.isEmpty())return false;if(start!=null&&!start.trim().isEmpty()&&!"DEVAM".equalsIgnoreCase(start)&&date.compareTo(start.trim())<0)return false;if(end==null||end.trim().isEmpty()||"DEVAM".equalsIgnoreCase(end))return true;String e=end.trim();if(date.compareTo(e)<=0)return true;return restart!=null&&!restart.trim().isEmpty()&&!"DEVAM".equalsIgnoreCase(restart)&&date.compareTo(restart.trim())>=0;}
    private boolean present636(long session,long athlete){Cursor c=db.getReadableDatabase().rawQuery("SELECT present FROM attendance_records WHERE sessionId=? AND athleteId=?",new String[]{String.valueOf(session),String.valueOf(athlete)});boolean yes=c.moveToFirst()&&c.getInt(0)==1;c.close();return yes;}

    private File exportDir636(){File d=new File(getCacheDir(),"attendance_exports");d.mkdirs();return d;}
    private String safeFile636(String s){return s.toUpperCase(new Locale("tr","TR")).replace('İ','I').replace('Ş','S').replace('Ğ','G').replace('Ü','U').replace('Ö','O').replace('Ç','C').replaceAll("[^A-Z0-9_-]+","_");}
    private void exportPdf636(String group,int months,String period){
        ArrayList<S636> ss=sessions636(group,months);if(ss.isEmpty()){toast("Seçilen aralıkta yoklama günü yok.");return;}ArrayList<A636> aa=athletes636(group,ss);if(aa.isEmpty()){toast("Seçilen aralıkta sporcu yok.");return;}
        File out=new File(exportDir636(),"Yoklama_"+safeFile636(group)+"_"+months+"ay.pdf");PdfDocument doc=new PdfDocument();Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        final int W=842,H=595,ML=24,MR=24,MT=24,MB=22,NAMEW=190,DATEW=34,ROWH=19,HEADH=31,TITLEH=34;int datePer=Math.max(1,(W-ML-MR-NAMEW)/DATEW);int rowsPer=Math.max(1,(H-MT-MB-TITLEH-HEADH)/ROWH);int pageNo=0;
        try{
            for(int ds=0;ds<ss.size();ds+=datePer){int de=Math.min(ss.size(),ds+datePer);for(int as=0;as<aa.size();as+=rowsPer){int ae=Math.min(aa.size(),as+rowsPer);PdfDocument.PageInfo pi=new PdfDocument.PageInfo.Builder(W,H,++pageNo).create();PdfDocument.Page pg=doc.startPage(pi);Canvas c=pg.getCanvas();c.drawColor(Color.WHITE);
                p.setColor(Color.BLACK);p.setTextSize(15);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("PARİON • "+group+" • YOKLAMA LİSTESİ",ML,MT+14,p);p.setTextSize(8);p.setTypeface(Typeface.DEFAULT);c.drawText(period+" • Geldiği günler işaretlidir • Sayfa "+pageNo,ML,MT+28,p);
                float y=MT+TITLEH;drawRect636(c,p,ML,y,NAMEW,HEADH,Color.rgb(242,242,242));p.setColor(Color.BLACK);p.setTextSize(9);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("SPORCU",ML+5,y+19,p);
                for(int j=ds;j<de;j++){float x=ML+NAMEW+(j-ds)*DATEW;drawRect636(c,p,x,y,DATEW,HEADH,Color.rgb(242,242,242));p.setColor(Color.BLACK);p.setTextSize(7.5f);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText(shortDate636(ss.get(j).date),x+3,y+19,p);}y+=HEADH;
                int order=as+1;for(int i=as;i<ae;i++,order++){A636 a=aa.get(i);drawRect636(c,p,ML,y,NAMEW,ROWH,Color.WHITE);p.setColor(Color.BLACK);p.setTextSize(8);p.setTypeface(Typeface.DEFAULT);String nm=order+". "+a.name;if(nm.length()>35)nm=nm.substring(0,35);c.drawText(nm,ML+4,y+13,p);
                    for(int j=ds;j<de;j++){S636 s=ss.get(j);float x=ML+NAMEW+(j-ds)*DATEW;boolean eligible=activeOn636(s.date,a.start,a.end,a.restart);drawRect636(c,p,x,y,DATEW,ROWH,eligible?Color.WHITE:Color.rgb(247,247,247));if(eligible){float bx=x+DATEW/2f-5,by=y+ROWH/2f-5;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(Color.rgb(85,85,85));c.drawRect(bx,by,bx+10,by+10,p);if(present636(s.id,a.id)){p.setStrokeWidth(1.8f);p.setColor(Color.rgb(20,120,55));Path path=new Path();path.moveTo(bx+2,by+5);path.lineTo(bx+4.5f,by+8);path.lineTo(bx+9,by+2);c.drawPath(path,p);}p.setStyle(Paint.Style.FILL);}}
                    y+=ROWH;
                }
                doc.finishPage(pg);
            }}
            FileOutputStream fos=new FileOutputStream(out);doc.writeTo(fos);fos.close();doc.close();share636(out,"application/pdf",group+" yoklama listesi");
        }catch(Exception e){try{doc.close();}catch(Exception ignored){}toast("PDF oluşturulamadı: "+e.getMessage());}
    }
    private void drawRect636(Canvas c,Paint p,float x,float y,float w,float h,int fill){p.setStyle(Paint.Style.FILL);p.setColor(fill);c.drawRect(x,y,x+w,y+h,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.6f);p.setColor(Color.rgb(190,190,190));c.drawRect(x,y,x+w,y+h,p);p.setStyle(Paint.Style.FILL);}
    private String shortDate636(String iso){try{return SHORT636.format(ISO636.parse(iso));}catch(Exception e){return iso;}}

    private void exportCsv636(String group,int months,String period){ArrayList<S636> ss=sessions636(group,months);if(ss.isEmpty()){toast("Seçilen aralıkta yoklama günü yok.");return;}ArrayList<A636> aa=athletes636(group,ss);File out=new File(exportDir636(),"Yoklama_"+safeFile636(group)+"_"+months+"ay.csv");try{OutputStreamWriter w=new OutputStreamWriter(new FileOutputStream(out),"UTF-8");w.write('\ufeff');w.write("SPORCU");for(S636 s:ss)w.write(";"+shortDate636(s.date));w.write("\n");for(A636 a:aa){w.write(csv636(a.name));for(S636 s:ss){if(!activeOn636(s.date,a.start,a.end,a.restart))w.write(";");else w.write(present636(s.id,a.id)?";GELDİ":";GELMEDİ");}w.write("\n");}w.flush();w.close();share636(out,"text/csv",group+" yoklama listesi");}catch(Exception e){toast("CSV oluşturulamadı: "+e.getMessage());}}
    private String csv636(String s){return '"'+s.replace("\"","\"\"")+'"';}
    private void share636(File f,String mime,String title){try{Uri u=Uri.parse("content://com.parion.aidat.share/export/"+Uri.encode(f.getName()));Intent i=new Intent(Intent.ACTION_SEND);i.setType(mime);i.putExtra(Intent.EXTRA_STREAM,u);i.putExtra(Intent.EXTRA_SUBJECT,title);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"PAYLAŞ"));}catch(Exception e){toast("Paylaşım açılamadı: "+e.getMessage());}}
    private String safe636(String s){return s==null?"":s;}
}

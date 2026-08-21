package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivityV398 extends MainActivityV397 {
    private static final int REQ_FORM_GALLERY=3840;
    private static final int REQ_MISSING_GALLERY=3980;
    private long missingPhotoAthlete=-1;
    private static final int PAID_GREEN=Color.rgb(9,242,153);

    @Override void showHome(){
        super.showHome();
        DashData d=dashboardData();
        addDashboardAthleteCounts(root,d);
        replaceMissingPhotoClick398(root);
    }

    private void addDashboardAthleteCounts(View v,DashData d){
        if(v instanceof TextView){
            TextView t=(TextView)v;String s=String.valueOf(t.getText());
            if("TAHSİL EDİLEN".equals(s))t.setText("TAHSİL EDİLEN\n"+d.collected.size()+" SPORCU");
            else if("BEKLENEN".equals(s))t.setText("BEKLENEN\n"+d.expected.size()+" SPORCU");
            else if("GECİKMİŞ".equals(s))t.setText("GECİKMİŞ\n"+d.overdue.size()+" SPORCU");
            else if("AY SONUNA KADAR\nGELECEK".equals(s))t.setText("AY SONUNA KADAR\nGELECEK • "+d.upcoming.size()+" SPORCU");
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)addDashboardAthleteCounts(g.getChildAt(i),d);}
    }

    private void replaceMissingPhotoClick398(View v){
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                View ch=g.getChildAt(i);
                if(ch instanceof TextView&&String.valueOf(((TextView)ch).getText()).contains("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR")){
                    View parent=(View)ch.getParent();if(parent!=null)parent.setOnClickListener(x->showMissingGallery398());
                }
                replaceMissingPhotoClick398(ch);
            }
        }
    }

    @Override void form(long id){
        super.form(id);
        replaceFormPhotoButtons(root);
    }

    private void replaceFormPhotoButtons(View v){
        if(v instanceof Button){
            Button b=(Button)v;String s=String.valueOf(b.getText());
            if(s.contains("FOTOĞRAF EKLE")||s.contains("FOTOĞRAFI DEĞİŞTİR"))b.setOnClickListener(x->openRecentPhotoPicker(REQ_FORM_GALLERY));
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)replaceFormPhotoButtons(g.getChildAt(i));}
    }

    private void openRecentPhotoPicker(int requestCode){
        try{
            Intent i;
            if(Build.VERSION.SDK_INT>=33){
                i=new Intent(MediaStore.ACTION_PICK_IMAGES);
                i.setType("image/*");
            }else{
                i=new Intent(Intent.ACTION_PICK);
                i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
            }
            startActivityForResult(i,requestCode);
        }catch(Exception e){
            Intent fallback=new Intent(Intent.ACTION_GET_CONTENT);fallback.setType("image/*");fallback.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(fallback,requestCode);
        }
    }

    private boolean missing398(String photo){
        String p=photo==null?"":photo.trim(),up=p.toUpperCase(new Locale("tr","TR"));
        if(p.isEmpty()||"NONE".equalsIgnoreCase(p)||up.contains("0000 BOS"))return true;
        if(p.startsWith("USER:")){File f=new File(new File(getFilesDir(),"athlete_photos"),p.substring(5));return !f.isFile();}
        try(InputStream in=getAssets().open("photos/"+p)){
            Bitmap src=BitmapFactory.decodeStream(in);if(src==null)return true;Bitmap b=Bitmap.createScaledBitmap(src,32,32,true);if(src!=b)src.recycle();int lowSat=0,light=0,dark=0,n=1024;
            for(int y=0;y<32;y++)for(int x=0;x<32;x++){int c=b.getPixel(x,y),r=Color.red(c),g=Color.green(c),bl=Color.blue(c),mx=Math.max(r,Math.max(g,bl)),mn=Math.min(r,Math.min(g,bl)),lum=(r+g+bl)/3;if(mx-mn<14)lowSat++;if((lum>=188&&lum<=238)||lum>=246)light++;if(lum<175)dark++;}
            b.recycle();return lowSat>n*.985&&light>n*.94&&dark<n*.015;
        }catch(Exception e){return true;}
    }

    private void showMissingGallery398(){
        page="MISSING_PHOTO_398";base("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR",true);
        TextView info=tv("Sporcuya dokunun; galeriniz küçük resimler halinde en yeni fotoğraflardan eskiye doğru açılır.",12,Color.DKGRAY,false);info.setGravity(Gravity.CENTER);root.addView(info);
        ScrollView sv=scroll();LinearLayout b=box(sv);Cursor c=db.athletes("","AKTİF");int n=0;
        while(c.moveToNext()){A x=a(c);if(!missing398(x.photo))continue;addMissingGalleryRow398(b,x);n++;}c.close();
        if(n==0)b.addView(tv("Fotoğrafı olmayan aktif sporcu bulunmuyor.",14,GREEN,true));
    }

    private void addMissingGalleryRow398(LinearLayout b,A x){
        LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(8),dp(8),dp(8),dp(8));r.setBackground(round(Color.WHITE,10));
        ImageView av=new ImageView(this);av.setScaleType(ImageView.ScaleType.CENTER_CROP);setAthletePhoto(av,x.photo);r.addView(av,new LinearLayout.LayoutParams(dp(60),dp(60)));
        LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.addView(tv((x.by>0?x.by+" • ":"")+x.name,15,BLACK,true));t.addView(tv(x.cat+" • FOTOĞRAF EKLE",12,Color.DKGRAY,true));r.addView(t,new LinearLayout.LayoutParams(0,-2,1));
        final long aid=x.id;r.setOnClickListener(v->{missingPhotoAthlete=aid;openRecentPhotoPicker(REQ_MISSING_GALLERY);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(r,lp);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==REQ_MISSING_GALLERY){
            if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null&&missingPhotoAthlete>0)saveMissingPhoto398(data.getData(),missingPhotoAthlete);
            missingPhotoAthlete=-1;showMissingGallery398();return;
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    private void saveMissingPhoto398(Uri u,long athleteId){
        try{
            File dir=new File(getFilesDir(),"athlete_photos");if(!dir.exists())dir.mkdirs();String fn="user_"+System.currentTimeMillis()+".img";File out=new File(dir,fn);
            try(InputStream in=getContentResolver().openInputStream(u);OutputStream os=new FileOutputStream(out)){byte[] buf=new byte[8192];int n;while(in!=null&&(n=in.read(buf))>0)os.write(buf,0,n);}
            Bitmap check=BitmapFactory.decodeFile(out.getAbsolutePath());if(check==null){out.delete();toast("Fotoğraf okunamadı.");return;}check.recycle();
            ContentValues cv=new ContentValues();cv.put("photo","USER:"+fn);db.getWritableDatabase().update("athletes",cv,"id=?",new String[]{String.valueOf(athleteId)});toast("Fotoğraf kaydedildi.");
        }catch(Exception e){toast("Fotoğraf eklenemedi: "+e.getMessage());}
    }

    @Override void goBack(){if("MISSING_PHOTO_398".equals(page)){showHome();return;}super.goBack();}

    @Override void createRollingReport(long id,int monthCount,String filterName){
        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return;}String name=s(a,"name"),category=s(a,"category"),sibling=s(a,"sibling"),start=s(a,"startDate"),end=s(a,"endDate"),restart=s(a,"restartDate");int by=a.getInt(a.getColumnIndexOrThrow("birthYear"));a.close();
        HashMap<Integer,PayRec> pays=new HashMap<>();Cursor pc=db.payments(id);while(pc.moveToNext()){int yy=pc.getInt(pc.getColumnIndexOrThrow("year")),mm=pc.getInt(pc.getColumnIndexOrThrow("month"));pays.put(yy*100+mm,new PayRec(s(pc,"marker"),pc.getInt(pc.getColumnIndexOrThrow("amount"))));}pc.close();
        Calendar now=Calendar.getInstance();int anchor=anchorDay(start),currentKey=currentCycleKey(now,anchor),waitingKey=shiftMonth(currentKey,1),startKey=monthCount>0?shiftMonth(currentKey,-(monthCount-1)):registrationMonth(start,id,currentKey);int reg=parseMonthKey(start);if(reg>0&&reg>startKey)startKey=reg;if(startKey>currentKey)startKey=currentKey;
        ArrayList<Integer> months=new ArrayList<>();for(int k=startKey;;k=shiftMonth(k,1)){months.add(k);if(k==currentKey)break;if(months.size()>240)break;}months.add(waitingKey);
        int[] shirts=shirtSummary398(id);int W=1400,rowH=86,H=700+months.size()*rowH+360;Bitmap bm=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bm);c.drawColor(Color.WHITE);Paint q=new Paint(Paint.ANTI_ALIAS_FLAG);
        q.setColor(Color.DKGRAY);q.setTypeface(Typeface.DEFAULT);q.setTextSize(18);String created=new SimpleDateFormat("dd/MM/yyyy",Locale.US).format(new Date());c.drawText("Oluşturma Tarihi: "+created,55,25,q);
        Bitmap logo=loadReportLogo398();if(logo!=null){float scale=Math.min(145f/logo.getWidth(),145f/logo.getHeight());int lw=Math.max(1,Math.round(logo.getWidth()*scale)),lh=Math.max(1,Math.round(logo.getHeight()*scale));Rect dst=new Rect(55,42,55+lw,42+lh);c.drawBitmap(logo,null,dst,q);logo.recycle();}
        q.setTypeface(Typeface.DEFAULT_BOLD);q.setColor(BLACK);q.setTextSize(50);c.drawText("PARİON SPOR KULÜBÜ",230,82,q);q.setColor(GOLD);q.setTextSize(38);c.drawText("ÖDEME BİLGİSİ",230,132,q);
        q.setColor(BLACK);q.setTextSize(28);c.drawText("Sporcu: "+name,60,205,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(23);c.drawText("Doğum Yılı: "+by+"   Grup: "+category+"   Filtre: "+filterName,60,245,q);c.drawText("Dönem: "+keyLabel(startKey)+" – "+keyLabel(waitingKey),60,281,q);
        q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);q.setColor(Color.DKGRAY);c.drawText("AY",60,343,q);c.drawText("ÖDEME TARİHİ",300,343,q);c.drawText("BEKLENEN",610,343,q);c.drawText("ÖDENEN",830,343,q);c.drawText("DURUM",1050,343,q);q.setStrokeWidth(2);c.drawLine(60,363,W-60,363,q);
        int y=393,totalPaid=0,totalExpected=0;
        for(int key:months){int yr=key/100,mo=key%100;boolean future=key==waitingKey;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);int expected=expectedFeeAt(id,yr,mo,r);boolean active=activeAt(yr,mo,start,end,restart);String status,date="—";int color;
            if(future){status="BEKLİYOR";color=Color.WHITE;}else if("X".equals(r.marker)){status="ARA VERDİ";color=GRAY;expected=0;}else if("!".equals(r.marker)||"!!".equals(r.marker)){status=expected>0?(r.amount<expected?"EKSİK ÖDEME":r.amount>expected?"FAZLA ÖDEME":"ÖDENDİ"):"FARKLI TUTAR";color=status.equals("ÖDENDİ")?PAID_GREEN:ORANGE;}else if(isDate(r.marker)){date=dateTr(r.marker);status=expected>0&&r.amount!=expected?(r.amount<expected?"EKSİK ÖDEME":"FAZLA ÖDEME"):"ÖDENDİ";color=status.equals("ÖDENDİ")?PAID_GREEN:ORANGE;}else if(!active){status="AKTİF DEĞİL";color=Color.rgb(225,225,225);expected=0;}else if("BURSLU".equalsIgnoreCase(sibling)||expected==0&&yr>=2026&&currentMonthlyFee(id)==0){status="BURSLU";color=Color.rgb(225,225,225);expected=0;}else if(expected==0&&yr<2026){status="VERİ YOK";color=Color.rgb(225,225,225);}else{status="ÖDEME DÖNEMİ";color=YELLOW;}
            if(!future&&expected>0&&active&&!"X".equals(r.marker))totalExpected+=expected;if(!future&&r.amount>0)totalPaid+=r.amount;q.setColor(color);c.drawRoundRect(48,y-38,W-48,y+34,16,16,q);q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);c.drawText(monthName(mo)+" "+yr,60,y,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(21);c.drawText(date,300,y,q);c.drawText(expected>0?money(expected):"—",610,y,q);c.drawText(!future&&r.amount>0?money(r.amount):"—",830,y,q);drawFit(c,q,status,1050,y,280);y+=rowH;
        }
        q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(27);c.drawText("TOPLAM BEKLENEN: "+money(totalExpected),60,y+35,q);c.drawText("TOPLAM ÖDENEN: "+money(totalPaid),520,y+35,q);int diff=totalPaid-totalExpected;q.setColor(diff<0?RED:diff>0?ORANGE:PAID_GREEN);c.drawText("FARK: "+(diff>0?"+":"")+money(diff),980,y+35,q);
        y+=82;q.setColor(Color.rgb(245,245,245));c.drawRoundRect(50,y-25,W-50,y+105,18,18,q);q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(24);c.drawText("TİŞÖRT / MALZEME TAHSİLAT ÖZETİ",70,y+8,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(22);c.drawText("Verilen tişört: "+shirts[0]+" adet   •   Tutar: "+money(shirts[1])+"   •   Tahsil edilen: "+money(shirts[2]),70,y+48,q);int due=Math.max(0,shirts[1]-shirts[2]);q.setColor(due>0?RED:PAID_GREEN);q.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("Kalan tişört/malzeme borcu: "+money(due),70,y+82,q);
        y+=145;String from=cycleDateLabel(waitingKey,anchor),to=cycleDateLabel(shiftMonth(waitingKey,1),anchor);q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);c.drawText("Sayın Velimiz,",60,y+30,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(20);c.drawText(monthName(waitingKey%100)+" ayı aidat ödeme aralığınız "+from+" – "+to+"'dir.",60,y+64,q);c.drawText("Aidat takibiniz sporcumuzun kulübe başlangıç gününe göre aylık olarak hesaplanmaktadır.",60,y+96,q);
        q.setColor(Color.DKGRAY);q.setTypeface(Typeface.DEFAULT);q.setTextSize(19);q.setTextAlign(Paint.Align.CENTER);c.drawText("Bu belge bilgilendirme amaçlıdır. Makbuz niteliği taşımamaktadır.",W/2f,H-55,q);q.setTextAlign(Paint.Align.LEFT);saveAndShare(bm,name,filterName);
    }

    private Bitmap loadReportLogo398(){try(InputStream in=getAssets().open("parion_logo.png")){return BitmapFactory.decodeStream(in);}catch(Exception ignored){}try{return BitmapFactory.decodeResource(getResources(),R.drawable.parion_logo);}catch(Exception e){return null;}}
    private int[] shirtSummary398(long athleteId){int qty=0,total=0,paid=0;try{Cursor c=db.getReadableDatabase().rawQuery("SELECT COALESCE(SUM(qty),0),COALESCE(SUM(total),0),COALESCE(SUM(paidAmount),0) FROM material_transactions WHERE athleteId=? AND UPPER(product) LIKE '%TİŞÖRT%'",new String[]{String.valueOf(athleteId)});if(c.moveToFirst()){qty=c.getInt(0);total=c.getInt(1);paid=c.getInt(2);}c.close();}catch(Exception ignored){}return new int[]{qty,total,paid};}
}

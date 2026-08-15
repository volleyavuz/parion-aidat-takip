package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.text.Normalizer;
import java.util.*;

public class MainActivityV399 extends MainActivityV398 {
    private static final int REQ_MISSING_FORM=3990;
    private static final int REQ_PROFILE_FORM=3991;
    private long formAthlete=-1;
    private boolean returnToMissingForms=false;
    private boolean bundledLoaded399=false;
    private final Map<Long,String[]> bundledForms399=new HashMap<>();

    @Override public void onCreate(android.os.Bundle b){super.onCreate(b);ensureRegistrationForms399();}

    private void ensureRegistrationForms399(){
        try{if(db!=null)db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS registration_forms (athleteId INTEGER PRIMARY KEY, fileRef TEXT NOT NULL DEFAULT '', mimeType TEXT NOT NULL DEFAULT '', originalName TEXT NOT NULL DEFAULT '', updatedAt TEXT NOT NULL DEFAULT '')");}catch(Exception ignored){}
    }

    @Override void showHome(){
        super.showHome();
        // showHome can be dynamically dispatched while a parent onCreate is still running.
        // Ensure the table exists before the first missing-form count is queried.
        ensureRegistrationForms399();
        addMissingFormsCard399();
    }

    private void addMissingFormsCard399(){
        ScrollView sv=findScroll399(root);if(sv==null)return;LinearLayout b=(LinearLayout)sv.getChildAt(0);if(b==null)return;
        int n=countMissingForms399();
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(8),dp(10),dp(8),dp(10));card.setBackground(round(Color.WHITE,12));
        TextView number=tv(String.valueOf(n),24,n>0?RED:GREEN,true);number.setGravity(Gravity.CENTER);number.setMinHeight(dp(46));card.addView(number,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView label=tv("KAYIT FORMU OLMAYAN AKTİF SPORCULAR",12,Color.DKGRAY,true);label.setGravity(Gravity.CENTER);card.addView(label,new LinearLayout.LayoutParams(-1,dp(42)));
        card.setOnClickListener(v->showMissingForms399());card.setClickable(true);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(8),0,dp(8));b.addView(card,lp);
    }
    private ScrollView findScroll399(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll399(g.getChildAt(i));if(s!=null)return s;}}return null;}

    private String normForm399(String s){
        if(s==null)return "";
        String x=s.toUpperCase(new Locale("tr","TR")).replace('İ','I').replace('ı','I');
        x=Normalizer.normalize(x,Normalizer.Form.NFD).replaceAll("\\p{M}+","");
        return x.replaceAll("[^A-Z0-9]","");
    }

    private void ensureBundledIndex399(){
        if(bundledLoaded399)return;bundledLoaded399=true;
        try{
            Map<String,Long> athletes=new HashMap<>();
            Cursor a=db.getReadableDatabase().rawQuery("SELECT id,name,birthYear FROM athletes",null);
            while(a.moveToNext())athletes.put(a.getInt(2)+"|"+normForm399(a.getString(1)),a.getLong(0));
            a.close();
            try(InputStream in=getAssets().open("forms/index.tsv");BufferedReader br=new BufferedReader(new InputStreamReader(in,"UTF-8"))){
                String line;while((line=br.readLine())!=null){
                    String[] z=line.split("\\t",5);if(z.length<5)continue;
                    int year;try{year=Integer.parseInt(z[0]);}catch(Exception e){continue;}
                    Long id=athletes.get(year+"|"+normForm399(z[1]));if(id==null)continue;
                    bundledForms399.put(id,new String[]{"ASSET:bundled/"+z[2],z[3],z[4]});
                }
            }
        }catch(Exception ignored){}
    }

    private boolean hasLocalForm399(long id){
        ensureRegistrationForms399();
        try{Cursor c=db.getReadableDatabase().rawQuery("SELECT fileRef FROM registration_forms WHERE athleteId=?",new String[]{String.valueOf(id)});boolean ok=false;if(c.moveToFirst()){String r=c.getString(0);ok=r!=null&&!r.trim().isEmpty()&&formFileExists399(r);}c.close();return ok;}catch(Exception e){return false;}
    }
    private boolean hasForm399(long id){
        if(hasLocalForm399(id))return true;
        ensureBundledIndex399();String[] x=bundledForms399.get(id);return x!=null&&formFileExists399(x[0]);
    }
    private boolean formFileExists399(String ref){
        if(ref==null||ref.trim().isEmpty())return false;
        if(ref.startsWith("ASSET:")){try(InputStream in=getAssets().open("forms/"+ref.substring(6))){return true;}catch(Exception e){return false;}}
        if(ref.startsWith("USER:"))return new File(new File(getFilesDir(),"registration_forms"),ref.substring(5)).isFile();
        return false;
    }
    private int countMissingForms399(){int n=0;Cursor c=db.athletes("","AKTİF");while(c.moveToNext()){A x=a(c);if(!hasForm399(x.id))n++;}c.close();return n;}

    private void showMissingForms399(){
        page="MISSING_FORMS";base("KAYIT FORMU OLMAYAN AKTİF SPORCULAR",true);
        TextView info=tv("Sporcuya dokunun; JPEG/PNG veya PDF kayıt formunu seçin. Yükleme tamamlanınca bu listeye geri dönülür.",12,Color.DKGRAY,false);info.setGravity(Gravity.CENTER);root.addView(info);
        ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;Cursor c=db.athletes("","AKTİF");while(c.moveToNext()){A x=a(c);if(hasForm399(x.id))continue;addMissingFormRow399(b,x);n++;}c.close();
        if(n==0)b.addView(tv("Kayıt formu olmayan aktif sporcu bulunmuyor.",14,GREEN,true));
    }
    private void addMissingFormRow399(LinearLayout b,A x){
        LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(8),dp(8),dp(8),dp(8));r.setBackground(round(Color.WHITE,10));
        ImageView av=new ImageView(this);av.setScaleType(ImageView.ScaleType.CENTER_CROP);setAthletePhoto(av,x.photo);r.addView(av,new LinearLayout.LayoutParams(dp(60),dp(60)));
        LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.addView(tv((x.by>0?x.by+" • ":"")+x.name,15,BLACK,true));t.addView(tv(x.cat+" • KAYIT FORMU EKLE",12,Color.DKGRAY,true));r.addView(t,new LinearLayout.LayoutParams(0,-2,1));
        final long aid=x.id;r.setOnClickListener(v->{formAthlete=aid;returnToMissingForms=true;pickForm399(REQ_MISSING_FORM);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(r,lp);
    }

    @Override void showProfile(long id){super.showProfile(id);addRegistrationFormButton399(id);}
    private void addRegistrationFormButton399(long id){
        LinearLayout b=findBox(root);if(b==null)return;boolean exists=hasForm399(id);Button f=btn(exists?"KAYIT FORMUNU GÖRÜNTÜLE":"KAYIT FORMU EKLE");
        f.setOnClickListener(v->{if(hasForm399(id))showRegistrationForm399(id);else{formAthlete=id;returnToMissingForms=false;pickForm399(REQ_PROFILE_FORM);}});
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(56));lp.setMargins(0,dp(8),0,0);b.addView(f,lp);
        if(exists){Button change=btn("KAYIT FORMUNU DEĞİŞTİR");change.setOnClickListener(v->{formAthlete=id;returnToMissingForms=false;pickForm399(REQ_PROFILE_FORM);});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(52));cp.setMargins(0,dp(5),0,0);b.addView(change,cp);}
    }

    private void pickForm399(int request){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/jpeg","image/png","image/webp","application/pdf"});startActivityForResult(i,request);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==REQ_MISSING_FORM||requestCode==REQ_PROFILE_FORM){
            long id=formAthlete;boolean missing=returnToMissingForms;formAthlete=-1;returnToMissingForms=false;
            if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null&&id>0)saveRegistrationForm399(data.getData(),id);
            if(missing)showMissingForms399();else if(id>0)showProfile(id);else showHome();return;
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    private void saveRegistrationForm399(Uri uri,long id){
        try{
            String mime=getContentResolver().getType(uri);if(mime==null)mime="application/octet-stream";String ext=mime.contains("pdf")?".pdf":mime.contains("png")?".png":mime.contains("webp")?".webp":".jpg";
            File dir=new File(getFilesDir(),"registration_forms");if(!dir.exists())dir.mkdirs();String fn="form_"+id+"_"+System.currentTimeMillis()+ext;File out=new File(dir,fn);
            try(InputStream in=getContentResolver().openInputStream(uri);OutputStream os=new FileOutputStream(out)){byte[] buf=new byte[16384];int n;while(in!=null&&(n=in.read(buf))>0)os.write(buf,0,n);}
            if(out.length()==0){out.delete();toast("Kayıt formu okunamadı.");return;}
            ensureRegistrationForms399();db.getWritableDatabase().execSQL("INSERT OR REPLACE INTO registration_forms(athleteId,fileRef,mimeType,originalName,updatedAt) VALUES(?,?,?,?,datetime('now'))",new Object[]{id,"USER:"+fn,mime,fn});toast("Kayıt formu kaydedildi.");
        }catch(Exception e){toast("Kayıt formu eklenemedi: "+e.getMessage());}
    }

    private String[] formInfo399(long id){
        ensureRegistrationForms399();
        try{Cursor c=db.getReadableDatabase().rawQuery("SELECT fileRef,mimeType,originalName FROM registration_forms WHERE athleteId=?",new String[]{String.valueOf(id)});if(c.moveToFirst()){String ref=c.getString(0),mime=c.getString(1),name=c.getString(2);c.close();if(formFileExists399(ref))return new String[]{ref,mime,name};}else c.close();}catch(Exception ignored){}
        ensureBundledIndex399();return bundledForms399.get(id);
    }
    private void showRegistrationForm399(long id){
        String[] info=formInfo399(id);if(info==null){toast("Kayıt formu bulunamadı.");return;}String ref=info[0],mime=info[1];
        try{if(mime!=null&&mime.contains("pdf"))showPdf399(ref);else showImageForm399(ref);}catch(Exception e){toast("Form açılamadı: "+e.getMessage());}
    }
    private InputStream openFormStream399(String ref)throws Exception{if(ref.startsWith("ASSET:"))return getAssets().open("forms/"+ref.substring(6));if(ref.startsWith("USER:"))return new FileInputStream(new File(new File(getFilesDir(),"registration_forms"),ref.substring(5)));throw new FileNotFoundException();}
    private File materializeForm399(String ref,String suffix)throws Exception{File f=File.createTempFile("parion_form_",suffix,getCacheDir());try(InputStream in=openFormStream399(ref);OutputStream out=new FileOutputStream(f)){byte[] b=new byte[16384];int n;while((n=in.read(b))>0)out.write(b,0,n);}return f;}
    private void showImageForm399(String ref)throws Exception{
        BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;try(InputStream in=openFormStream399(ref)){BitmapFactory.decodeStream(in,null,bounds);}int sample=1;while(bounds.outWidth/sample>1800||bounds.outHeight/sample>2600)sample*=2;BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=Math.max(1,sample);o.inPreferredConfig=Bitmap.Config.RGB_565;
        Bitmap bm;try(InputStream in=openFormStream399(ref)){bm=BitmapFactory.decodeStream(in,null,o);}if(bm==null)throw new IOException("Görsel okunamadı");
        ImageView iv=new ImageView(this);iv.setImageBitmap(bm);iv.setAdjustViewBounds(true);iv.setScaleType(ImageView.ScaleType.FIT_CENTER);ScrollView s=new ScrollView(this);s.addView(iv,new ScrollView.LayoutParams(-1,-2));new AlertDialog.Builder(this).setTitle("KAYIT FORMU").setView(s).setPositiveButton("KAPAT",null).show();
    }
    private void showPdf399(String ref)throws Exception{
        File f=materializeForm399(ref,".pdf");ParcelFileDescriptor pfd=ParcelFileDescriptor.open(f,ParcelFileDescriptor.MODE_READ_ONLY);PdfRenderer renderer=new PdfRenderer(pfd);LinearLayout pages=new LinearLayout(this);pages.setOrientation(LinearLayout.VERTICAL);
        int max=Math.min(renderer.getPageCount(),20);for(int i=0;i<max;i++){PdfRenderer.Page p=renderer.openPage(i);int w=900,h=Math.max(1,(int)(w*((float)p.getHeight()/p.getWidth())));Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.RGB_565);b.eraseColor(Color.WHITE);p.render(b,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);p.close();ImageView iv=new ImageView(this);iv.setImageBitmap(b);iv.setAdjustViewBounds(true);pages.addView(iv,new LinearLayout.LayoutParams(-1,-2));}
        renderer.close();pfd.close();ScrollView sv=new ScrollView(this);sv.addView(pages);new AlertDialog.Builder(this).setTitle("KAYIT FORMU • PDF").setView(sv).setPositiveButton("KAPAT",null).show();
    }

    @Override void goBack(){if("MISSING_FORMS".equals(page)){showHome();return;}super.goBack();}
}

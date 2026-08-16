package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.view.*;
import android.widget.*;
import org.json.JSONObject;
import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

public class MainActivityV412 extends MainActivityV411 {
    private static final int REQ_PHOTO_412=4121, REQ_FORM_412=4122;
    private final ExecutorService media412=Executors.newFixedThreadPool(2);
    private long mediaTarget412=-1;
    private boolean uploadingLocal412=false;

    @Override void base(String title,boolean back){
        super.base(title==null?"":title.toUpperCase(new Locale("tr","TR")),back);
        root.postDelayed(()->uppercaseTree412(root),80);
    }

    private void uppercaseTree412(View v){
        if(v instanceof EditText){
            EditText e=(EditText)v;
            String h=e.getHint()==null?"":e.getHint().toString();
            if(!h.isEmpty())e.setHint(h.toUpperCase(new Locale("tr","TR")));
            String s=e.getText().toString();
            if(!s.isEmpty()&&!s.matches("[0-9 ()+./:-]+")){
                int p=e.getSelectionStart();String u=s.toUpperCase(new Locale("tr","TR"));if(!u.equals(s)){e.setText(u);e.setSelection(Math.min(Math.max(0,p),e.length()));}
            }
            if(!isNumericLike412(e)){
                InputFilter[] old=e.getFilters();InputFilter[] n=Arrays.copyOf(old,old.length+1);n[old.length]=new InputFilter.AllCaps();e.setFilters(n);
            }
            return;
        }
        if(v instanceof TextView){TextView t=(TextView)v;String s=String.valueOf(t.getText());if(!s.isEmpty())t.setText(s.toUpperCase(new Locale("tr","TR")));}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)uppercaseTree412(g.getChildAt(i));}
    }
    private boolean isNumericLike412(EditText e){String h=e.getHint()==null?"":e.getHint().toString().toUpperCase(new Locale("tr","TR"));return h.contains("TELEFON")||h.contains("TARİH")||h.contains("AİDAT")||h.contains("TUTAR")||h.contains("ADET")||h.contains("TCKN")||h.contains("FİYAT");}

    @Override void form(long id){
        super.form(id);
        patchEndDateClear412(root);
        if(id>0)patchPhotoButtons412(root,id);
        root.postDelayed(()->uppercaseTree412(root),60);
    }

    private void patchEndDateClear412(View v){
        EditText end=findEditByHint412(v,"BİTİŞ");if(end==null)return;
        ViewParent vp=end.getParent();if(!(vp instanceof LinearLayout))return;LinearLayout p=(LinearLayout)vp;
        for(int i=0;i<p.getChildCount();i++)if(p.getChildAt(i) instanceof Button&&String.valueOf(((Button)p.getChildAt(i)).getText()).contains("BİTİŞ TARİHİNİ TEMİZLE"))return;
        Button clear=btn("BİTİŞ TARİHİNİ TEMİZLE");clear.setBackground(round(Color.rgb(235,235,235),10));clear.setOnClickListener(x->{end.setText("");toast("BİTİŞ TARİHİ TEMİZLENDİ. DEĞİŞİKLİKLERİ KAYDEDİN.");});
        int pos=p.indexOfChild(end);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(46));lp.setMargins(0,dp(3),0,dp(5));p.addView(clear,Math.min(pos+1,p.getChildCount()),lp);
    }
    private EditText findEditByHint412(View v,String term){
        if(v instanceof EditText){String h=((EditText)v).getHint()==null?"":((EditText)v).getHint().toString().toUpperCase(new Locale("tr","TR"));if(h.contains(term))return (EditText)v;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){EditText e=findEditByHint412(g.getChildAt(i),term);if(e!=null)return e;}}return null;
    }

    @Override void showProfile(long id){
        super.showProfile(id);
        patchCloudFormButtons412(root,id);
        patchPhoneLinks412(root);
        autoUploadLocalPhoto412(id);
        root.postDelayed(()->uppercaseTree412(root),50);
    }

    private void patchPhoneLinks412(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;String s=String.valueOf(t.getText());Matcher m=Pattern.compile("0\\s*\\(5\\d{2}\\)\\s*\\d{3}\\s*\\d{2}\\s*\\d{2}|05\\d{9}").matcher(s);
            if(m.find()){String digits=m.group().replaceAll("[^0-9]","");if(digits.length()==11){t.setTextColor(Color.BLACK);t.setClickable(true);t.setOnClickListener(x->{Intent i=new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+digits));startActivity(i);});}}
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchPhoneLinks412(g.getChildAt(i));}
    }

    @SuppressWarnings("unchecked") private ConcurrentHashMap<Long,String> photoMap412(){try{Field f=MainActivityV405.class.getDeclaredField("photoPath");f.setAccessible(true);return (ConcurrentHashMap<Long,String>)f.get(this);}catch(Exception e){return new ConcurrentHashMap<>();}}
    @SuppressWarnings("unchecked") private ConcurrentHashMap<Long,String> formMap412(){try{Field f=MainActivityV405.class.getDeclaredField("formPath");f.setAccessible(true);return (ConcurrentHashMap<Long,String>)f.get(this);}catch(Exception e){return new ConcurrentHashMap<>();}}

    private void patchPhotoButtons412(View v,long id){
        if(v instanceof Button){Button b=(Button)v;String s=String.valueOf(b.getText()).toUpperCase(new Locale("tr","TR"));if(s.contains("FOTOĞRAF EKLE")||s.contains("FOTOĞRAFI DEĞİŞTİR"))b.setOnClickListener(x->{mediaTarget412=id;pickImage412(REQ_PHOTO_412);});}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchPhotoButtons412(g.getChildAt(i),id);}
    }
    private void patchCloudFormButtons412(View v,long id){
        if(v instanceof Button){Button b=(Button)v;String s=String.valueOf(b.getText()).toUpperCase(new Locale("tr","TR"));if(s.contains("KAYIT FORMU EKLE")||s.contains("KAYIT FORMUNU GÜNCELLE")){b.setOnClickListener(x->{mediaTarget412=id;pickImage412(REQ_FORM_412);});}}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchCloudFormButtons412(g.getChildAt(i),id);}
    }
    private void pickImage412(int req){Intent i;if(Build.VERSION.SDK_INT>=33){i=new Intent(MediaStore.ACTION_PICK_IMAGES);i.setType("image/*");}else{i=new Intent(Intent.ACTION_PICK);i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");}startActivityForResult(i,req);}

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==REQ_PHOTO_412||requestCode==REQ_FORM_412){long id=mediaTarget412;mediaTarget412=-1;if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null&&id>0)uploadMedia412(id,data.getData(),requestCode==REQ_PHOTO_412);else if(id>0)showProfile(id);return;}
        super.onActivityResult(requestCode,resultCode,data);
    }

    private void autoUploadLocalPhoto412(long id){
        if(uploadingLocal412||photoMap412().containsKey(id))return;Cursor c=db.athlete(id);String p="";if(c.moveToFirst())p=v(c,"photo");c.close();if(p==null||!p.startsWith("USER:"))return;
        File f=new File(new File(getFilesDir(),"athlete_photos"),p.substring(5));if(!f.isFile())return;uploadingLocal412=true;
        media412.execute(()->{try{byte[] jpg=jpegFile412(f,1600,84);saveMediaBytes412(id,jpg,true);runOnUiThread(()->toast("YENİ SPORCU FOTOĞRAFI BULUTA KAYDEDİLDİ."));}catch(Exception ignored){}finally{uploadingLocal412=false;}});
    }
    private void uploadMedia412(long id,Uri uri,boolean photo){
        media412.execute(()->{try{byte[] jpg=jpegUri412(uri,photo?1600:2400,photo?84:82);saveMediaBytes412(id,jpg,photo);runOnUiThread(()->{toast(photo?"SPORCU FOTOĞRAFI BULUTA KAYDEDİLDİ.":"KAYIT FORMU BULUTA KAYDEDİLDİ.");showProfile(id);});}catch(Exception e){runOnUiThread(()->{toast(photo?"FOTOĞRAF YÜKLENEMEDİ.":"KAYIT FORMU YÜKLENEMEDİ.");showProfile(id);});}});
    }
    private void saveMediaBytes412(long id,byte[] jpg,boolean photo)throws Exception{
        String token=cloudPrefs==null?"":cloudPrefs.getString("access_token","");if(token.isEmpty())throw new IOException("Oturum yok");
        String bucket=photo?"athlete-photos":"registration-forms",path="user/"+id+"/"+(photo?"photo_":"form_")+System.currentTimeMillis()+".jpg";
        if(!storagePut412(bucket,path,jpg,token))throw new IOException("storage");
        JSONObject body=new JSONObject().put("p_legacy_id",id).put("p_kind",photo?"photo":"form").put("p_path",path);
        HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/set_athlete_media_path",body.toString(),token);if(r.code<200||r.code>=300)throw new IOException("path");
        if(photo){photoMap412().put(id,path);db.getWritableDatabase().execSQL("UPDATE athletes SET photo=? WHERE id=?",new Object[]{"CLOUD:"+path,id});}else formMap412().put(id,path);
    }
    private boolean storagePut412(String bucket,String path,byte[] data,String token)throws Exception{
        URL u=new URL(SUPABASE_URL+"/storage/v1/object/"+bucket+"/"+encPath412(path));HttpURLConnection h=(HttpURLConnection)u.openConnection();h.setRequestMethod("POST");h.setDoOutput(true);h.setConnectTimeout(15000);h.setReadTimeout(30000);h.setRequestProperty("apikey",SUPABASE_KEY);h.setRequestProperty("Authorization","Bearer "+token);h.setRequestProperty("Content-Type","image/jpeg");h.setRequestProperty("x-upsert","true");try(OutputStream o=h.getOutputStream()){o.write(data);}int c=h.getResponseCode();h.disconnect();return c/100==2;
    }
    private String encPath412(String p)throws Exception{StringBuilder b=new StringBuilder();String[] z=p.split("/");for(int i=0;i<z.length;i++){if(i>0)b.append('/');b.append(URLEncoder.encode(z[i],"UTF-8").replace("+","%20"));}return b.toString();}
    private byte[] jpegUri412(Uri uri,int target,int quality)throws Exception{BitmapFactory.Options b=new BitmapFactory.Options();b.inJustDecodeBounds=true;try(InputStream in=getContentResolver().openInputStream(uri)){BitmapFactory.decodeStream(in,null,b);}int s=1;while(b.outWidth>0&&b.outHeight>0&&(b.outWidth/s>target*2||b.outHeight/s>target*2))s*=2;BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=Math.max(1,s);Bitmap bm;try(InputStream in=getContentResolver().openInputStream(uri)){bm=BitmapFactory.decodeStream(in,null,o);}if(bm==null)throw new IOException();ByteArrayOutputStream out=new ByteArrayOutputStream();bm.compress(Bitmap.CompressFormat.JPEG,quality,out);bm.recycle();return out.toByteArray();}
    private byte[] jpegFile412(File f,int target,int quality)throws Exception{Bitmap bm=BitmapFactory.decodeFile(f.getAbsolutePath());if(bm==null)throw new IOException();int w=bm.getWidth(),h=bm.getHeight();if(Math.max(w,h)>target){float sc=(float)target/Math.max(w,h);Bitmap sm=Bitmap.createScaledBitmap(bm,Math.max(1,Math.round(w*sc)),Math.max(1,Math.round(h*sc)),true);if(sm!=bm)bm.recycle();bm=sm;}ByteArrayOutputStream out=new ByteArrayOutputStream();bm.compress(Bitmap.CompressFormat.JPEG,quality,out);bm.recycle();return out.toByteArray();}

    @Override void editPayment(long id,int month,int fee,String marker,int amount){
        Calendar now=Calendar.getInstance();if(now.get(Calendar.YEAR)==2026&&month>Math.min(12,now.get(Calendar.MONTH)+2)){toast("ERKEN ÖDEME SADECE BİR SONRAKİ AY İÇİN GİRİLEBİLİR.");return;}
        final String[] opts={"ÖDEME GİR","ARA VERDİ (X)","FARKLI TUTAR (!)","KAYDI TEMİZLE"};
        new AlertDialog.Builder(this).setTitle(monthName(month)+" 2026").setItems(opts,(d,w)->{
            if(w==1){db.updatePayment(id,month,"X",0);showProfile(id);return;}if(w==3){db.updatePayment(id,month,"",0);showProfile(id);return;}
            LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(dp(20),dp(6),dp(20),0);
            Button date=btn("ÖDEME TARİHİ SEÇ");final String[] iso={todayIso412()};if(marker!=null&&marker.matches("\\d{4}-\\d{2}-\\d{2}"))iso[0]=marker;date.setText("ÖDEME TARİHİ: "+dateTr(iso[0]));date.setOnClickListener(v->pickPaymentDate412(date,iso));
            EditText am=new EditText(this);am.setHint("TUTAR ₺");am.setInputType(2);am.setText(String.valueOf(amount>0?amount:fee));x.addView(date);x.addView(am);
            new AlertDialog.Builder(this).setTitle(w==2?"FARKLI TUTAR":"AİDAT ÖDEMESİ").setView(x).setPositiveButton("KAYDET",(a,z)->{int val=parseInt(am.getText().toString());db.updatePayment(id,month,w==2?"!":iso[0],val);showProfile(id);}).setNegativeButton("VAZGEÇ",null).show();
        }).show();
    }
    private String todayIso412(){Calendar c=Calendar.getInstance();return String.format(Locale.US,"%04d-%02d-%02d",c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH));}
    private void pickPaymentDate412(Button b,String[] iso){Calendar c=Calendar.getInstance();try{String[] p=iso[0].split("-");c.set(Integer.parseInt(p[0]),Integer.parseInt(p[1])-1,Integer.parseInt(p[2]));}catch(Exception ignored){}new DatePickerDialog(this,(v,y,m,d)->{iso[0]=String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d);b.setText("ÖDEME TARİHİ: "+dateTr(iso[0]));},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}

    @Override void goBack(){if("HOME".equals(page)){confirmExit412();return;}super.goBack();}
    @Override public void onBackPressed(){if("HOME".equals(page)){confirmExit412();return;}super.onBackPressed();}
    private void confirmExit412(){new AlertDialog.Builder(this).setTitle("ÇIKIŞ?").setMessage("UYGULAMADAN ÇIKMAK İSTİYOR MUSUNUZ?").setPositiveButton("EVET",(d,w)->finish()).setNegativeButton("HAYIR",null).show();}

    @Override protected void onDestroy(){media412.shutdownNow();super.onDestroy();}
}

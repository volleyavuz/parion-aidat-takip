package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.text.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

public class MainActivityV390 extends MainActivityV389 {
    private static final String DELETED_PAGE="DELETED";
    private final SimpleDateFormat isoUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

    @Override public void onCreate(android.os.Bundle b){
        isoUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
        super.onCreate(b);
        ensureRecycleColumns();
        normalizeAllLocalPhones();
        purgeExpiredLocal();
        syncDeletedFromCloud(false);
        showHome();
    }

    private boolean hasColumn(String col){
        try{Cursor c=db.getReadableDatabase().rawQuery("PRAGMA table_info(athletes)",null);while(c.moveToNext()){if(col.equalsIgnoreCase(c.getString(c.getColumnIndexOrThrow("name")))){c.close();return true;}}c.close();}catch(Exception ignored){}
        return false;
    }
    private void ensureRecycleColumns(){
        try{if(!hasColumn("deletedAt"))db.getWritableDatabase().execSQL("ALTER TABLE athletes ADD COLUMN deletedAt TEXT");}catch(Exception ignored){}
        try{if(!hasColumn("deletedPrevStatus"))db.getWritableDatabase().execSQL("ALTER TABLE athletes ADD COLUMN deletedPrevStatus TEXT");}catch(Exception ignored){}
    }

    private String phoneDigits(String raw){
        String x=raw==null?"":raw.replaceAll("[^0-9]","");
        if(x.startsWith("90")&&x.length()==12)x="0"+x.substring(2);
        if(x.length()==10&&x.startsWith("5"))x="0"+x;
        if(x.length()>11)x=x.substring(0,11);
        return x;
    }
    private String savedPhone(String raw){String x=phoneDigits(raw);return x.length()==11&&x.startsWith("05")?x:"";}
    private String displayPhone(String raw){
        String x=phoneDigits(raw); if(x.length()!=11||!x.startsWith("05"))return "";
        return "0 ("+x.substring(1,4)+") "+x.substring(4,7)+" "+x.substring(7,9)+" "+x.substring(9,11);
    }
    private void normalizeAllLocalPhones(){
        try{
            SQLiteDatabase d=db.getWritableDatabase();Cursor c=d.rawQuery("SELECT id,phone,motherPhone,fatherPhone FROM athletes",null);d.beginTransaction();
            while(c.moveToNext()){
                ContentValues v=new ContentValues();long id=c.getLong(0);
                String p=savedPhone(c.getString(1)),m=savedPhone(c.getString(2)),f=savedPhone(c.getString(3));
                if(!p.equals(c.getString(1)==null?"":c.getString(1)))v.put("phone",p);
                if(!m.equals(c.getString(2)==null?"":c.getString(2)))v.put("motherPhone",m);
                if(!f.equals(c.getString(3)==null?"":c.getString(3)))v.put("fatherPhone",f);
                if(v.size()>0)d.update("athletes",v,"id=?",new String[]{String.valueOf(id)});
            }c.close();d.setTransactionSuccessful();d.endTransaction();
        }catch(Exception ignored){}
    }

    @Override void form(long id){
        super.form(id);
        ArrayList<EditText> phones=new ArrayList<>(); collectPhoneFields(root,phones);
        for(EditText e:phones)configurePrettyPhoneField(e);
        Button save=findSaveButton(root);
        if(save!=null)save.setOnTouchListener((v,event)->{if(event.getAction()==android.view.MotionEvent.ACTION_DOWN){for(EditText e:phones)e.setText(savedPhone(e.getText().toString()));}return false;});
    }
    private void collectPhoneFields(View v,ArrayList<EditText> out){
        if(v instanceof EditText){EditText e=(EditText)v;String h=e.getHint()==null?"":e.getHint().toString().toUpperCase(new Locale("tr","TR"));if(h.contains("TELEFON"))out.add(e);return;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectPhoneFields(g.getChildAt(i),out);}
    }
    private void configurePrettyPhoneField(EditText e){
        e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(17)});
        String raw=phoneDigits(e.getText().toString());
        if(raw.isEmpty())raw="05";
        e.setText(raw.length()==11?displayPhone(raw):raw);e.setSelection(e.length());
        final boolean[] busy={false};
        e.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){if(busy[0])return;String d=phoneDigits(s.toString());if(d.length()>11)d=d.substring(0,11);String n=d.length()==11?displayPhone(d):d;if(!n.equals(s.toString())){busy[0]=true;e.setText(n);e.setSelection(e.length());busy[0]=false;}}public void afterTextChanged(Editable x){}});
        e.setOnFocusChangeListener((v,has)->{if(has){String d=phoneDigits(e.getText().toString());e.setText(d.isEmpty()?"05":d);e.setSelection(e.length());}else{String d=savedPhone(e.getText().toString());e.setText(d.isEmpty()?(phoneDigits(e.getText().toString()).length()<=2?"05":""):displayPhone(d));e.setSelection(e.length());}});
    }
    private Button findSaveButton(View v){
        if(v instanceof Button){String t=String.valueOf(((Button)v).getText()).toUpperCase(new Locale("tr","TR"));if(t.contains("KAYDI OLUŞTUR")||t.contains("DEĞİŞİKLİKLERİ KAYDET"))return (Button)v;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){Button b=findSaveButton(g.getChildAt(i));if(b!=null)return b;}}return null;
    }

    @Override void showProfile(long id){
        super.showProfile(id);
        Cursor c=db.athlete(id);if(!c.moveToFirst()){c.close();return;}
        String cat=v(c,"category"),status=v(c,"status"),athletePhone=v(c,"phone");c.close();
        replaceCategoryStatus(root,cat,status);
        formatPhoneTexts(root);
        if(savedPhone(athletePhone).isEmpty())removeEmptyPhoneRow(root,"SPORCU TEL");
    }
    private void formatPhoneTexts(View v){
        if(v instanceof TextView){TextView t=(TextView)v;String s=String.valueOf(t.getText());Matcher m=Pattern.compile("(?<!\\d)(?:0?5\\d{9})(?!\\d)").matcher(s);StringBuffer b=new StringBuffer();boolean found=false;while(m.find()){String d=displayPhone(m.group());if(d.isEmpty())continue;m.appendReplacement(b,Matcher.quoteReplacement(d));found=true;}if(found){m.appendTail(b);t.setText(b.toString());String raw=extractFirstMobile(b.toString());if(raw!=null){t.setClickable(true);t.setTextColor(Color.rgb(0,90,180));t.setOnClickListener(x->openDialer(raw));}}}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)formatPhoneTexts(g.getChildAt(i));}
    }
    private String extractFirstMobile(String s){Matcher m=Pattern.compile("0\\s*\\(5\\d{2}\\)\\s*\\d{3}\\s*\\d{2}\\s*\\d{2}").matcher(s);if(!m.find())return null;return savedPhone(m.group());}
    private void removeEmptyPhoneRow(View v,String label){
        if(!(v instanceof ViewGroup))return;ViewGroup g=(ViewGroup)v;
        for(int i=g.getChildCount()-1;i>=0;i--){View child=g.getChildAt(i);if(child instanceof LinearLayout){LinearLayout r=(LinearLayout)child;if(r.getChildCount()>=2&&r.getChildAt(0) instanceof TextView&&label.equalsIgnoreCase(String.valueOf(((TextView)r.getChildAt(0)).getText()).trim())){String val=r.getChildAt(1) instanceof TextView?String.valueOf(((TextView)r.getChildAt(1)).getText()).trim():"";if(val.isEmpty()||"—".equals(val)){g.removeViewAt(i);continue;}}}removeEmptyPhoneRow(child,label);}
    }
    private void replaceCategoryStatus(View v,String cat,String status){
        if(!(v instanceof ViewGroup))return;ViewGroup g=(ViewGroup)v;
        String target=cat+" • "+status;
        for(int i=0;i<g.getChildCount();i++){
            View ch=g.getChildAt(i);
            if(ch instanceof TextView&&target.equals(String.valueOf(((TextView)ch).getText()))){
                LinearLayout badges=new LinearLayout(this);badges.setGravity(Gravity.CENTER);badges.setOrientation(LinearLayout.HORIZONTAL);badges.setPadding(0,dp(4),0,dp(4));
                TextView group=tv(cat,12,groupColor(cat),true);group.setGravity(Gravity.CENTER);group.setTextColor(contrastText(groupColor(cat)));group.setBackground(round(groupColor(cat),10));
                TextView stat=tv(status,12,statusColor(status),true);stat.setGravity(Gravity.CENTER);int sc="AKTİF".equalsIgnoreCase(status)?GREEN:statusColor(status);stat.setTextColor(contrastText(sc));stat.setBackground(round(sc,10));
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,dp(38));p.setMargins(dp(3),0,dp(3),0);badges.addView(group,p);badges.addView(stat,p);g.removeViewAt(i);g.addView(badges,i);return;
            }
            replaceCategoryStatus(ch,cat,status);
        }
    }
    private int contrastText(int c){double y=(0.299*Color.red(c)+0.587*Color.green(c)+0.114*Color.blue(c));return y>155?Color.BLACK:Color.WHITE;}
    private int groupColor(String cat){
        String x=cat==null?"":cat.toUpperCase(new Locale("tr","TR"));
        if(x.contains("SO 1"))return Color.rgb(0,122,204);
        if(x.contains("SO 2"))return Color.rgb(126,87,194);
        if(x.contains("SO 3"))return Color.rgb(239,108,0);
        if(x.contains("MİNİ"))return Color.rgb(216,27,96);
        if(x.contains("MİDİ"))return Color.rgb(0,137,123);
        if(x.contains("KÜÇÜK"))return Color.rgb(245,166,35);
        if(x.contains("YILDIZ"))return Color.rgb(94,53,177);
        if(x.contains("GENÇ"))return Color.rgb(198,40,40);
        int[] p={Color.rgb(2,119,189),Color.rgb(0,121,107),Color.rgb(123,31,162),Color.rgb(230,81,0),Color.rgb(46,125,50)};return p[Math.abs(x.hashCode())%p.length];
    }

    @Override void showHome(){
        super.showHome();
        try{
            if(!hasColumn("deletedAt"))return;
            int n=countDeleted();Button b=btn("SİLİNEN SPORCULAR ("+n+")");b.setBackground(round(Color.rgb(225,225,225),12));b.setOnClickListener(v->showDeletedAthletes());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(56));lp.setMargins(dp(8),dp(4),dp(8),dp(8));root.addView(b,lp);
        }catch(Exception ignored){}
    }
    private int countDeleted(){Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE TRIM(COALESCE(deletedAt,''))<>''",null);c.moveToFirst();int n=c.getInt(0);c.close();return n;}

    @Override void load(LinearLayout b,String q,String stat,String cat,String by,String note,int sort){
        b.removeAllViews();Cursor c=db.athletes(q,stat.startsWith("TÜM")?"TÜMÜ":stat);ArrayList<A> xs=new ArrayList<>();int y=0;try{y=Integer.parseInt(by.trim());}catch(Exception ignored){}
        while(c.moveToNext()){int di=c.getColumnIndex("deletedAt");if(di>=0&&!c.isNull(di)&&!c.getString(di).trim().isEmpty())continue;A x=a(c);if(!cat.startsWith("TÜM")&&!cat.equalsIgnoreCase(x.cat))continue;if(y>0&&x.by!=y)continue;if(!note.startsWith("TÜM")&&!note.trim().equals(x.notes==null?"":x.notes.trim()))continue;xs.add(x);}c.close();Collections.sort(xs,cmp(sort));for(A x:xs)row(b,x,null,0);if(xs.isEmpty())b.addView(tv("FİLTRELERE UYGUN SPORCU BULUNAMADI",14,Color.DKGRAY,true));
    }

    @Override DashData dashboardData(){
        DashData d=super.dashboardData();HashSet<Long> deleted=new HashSet<>();
        try{Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes WHERE TRIM(COALESCE(deletedAt,''))<>''",null);while(c.moveToNext())deleted.add(c.getLong(0));c.close();}catch(Exception ignored){}
        d.collectedTotal=removeDeleted(d.collected,deleted);d.expectedTotal=removeDeleted(d.expected,deleted);d.overdueTotal=removeDeleted(d.overdue,deleted);d.upcomingTotal=removeDeleted(d.upcoming,deleted);return d;
    }
    private int removeDeleted(LinkedHashMap<Long,DashItem> map,Set<Long> deleted){Iterator<Map.Entry<Long,DashItem>> it=map.entrySet().iterator();int total=0;while(it.hasNext()){Map.Entry<Long,DashItem> e=it.next();if(deleted.contains(e.getKey()))it.remove();else total+=e.getValue().amount;}return total;}

    @Override void confirmDelete(long id){
        Cursor c=db.athlete(id);String name="SPORCU";if(c.moveToFirst())name=v(c,"name");c.close();String n=name;
        new AlertDialog.Builder(this).setTitle("SPORCUYU SİL").setMessage(n+" Silinen Sporcular havuzuna taşınsın mı? Aidat ve diğer geçmişi 1 yıl boyunca korunacaktır.").setPositiveButton("HAVUZA TAŞI",(d,w)->del(id)).setNegativeButton("VAZGEÇ",null).show();
    }
    @Override void del(long id){
        ensureRecycleColumns();Cursor c=db.athlete(id);String prev="AKTİF";if(c.moveToFirst())prev=v(c,"status");c.close();String at=isoUtc.format(new Date());
        ContentValues cv=new ContentValues();cv.put("deletedAt",at);cv.put("deletedPrevStatus",prev);cv.put("status","SİLİNDİ");db.getWritableDatabase().update("athletes",cv,"id=?",new String[]{String.valueOf(id)});
        cloudSetDeleted(id,at);Toast.makeText(this,"Sporcu Silinen Sporcular havuzuna taşındı.",Toast.LENGTH_SHORT).show();showAthletes();
    }

    private void showDeletedAthletes(){
        page=DELETED_PAGE;base("SİLİNEN SPORCULAR",true);TextView info=tv("Silinen kayıtlar 1 yıl boyunca burada tutulur. Süre dolunca sistem otomatik olarak kalıcı siler.",12,Color.DKGRAY,false);root.addView(info);
        ScrollView sv=scroll();LinearLayout b=box(sv);loadDeletedRows(b);syncDeletedFromCloud(true);
    }
    private void loadDeletedRows(LinearLayout b){
        b.removeAllViews();Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))<>'' ORDER BY deletedAt DESC",null);int n=0;
        while(c.moveToNext()){long id=c.getLong(c.getColumnIndexOrThrow("id"));String name=v(c,"name"),cat=v(c,"category"),photo=v(c,"photo"),at=v(c,"deletedAt");int by=c.getInt(c.getColumnIndexOrThrow("birthYear"));
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(8),dp(8),dp(8),dp(8));row.setBackground(round(Color.WHITE,10));ImageView av=new ImageView(this);av.setScaleType(ImageView.ScaleType.CENTER_CROP);setAthletePhoto(av,photo);row.addView(av,new LinearLayout.LayoutParams(dp(58),dp(58)));
            LinearLayout text=new LinearLayout(this);text.setOrientation(LinearLayout.VERTICAL);text.addView(tv((by>0?by+" • ":"")+name,14,BLACK,true));text.addView(tv(cat+" • Silinme: "+deletedDateLabel(at),11,Color.DKGRAY,false));text.addView(tv(remainingLabel(at),11,RED,true));row.addView(text,new LinearLayout.LayoutParams(0,-2,1));Button restore=btn("GERİ YÜKLE");restore.setOnClickListener(v->confirmRestore(id,name));row.addView(restore,new LinearLayout.LayoutParams(dp(115),dp(50)));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(row,lp);n++;}
        c.close();if(n==0)b.addView(tv("Silinen sporcu bulunmuyor.",14,Color.DKGRAY,true));
    }
    private String deletedDateLabel(String at){try{Date d=isoUtc.parse(at);return new SimpleDateFormat("dd.MM.yyyy HH:mm",TR).format(d);}catch(Exception e){return at;}}
    private String remainingLabel(String at){try{Date d=isoUtc.parse(at);Calendar purge=Calendar.getInstance();purge.setTime(d);purge.add(Calendar.YEAR,1);long ms=purge.getTimeInMillis()-System.currentTimeMillis();long days=Math.max(0,(ms+86399999L)/86400000L);return "Kalıcı silinmeye yaklaşık "+days+" gün";}catch(Exception e){return "1 yıl sonra kalıcı silinir";}}
    private void confirmRestore(long id,String name){new AlertDialog.Builder(this).setTitle("SPORCUYU GERİ YÜKLE").setMessage(name+" yeniden aktif kayıtlara alınsın mı?").setPositiveButton("GERİ YÜKLE",(d,w)->restoreAthlete(id)).setNegativeButton("VAZGEÇ",null).show();}
    private void restoreAthlete(long id){Cursor c=db.athlete(id);String prev="AKTİF";if(c.moveToFirst()){String p=v(c,"deletedPrevStatus");if(!p.isEmpty())prev=p;}c.close();ContentValues cv=new ContentValues();cv.put("deletedAt","");cv.put("deletedPrevStatus","");cv.put("status",prev);db.getWritableDatabase().update("athletes",cv,"id=?",new String[]{String.valueOf(id)});cloudSetDeleted(id,null);Toast.makeText(this,"Sporcu geri yüklendi.",Toast.LENGTH_SHORT).show();showDeletedAthletes();}

    private void cloudSetDeleted(long id,String deletedAt){
        if(cloudPrefs==null)return;String token=cloudPrefs.getString("access_token","");if(token.isEmpty())return;
        new Thread(()->{try{JSONObject j=new JSONObject();if(deletedAt==null)j.put("deleted_at",JSONObject.NULL);else j.put("deleted_at",deletedAt);HttpResult r=request("PATCH",SUPABASE_URL+"/rest/v1/athletes?legacy_id=eq."+id,j.toString(),token);if(r.code<200||r.code>=300)runOnUiThread(()->Toast.makeText(this,"Online silme/geri yükleme eşitlenemedi ("+r.code+").",Toast.LENGTH_LONG).show());}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Online eşitleme hatası: "+shortMsg(e),Toast.LENGTH_LONG).show());}}).start();
    }

    private void syncDeletedFromCloud(boolean refreshUi){
        if(cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty())return;
        new Thread(()->{try{HttpResult r=getAuthed("/rest/v1/mobile_deleted_athletes?select=*");if(r.code<200||r.code>=300)return;JSONArray a=new JSONArray(r.body);SQLiteDatabase d=db.getWritableDatabase();for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id<=0)continue;Cursor c=db.athlete(id);if(c.moveToFirst()){String current=v(c,"status");String oldPrev=v(c,"deletedPrevStatus");ContentValues cv=new ContentValues();cv.put("deletedAt",o.optString("deleted_at",""));cv.put("deletedPrevStatus",oldPrev.isEmpty()&& !"SİLİNDİ".equals(current)?current:o.optString("status","AKTİF"));cv.put("status","SİLİNDİ");d.update("athletes",cv,"id=?",new String[]{String.valueOf(id)});}c.close();}if(refreshUi)runOnUiThread(()->{if(DELETED_PAGE.equals(page)){LinearLayout b=findBox(root);if(b!=null)loadDeletedRows(b);}});}catch(Exception ignored){}}).start();
    }

    @Override int[] applyCloudCache(JSONArray athletes,JSONArray payments)throws Exception{
        int[] r=super.applyCloudCache(athletes,payments);SQLiteDatabase d=db.getWritableDatabase();for(int i=0;i<athletes.length();i++){JSONObject o=athletes.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id<=0)continue;ContentValues cv=new ContentValues();cv.put("deletedAt","");cv.put("deletedPrevStatus","");if(o.has("tckn")&&!o.isNull("tckn"))cv.put("tckn",o.optString("tckn",""));d.update("athletes",cv,"id=?",new String[]{String.valueOf(id)});}return r;
    }

    private void purgeExpiredLocal(){
        if(!hasColumn("deletedAt"))return;Calendar threshold=Calendar.getInstance(TimeZone.getTimeZone("UTC"));threshold.add(Calendar.YEAR,-1);String t=isoUtc.format(threshold.getTime());SQLiteDatabase d=db.getWritableDatabase();Cursor c=d.rawQuery("SELECT id FROM athletes WHERE TRIM(COALESCE(deletedAt,''))<>'' AND deletedAt<=?",new String[]{t});ArrayList<Long> ids=new ArrayList<>();while(c.moveToNext())ids.add(c.getLong(0));c.close();if(ids.isEmpty())return;d.beginTransaction();try{for(long id:ids){d.delete("payments","athleteId=?",new String[]{String.valueOf(id)});d.delete("fee_history","athleteId=?",new String[]{String.valueOf(id)});d.delete("athletes","id=?",new String[]{String.valueOf(id)});}d.setTransactionSuccessful();}finally{d.endTransaction();}
    }

    @Override public void onBackPressed(){if(DELETED_PAGE.equals(page)){showHome();return;}super.onBackPressed();}
}

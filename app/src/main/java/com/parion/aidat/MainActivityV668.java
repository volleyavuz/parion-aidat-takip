package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.68 - targeted dashboard additions on the ANR-safe path. */
public class MainActivityV668 extends MainActivityV667 {
    private static final int GOLD668=Color.rgb(205,156,34);

    @Override void showHome(){
        super.showHome();
        if(root!=null)root.post(this::patch668);
    }

    private void patch668(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        renameWinter668(root);
        ensureCurrentStarts668();
        addDueNowCard668();
    }

    private void renameWinter668(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;
            String n=norm668(String.valueOf(t.getText()));
            if(n.equals("KIŞIN ARANACAKLAR")||n.equals("KISIN ARANACAKLAR"))t.setText("Kışın Aranacak");
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++)renameWinter668(g.getChildAt(i));
        }
    }

    private void ensureCurrentStarts668(){
        if(findText668(root,"BU AY BAŞLAYANLAR","BU AY BAŞLAYAN SPORCULAR")!=null)return;
        View row=findTag668(root,"v662-start-row");
        if(row instanceof LinearLayout){
            ((LinearLayout)row).addView(buildCurrentStarts668(),0,cell668());
            return;
        }

        ScrollView sv=findScroll668(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        TextView previousText=findText668(box,"GEÇEN AY BAŞLAYANLAR","GEÇEN AY BAŞLAYAN SPORCULAR");
        if(previousText==null)return;
        View previous=nearestVerticalCard668(previousText,box);
        if(previous==null)return;
        View top=topChild668(box,previous);
        int idx=top==null?box.getChildCount():box.indexOfChild(top);
        ViewParent pp=previous.getParent();
        if(pp instanceof ViewGroup)((ViewGroup)pp).removeView(previous);
        if(top!=null&&top instanceof ViewGroup&&((ViewGroup)top).getChildCount()==0&&top.getParent()==box)box.removeView(top);

        LinearLayout newRow=new LinearLayout(this);
        newRow.setTag("v662-start-row");
        newRow.setOrientation(LinearLayout.HORIZONTAL);
        newRow.setGravity(Gravity.TOP);
        newRow.addView(buildCurrentStarts668(),cell668());
        newRow.addView(previous,cell668());
        LinearLayout.LayoutParams rlp=new LinearLayout.LayoutParams(-1,ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.setMargins(0,dp(3),0,dp(3));
        box.addView(newRow,Math.max(0,Math.min(idx,box.getChildCount())),rlp);
    }

    private View buildCurrentStarts668(){
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(10),dp(9),dp(10),dp(10));
        GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));card.setBackground(bg);card.setElevation(dp(1));
        TextView h=text668("BU AY BAŞLAYANLAR",10.3f,true);h.setGravity(Gravity.CENTER);h.setPadding(dp(2),dp(2),dp(2),dp(6));card.addView(h);
        ArrayList<Long> ids=new ArrayList<>();ArrayList<String> names=new ArrayList<>();
        Calendar now=Calendar.getInstance();String from=String.format(Locale.US,"%04d-%02d-01",now.get(Calendar.YEAR),now.get(Calendar.MONTH)+1);
        Calendar nx=(Calendar)now.clone();nx.set(Calendar.DAY_OF_MONTH,1);nx.add(Calendar.MONTH,1);String to=String.format(Locale.US,"%04d-%02d-01",nx.get(Calendar.YEAR),nx.get(Calendar.MONTH)+1);
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' AND startDate>=? AND startDate<? ORDER BY startDate,name COLLATE NOCASE",new String[]{from,to});
        while(c.moveToNext()){ids.add(c.getLong(0));names.add(c.getString(1)==null?"":c.getString(1));}c.close();
        if(names.isEmpty()){
            TextView e=text668("Sporcu yok",10f,false);e.setTextColor(Color.GRAY);e.setGravity(Gravity.CENTER);card.addView(e);
        }else{
            for(int i=0;i<Math.min(3,names.size());i++){
                final long id=ids.get(i);TextView n=text668("• "+names.get(i),10f,false);n.setPadding(dp(3),dp(3),dp(3),dp(3));n.setMaxLines(2);n.setOnClickListener(v->showProfile(id));card.addView(n);
            }
            if(names.size()>3){TextView more=text668("Tümünü Gör ("+names.size()+")  ›",10f,true);more.setTextColor(GOLD668);more.setGravity(Gravity.CENTER);more.setPadding(dp(3),dp(7),dp(3),dp(2));more.setOnClickListener(v->showCurrentStarts668());card.addView(more);}
        }
        return card;
    }

    private void showCurrentStarts668(){
        page="STARTS_CURRENT_668";base("BU AY BAŞLAYANLAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);
        Calendar now=Calendar.getInstance();String from=String.format(Locale.US,"%04d-%02d-01",now.get(Calendar.YEAR),now.get(Calendar.MONTH)+1);
        Calendar nx=(Calendar)now.clone();nx.set(Calendar.DAY_OF_MONTH,1);nx.add(Calendar.MONTH,1);String to=String.format(Locale.US,"%04d-%02d-01",nx.get(Calendar.YEAR),nx.get(Calendar.MONTH)+1);
        Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' AND startDate>=? AND startDate<? ORDER BY startDate,name COLLATE NOCASE",new String[]{from,to});int n=0;
        while(c.moveToNext()){row(b,a(c),null,0);n++;}c.close();if(n==0)b.addView(tv("Bu ay başlayan sporcu bulunmuyor.",14,Color.DKGRAY,true));
    }

    private void addDueNowCard668(){
        View fresh=findTag668(root,"v657-fresh");
        if(!(fresh instanceof LinearLayout))return;
        LinearLayout f=(LinearLayout)fresh;
        if(findTag668(f,"v668-due-now")!=null)return;
        int count=countDueNow668();
        LinearLayout card=new LinearLayout(this);card.setTag("v668-due-now");card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(12),dp(10),dp(12),dp(10));
        GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));card.setBackground(bg);card.setElevation(dp(2));card.setClickable(true);card.setOnClickListener(v->showDueNow668());
        ImageView icon=new ImageView(this);icon.setImageResource(android.R.drawable.ic_menu_recent_history);icon.setColorFilter(GOLD668);icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);card.addView(icon,new LinearLayout.LayoutParams(dp(24),dp(24)));
        TextView val=text668(String.valueOf(count),27f,true);val.setTextColor(GOLD668);val.setGravity(Gravity.CENTER);card.addView(val,new LinearLayout.LayoutParams(-1,-2));
        TextView title=text668("ÖDEME VADESİ GELENLER",11.2f,true);title.setGravity(Gravity.CENTER);title.setMaxLines(2);card.addView(title,new LinearLayout.LayoutParams(-1,-2));
        TextView sub=text668(count+" sporcu",9.3f,false);sub.setTextColor(Color.rgb(105,105,105));sub.setGravity(Gravity.CENTER);card.addView(sub,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(126));lp.setMargins(dp(4),0,dp(4),dp(9));
        int targetIndex=findTargetIndex668(f);
        f.addView(card,Math.min(targetIndex+1,f.getChildCount()),lp);
    }

    private int findTargetIndex668(LinearLayout f){
        for(int i=0;i<f.getChildCount();i++)if(findText668(f.getChildAt(i),"AYLIK HEDEF")!=null)return i;
        return Math.min(3,f.getChildCount()-1);
    }

    private int countDueNow668(){
        int n=0;Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes WHERE TRIM(COALESCE(deletedAt,''))=''",null);
        while(c.moveToNext())if(currentDue668(c.getLong(0))>0)n++;c.close();return n;
    }

    private int currentDue668(long id){
        Cursor ac=db.athlete(id);if(!ac.moveToFirst()){ac.close();return 0;}
        String start=s668(ac,"startDate"),end=s668(ac,"endDate"),restart=s668(ac,"restartDate"),sib=s668(ac,"sibling");ac.close();
        if("BURSLU".equalsIgnoreCase(sib)||start==null||!start.matches("\\d{4}-\\d{2}-\\d{2}"))return 0;
        Calendar today=Calendar.getInstance();int anchor=anchorDay(start),key=currentCycleKey(today,anchor);Calendar cycleStart=cycleDate(key,anchor);
        if(cycleStart.after(today))return 0;int y=key/100,m=key%100;if(!activeAt(y,m,start,end,restart))return 0;
        HashMap<Integer,PayRec> pays=paymentMap(id);PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);if("X".equals(r.marker))return 0;
        int expected=expectedFeeAt(id,y,m,r);if(expected<=0)return 0;return Math.max(0,expected-r.amount);
    }

    private void showDueNow668(){
        page="DUE_NOW_668";base("ÖDEME VADESİ GELENLER",true);ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;
        Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);
        while(c.moveToNext()){A x=a(c);int due=currentDue668(x.id);if(due<=0)continue;row(b,x,"ÖDEME VADESİ GELDİ",due);n++;}c.close();
        if(n==0)b.addView(tv("Ödeme vadesi gelen sporcu bulunmuyor.",14,Color.DKGRAY,true));
    }

    @Override void goBack(){
        if("STARTS_CURRENT_668".equals(page)||"DUE_NOW_668".equals(page)){showHome();return;}
        super.goBack();
    }

    private String s668(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}
    private LinearLayout.LayoutParams cell668(){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f);lp.setMargins(dp(3),0,dp(3),0);return lp;}
    private TextView text668(String s,float sp,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(Color.rgb(42,42,42));if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private View nearestVerticalCard668(View v,LinearLayout box){View cur=v,best=v;while(cur.getParent() instanceof View&&cur.getParent()!=box){View p=(View)cur.getParent();if(p instanceof LinearLayout){LinearLayout l=(LinearLayout)p;if(l.getOrientation()==LinearLayout.VERTICAL)best=p;if(l.getOrientation()==LinearLayout.HORIZONTAL)break;}cur=p;}return best;}
    private View topChild668(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private ScrollView findScroll668(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll668(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private View findTag668(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag668(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private TextView findText668(View v,String... needles){if(v instanceof TextView){String u=norm668(String.valueOf(((TextView)v).getText()));for(String n:needles)if(u.contains(norm668(n)))return(TextView)v;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText668(g.getChildAt(i),needles);if(r!=null)return r;}}return null;}
    private String norm668(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}

package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.84 - definitive dashboard cleanup: positional legacy Winter removal + explicit New Starters card. */
public class MainActivityV684 extends MainActivityV683 {
    private static final int GOLD684=Color.rgb(205,156,34);
    private static final int TEXT684=Color.rgb(42,42,42);
    private static final int MUTED684=Color.rgb(105,105,105);

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.post(this::patchDashboard684);
            root.postDelayed(this::patchDashboard684,260);
        }
    }

    private void patchDashboard684(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll684(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        removeWinterBelowTshirt684(box);
        ensureNewStarters684(box);
    }

    /**
     * User-visible symptom is a legacy Winter card directly below T-shirt card.
     * Remove the actual visual card/container by relative position, not by a historical tag.
     */
    private void removeWinterBelowTshirt684(LinearLayout box){
        TextView tshirt=findText684(box,"TİŞÖRT ALMAYAN","TISORT ALMAYAN");
        if(tshirt==null)return;
        View tshirtTop=topChild684(box,tshirt);
        if(tshirtTop==null)return;
        int tshirtIndex=box.indexOfChild(tshirtTop);
        if(tshirtIndex<0)return;

        ArrayList<TextView> winters=new ArrayList<>();
        collectTextHits684(box,"KIŞIN ARANACAK",winters);
        collectTextHits684(box,"KISIN ARANACAK",winters);
        LinkedHashSet<View> remove=new LinkedHashSet<>();
        for(TextView w:winters){
            View top=topChild684(box,w);
            if(top==null)continue;
            int idx=box.indexOfChild(top);
            if(idx<=tshirtIndex)continue;
            View card=nearestCard684(w,box);
            if(card!=null)remove.add(card);
        }
        for(View card:remove){
            ViewParent p=card.getParent();
            if(p instanceof ViewGroup)((ViewGroup)p).removeView(card);
        }
        cleanupEmpty684(box);
    }

    /** Always provide one stable, visible "Yeni Başlayanlar" card for the current month. */
    private void ensureNewStarters684(LinearLayout box){
        View old=findTag684(box,"v684-new-starters");
        if(old!=null){ViewParent p=old.getParent();if(p instanceof ViewGroup)((ViewGroup)p).removeView(old);}

        int count=countCurrentMonthStarts684();
        LinearLayout card=new LinearLayout(this);
        card.setTag("v684-new-starters");
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(12),dp(10),dp(12),dp(10));
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(Color.WHITE);bg.setCornerRadius(dp(15));
        card.setBackground(bg);card.setElevation(dp(1));

        ImageView icon=new ImageView(this);
        icon.setImageResource(android.R.drawable.ic_menu_add);
        icon.setColorFilter(GOLD684);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        card.addView(icon,new LinearLayout.LayoutParams(dp(24),dp(24)));

        TextView value=new TextView(this);
        value.setText(String.valueOf(count));value.setTextSize(27f);value.setTextColor(GOLD684);
        value.setTypeface(Typeface.DEFAULT,Typeface.BOLD);value.setGravity(Gravity.CENTER);
        card.addView(value,new LinearLayout.LayoutParams(-1,-2));

        TextView title=new TextView(this);
        title.setText("YENİ BAŞLAYANLAR");title.setTextSize(11f);title.setTextColor(TEXT684);
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);title.setGravity(Gravity.CENTER);
        card.addView(title,new LinearLayout.LayoutParams(-1,-2));

        TextView sub=new TextView(this);
        sub.setText("Bu ay başlayan sporcular");sub.setTextSize(9.3f);sub.setTextColor(MUTED684);sub.setGravity(Gravity.CENTER);
        card.addView(sub,new LinearLayout.LayoutParams(-1,-2));
        card.setClickable(true);card.setOnClickListener(v->showNewStarters684());

        int insert=findMovementInsert684(box);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(124));
        lp.setMargins(dp(4),dp(3),dp(4),dp(8));
        box.addView(card,Math.max(0,Math.min(insert,box.getChildCount())),lp);
    }

    private int findMovementInsert684(LinearLayout box){
        TextView heading=findText684(box,"SPORCU HAREKETLERİ");
        if(heading!=null){View top=topChild684(box,heading);if(top!=null){int i=box.indexOfChild(top);if(i>=0)return i+1;}}
        TextView follow=findText684(box,"TAKİP GEREKTİRENLER");
        if(follow!=null){View top=topChild684(box,follow);if(top!=null){int i=box.indexOfChild(top);if(i>=0)return i;}}
        return box.getChildCount();
    }

    private int countCurrentMonthStarts684(){
        String from=firstDay684(0),to=firstDay684(1);
        Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' AND startDate>=? AND startDate<?",new String[]{from,to});
        int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;
    }

    private void showNewStarters684(){
        page="LIST";base("YENİ BAŞLAYANLAR",true);
        ScrollView sv=scroll();LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(12),dp(12),dp(12),dp(24));sv.addView(box);
        String from=firstDay684(0),to=firstDay684(1);
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name,startDate FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' AND startDate>=? AND startDate<? ORDER BY startDate DESC,name COLLATE NOCASE",new String[]{from,to});
        if(!c.moveToFirst()){
            TextView empty=text684("Bu ay yeni başlayan sporcu yok.",14f,MUTED684,false);box.addView(empty,new LinearLayout.LayoutParams(-1,-2));c.close();return;
        }
        do{
            long id=c.getLong(0);String name=c.getString(1);String date=c.getString(2);
            TextView row=text684(name+(date==null||date.trim().isEmpty()?"":"\nBaşlangıç: "+date),14f,TEXT684,true);
            row.setPadding(dp(14),dp(12),dp(14),dp(12));row.setClickable(true);row.setOnClickListener(v->showProfile(id));
            GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(12));row.setBackground(bg);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(8));box.addView(row,lp);
        }while(c.moveToNext());
        c.close();
    }

    private String firstDay684(int addMonths){Calendar c=Calendar.getInstance();c.set(Calendar.DAY_OF_MONTH,1);c.add(Calendar.MONTH,addMonths);return String.format(Locale.US,"%04d-%02d-01",c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1);}
    private TextView text684(String s,float sp,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}

    private View nearestCard684(View v,LinearLayout box){
        View cur=v,best=null;
        while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box){
            if(cur.hasOnClickListeners()||cur.isClickable())best=cur;
            View p=(View)cur.getParent();
            if(p instanceof LinearLayout&&((LinearLayout)p).getOrientation()==LinearLayout.VERTICAL)best=p;
            cur=p;
        }
        if(best!=null)return best;
        return topChild684(box,v);
    }
    private void collectTextHits684(View v,String needle,List<TextView> out){if(v instanceof TextView&&norm684(String.valueOf(((TextView)v).getText())).contains(norm684(needle))&&!out.contains(v))out.add((TextView)v);if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectTextHits684(g.getChildAt(i),needle,out);}}
    private TextView findText684(View v,String... needles){if(v instanceof TextView){String n=norm684(String.valueOf(((TextView)v).getText()));for(String s:needles)if(n.contains(norm684(s)))return(TextView)v;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText684(g.getChildAt(i),needles);if(r!=null)return r;}}return null;}
    private View topChild684(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private void cleanupEmpty684(ViewGroup g){for(int i=g.getChildCount()-1;i>=0;i--){View v=g.getChildAt(i);if(v instanceof ViewGroup){cleanupEmpty684((ViewGroup)v);if(((ViewGroup)v).getChildCount()==0)g.removeViewAt(i);}}}
    private ScrollView findScroll684(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll684(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private View findTag684(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag684(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private String norm684(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}

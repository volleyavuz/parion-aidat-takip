package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.20 - dashboard placement corrections, active count and winter card frame. */
public class MainActivityV620 extends MainActivityV619 {
    private static final int GOLD620=Color.rgb(205,156,34);
    private static final int BLUE620=Color.rgb(72,103,132);
    private static final int TEXT620=Color.rgb(34,34,34);
    private static final int MUTED620=Color.rgb(112,112,112);

    @Override void showHome(){
        super.showHome();
        root.post(this::patch620);
    }

    private void patch620(){
        ScrollView sv=findScroll620(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        addActiveCount620(box);
        frameWinter620(box);
        rotateRequestedCards620(box);
        moveDeletedBottom620(box);
    }

    private void addActiveCount620(LinearLayout box){
        TextView active=findText620(box,"AKTİF SPORCU");if(active==null)return;
        View card=card620(active);if(!(card instanceof ViewGroup))return;
        if(findTag620(card,"v620-active-count")!=null)return;
        int count=0;Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE status='AKTİF' AND TRIM(COALESCE(deletedAt,''))=''",null);if(c.moveToFirst())count=c.getInt(0);c.close();
        TextView t=new TextView(this);t.setTag("v620-active-count");t.setText(count+" aktif sporcu");t.setTextSize(11f);t.setTextColor(GOLD620);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER);t.setPadding(dp(4),dp(3),dp(4),dp(2));
        ((ViewGroup)card).addView(t);
    }

    private void frameWinter620(LinearLayout box){
        TextView winter=findText620(box,"KIŞIN ARANACAK");if(winter==null)return;
        View card=card620(winter);if(card==null&&winter.getParent() instanceof View)card=(View)winter.getParent();if(card==null)return;
        GradientDrawable d=new GradientDrawable();d.setColor(Color.WHITE);d.setCornerRadius(dp(18));d.setStroke(dp(1),BLUE620);card.setBackground(d);card.setPadding(dp(10),dp(9),dp(10),dp(9));
        winter.setText("Kışın Aranacak");winter.setTextSize(10.5f);winter.setTextColor(TEXT620);winter.setGravity(Gravity.CENTER);winter.setMaxLines(2);
    }

    /**
     * Requested combined placement:
     * - Net 3-month metric takes current-starts old full-width slot.
     * - Current-starts takes the due-card slot next to previous-month starts.
     * - Due-card takes the net-metric slot next to the 90-day leavers list.
     */
    private void rotateRequestedCards620(LinearLayout box){
        TextView startsTitle=findText620(box,"BU AY BAŞLAYAN SPORCULAR");
        TextView dueTitle=findText620(box,"BU AY SONUNA KADAR ÖDEME YAPACAK","AY SONUNA KADAR GELECEK");
        TextView netTitle=findText620(box,"SON 3 AYDA");
        if(startsTitle==null||dueTitle==null||netTitle==null)return;

        View starts=directCard620(startsTitle);View due=directCard620(dueTitle);View net=directCard620(netTitle);
        if(starts==null||due==null||net==null)return;

        ViewParent startsP=starts.getParent(),dueP=due.getParent(),netP=net.getParent();
        if(!(startsP instanceof ViewGroup)||!(dueP instanceof ViewGroup)||!(netP instanceof ViewGroup))return;
        ViewGroup pStarts=(ViewGroup)startsP,pDue=(ViewGroup)dueP,pNet=(ViewGroup)netP;
        int iStarts=pStarts.indexOfChild(starts),iDue=pDue.indexOfChild(due),iNet=pNet.indexOfChild(net);
        if(iStarts<0||iDue<0||iNet<0)return;

        ViewGroup.LayoutParams startsLp=starts.getLayoutParams();ViewGroup.LayoutParams dueLp=due.getLayoutParams();ViewGroup.LayoutParams netLp=net.getLayoutParams();
        pStarts.removeView(starts);pDue.removeView(due);pNet.removeView(net);

        pStarts.addView(net,Math.min(iStarts,pStarts.getChildCount()),cloneLP620(startsLp));
        pDue.addView(starts,Math.min(iDue,pDue.getChildCount()),cloneLP620(dueLp));
        pNet.addView(due,Math.min(iNet,pNet.getChildCount()),cloneLP620(netLp));
    }

    private ViewGroup.LayoutParams cloneLP620(ViewGroup.LayoutParams src){
        if(src instanceof LinearLayout.LayoutParams){LinearLayout.LayoutParams s=(LinearLayout.LayoutParams)src;LinearLayout.LayoutParams d=new LinearLayout.LayoutParams(s.width,s.height,s.weight);d.setMargins(s.leftMargin,s.topMargin,s.rightMargin,s.bottomMargin);d.gravity=s.gravity;return d;}
        return new ViewGroup.LayoutParams(src.width,src.height);
    }

    private void moveDeletedBottom620(LinearLayout box){
        TextView deleted=findText620(box,"SİLİNEN SPORCULAR");if(deleted==null)return;
        View card=card620(deleted);if(card==null)card=deleted;
        View cur=card;while(cur.getParent() instanceof View && cur.getParent()!=box)cur=(View)cur.getParent();
        if(cur.getParent()!=box)return;
        box.removeView(cur);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(14),0,dp(6));
        box.addView(cur,lp);
    }

    private View directCard620(TextView title){
        View cur=title;View best=title;
        while(cur.getParent() instanceof View){
            View p=(View)cur.getParent();
            if(p instanceof LinearLayout){best=p;if(p.getParent() instanceof LinearLayout)break;}
            cur=p;
        }
        if(title.getParent() instanceof LinearLayout)best=(View)title.getParent();
        return best;
    }
    private View card620(View v){View cur=v,best=v;while(cur!=null&&cur!=root){if(cur.hasOnClickListeners()||cur.isClickable())best=cur;ViewParent p=cur.getParent();if(!(p instanceof View))break;cur=(View)p;}return best;}
    private TextView findText620(View v,String... needles){if(v instanceof TextView){String u=norm620(String.valueOf(((TextView)v).getText()));for(String n:needles)if(u.contains(norm620(n)))return (TextView)v;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText620(g.getChildAt(i),needles);if(r!=null)return r;}}return null;}
    private View findTag620(View v,String tag){if(tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag620(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private ScrollView findScroll620(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll620(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm620(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}

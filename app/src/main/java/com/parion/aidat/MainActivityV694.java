package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.94 - deterministic Winter/Summer cards: fixed geometry, visible count + title, preserved click behavior. */
public class MainActivityV694 extends MainActivityV692 {
    private static final int CARD_H_DP = 118;

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.postDelayed(this::stabilizeSeasonCards694,7000);
            root.postDelayed(this::stabilizeSeasonCards694,7700);
        }
    }

    private void stabilizeSeasonCards694(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll694(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);

        TextView wt=findText694(box,"KIŞIN ARANACAK","KISIN ARANACAK");
        TextView st=findText694(box,"YAZIN ARANACAK");
        if(wt==null||st==null)return;

        View winterTop=topChild694(box,wt);
        View summerTop=topChild694(box,st);
        if(!(winterTop instanceof ViewGroup)||!(summerTop instanceof ViewGroup)||winterTop==summerTop)return;

        rebuildCard694((ViewGroup)winterTop,"KIŞIN ARANACAK",winterCount694());
        rebuildCard694((ViewGroup)summerTop,"YAZIN ARANACAK",summerCount694());

        // Ensure requested order remains Winter -> Summer.
        int wi=box.indexOfChild(winterTop),si=box.indexOfChild(summerTop);
        if(wi>=0&&si>=0&&si!=wi+1){
            box.removeView(summerTop);
            wi=box.indexOfChild(winterTop);
            box.addView(summerTop,Math.min(wi+1,box.getChildCount()));
        }
    }

    private void rebuildCard694(ViewGroup card,String titleText,int countValue){
        // Preserve a working click target before legacy descendants are removed.
        final View delegate=findClickableDescendant694(card);
        boolean outerHasClick=card.isClickable()||card.hasOnClickListeners();
        if(!outerHasClick&&delegate!=null){
            card.setClickable(true);
            card.setOnClickListener(v->delegate.performClick());
        }

        card.removeAllViews();
        if(card instanceof LinearLayout){
            LinearLayout ll=(LinearLayout)card;
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setGravity(Gravity.CENTER);
        }

        TextView count=new TextView(this);
        count.setText(String.valueOf(countValue));
        count.setTextSize(28f);
        count.setTextColor(Color.rgb(28,28,28));
        count.setTypeface(Typeface.DEFAULT,Typeface.NORMAL);
        count.setGravity(Gravity.CENTER);
        count.setPadding(dp(4),dp(2),dp(4),dp(4));

        TextView title=new TextView(this);
        title.setText(titleText);
        title.setTextSize(12f);
        title.setTextColor(Color.rgb(35,35,35));
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setMaxLines(2);
        title.setPadding(dp(4),dp(5),dp(4),dp(2));

        LinearLayout.LayoutParams countLp=new LinearLayout.LayoutParams(-1,-2);
        LinearLayout.LayoutParams titleLp=new LinearLayout.LayoutParams(-1,-2);
        card.addView(count,countLp);
        card.addView(title,titleLp);

        GradientDrawable bg=new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1),Color.rgb(222,222,222));
        card.setBackground(bg);
        card.setElevation(dp(1));
        card.setPadding(dp(12),dp(10),dp(12),dp(10));
        card.setMinimumHeight(0);

        ViewGroup.LayoutParams raw=card.getLayoutParams();
        if(raw instanceof LinearLayout.LayoutParams){
            LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)raw;
            lp.width=ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height=dp(CARD_H_DP);
            lp.setMargins(lp.leftMargin,dp(4),lp.rightMargin,dp(4));
            card.setLayoutParams(lp);
        }else if(raw!=null){
            raw.width=ViewGroup.LayoutParams.MATCH_PARENT;
            raw.height=dp(CARD_H_DP);
            card.setLayoutParams(raw);
        }
    }

    private int winterCount694(){return countFlag694("winterCall");}
    private int summerCount694(){return countFlag694("summerCall");}

    private int countFlag694(String column){
        Cursor c=null;
        try{
            c=db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' AND COALESCE("+column+",0)<>0",null);
            return c.moveToFirst()?c.getInt(0):0;
        }catch(Exception ignored){return 0;}finally{if(c!=null)c.close();}
    }

    private View findClickableDescendant694(View v){
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                View c=g.getChildAt(i);
                if(c.isClickable()||c.hasOnClickListeners())return c;
                View r=findClickableDescendant694(c);if(r!=null)return r;
            }
        }
        return null;
    }
    private TextView findText694(View v,String... needles){
        if(v instanceof TextView){String n=norm694(String.valueOf(((TextView)v).getText()));for(String s:needles)if(n.contains(norm694(s)))return(TextView)v;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText694(g.getChildAt(i),needles);if(r!=null)return r;}}return null;
    }
    private View topChild694(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private ScrollView findScroll694(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll694(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm694(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}

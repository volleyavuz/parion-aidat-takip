package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.89 - show Winter follow-up count and visually synchronize Winter/Summer cards. */
public class MainActivityV689 extends MainActivityV688 {
    private static final int TEXT=Color.rgb(35,35,35);
    private static final int MUTED=Color.rgb(104,104,104);
    private static final int LINE=Color.rgb(224,224,224);

    @Override void showHome(){
        super.showHome();
        // V688 restores/reorders seasonal cards after the inherited cleanup. Style afterwards.
        if(root!=null){
            root.postDelayed(this::syncSeasonal689,2450);
            root.postDelayed(this::syncSeasonal689,3000);
        }
    }

    private void syncSeasonal689(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll689(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);

        TextView wt=findText689(box,"KIŞIN ARANACAK","KISIN ARANACAK");
        TextView st=findText689(box,"YAZIN ARANACAK");
        if(wt==null)return;

        View winter=cardShell689(wt,box);
        View summer=st==null?null:cardShell689(st,box);
        if(winter==null)return;

        // Existing Winter click/list behavior is intentionally untouched.
        ViewGroup host=bestTextHost689(wt,winter);
        TextView wc=findTaggedText689(winter,"v689-winter-count");
        if(wc==null){
            wc=new TextView(this);
            wc.setTag("v689-winter-count");
            host.addView(wc,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        wc.setText(String.valueOf(winterCount689()));

        // Mirror the visible Summer number styling where possible.
        TextView summerNumber=summer==null?null:findNumeric689(summer);
        styleTitle689(wt);
        styleCount689(wc,summerNumber);
        if(st!=null)styleTitle689(st);

        // One common card language: same border, radius, padding, elevation and minimum height.
        styleFrame689(winter);
        if(summer!=null)styleFrame689(summer);

        // Keep the requested order: Winter immediately followed by Summer.
        View winterTop=topChild689(box,winter);
        View summerTop=summer==null?null:topChild689(box,summer);
        if(winterTop!=null&&summerTop!=null&&winterTop!=summerTop){
            int wi=box.indexOfChild(winterTop),si=box.indexOfChild(summerTop);
            if(wi>=0&&si>=0&&si!=wi+1){
                box.removeView(summerTop);
                wi=box.indexOfChild(winterTop);
                box.addView(summerTop,Math.min(wi+1,box.getChildCount()),full689(dp(4),dp(8)));
            }
        }
    }

    private int winterCount689(){
        Cursor c=null;
        try{
            c=db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' AND COALESCE(winterCall,0)<>0",null);
            return c.moveToFirst()?c.getInt(0):0;
        }catch(Exception ignored){return 0;}
        finally{if(c!=null)c.close();}
    }

    private void styleFrame689(View card){
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1),LINE);
        card.setBackground(bg);
        card.setElevation(dp(1));
        card.setPadding(dp(12),dp(10),dp(12),dp(10));
        card.setMinimumHeight(dp(88));
        ViewGroup.LayoutParams raw=card.getLayoutParams();
        if(raw instanceof LinearLayout.LayoutParams){
            LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)raw;
            lp.height=ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.setMargins(0,dp(4),0,dp(4));
            card.setLayoutParams(lp);
        }
    }

    private void styleTitle689(TextView t){
        t.setTextSize(12f);t.setTextColor(TEXT);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        t.setGravity(Gravity.CENTER);t.setMaxLines(2);t.setPadding(dp(4),dp(3),dp(4),dp(3));
    }

    private void styleCount689(TextView target,TextView source){
        target.setGravity(Gravity.CENTER);
        target.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        target.setPadding(dp(4),dp(2),dp(4),dp(4));
        if(source!=null){
            target.setTextSize(source.getTextSize()/getResources().getDisplayMetrics().scaledDensity);
            target.setTextColor(source.getCurrentTextColor());
        }else{
            target.setTextSize(24f);target.setTextColor(Color.rgb(55,105,160));
        }
    }

    private TextView findNumeric689(View v){
        if(v instanceof TextView){
            String s=String.valueOf(((TextView)v).getText()).trim();
            if(s.matches("[0-9]+"))return(TextView)v;
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findNumeric689(g.getChildAt(i));if(r!=null)return r;}}
        return null;
    }
    private TextView findTaggedText689(View v,String tag){View r=findTag689(v,tag);return r instanceof TextView?(TextView)r:null;}
    private ViewGroup bestTextHost689(TextView title,View card){
        ViewParent p=title.getParent();if(p instanceof ViewGroup)return(ViewGroup)p;
        if(card instanceof ViewGroup)return(ViewGroup)card;
        throw new IllegalStateException("Seasonal card has no ViewGroup host");
    }
    private View cardShell689(View v,LinearLayout box){
        View cur=v,best=v;
        while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box){
            View p=(View)cur.getParent();
            if(cur.isClickable()||cur.hasOnClickListeners())best=cur;
            if(p instanceof LinearLayout&&((LinearLayout)p).getOrientation()==LinearLayout.HORIZONTAL)return cur;
            cur=p;
        }
        if(best!=v)return best;
        View top=topChild689(box,v);return top==null?v:top;
    }
    private TextView findText689(View v,String... needles){
        if(v instanceof TextView){String n=norm689(String.valueOf(((TextView)v).getText()));for(String s:needles)if(n.contains(norm689(s)))return(TextView)v;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText689(g.getChildAt(i),needles);if(r!=null)return r;}}
        return null;
    }
    private View topChild689(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private View findTag689(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag689(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private ScrollView findScroll689(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll689(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private LinearLayout.LayoutParams full689(int top,int bottom){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,ViewGroup.LayoutParams.WRAP_CONTENT);lp.setMargins(0,top,0,bottom);return lp;}
    private String norm689(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}

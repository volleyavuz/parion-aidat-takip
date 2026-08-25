package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.92 - rebuild only Winter visible content so its geometry matches Summer exactly. */
public class MainActivityV692 extends MainActivityV691 {
    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.postDelayed(this::matchWinterToSummer692,5700);
            root.postDelayed(this::matchWinterToSummer692,6400);
        }
    }

    private void matchWinterToSummer692(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll692(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        TextView wt=findText692(box,"KIŞIN ARANACAK","KISIN ARANACAK");
        TextView st=findText692(box,"YAZIN ARANACAK");
        if(wt==null||st==null)return;

        View winterTop=topChild692(box,wt);
        View summerTop=topChild692(box,st);
        if(!(winterTop instanceof ViewGroup)||summerTop==null||winterTop==summerTop)return;
        ViewGroup winter=(ViewGroup)winterTop;

        // Preserve the already-working click behavior. If it lives on a nested legacy view,
        // delegate the outer card click to that existing listener before rebuilding children.
        final View delegate=findClickableDescendant692(winter);
        if(!winterTop.isClickable()&&!winterTop.hasOnClickListeners()&&delegate!=null){
            winterTop.setClickable(true);
            winterTop.setOnClickListener(v->delegate.performClick());
        }

        // Rebuild the visible Winter content directly in the top-level card.
        // This removes transparent legacy containers that were still consuming vertical space.
        winter.removeAllViews();
        if(winter instanceof LinearLayout){
            LinearLayout ll=(LinearLayout)winter;
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setGravity(Gravity.CENTER);
        }

        TextView summerNumber=findNumeric692(summerTop);
        TextView count=new TextView(this);
        count.setTag("v692-winter-count");
        count.setText(String.valueOf(winterCount692()));
        copyNumberStyle692(count,summerNumber);

        TextView title=new TextView(this);
        title.setTag("v692-winter-title");
        title.setText("KIŞIN ARANACAK");
        copyTitleStyle692(title,st);

        winter.addView(count,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        winter.addView(title,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));

        // Match Summer's outer visual language and actual measured height.
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));bg.setStroke(dp(1),Color.rgb(222,222,222));
        winterTop.setBackground(bg);winterTop.setElevation(dp(1));
        winterTop.setPadding(summerTop.getPaddingLeft(),summerTop.getPaddingTop(),summerTop.getPaddingRight(),summerTop.getPaddingBottom());
        winterTop.setMinimumHeight(0);

        int sh=summerTop.getHeight();
        ViewGroup.LayoutParams raw=winterTop.getLayoutParams();
        if(raw instanceof LinearLayout.LayoutParams){
            LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)raw;
            lp.width=ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height=sh>0?sh:ViewGroup.LayoutParams.WRAP_CONTENT;
            ViewGroup.LayoutParams sr=summerTop.getLayoutParams();
            if(sr instanceof LinearLayout.LayoutParams){
                LinearLayout.LayoutParams slp=(LinearLayout.LayoutParams)sr;
                lp.setMargins(slp.leftMargin,slp.topMargin,slp.rightMargin,slp.bottomMargin);
            }
            winterTop.setLayoutParams(lp);
        }

        // Keep Winter directly above Summer.
        int wi=box.indexOfChild(winterTop),si=box.indexOfChild(summerTop);
        if(wi>=0&&si>=0&&si!=wi+1){
            box.removeView(summerTop);
            wi=box.indexOfChild(winterTop);
            box.addView(summerTop,Math.min(wi+1,box.getChildCount()));
        }
    }

    private int winterCount692(){
        Cursor c=null;
        try{
            c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' AND COALESCE(winterCall,0)<>0",null);
            return c.moveToFirst()?c.getInt(0):0;
        }catch(Exception ignored){return 0;}finally{if(c!=null)c.close();}
    }

    private void copyNumberStyle692(TextView dst,TextView src){
        dst.setGravity(Gravity.CENTER);dst.setPadding(dp(4),dp(2),dp(4),dp(2));
        if(src!=null){
            dst.setTextSize(src.getTextSize()/getResources().getDisplayMetrics().scaledDensity);
            dst.setTextColor(src.getCurrentTextColor());dst.setTypeface(src.getTypeface());
        }else{dst.setTextSize(28f);dst.setTextColor(Color.BLACK);dst.setTypeface(Typeface.DEFAULT,Typeface.NORMAL);}
    }

    private void copyTitleStyle692(TextView dst,TextView src){
        dst.setGravity(Gravity.CENTER);dst.setMaxLines(2);
        if(src!=null){
            dst.setTextSize(src.getTextSize()/getResources().getDisplayMetrics().scaledDensity);
            dst.setTextColor(src.getCurrentTextColor());dst.setTypeface(src.getTypeface());
            dst.setPadding(src.getPaddingLeft(),src.getPaddingTop(),src.getPaddingRight(),src.getPaddingBottom());
        }else{
            dst.setTextSize(12f);dst.setTextColor(Color.rgb(35,35,35));dst.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            dst.setPadding(dp(4),dp(4),dp(4),dp(4));
        }
    }

    private View findClickableDescendant692(View v){
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                View c=g.getChildAt(i);
                if(c.isClickable()||c.hasOnClickListeners())return c;
                View r=findClickableDescendant692(c);if(r!=null)return r;
            }
        }
        return null;
    }
    private TextView findNumeric692(View v){
        if(v instanceof TextView){String s=String.valueOf(((TextView)v).getText()).trim();if(s.matches("[0-9]+"))return(TextView)v;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findNumeric692(g.getChildAt(i));if(r!=null)return r;}}return null;
    }
    private TextView findText692(View v,String... needles){
        if(v instanceof TextView){String n=norm692(String.valueOf(((TextView)v).getText()));for(String s:needles)if(n.contains(norm692(s)))return(TextView)v;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText692(g.getChildAt(i),needles);if(r!=null)return r;}}return null;
    }
    private View topChild692(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private ScrollView findScroll692(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll692(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm692(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}

package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.90 - flatten Winter card, show its count, and match Summer card geometry. */
public class MainActivityV690 extends MainActivityV689 {
    private static final int TEXT690=Color.rgb(35,35,35);
    private static final int LINE690=Color.rgb(222,222,222);

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.postDelayed(this::fixSeasonal690,3450);
            root.postDelayed(this::fixSeasonal690,4100);
        }
    }

    private void fixSeasonal690(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll690(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        TextView wt=findText690(box,"KIŞIN ARANACAK","KISIN ARANACAK");
        TextView st=findText690(box,"YAZIN ARANACAK");
        if(wt==null)return;

        View winterTop=topChild690(box,wt);
        View summerTop=st==null?null:topChild690(box,st);
        if(winterTop==null)return;

        // Remove the legacy double-card look without touching any click listener.
        flattenPath690(wt,winterTop);
        hideImages690(winterTop);
        styleOuter690(winterTop,summerTop);
        if(summerTop!=null)styleOuter690(summerTop,null);

        ViewParent parent=wt.getParent();
        if(parent instanceof ViewGroup){
            ViewGroup host=(ViewGroup)parent;
            if(host instanceof LinearLayout){
                LinearLayout ll=(LinearLayout)host;
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.setGravity(Gravity.CENTER);
            }
            TextView count=findTagged690(winterTop,"v690-winter-count");
            if(count==null){
                // Remove the v689 count if it exists in a clipped/legacy host.
                View old=findTag690(winterTop,"v689-winter-count");
                if(old!=null&&old.getParent() instanceof ViewGroup)((ViewGroup)old.getParent()).removeView(old);
                count=new TextView(this);
                count.setTag("v690-winter-count");
            }else if(count.getParent() instanceof ViewGroup){
                ((ViewGroup)count.getParent()).removeView(count);
            }
            int titleIndex=host.indexOfChild(wt);
            host.addView(count,Math.max(0,titleIndex),new ViewGroup.LayoutParams(-1,-2));
            count.setText(String.valueOf(winterCount690()));
            TextView summerNumber=summerTop==null?null:findNumeric690(summerTop);
            styleCount690(count,summerNumber);
        }
        styleTitle690(wt);
        if(st!=null)styleTitle690(st);

        // Keep Winter immediately above Summer.
        if(summerTop!=null&&winterTop!=summerTop){
            int wi=box.indexOfChild(winterTop),si=box.indexOfChild(summerTop);
            if(wi>=0&&si>=0&&si!=wi+1){
                box.removeView(summerTop);
                wi=box.indexOfChild(winterTop);
                box.addView(summerTop,Math.min(wi+1,box.getChildCount()),full690(dp(4),dp(8)));
            }
        }
    }

    private int winterCount690(){
        Cursor c=null;
        try{
            c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' AND COALESCE(winterCall,0)<>0",null);
            return c.moveToFirst()?c.getInt(0):0;
        }catch(Exception ignored){return 0;}finally{if(c!=null)c.close();}
    }

    private void flattenPath690(View title,View outer){
        View cur=title;
        while(cur!=null&&cur!=outer&&cur.getParent() instanceof View){
            View p=(View)cur.getParent();
            if(p!=outer){
                p.setBackgroundColor(Color.TRANSPARENT);
                p.setElevation(0f);
                p.setPadding(0,0,0,0);
                ViewGroup.LayoutParams raw=p.getLayoutParams();
                if(raw!=null){raw.height=ViewGroup.LayoutParams.WRAP_CONTENT;raw.width=ViewGroup.LayoutParams.MATCH_PARENT;p.setLayoutParams(raw);}
            }
            cur=p;
        }
    }

    private void hideImages690(View v){
        if(v instanceof ImageView){v.setVisibility(View.GONE);return;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)hideImages690(g.getChildAt(i));}
    }

    private void styleOuter690(View card,View geometrySource){
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));bg.setStroke(dp(1),LINE690);
        card.setBackground(bg);card.setElevation(dp(1));card.setPadding(dp(12),dp(14),dp(12),dp(14));
        int mh=dp(96);if(geometrySource!=null&&geometrySource.getHeight()>0)mh=geometrySource.getHeight();
        card.setMinimumHeight(mh);
        ViewGroup.LayoutParams raw=card.getLayoutParams();
        if(raw instanceof LinearLayout.LayoutParams){
            LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)raw;
            lp.width=ViewGroup.LayoutParams.MATCH_PARENT;lp.height=ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.setMargins(0,dp(4),0,dp(4));card.setLayoutParams(lp);
        }
    }

    private void styleTitle690(TextView t){
        t.setTextSize(12f);t.setTextColor(TEXT690);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        t.setGravity(Gravity.CENTER);t.setPadding(dp(4),dp(5),dp(4),dp(4));t.setMaxLines(2);
    }

    private void styleCount690(TextView t,TextView source){
        t.setGravity(Gravity.CENTER);t.setTypeface(Typeface.DEFAULT,Typeface.NORMAL);t.setPadding(dp(4),dp(2),dp(4),dp(2));
        if(source!=null){
            t.setTextSize(source.getTextSize()/getResources().getDisplayMetrics().scaledDensity);
            t.setTextColor(source.getCurrentTextColor());
            t.setTypeface(source.getTypeface());
        }else{t.setTextSize(28f);t.setTextColor(Color.BLACK);}
    }

    private TextView findNumeric690(View v){
        if(v instanceof TextView){String s=String.valueOf(((TextView)v).getText()).trim();if(s.matches("[0-9]+"))return(TextView)v;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findNumeric690(g.getChildAt(i));if(r!=null)return r;}}return null;
    }
    private TextView findTagged690(View v,String tag){View x=findTag690(v,tag);return x instanceof TextView?(TextView)x:null;}
    private View findTag690(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag690(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private TextView findText690(View v,String... needles){if(v instanceof TextView){String n=norm690(String.valueOf(((TextView)v).getText()));for(String s:needles)if(n.contains(norm690(s)))return(TextView)v;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText690(g.getChildAt(i),needles);if(r!=null)return r;}}return null;}
    private View topChild690(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private ScrollView findScroll690(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll690(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private LinearLayout.LayoutParams full690(int top,int bottom){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,top,0,bottom);return lp;}
    private String norm690(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}

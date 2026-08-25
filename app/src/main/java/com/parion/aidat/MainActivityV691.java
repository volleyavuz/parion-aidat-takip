package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.91 - render Winter as one flat card: one count + one title, matching Summer. */
public class MainActivityV691 extends MainActivityV690 {
    private static final int TEXT691=Color.rgb(35,35,35);
    private static final int LINE691=Color.rgb(222,222,222);

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.postDelayed(this::normalizeWinter691,4550);
            root.postDelayed(this::normalizeWinter691,5200);
        }
    }

    private void normalizeWinter691(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll691(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        TextView winterTitle=findText691(box,"KIŞIN ARANACAK","KISIN ARANACAK");
        TextView summerTitle=findText691(box,"YAZIN ARANACAK");
        if(winterTitle==null)return;

        View winterTop=topChild691(box,winterTitle);
        View summerTop=summerTitle==null?null:topChild691(box,summerTitle);
        if(!(winterTop instanceof ViewGroup))return;

        // Keep the working Winter click target intact. Only normalize its descendants visually.
        clearNestedVisuals691((ViewGroup)winterTop,winterTitle);
        removeAllNumericTexts691((ViewGroup)winterTop);
        hideAllImages691((ViewGroup)winterTop);

        // Use the title's existing host, but make that host visually transparent and centered.
        ViewGroup host=(winterTitle.getParent() instanceof ViewGroup)?(ViewGroup)winterTitle.getParent():(ViewGroup)winterTop;
        host.setBackgroundColor(Color.TRANSPARENT);
        host.setElevation(0f);
        host.setPadding(0,0,0,0);
        if(host instanceof LinearLayout){
            ((LinearLayout)host).setOrientation(LinearLayout.VERTICAL);
            ((LinearLayout)host).setGravity(Gravity.CENTER);
        }

        TextView count=new TextView(this);
        count.setTag("v691-winter-count");
        count.setText(String.valueOf(winterCount691()));
        TextView summerNumber=summerTop==null?null:findNumeric691(summerTop);
        styleCount691(count,summerNumber);
        int titleIndex=host.indexOfChild(winterTitle);
        host.addView(count,Math.max(0,titleIndex),new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        styleTitle691(winterTitle);

        // Only the top-level Winter card gets a visible frame.
        styleOuter691(winterTop,summerTop);

        // Preserve requested order: Winter immediately above Summer.
        if(summerTop!=null&&winterTop!=summerTop){
            int wi=box.indexOfChild(winterTop),si=box.indexOfChild(summerTop);
            if(wi>=0&&si>=0&&si!=wi+1){
                box.removeView(summerTop);
                wi=box.indexOfChild(winterTop);
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
                lp.setMargins(0,dp(4),0,dp(8));
                box.addView(summerTop,Math.min(wi+1,box.getChildCount()),lp);
            }
        }
    }

    private int winterCount691(){
        Cursor c=null;
        try{
            c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' AND COALESCE(winterCall,0)<>0",null);
            return c.moveToFirst()?c.getInt(0):0;
        }catch(Exception ignored){return 0;}finally{if(c!=null)c.close();}
    }

    private void clearNestedVisuals691(ViewGroup root,TextView title){
        for(int i=0;i<root.getChildCount();i++){
            View v=root.getChildAt(i);
            if(v==title)continue;
            if(v instanceof ViewGroup){
                v.setBackgroundColor(Color.TRANSPARENT);
                v.setElevation(0f);
                v.setPadding(0,0,0,0);
                ViewGroup.LayoutParams raw=v.getLayoutParams();
                if(raw!=null){raw.height=ViewGroup.LayoutParams.WRAP_CONTENT;raw.width=ViewGroup.LayoutParams.MATCH_PARENT;v.setLayoutParams(raw);}
                clearNestedVisuals691((ViewGroup)v,title);
            }
        }
    }

    private void removeAllNumericTexts691(ViewGroup g){
        for(int i=g.getChildCount()-1;i>=0;i--){
            View v=g.getChildAt(i);
            if(v instanceof TextView){
                String s=String.valueOf(((TextView)v).getText()).trim();
                if(s.matches("[0-9]+")){g.removeViewAt(i);continue;}
            }
            if(v instanceof ViewGroup)removeAllNumericTexts691((ViewGroup)v);
        }
    }

    private void hideAllImages691(ViewGroup g){
        for(int i=0;i<g.getChildCount();i++){
            View v=g.getChildAt(i);
            if(v instanceof ImageView)v.setVisibility(View.GONE);
            else if(v instanceof ViewGroup)hideAllImages691((ViewGroup)v);
        }
    }

    private void styleOuter691(View card,View summer){
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));bg.setStroke(dp(1),LINE691);
        card.setBackground(bg);card.setElevation(dp(1));card.setPadding(dp(12),dp(14),dp(12),dp(14));
        int h=dp(96);if(summer!=null&&summer.getHeight()>0)h=summer.getHeight();
        card.setMinimumHeight(h);
        ViewGroup.LayoutParams raw=card.getLayoutParams();
        if(raw instanceof LinearLayout.LayoutParams){
            LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)raw;
            lp.width=ViewGroup.LayoutParams.MATCH_PARENT;lp.height=ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.setMargins(0,dp(4),0,dp(4));card.setLayoutParams(lp);
        }
    }

    private void styleTitle691(TextView t){
        t.setText("KIŞIN ARANACAK");t.setTextSize(12f);t.setTextColor(TEXT691);
        t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER);
        t.setPadding(dp(4),dp(4),dp(4),dp(4));t.setMaxLines(2);
        t.setBackgroundColor(Color.TRANSPARENT);t.setElevation(0f);
    }

    private void styleCount691(TextView t,TextView source){
        t.setGravity(Gravity.CENTER);t.setPadding(dp(4),dp(2),dp(4),dp(2));
        if(source!=null){
            t.setTextSize(source.getTextSize()/getResources().getDisplayMetrics().scaledDensity);
            t.setTextColor(source.getCurrentTextColor());t.setTypeface(source.getTypeface());
        }else{t.setTextSize(28f);t.setTextColor(Color.BLACK);t.setTypeface(Typeface.DEFAULT,Typeface.NORMAL);}
    }

    private TextView findNumeric691(View v){
        if(v instanceof TextView){String s=String.valueOf(((TextView)v).getText()).trim();if(s.matches("[0-9]+"))return(TextView)v;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findNumeric691(g.getChildAt(i));if(r!=null)return r;}}return null;
    }
    private TextView findText691(View v,String... needles){
        if(v instanceof TextView){String n=norm691(String.valueOf(((TextView)v).getText()));for(String s:needles)if(n.contains(norm691(s)))return(TextView)v;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText691(g.getChildAt(i),needles);if(r!=null)return r;}}return null;
    }
    private View topChild691(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private ScrollView findScroll691(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll691(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm691(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}

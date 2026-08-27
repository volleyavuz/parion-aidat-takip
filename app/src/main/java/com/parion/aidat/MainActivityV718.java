package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.1.41 - dashboard shows one Finance card; finance actions open on a dedicated page. */
public class MainActivityV718 extends MainActivityV717 {
    private static final int GOLD718=Color.rgb(205,156,34);
    private static final int TEXT718=Color.rgb(35,35,35);
    private final ArrayList<View> financeCards718=new ArrayList<>();

    @Override void showHome(){
        financeCards718.clear();
        super.showHome();
        if(root!=null){
            // V717 finishes its final regroup at 10.5s. Convert that visible wrapper into
            // one dashboard entry after all inherited HOME patches have settled.
            root.postDelayed(this::collapseFinanceToEntry718,10800);
            root.postDelayed(this::collapseFinanceToEntry718,11600);
        }
    }

    private void collapseFinanceToEntry718(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll718(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);

        View wrapper=findTag718(box,"v717-finance-container");
        if(!(wrapper instanceof ViewGroup)){
            // Already collapsed on the second pass.
            if(findTag718(box,"v718-finance-entry")!=null)return;
            return;
        }
        ViewGroup wg=(ViewGroup)wrapper;
        int insert=box.indexOfChild(wrapper);
        if(insert<0)insert=Math.min(2,box.getChildCount());

        financeCards718.clear();
        for(int i=0;i<wg.getChildCount();i++){
            View child=wg.getChildAt(i);
            if(child.isClickable()||child.hasOnClickListeners())financeCards718.add(child);
        }
        // Detach captured cards first so their original listeners survive.
        for(View card:new ArrayList<>(financeCards718)){
            ViewParent p=card.getParent();if(p instanceof ViewGroup)((ViewGroup)p).removeView(card);
        }
        if(wrapper.getParent() instanceof ViewGroup)((ViewGroup)wrapper.getParent()).removeView(wrapper);

        // Avoid duplicate dashboard entry if a delayed pass runs again.
        View old=findTag718(box,"v718-finance-entry");
        if(old!=null&&old.getParent() instanceof ViewGroup)((ViewGroup)old.getParent()).removeView(old);
        insert=Math.max(0,Math.min(insert,box.getChildCount()));
        box.addView(buildFinanceEntry718(),insert,entryLp718());
    }

    private View buildFinanceEntry718(){
        LinearLayout card=new LinearLayout(this);card.setTag("v718-finance-entry");card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(14),dp(13),dp(14),dp(13));
        GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));bg.setStroke(dp(1),Color.rgb(222,198,128));card.setBackground(bg);card.setElevation(dp(2));
        ImageView icon=new ImageView(this);icon.setImageResource(android.R.drawable.ic_menu_manage);icon.setColorFilter(GOLD718);icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);card.addView(icon,new LinearLayout.LayoutParams(dp(27),dp(27)));
        TextView title=text718("FİNANS",14.5f,TEXT718,true);title.setGravity(Gravity.CENTER);title.setPadding(0,dp(5),0,dp(2));card.addView(title,new LinearLayout.LayoutParams(-1,-2));
        TextView sub=text718("Aidat, tahsilat, vade, borç ve ödeme işlemleri",10.3f,Color.rgb(100,100,100),false);sub.setGravity(Gravity.CENTER);sub.setMaxLines(2);card.addView(sub,new LinearLayout.LayoutParams(-1,-2));
        card.setClickable(true);card.setOnClickListener(v->showFinancePage718());
        return card;
    }

    private void showFinancePage718(){
        page="FINANCE_718";
        base("FİNANS",true);
        ScrollView sv=scroll();LinearLayout b=box(sv);b.setPadding(dp(10),dp(10),dp(10),dp(28));
        TextView intro=text718("Aidat ve tahsilat işlemleri",12f,Color.rgb(90,90,90),false);intro.setPadding(dp(4),0,dp(4),dp(10));b.addView(intro,new LinearLayout.LayoutParams(-1,-2));
        if(financeCards718.isEmpty()){
            TextView empty=text718("Finans seçenekleri yeniden yükleniyor. Anasayfaya dönüp tekrar deneyin.",12f,Color.DKGRAY,false);b.addView(empty,new LinearLayout.LayoutParams(-1,-2));return;
        }
        for(View card:new ArrayList<>(financeCards718)){
            ViewParent p=card.getParent();if(p instanceof ViewGroup)((ViewGroup)p).removeView(card);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,ViewGroup.LayoutParams.WRAP_CONTENT);lp.setMargins(dp(2),0,dp(2),dp(9));b.addView(card,lp);
        }
    }

    @Override void goBack(){
        if("FINANCE_718".equals(page)){showHome();return;}
        super.goBack();
    }

    private LinearLayout.LayoutParams entryLp718(){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(120));lp.setMargins(dp(4),dp(7),dp(4),dp(10));return lp;}
    private TextView text718(String s,float sp,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private ScrollView findScroll718(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll718(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private View findTag718(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag718(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
}

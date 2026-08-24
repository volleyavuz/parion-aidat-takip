package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.63 - organize Follow-up dashboard cards without touching data/startup logic. */
public class MainActivityV663 extends MainActivityV662 {

    @Override void showHome(){
        super.showHome();
        if(root!=null)root.post(this::patchFollowup663);
    }

    private void patchFollowup663(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll663(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);

        View photo=cardByText663(box,"FOTOĞRAF EKSİK","FOTOĞRAFI OLMAYAN AKTİF SPORCULAR");
        View form=cardByText663(box,"KAYIT FORMU EKSİK","KAYIT FORMU OLMAYAN AKTİF SPORCULAR");
        View summer=cardByText663(box,"YAZIN ARANACAK");
        View winter=cardByText663(box,"KIŞIN ARANACAK");
        View tshirt=cardByText663(box,"TİŞÖRT ALMAYANLAR","TİŞÖRT ALMAYAN SPORCULAR","TİŞÖRT ALMAYAN");
        View absent=cardByText663(box,"DEVAMSIZLAR");

        TextView heading=findText663(box,"TAKİP GEREKTİRENLER");
        View headingTop=heading==null?null:topChild663(box,heading);
        int insert=headingTop!=null?box.indexOfChild(headingTop)+1:firstIndex663(box,photo,form,summer,winter,tshirt,absent);
        if(insert<0)insert=box.getChildCount();

        detach663(photo);detach663(form);detach663(summer);detach663(winter);detach663(tshirt);detach663(absent);
        cleanupEmpty663(box);

        if(headingTop==null){
            TextView h=heading663();
            box.addView(h,Math.min(insert,box.getChildCount()));
            insert++;
        }else{
            int hi=box.indexOfChild(headingTop);
            insert=hi>=0?hi+1:Math.min(insert,box.getChildCount());
            normalizeHeading663(heading);
        }

        if(photo!=null||form!=null){
            LinearLayout row=row663("v663-doc-row");
            if(photo!=null)row.addView(photo,cell663());
            if(form!=null)row.addView(form,cell663());
            box.addView(row,Math.min(insert++,box.getChildCount()),full663(dp(3),dp(3)));
        }

        if(summer!=null||winter!=null){
            LinearLayout row=row663("v663-call-row");
            if(summer!=null)row.addView(summer,cell663());
            if(winter!=null)row.addView(winter,cell663());
            box.addView(row,Math.min(insert++,box.getChildCount()),full663(dp(3),dp(3)));
        }

        if(tshirt!=null)box.addView(tshirt,Math.min(insert++,box.getChildCount()),full663(dp(4),dp(4)));
        if(absent!=null)box.addView(absent,Math.min(insert++,box.getChildCount()),full663(dp(4),dp(10)));

        style663(photo);style663(form);style663(summer);style663(winter);style663(tshirt);style663(absent);
    }

    private LinearLayout row663(String tag){
        LinearLayout r=new LinearLayout(this);r.setTag(tag);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.TOP);return r;
    }

    private TextView heading663(){
        TextView t=new TextView(this);t.setTag("v663-followup-heading");
        t.setText("TAKİP GEREKTİRENLER\nEksik kayıtlar ve takip edilmesi gereken sporcular");
        t.setTextColor(Color.rgb(32,32,32));t.setTextSize(14f);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        t.setPadding(dp(4),dp(18),dp(4),dp(9));t.setLineSpacing(dp(1),1f);return t;
    }

    private void normalizeHeading663(TextView t){
        if(t==null)return;t.setText("TAKİP GEREKTİRENLER\nEksik kayıtlar ve takip edilmesi gereken sporcular");
        t.setTextSize(14f);t.setTextColor(Color.rgb(32,32,32));t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        t.setPadding(dp(4),dp(18),dp(4),dp(9));
    }

    private void style663(View card){
        if(card==null)return;
        android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));card.setBackground(bg);card.setElevation(dp(1));
    }

    private LinearLayout.LayoutParams cell663(){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f);lp.setMargins(dp(3),0,dp(3),0);return lp;}
    private LinearLayout.LayoutParams full663(int top,int bottom){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,ViewGroup.LayoutParams.WRAP_CONTENT);lp.setMargins(0,top,0,bottom);return lp;}

    private View cardByText663(LinearLayout box,String... needles){TextView t=findText663(box,needles);return t==null?null:nearestCard663(t,box);}
    private View nearestCard663(View v,LinearLayout box){
        View cur=v,best=v;
        while(cur.getParent() instanceof View&&cur.getParent()!=box){
            View p=(View)cur.getParent();
            if(p instanceof LinearLayout){LinearLayout l=(LinearLayout)p;if(l.getOrientation()==LinearLayout.VERTICAL)best=p;if(l.getOrientation()==LinearLayout.HORIZONTAL)break;}
            cur=p;
        }
        if(best==v){View top=topChild663(box,v);return top==null?v:top;}return best;
    }
    private void detach663(View v){if(v==null)return;ViewParent p=v.getParent();if(p instanceof ViewGroup)((ViewGroup)p).removeView(v);}
    private void cleanupEmpty663(LinearLayout box){boolean ch=true;while(ch){ch=false;for(int i=box.getChildCount()-1;i>=0;i--){View v=box.getChildAt(i);if(v instanceof ViewGroup&&((ViewGroup)v).getChildCount()==0){box.removeViewAt(i);ch=true;}}}}
    private int firstIndex663(LinearLayout box,View... vs){int best=Integer.MAX_VALUE;for(View v:vs){if(v==null)continue;View top=topChild663(box,v);if(top!=null){int i=box.indexOfChild(top);if(i>=0&&i<best)best=i;}}return best==Integer.MAX_VALUE?-1:best;}
    private View topChild663(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private TextView findText663(View v,String... needles){if(v instanceof TextView){String u=norm663(String.valueOf(((TextView)v).getText()));for(String n:needles)if(u.contains(norm663(n)))return(TextView)v;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText663(g.getChildAt(i),needles);if(r!=null)return r;}}return null;}
    private ScrollView findScroll663(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll663(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm663(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}

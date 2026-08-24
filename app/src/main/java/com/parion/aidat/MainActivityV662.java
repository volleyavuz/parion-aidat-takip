package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.62 - clean lower dashboard after inherited legacy placement posts have finished. */
public class MainActivityV662 extends MainActivityV661 {

    @Override void showHome(){
        super.showHome();
        // Runs after the inherited V619/V620 placement posts. Pure UI rearrangement only.
        if(root!=null)root.post(this::patchMovements662);
    }

    private void patchMovements662(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll662(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);

        // The old future-payment card must not live on the dashboard.
        removeCardByText662(box,"AY SONUNA KADAR GELECEK","BU AY SONUNA KADAR ÖDEME YAPACAK");

        View net=cardByText662(box,"SON 3 AYDA");
        View current=cardByText662(box,"BU AY BAŞLAYANLAR","BU AY BAŞLAYAN SPORCULAR");
        View previous=cardByText662(box,"GEÇEN AY BAŞLAYANLAR","GEÇEN AY BAŞLAYAN SPORCULAR");
        View leavers=cardByText662(box,"SON 3 AY İÇİNDE BIRAKANLAR");

        TextView heading=findText662(box,"SPORCU HAREKETLERİ");
        View headingTop=heading==null?null:topChild662(box,heading);
        int insert=headingTop!=null?box.indexOfChild(headingTop)+1:firstIndex662(box,net,current,previous,leavers);
        if(insert<0)insert=box.getChildCount();

        detachCard662(net,box);
        detachCard662(current,box);
        detachCard662(previous,box);
        detachCard662(leavers,box);
        cleanupEmptyTop662(box);

        if(headingTop==null){
            TextView h=heading662();
            box.addView(h,Math.min(insert,box.getChildCount()));
            insert++;
        }else{
            int hi=box.indexOfChild(headingTop);
            insert=hi>=0?hi+1:Math.min(insert,box.getChildCount());
        }

        if(net!=null){
            box.addView(net,Math.min(insert++,box.getChildCount()),fullLp662(dp(3),dp(3)));
        }

        if(current!=null||previous!=null){
            LinearLayout row=new LinearLayout(this);
            row.setTag("v662-start-row");
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.TOP);
            row.setPadding(0,0,0,dp(3));
            if(current!=null)row.addView(current,cellLp662());
            if(previous!=null)row.addView(previous,cellLp662());
            box.addView(row,Math.min(insert++,box.getChildCount()),fullLp662(dp(3),dp(3)));
        }

        if(leavers!=null){
            box.addView(leavers,Math.min(insert++,box.getChildCount()),fullLp662(dp(3),dp(8)));
        }

        styleMovementCard662(net);
        styleMovementCard662(current);
        styleMovementCard662(previous);
        styleMovementCard662(leavers);
    }

    private void removeCardByText662(LinearLayout box,String... needles){
        TextView t=findText662(box,needles);if(t==null)return;
        View c=nearestCard662(t,box);if(c==null)return;
        ViewParent p=c.getParent();if(p instanceof ViewGroup)((ViewGroup)p).removeView(c);
        cleanupEmptyTop662(box);
    }

    private View cardByText662(LinearLayout box,String... needles){
        TextView t=findText662(box,needles);if(t==null)return null;
        return nearestCard662(t,box);
    }

    private View nearestCard662(View v,LinearLayout box){
        View cur=v;
        View best=v;
        while(cur.getParent() instanceof View && cur.getParent()!=box){
            View p=(View)cur.getParent();
            if(p instanceof LinearLayout){
                // A card is normally the first vertical LinearLayout above its text.
                LinearLayout l=(LinearLayout)p;
                if(l.getOrientation()==LinearLayout.VERTICAL)best=p;
                // Stop before climbing into the horizontal two-card row.
                if(l.getOrientation()==LinearLayout.HORIZONTAL)break;
            }
            cur=p;
        }
        if(best==v){
            View top=topChild662(box,v);return top==null?v:top;
        }
        return best;
    }

    private void detachCard662(View card,LinearLayout box){
        if(card==null)return;
        ViewParent p=card.getParent();if(p instanceof ViewGroup)((ViewGroup)p).removeView(card);
    }

    private void cleanupEmptyTop662(LinearLayout box){
        boolean changed=true;
        while(changed){
            changed=false;
            for(int i=box.getChildCount()-1;i>=0;i--){
                View v=box.getChildAt(i);
                if(v instanceof ViewGroup && ((ViewGroup)v).getChildCount()==0){box.removeViewAt(i);changed=true;}
            }
        }
    }

    private int firstIndex662(LinearLayout box,View... cards){
        int best=Integer.MAX_VALUE;
        for(View c:cards){if(c==null)continue;View top=topChild662(box,c);if(top!=null){int i=box.indexOfChild(top);if(i>=0&&i<best)best=i;}}
        return best==Integer.MAX_VALUE?-1:best;
    }

    private View topChild662(LinearLayout box,View v){
        View cur=v;
        while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();
        return cur!=null&&cur.getParent()==box?cur:null;
    }

    private TextView heading662(){
        TextView t=new TextView(this);
        t.setTag("v662-movement-heading");
        t.setText("SPORCU HAREKETLERİ\nYeni başlayan ve ayrılan sporcular");
        t.setTextColor(Color.rgb(32,32,32));
        t.setTextSize(14f);
        t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        t.setPadding(dp(4),dp(18),dp(4),dp(9));
        t.setLineSpacing(dp(1),1f);
        return t;
    }

    private LinearLayout.LayoutParams cellLp662(){
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f);
        lp.setMargins(dp(3),0,dp(3),0);
        return lp;
    }
    private LinearLayout.LayoutParams fullLp662(int top,int bottom){
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0,top,0,bottom);return lp;
    }

    private void styleMovementCard662(View card){
        if(card==null)return;
        android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));card.setBackground(bg);card.setElevation(dp(1));
    }

    private TextView findText662(View v,String... needles){
        if(v instanceof TextView){String u=norm662(String.valueOf(((TextView)v).getText()));for(String n:needles)if(u.contains(norm662(n)))return(TextView)v;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText662(g.getChildAt(i),needles);if(r!=null)return r;}}
        return null;
    }
    private ScrollView findScroll662(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll662(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm662(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}

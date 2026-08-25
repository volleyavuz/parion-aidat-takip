package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.88 - keep the already-working empty Winter card, restore only its label, and place Summer directly below it. */
public class MainActivityV688 extends MainActivityV687 {
    @Override void showHome(){
        super.showHome();
        // V687 finishes its legacy Winter cleanup at 1400 ms. Patch only after that,
        // so the restored label is not removed again by the inherited cleanup.
        if(root!=null){
            root.postDelayed(this::patchWinterAndSummer688,1650);
            root.postDelayed(this::patchWinterAndSummer688,2200);
        }
    }

    private void patchWinterAndSummer688(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll688(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);

        View winter=findEmptyWorkingWinter688(box);
        if(winter==null)return;

        // Restore ONLY the missing visible title. Do not replace listener/click behavior.
        if(findText688(winter,"KIŞIN ARANACAK","KISIN ARANACAK")==null){
            ViewGroup target=bestLabelHost688(winter);
            TextView title=new TextView(this);
            title.setTag("v688-winter-title");
            title.setText("KIŞIN ARANACAK");
            title.setTextSize(12f);
            title.setTextColor(Color.rgb(35,35,35));
            title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            title.setGravity(Gravity.CENTER);
            title.setPadding(dp(6),dp(5),dp(6),dp(7));
            target.addView(title,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        // Promote the existing Winter card itself to a full-width dashboard row, preserving its listener.
        View winterCard=cardShell688(winter,box);
        if(winterCard==null)winterCard=winter;
        detach688(winterCard);
        cleanupEmptyRows688(box);
        int anchor=findFollowupAnchor688(box);
        box.addView(winterCard,Math.max(0,Math.min(anchor,box.getChildCount())),full688(dp(4),dp(4)));

        // Move the existing Summer card immediately below Winter; keep its own click function unchanged.
        TextView summerText=findText688(box,"YAZIN ARANACAK");
        if(summerText!=null){
            View summer=cardShell688(summerText,box);
            if(summer==null)summer=topChild688(box,summerText);
            if(summer!=null&&summer!=winterCard){
                detach688(summer);
                cleanupEmptyRows688(box);
                int wi=box.indexOfChild(winterCard);
                box.addView(summer,Math.max(0,Math.min(wi+1,box.getChildCount())),full688(dp(4),dp(8)));
            }
        }
    }

    /**
     * v4.0.87 intentionally removed the Winter text but left the actual clickable card shell.
     * Prefer a text-empty clickable card that still carries the old phone/icon visual.
     */
    private View findEmptyWorkingWinter688(LinearLayout box){
        TextView tshirt=findText688(box,"TİŞÖRT ALMAYAN","TISORT ALMAYAN");
        View tshirtTop=tshirt==null?null:topChild688(box,tshirt);
        int tshirtIndex=tshirtTop==null?-1:box.indexOfChild(tshirtTop);

        ArrayList<View> candidates=new ArrayList<>();
        collectEmptyClickable688(box,candidates);
        View best=null;int bestScore=Integer.MIN_VALUE;
        for(View v:candidates){
            int score=0;
            if(findTag688(v,"v617-icon")!=null)score+=100;
            if(hasImage688(v))score+=20;
            View top=topChild688(box,v);
            if(top!=null){
                int idx=box.indexOfChild(top);
                if(tshirtIndex>=0&&idx>tshirtIndex)score+=40;
                score-=Math.max(0,idx-(tshirtIndex<0?idx:tshirtIndex));
            }
            if(v.hasOnClickListeners()||v.isClickable())score+=30;
            if(score>bestScore){bestScore=score;best=v;}
        }
        return best;
    }

    private void collectEmptyClickable688(View v,List<View> out){
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            boolean clickable=v.isClickable()||v.hasOnClickListeners();
            if(clickable && !hasMeaningfulText688(v) && hasImage688(v))out.add(v);
            for(int i=0;i<g.getChildCount();i++)collectEmptyClickable688(g.getChildAt(i),out);
        }
    }

    private ViewGroup bestLabelHost688(View v){
        if(v instanceof LinearLayout && ((LinearLayout)v).getOrientation()==LinearLayout.VERTICAL)return (ViewGroup)v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                View c=g.getChildAt(i);
                if(c instanceof LinearLayout && ((LinearLayout)c).getOrientation()==LinearLayout.VERTICAL)return (ViewGroup)c;
            }
            return g;
        }
        throw new IllegalStateException("Winter card is not a ViewGroup");
    }

    private View cardShell688(View v,LinearLayout box){
        View cur=v,best=v;
        while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box){
            View p=(View)cur.getParent();
            if(cur.isClickable()||cur.hasOnClickListeners())best=cur;
            if(p instanceof LinearLayout&&((LinearLayout)p).getOrientation()==LinearLayout.HORIZONTAL){
                // current child is the actual card/cell inside a two-column row
                return cur;
            }
            cur=p;
        }
        if(best!=v)return best;
        View top=topChild688(box,v);
        return top==null?v:top;
    }

    private int findFollowupAnchor688(LinearLayout box){
        TextView tshirt=findText688(box,"TİŞÖRT ALMAYAN","TISORT ALMAYAN");
        if(tshirt!=null){View top=topChild688(box,tshirt);if(top!=null){int i=box.indexOfChild(top);if(i>=0)return i+1;}}
        TextView absent=findText688(box,"DEVAMSIZLAR");
        if(absent!=null){View top=topChild688(box,absent);if(top!=null){int i=box.indexOfChild(top);if(i>=0)return i;}}
        return box.getChildCount();
    }

    private boolean hasMeaningfulText688(View v){
        if(v instanceof TextView){String s=String.valueOf(((TextView)v).getText()).trim();return !s.isEmpty();}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(hasMeaningfulText688(g.getChildAt(i)))return true;}
        return false;
    }
    private boolean hasImage688(View v){
        if(v instanceof ImageView)return true;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(hasImage688(g.getChildAt(i)))return true;}
        return false;
    }
    private void detach688(View v){if(v==null)return;ViewParent p=v.getParent();if(p instanceof ViewGroup)((ViewGroup)p).removeView(v);}
    private void cleanupEmptyRows688(ViewGroup g){for(int i=g.getChildCount()-1;i>=0;i--){View v=g.getChildAt(i);if(v instanceof ViewGroup){cleanupEmptyRows688((ViewGroup)v);if(((ViewGroup)v).getChildCount()==0)g.removeViewAt(i);}}}
    private LinearLayout.LayoutParams full688(int top,int bottom){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,ViewGroup.LayoutParams.WRAP_CONTENT);lp.setMargins(0,top,0,bottom);return lp;}
    private TextView findText688(View v,String... needles){if(v instanceof TextView){String n=norm688(String.valueOf(((TextView)v).getText()));for(String s:needles)if(n.contains(norm688(s)))return(TextView)v;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText688(g.getChildAt(i),needles);if(r!=null)return r;}}return null;}
    private View topChild688(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private View findTag688(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag688(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private ScrollView findScroll688(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll688(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm688(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}

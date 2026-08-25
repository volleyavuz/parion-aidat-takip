package com.parion.aidat;

import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.87 - remove Kışın Aranacak from dashboard completely; preserve all other dashboard cards. */
public class MainActivityV687 extends MainActivityV686 {
    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.post(this::removeAllWinter687);
            root.postDelayed(this::removeAllWinter687,300);
            root.postDelayed(this::removeAllWinter687,800);
            root.postDelayed(this::removeAllWinter687,1400);
        }
    }

    private void removeAllWinter687(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ArrayList<TextView> hits=new ArrayList<>();
        collectWinter687(root,hits);
        LinkedHashSet<View> cards=new LinkedHashSet<>();
        for(TextView t:hits){
            View card=cardForWinter687(t);
            if(card!=null)cards.add(card);
        }
        for(View card:cards){
            ViewParent p=card.getParent();
            if(p instanceof ViewGroup)((ViewGroup)p).removeView(card);
        }
        pruneEmpty687(root);
    }

    /**
     * Prefer removing the cell/card itself. If the card sits in a horizontal seasonal row,
     * return the direct child of that row so the Yazın Aranacak sibling is preserved.
     */
    private View cardForWinter687(View v){
        View cur=v;
        View best=v;
        while(cur!=null&&cur.getParent() instanceof View){
            ViewGroup parent=(ViewGroup)cur.getParent();
            if(parent instanceof LinearLayout && ((LinearLayout)parent).getOrientation()==LinearLayout.HORIZONTAL){
                return cur;
            }
            if(cur.hasOnClickListeners()||cur.isClickable())best=cur;
            if(cur instanceof LinearLayout && ((LinearLayout)cur).getOrientation()==LinearLayout.VERTICAL)best=cur;
            if(parent==root)break;
            cur=(View)parent;
        }
        return best;
    }

    private void collectWinter687(View v,List<TextView> out){
        if(v instanceof TextView){
            String n=norm687(String.valueOf(((TextView)v).getText()));
            if(n.contains("KIŞIN ARANACAK")||n.contains("KISIN ARANACAK"))out.add((TextView)v);
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++)collectWinter687(g.getChildAt(i),out);
        }
    }

    private void pruneEmpty687(View v){
        if(!(v instanceof ViewGroup))return;
        ViewGroup g=(ViewGroup)v;
        for(int i=g.getChildCount()-1;i>=0;i--){
            View c=g.getChildAt(i);
            if(c instanceof ViewGroup){
                pruneEmpty687(c);
                if(((ViewGroup)c).getChildCount()==0 && c!=root)g.removeViewAt(i);
            }
        }
    }

    private String norm687(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}

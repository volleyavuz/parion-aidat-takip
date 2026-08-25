package com.parion.aidat;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.86 - remove the old v617 winter visual, keep one canonical seasonal card, and force its correct position. */
public class MainActivityV686 extends MainActivityV685 {
    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.post(this::fixWinter686);
            root.postDelayed(this::fixWinter686,360);
            root.postDelayed(this::fixWinter686,900);
        }
    }

    private void fixWinter686(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll686(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);

        View canonical=findTag686(box,"v663-call-row");
        View canonicalWinter=canonical==null?null:findWinterCard686(canonical);

        // The old v4.0.17 designer layer injected a phone icon tagged v617-icon.
        // Strip that legacy visual from the one canonical winter card and normalize it.
        if(canonicalWinter!=null){
            removeTagged686(canonicalWinter,"v617-icon");
            GradientDrawable bg=new GradientDrawable();
            bg.setColor(Color.WHITE);bg.setCornerRadius(dp(15));
            canonicalWinter.setBackground(bg);canonicalWinter.setElevation(dp(1));
            canonicalWinter.setPadding(dp(10),dp(9),dp(10),dp(9));
        }

        // Purge every other Winter card/container left by older dashboard layers.
        ArrayList<TextView> hits=new ArrayList<>();collectWinter686(box,hits);
        LinkedHashSet<View> remove=new LinkedHashSet<>();
        for(TextView t:hits){
            if(canonical!=null&&isDescendant686(t,canonical))continue;
            View card=nearestVertical686(t,box);
            if(card!=null&&card!=canonical)remove.add(card);
        }
        for(View v:remove){ViewParent p=v.getParent();if(p instanceof ViewGroup)((ViewGroup)p).removeView(v);}
        pruneEmpty686(box);

        // Force the seasonal call row above T-shirt card. This prevents a stale Winter card
        // from appearing beneath T-shirt even if an inherited patch moved containers later.
        canonical=findTag686(box,"v663-call-row");
        TextView tshirt=findText686(box,"TİŞÖRT ALMAYAN","TISORT ALMAYAN");
        View tshirtTop=tshirt==null?null:topChild686(box,tshirt);
        View callTop=canonical==null?null:topChild686(box,canonical);
        if(callTop!=null&&tshirtTop!=null&&callTop!=tshirtTop){
            int tshirtIndex=box.indexOfChild(tshirtTop);
            int callIndex=box.indexOfChild(callTop);
            if(tshirtIndex>=0&&callIndex>=0&&callIndex>tshirtIndex){
                box.removeView(callTop);
                tshirtIndex=box.indexOfChild(tshirtTop);
                box.addView(callTop,Math.max(0,tshirtIndex));
            }
        }
    }

    private View findWinterCard686(View rootView){
        TextView t=findText686(rootView,"KIŞIN ARANACAK","KISIN ARANACAK");
        if(t==null)return null;
        View cur=t,best=null;
        while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=rootView){
            View p=(View)cur.getParent();
            if(p instanceof LinearLayout&&((LinearLayout)p).getOrientation()==LinearLayout.VERTICAL)best=p;
            cur=p;
        }
        return best!=null?best:t;
    }
    private void removeTagged686(View v,String tag){
        if(!(v instanceof ViewGroup))return;ViewGroup g=(ViewGroup)v;
        for(int i=g.getChildCount()-1;i>=0;i--){View c=g.getChildAt(i);if(tag.equals(c.getTag()))g.removeViewAt(i);else removeTagged686(c,tag);}
    }
    private void collectWinter686(View v,List<TextView> out){
        if(v instanceof TextView){String n=norm686(String.valueOf(((TextView)v).getText()));if(n.contains("KIŞIN ARANACAK")||n.contains("KISIN ARANACAK"))out.add((TextView)v);}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectWinter686(g.getChildAt(i),out);}
    }
    private View nearestVertical686(View v,LinearLayout box){
        View cur=v,best=null;
        while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box){View p=(View)cur.getParent();if(p instanceof LinearLayout&&((LinearLayout)p).getOrientation()==LinearLayout.VERTICAL)best=p;cur=p;}
        return best!=null?best:topChild686(box,v);
    }
    private TextView findText686(View v,String... needles){
        if(v instanceof TextView){String n=norm686(String.valueOf(((TextView)v).getText()));for(String s:needles)if(n.contains(norm686(s)))return(TextView)v;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText686(g.getChildAt(i),needles);if(r!=null)return r;}}
        return null;
    }
    private View topChild686(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private boolean isDescendant686(View child,View ancestor){View cur=child;while(cur!=null){if(cur==ancestor)return true;ViewParent p=cur.getParent();cur=p instanceof View?(View)p:null;}return false;}
    private void pruneEmpty686(ViewGroup g){for(int i=g.getChildCount()-1;i>=0;i--){View v=g.getChildAt(i);if(v instanceof ViewGroup){pruneEmpty686((ViewGroup)v);if(((ViewGroup)v).getChildCount()==0)g.removeViewAt(i);}}}
    private ScrollView findScroll686(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll686(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private View findTag686(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag686(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private String norm686(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}

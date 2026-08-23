package com.parion.aidat;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

/** v4.0.42 - reliably remove delta banner and athletes card after dashboard post-processing. */
public class MainActivityV642 extends MainActivityV641 {
    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.post(this::cleanup642);
            root.postDelayed(this::cleanup642,220);
            root.postDelayed(this::cleanup642,650);
            root.postDelayed(this::cleanup642,1200);
        }
    }

    private void cleanup642(){
        if(root==null || page==null || !"HOME".equalsIgnoreCase(page)) return;
        ScrollView sv=findScroll642(root);
        if(sv==null || sv.getChildCount()==0 || !(sv.getChildAt(0) instanceof ViewGroup)) return;
        ViewGroup dashboard=(ViewGroup)sv.getChildAt(0);
        removeAthletes642(dashboard);
        removeDelta642(dashboard);
    }

    private void removeAthletes642(ViewGroup dashboard){
        ArrayList<TextView> hits=new ArrayList<>();
        collect642(dashboard,hits,"SPORCULAR");
        for(TextView t:hits){
            View target=nearestClickable642(t,dashboard);
            if(target==null) target=t;
            removeOrHide642(target,dashboard);
        }
    }

    private void removeDelta642(ViewGroup dashboard){
        ArrayList<TextView> hits=new ArrayList<>();
        collect642(dashboard,hits,"ÇİFT YÖNLÜ DELTA");
        collect642(dashboard,hits,"CIFT YONLU DELTA");
        for(TextView t:hits){
            View target=compactContainer642(t,dashboard);
            removeOrHide642(target,dashboard);
        }
    }

    private void collect642(View v,ArrayList<TextView> out,String needle){
        if(v instanceof TextView){
            String s=norm642(String.valueOf(((TextView)v).getText()));
            if(s.contains(norm642(needle)) && !out.contains((TextView)v)) out.add((TextView)v);
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++) collect642(g.getChildAt(i),out,needle);
        }
    }

    private View nearestClickable642(View v,ViewGroup stop){
        View cur=v;
        View best=null;
        while(cur!=null && cur!=stop){
            if(cur.isClickable() || cur.hasOnClickListeners()) best=cur;
            ViewParent p=cur.getParent();
            if(!(p instanceof View)) break;
            cur=(View)p;
        }
        return best;
    }

    private View compactContainer642(View v,ViewGroup stop){
        View cur=v;
        while(cur!=null && cur!=stop){
            ViewParent p=cur.getParent();
            if(!(p instanceof ViewGroup)) break;
            ViewGroup pg=(ViewGroup)p;
            if(pg==stop) return cur;
            if(pg.getChildCount()<=3) cur=pg;
            else break;
        }
        return cur==null?v:cur;
    }

    private void removeOrHide642(View target,ViewGroup dashboard){
        if(target==null || target==dashboard) return;
        ViewParent p=target.getParent();
        if(p instanceof ViewGroup){
            try{((ViewGroup)p).removeView(target);return;}catch(Exception ignored){}
        }
        target.setVisibility(View.GONE);
    }

    private ScrollView findScroll642(View v){
        if(v instanceof ScrollView) return (ScrollView)v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                ScrollView s=findScroll642(g.getChildAt(i));
                if(s!=null) return s;
            }
        }
        return null;
    }

    private String norm642(String s){
        return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));
    }
}

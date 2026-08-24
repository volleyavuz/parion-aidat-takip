package com.parion.aidat;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

/** v4.0.42 - reliably remove delta banner and athletes card. */
public class MainActivityV642 extends MainActivityV641 {
    @Override void showHome(){super.showHome();if(root!=null)cleanup642();}
    private void cleanup642(){if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;ScrollView sv=findScroll642(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof ViewGroup))return;ViewGroup d=(ViewGroup)sv.getChildAt(0);removeAthletes642(d);removeDelta642(d);}
    private void removeAthletes642(ViewGroup d){ArrayList<TextView> h=new ArrayList<>();collect642(d,h,"SPORCULAR");for(TextView t:h){View x=nearestClickable642(t,d);if(x==null)x=t;removeOrHide642(x,d);}}
    private void removeDelta642(ViewGroup d){ArrayList<TextView> h=new ArrayList<>();collect642(d,h,"ÇİFT YÖNLÜ DELTA");collect642(d,h,"CIFT YONLU DELTA");for(TextView t:h)removeOrHide642(compactContainer642(t,d),d);}
    private void collect642(View v,ArrayList<TextView> o,String n){if(v instanceof TextView&&norm642(String.valueOf(((TextView)v).getText())).contains(norm642(n))&&!o.contains((TextView)v))o.add((TextView)v);if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collect642(g.getChildAt(i),o,n);}}
    private View nearestClickable642(View v,ViewGroup stop){View c=v,b=null;while(c!=null&&c!=stop){if(c.isClickable()||c.hasOnClickListeners())b=c;ViewParent p=c.getParent();if(!(p instanceof View))break;c=(View)p;}return b;}
    private View compactContainer642(View v,ViewGroup stop){View c=v;while(c!=null&&c!=stop){ViewParent p=c.getParent();if(!(p instanceof ViewGroup))break;ViewGroup g=(ViewGroup)p;if(g==stop)return c;if(g.getChildCount()<=3)c=g;else break;}return c==null?v:c;}
    private void removeOrHide642(View x,ViewGroup d){if(x==null||x==d)return;ViewParent p=x.getParent();if(p instanceof ViewGroup){try{((ViewGroup)p).removeView(x);return;}catch(Exception ignored){}}x.setVisibility(View.GONE);}
    private ScrollView findScroll642(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll642(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm642(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}

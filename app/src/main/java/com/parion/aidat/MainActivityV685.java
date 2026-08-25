package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.85 - fix New Starters ScrollView crash and keep only canonical Winter follow-up card. */
public class MainActivityV685 extends MainActivityV684 {
    private static final int TEXT685=Color.rgb(42,42,42);
    private static final int MUTED685=Color.rgb(105,105,105);

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.post(this::patch685);
            root.postDelayed(this::patch685,180);
            root.postDelayed(this::patch685,420);
        }
    }

    private void patch685(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        cleanupDuplicateWinter685();
        rewireStarter685();
    }

    private void cleanupDuplicateWinter685(){
        ScrollView sv=findScroll685(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        View canonical=findTag685(box,"v663-call-row");
        ArrayList<TextView> hits=new ArrayList<>();
        collectWinter685(box,hits);
        LinkedHashSet<View> remove=new LinkedHashSet<>();
        for(TextView t:hits){
            if(canonical!=null&&isDescendant685(t,canonical))continue;
            View card=nearestVerticalCard685(t,box);
            if(card!=null&&card!=canonical)remove.add(card);
        }
        for(View v:remove){ViewParent p=v.getParent();if(p instanceof ViewGroup)((ViewGroup)p).removeView(v);}
        pruneEmpty685(box);
    }

    private void showNewStartersFixed685(){
        page="LIST";
        base("YENİ BAŞLAYANLAR",true);
        ScrollView sv=scroll();
        LinearLayout list=box(sv);
        String from=firstDay685(0),to=firstDay685(1);
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name,startDate FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' AND startDate>=? AND startDate<? ORDER BY startDate DESC,name COLLATE NOCASE",new String[]{from,to});
        if(!c.moveToFirst()){
            list.addView(text685("Bu ay yeni başlayan sporcu yok.",14f,MUTED685,false),new LinearLayout.LayoutParams(-1,-2));
            c.close();return;
        }
        do{
            long id=c.getLong(0);String name=c.getString(1);String date=c.getString(2);
            TextView row=text685(name+(date==null||date.trim().isEmpty()?"":"\nBaşlangıç: "+date),14f,TEXT685,true);
            row.setPadding(dp(14),dp(12),dp(14),dp(12));row.setClickable(true);row.setOnClickListener(v->showProfile(id));
            GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(12));row.setBackground(bg);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(8));list.addView(row,lp);
        }while(c.moveToNext());
        c.close();
    }

    private void rewireStarter685(){View card=findTag685(root,"v684-new-starters");if(card!=null)card.setOnClickListener(v->showNewStartersFixed685());}
    private String firstDay685(int addMonths){Calendar c=Calendar.getInstance();c.set(Calendar.DAY_OF_MONTH,1);c.add(Calendar.MONTH,addMonths);return String.format(Locale.US,"%04d-%02d-01",c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1);}
    private TextView text685(String s,float sp,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private void collectWinter685(View v,List<TextView> out){if(v instanceof TextView){String n=norm685(String.valueOf(((TextView)v).getText()));if(n.contains("KIŞIN ARANACAK")||n.contains("KISIN ARANACAK"))out.add((TextView)v);}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectWinter685(g.getChildAt(i),out);}}
    private View nearestVerticalCard685(View v,LinearLayout box){View cur=v,best=null;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box){View p=(View)cur.getParent();if(p instanceof LinearLayout&&((LinearLayout)p).getOrientation()==LinearLayout.VERTICAL)best=p;cur=p;}return best!=null?best:topChild685(box,v);}
    private View topChild685(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private boolean isDescendant685(View child,View ancestor){View cur=child;while(cur!=null){if(cur==ancestor)return true;ViewParent p=cur.getParent();cur=p instanceof View?(View)p:null;}return false;}
    private void pruneEmpty685(ViewGroup g){for(int i=g.getChildCount()-1;i>=0;i--){View v=g.getChildAt(i);if(v instanceof ViewGroup){pruneEmpty685((ViewGroup)v);if(((ViewGroup)v).getChildCount()==0)g.removeViewAt(i);}}}
    private ScrollView findScroll685(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll685(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private View findTag685(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag685(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private String norm685(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}

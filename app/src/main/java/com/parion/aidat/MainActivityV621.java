package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.21 - tshirt follow-up card, uppercase winter label and stronger home branding. */
public class MainActivityV621 extends MainActivityV620 {
    private static final int GOLD621=Color.rgb(205,156,34);
    private static final int TEXT621=Color.rgb(20,20,20);
    private static final int MUTED621=Color.rgb(75,75,75);
    private static final int BLUE621=Color.rgb(72,103,132);

    @Override void showHome(){
        super.showHome();
        root.post(this::patch621);
    }

    private void patch621(){
        ScrollView sv=findScroll621(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        strengthenHeader621(root);
        patchWinterText621(box);
        addTshirtCard621(box);
    }

    private void strengthenHeader621(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;String u=norm621(String.valueOf(t.getText()));
            if(u.contains("PARİON SPORCU TAKİP UYGULAMASI")||u.contains("PARION SPORCU TAKİP UYGULAMASI")||u.equals("PARİON SPORCU TAKİP UYGULAMASI")||u.equals("PARION SPORCU TAKİP UYGULAMASI")){
                t.setText("PARİON\nSPORCU TAKİP UYGULAMASI");
                t.setTextColor(Color.BLACK);t.setTextSize(16f);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setMaxLines(2);t.setGravity(Gravity.CENTER_VERTICAL);t.setLineSpacing(dp(1),1.0f);t.setPadding(dp(6),dp(2),dp(6),dp(2));
                if(t.getParent() instanceof View){View p=(View)t.getParent();p.setBackground(round621(Color.WHITE,GOLD621,12,1));p.setPadding(dp(6),dp(4),dp(6),dp(4));}
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)strengthenHeader621(g.getChildAt(i));}
    }

    private void patchWinterText621(LinearLayout box){
        TextView winter=findText621(box,"KIŞIN ARANACAK");if(winter==null)return;
        winter.setText("KIŞIN ARANACAKLAR");winter.setAllCaps(false);winter.setTextSize(10.5f);winter.setTypeface(Typeface.DEFAULT,Typeface.BOLD);winter.setTextColor(TEXT621);winter.setGravity(Gravity.CENTER);winter.setMaxLines(2);
        View card=nearestCard621(winter);if(card==null&&winter.getParent() instanceof View)card=(View)winter.getParent();if(card!=null){card.setBackground(round621(Color.WHITE,BLUE621,18,1));card.setPadding(dp(10),dp(9),dp(10),dp(9));}
    }

    private void addTshirtCard621(LinearLayout box){
        if(findTag621(box,"v621-tshirt-card")!=null)return;
        TextView tracking=findText621(box,"TAKİP GEREKTİRENLER");
        TextView winter=findText621(box,"KIŞIN ARANACAKLAR","KIŞIN ARANACAK");
        if(tracking==null||winter==null)return;

        int count=countNoTshirt621();
        LinearLayout card=new LinearLayout(this);card.setTag("v621-tshirt-card");card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(12),dp(10),dp(12),dp(10));card.setBackground(round621(Color.WHITE,GOLD621,18,1));card.setClickable(true);card.setOnClickListener(v->showNoTshirt621());
        ImageView icon=new ImageView(this);icon.setImageResource(android.R.drawable.ic_menu_agenda);icon.setColorFilter(GOLD621);icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);card.addView(icon,new LinearLayout.LayoutParams(dp(25),dp(25)));
        TextView title=tv621("Tişört Almayan Sporcular",10.5f,TEXT621,true);title.setMaxLines(2);card.addView(title);
        TextView num=tv621(String.valueOf(count),27f,GOLD621,true);card.addView(num);
        TextView sub=tv621("Tişört sayısı 0",9.5f,MUTED621,false);card.addView(sub);

        View anchor=topChild621(box,winter);int idx=anchor==null?box.getChildCount():box.indexOfChild(anchor)+1;
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(116));lp.setMargins(dp(3),dp(4),dp(3),dp(8));box.addView(card,Math.max(0,Math.min(idx,box.getChildCount())),lp);
    }

    private int countNoTshirt621(){
        Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE COALESCE(tshirtQty,0)=0 AND TRIM(COALESCE(deletedAt,''))=''",null);int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;
    }

    private void showNoTshirt621(){
        page="NO_TSHIRT_621";base("TİŞÖRT ALMAYAN SPORCULAR",true);
        ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name,birthYear,status FROM athletes WHERE COALESCE(tshirtQty,0)=0 AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);
        while(c.moveToNext()){
            final long id=c.getLong(0);String name=c.getString(1);int by=c.getInt(2);String st=c.getString(3)==null?"":c.getString(3);
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(12),dp(10),dp(12),dp(10));row.setBackground(round621(Color.WHITE,GOLD621,12,1));row.setClickable(true);row.setOnClickListener(v->showProfile(id));
            TextView a=tv621(name,14f,TEXT621,true);a.setGravity(Gravity.START);row.addView(a);
            TextView d=tv621((by>0?by+" • ":"")+st+" • Tişört: 0",10.5f,MUTED621,false);d.setGravity(Gravity.START);row.addView(d);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(row,lp);n++;
        }
        c.close();
        if(n==0)b.addView(tv621("Tişört sayısı 0 olan sporcu bulunmuyor.",13f,MUTED621,true));
    }

    @Override void goBack(){if("NO_TSHIRT_621".equals(page)){showHome();return;}super.goBack();}

    private TextView tv621(String s,float sp,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);t.setGravity(Gravity.CENTER);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private View topChild621(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private View nearestCard621(View v){View cur=v,best=v;while(cur!=null&&cur!=root){if(cur.hasOnClickListeners()||cur.isClickable())best=cur;ViewParent p=cur.getParent();if(!(p instanceof View))break;cur=(View)p;}return best;}
    private TextView findText621(View v,String... needles){if(v instanceof TextView){String u=norm621(String.valueOf(((TextView)v).getText()));for(String n:needles)if(u.contains(norm621(n)))return (TextView)v;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText621(g.getChildAt(i),needles);if(r!=null)return r;}}return null;}
    private View findTag621(View v,String tag){if(tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag621(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private ScrollView findScroll621(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll621(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm621(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
    private GradientDrawable round621(int fill,int stroke,int radius,int width){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));d.setStroke(dp(width),stroke);return d;}
}

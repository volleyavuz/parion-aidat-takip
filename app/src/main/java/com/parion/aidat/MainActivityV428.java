package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivityV428 extends MainActivityV427 {
    private String statusOrigin428="";

    @Override void base(String title,boolean back){
        super.base(title,back);
        try{
            if(root==null||root.getChildCount()==0||!(root.getChildAt(0) instanceof LinearLayout))return;
            LinearLayout bar=(LinearLayout)root.getChildAt(0);
            // V427 başlığındaki marka metnini iki satıra sabitle.
            TextView brand=findText428(bar,"PARİON SPOR KULÜBÜ");
            if(brand!=null){
                brand.setText("PARİON SPOR KULÜBÜ\nAİDAT TAKİP SİSTEMİ");
                brand.setTextSize(11);
                brand.setMaxLines(2);
                brand.setSingleLine(false);
                brand.setLineSpacing(0f,0.92f);
            }
        }catch(Exception ignored){}
    }

    @Override void showHome(){
        super.showHome();
        removeTopYellowStrip428();
        replaceUpcomingCard428();
        addStatusCards428();
    }

    private void removeTopYellowStrip428(){
        try{
            // Ana sayfanın en üstünde kalan, metinsiz sarı dekoratif şeridi kaldır.
            if(root==null)return;
            for(int i=1;i<root.getChildCount();i++){
                View v=root.getChildAt(i);
                if(isEmptyGold428(v)){root.removeViewAt(i);return;}
                if(v instanceof ScrollView && ((ScrollView)v).getChildCount()>0 && ((ScrollView)v).getChildAt(0) instanceof ViewGroup){
                    ViewGroup g=(ViewGroup)((ScrollView)v).getChildAt(0);
                    int lim=Math.min(3,g.getChildCount());
                    for(int j=0;j<lim;j++){View x=g.getChildAt(j);if(isEmptyGold428(x)){g.removeViewAt(j);return;}}
                }
            }
        }catch(Exception ignored){}
    }
    private boolean isEmptyGold428(View v){
        if(v==null||v instanceof Button)return false;
        String txt=textOf428(v).trim();if(!txt.isEmpty())return false;
        if(!(v.getBackground() instanceof GradientDrawable))return false;
        try{android.content.res.ColorStateList c=((GradientDrawable)v.getBackground()).getColor();return c!=null&&c.getDefaultColor()==GOLD;}catch(Exception e){return false;}
    }

    private void replaceUpcomingCard428(){
        View label=findViewContaining428(root,"AY SONUNA KADAR");
        if(label==null)label=findViewContaining428(root,"GELECEK");
        if(label==null)return;
        View card=climbCard428(label);
        if(!(card instanceof ViewGroup))return;
        int[] x=activeTarget428();
        ViewGroup g=(ViewGroup)card;g.removeAllViews();
        LinearLayout inner=new LinearLayout(this);inner.setOrientation(LinearLayout.VERTICAL);inner.setGravity(Gravity.CENTER);inner.setPadding(dp(4),dp(5),dp(4),dp(5));
        TextView amount=tv(money(x[1]),18,GOLD,true);amount.setGravity(Gravity.CENTER);amount.setMaxLines(1);inner.addView(amount,new LinearLayout.LayoutParams(-1,-2));
        TextView title=tv("AYLIK HEDEF CİRO",10,Color.DKGRAY,true);title.setGravity(Gravity.CENTER);title.setPadding(1,1,1,1);inner.addView(title,new LinearLayout.LayoutParams(-1,-2));
        TextView count=tv(x[0]+" AKTİF SPORCU",10,Color.DKGRAY,false);count.setGravity(Gravity.CENTER);count.setPadding(1,1,1,1);inner.addView(count,new LinearLayout.LayoutParams(-1,-2));
        g.addView(inner,new ViewGroup.LayoutParams(-1,-1));
        card.setOnClickListener(v->new android.app.AlertDialog.Builder(this).setTitle("AYLIK HEDEF CİRO").setMessage(x[0]+" aktif sporcunun güncel aylık anlaşma tutarlarının toplamı:\n\n"+money(x[1])).setPositiveButton("TAMAM",null).show());
    }
    private int[] activeTarget428(){
        int count=0,total=0;Cursor c=null;try{c=db.athletes("","AKTİF");while(c.moveToNext()){long id=c.getLong(c.getColumnIndexOrThrow("id"));count++;total+=Math.max(0,currentMonthlyFee(id));}}catch(Exception ignored){}finally{if(c!=null)c.close();}return new int[]{count,total};
    }

    private void addStatusCards428(){
        ScrollView sv=findScroll428(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        if(findViewContaining428(box,"ARA VERENLER")!=null)return;
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER);
        int pause=countStatus428("ARA VERDİ"),left=countStatus428("BIRAKTI");
        row.addView(statusCard428("ARA VERENLER",pause,ORANGE,"ARA VERDİ"),new LinearLayout.LayoutParams(0,dp(94),1));
        row.addView(statusCard428("BIRAKANLAR",left,RED,"BIRAKTI"),new LinearLayout.LayoutParams(0,dp(94),1));
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(100));rp.setMargins(0,dp(5),0,dp(5));
        int at=findInsertPoint428(box);box.addView(row,Math.min(at,box.getChildCount()),rp);
    }
    private View statusCard428(String title,int count,int color,String status){
        LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER);c.setPadding(dp(5),dp(5),dp(5),dp(5));c.setBackground(round(Color.WHITE,10));
        TextView n=tv(String.valueOf(count),22,color,true);n.setGravity(Gravity.CENTER);c.addView(n);
        TextView t=tv(title,11,Color.DKGRAY,true);t.setGravity(Gravity.CENTER);t.setPadding(2,1,2,2);c.addView(t);
        c.setOnClickListener(v->showStatus428(status,title));LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)c.getLayoutParams();return c;
    }
    private int findInsertPoint428(LinearLayout box){
        for(int i=0;i<box.getChildCount();i++){View v=box.getChildAt(i);if(contains428(v,"ÖDENMEMİŞ MALZEME")||contains428(v,"FOTOĞRAFI OLMAYAN"))return i;}
        return Math.min(5,box.getChildCount());
    }
    private int countStatus428(String status){Cursor c=null;try{c=db.athletes("",status);int n=0;while(c.moveToNext())n++;return n;}catch(Exception e){return 0;}finally{if(c!=null)c.close();}}

    private void showStatus428(String status,String title){
        statusOrigin428=status;page="STATUS428_"+status;base(title,true);ScrollView sv=scroll();LinearLayout b=box(sv);Cursor c=db.athletes("",status);int n=0;
        while(c.moveToNext()){
            long id=c.getLong(c.getColumnIndexOrThrow("id"));String name=s(c,"name"),cat=s(c,"category");int by=c.getInt(c.getColumnIndexOrThrow("birthYear"));
            LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(10),dp(8),dp(10),dp(8));r.setBackground(round(Color.WHITE,10));
            LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.addView(tv((by>0?by+" • ":"")+name,15,BLACK,true));t.addView(tv(cat+" • "+status,12,status.equals("BIRAKTI")?RED:ORANGE,false));r.addView(t,new LinearLayout.LayoutParams(0,-2,1));
            r.setOnClickListener(v->{statusOrigin428=status;showProfile(id);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(6));b.addView(r,lp);n++;
        }c.close();if(n==0)b.addView(tv("BU DURUMDA SPORCU YOK.",13,Color.DKGRAY,true));
    }

    @Override void goBack(){
        if("MISSING_FORMS_416".equals(page)){showHome();return;}
        if(page!=null&&page.startsWith("STATUS428_")){statusOrigin428="";showHome();return;}
        if("PROFILE".equals(page)&&statusOrigin428!=null&&!statusOrigin428.isEmpty()){
            String s=statusOrigin428;showStatus428(s,"ARA VERDİ".equals(s)?"ARA VERENLER":"BIRAKANLAR");return;
        }
        super.goBack();
    }

    private ScrollView findScroll428(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll428(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private TextView findText428(View v,String term){if(v instanceof TextView&&String.valueOf(((TextView)v).getText()).toUpperCase(TR).contains(term))return (TextView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView t=findText428(g.getChildAt(i),term);if(t!=null)return t;}}return null;}
    private View findViewContaining428(View v,String term){if(v instanceof TextView&&String.valueOf(((TextView)v).getText()).toUpperCase(TR).contains(term))return v;if(v instanceof Button&&String.valueOf(((Button)v).getText()).toUpperCase(TR).contains(term))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View x=findViewContaining428(g.getChildAt(i),term);if(x!=null)return x;}}return null;}
    private boolean contains428(View v,String term){return findViewContaining428(v,term)!=null;}
    private View climbCard428(View v){View cur=v;for(int i=0;i<3;i++){ViewParent p=cur.getParent();if(!(p instanceof View))break;cur=(View)p;if(cur instanceof LinearLayout&&((LinearLayout)cur).getChildCount()>=2)return cur;}return cur;}
    private String textOf428(View v){if(v instanceof TextView)return String.valueOf(((TextView)v).getText());if(v instanceof ViewGroup){StringBuilder b=new StringBuilder();ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)b.append(textOf428(g.getChildAt(i)));return b.toString();}return "";}
}

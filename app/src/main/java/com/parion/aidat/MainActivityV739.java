package com.parion.aidat;

import android.database.Cursor;
import android.view.*;
import android.widget.ImageView;

/** v4.2.15 - target the real 92dp profile portrait slot instead of guessing the largest ImageView. */
public class MainActivityV739 extends MainActivityV738 {
    @Override void showProfile(long id){
        super.showProfile(id);
        if(root!=null){
            root.postDelayed(()->patchExactProfilePhoto739(id),40L);
            root.postDelayed(()->patchExactProfilePhoto739(id),220L);
            root.postDelayed(()->patchExactProfilePhoto739(id),900L);
        }
    }

    private void patchExactProfilePhoto739(long id){
        if(currentAthlete!=id||root==null||!"PROFILE".equals(page))return;
        String path=photoMap413().get(id);
        if(path==null||path.trim().isEmpty()){
            try{
                Cursor c=db.athlete(id);
                if(c.moveToFirst()){
                    int i=c.getColumnIndex("photo");
                    String p=i>=0&&!c.isNull(i)?c.getString(i):"";
                    if(p!=null&&p.startsWith("CLOUD:")&&p.length()>6)path=p.substring(6);
                }
                c.close();
            }catch(Exception ignored){}
        }
        if(path==null||path.trim().isEmpty())return;
        ImageView portrait=findExactPortrait739(root);
        if(portrait!=null)setAthletePhoto(portrait,"CLOUD:"+path);
    }

    private ImageView findExactPortrait739(View v){
        final int target=dp(92),tol=dp(8);
        if(v instanceof ImageView){
            ImageView iv=(ImageView)v;
            int w=iv.getWidth(),h=iv.getHeight();
            if(Math.abs(w-target)<=tol&&Math.abs(h-target)<=tol)return iv;
            ViewGroup.LayoutParams lp=iv.getLayoutParams();
            if(lp!=null&&Math.abs(lp.width-target)<=tol&&Math.abs(lp.height-target)<=tol)return iv;
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                ImageView x=findExactPortrait739(g.getChildAt(i));
                if(x!=null)return x;
            }
        }
        return null;
    }
}

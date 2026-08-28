package com.parion.aidat;

import android.database.Cursor;
import android.view.*;
import android.widget.ImageView;

/** v4.2.14 - force the same cloud thumbnail used by athlete lists into the profile portrait slot. */
public class MainActivityV738 extends MainActivityV737 {
    @Override void showProfile(long id){
        super.showProfile(id);
        if(root!=null){
            root.postDelayed(()->patchVisibleProfilePhoto738(id),120L);
            root.postDelayed(()->patchVisibleProfilePhoto738(id),650L);
        }
    }

    private void patchVisibleProfilePhoto738(long id){
        if(currentAthlete!=id||root==null)return;
        String path=photoMap413().get(id);
        if(path==null||path.trim().isEmpty()){
            try{Cursor c=db.athlete(id);if(c.moveToFirst()){int i=c.getColumnIndex("photo");String p=i>=0&&!c.isNull(i)?c.getString(i):"";if(p!=null&&p.startsWith("CLOUD:")&&p.length()>6)path=p.substring(6);}c.close();}catch(Exception ignored){}
        }
        if(path==null||path.trim().isEmpty())return;
        ImageView target=findPortrait738(root,null,-1);
        if(target!=null)setAthletePhoto(target,"CLOUD:"+path);
    }

    private ImageView findPortrait738(View v,ImageView best,long bestScore){
        if(v instanceof ImageView){
            ImageView iv=(ImageView)v;int w=iv.getWidth(),h=iv.getHeight();int[] loc=new int[2];iv.getLocationOnScreen(loc);
            int min=dp(64),max=dp(240);boolean candidate=w>=min&&h>=min&&w<=max&&h<=max&&loc[1]>=dp(75)&&loc[1]<=dp(520);
            if(candidate){long area=(long)w*h;long squarePenalty=Math.abs(w-h)*20L;long score=area-squarePenalty;if(score>bestScore){best=iv;bestScore=score;}}
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ImageView before=best;ImageView x=findPortrait738(g.getChildAt(i),best,bestScore);if(x!=best){best=x;int w=x.getWidth(),h=x.getHeight();bestScore=(long)w*h-Math.abs(w-h)*20L;}}}
        return best;
    }
}

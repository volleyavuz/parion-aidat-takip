package com.parion.aidat;

/** v4.2.23 - only ATHLETE pending rows gate canonical athlete/payment sync. */
public class MainActivityV744 extends MainActivityV743 {
    @Override protected int pendingCount741(){
        try{
            android.database.Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM pending_sync WHERE kind='ATHLETE'",null);
            int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;
        }catch(Exception e){return 0;}
    }
}

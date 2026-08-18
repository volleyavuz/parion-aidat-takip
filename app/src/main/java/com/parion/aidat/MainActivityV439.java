package com.parion.aidat;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Universal navigation safety layer.
 *
 * Every screen in the app is built through base(). Before a different screen is
 * rendered, keep the previous root View exactly as it is. Back therefore restores
 * the actual previous screen instead of trying to guess a route from page names.
 * This also preserves filters, text fields, spinner selections and scroll position.
 */
public class MainActivityV439 extends MainActivityV438 {
    private static final int MAX_BACK_439 = 10;

    private static class NavEntry439 {
        final LinearLayout root;
        final String page;
        final long athlete;
        NavEntry439(LinearLayout r,String p,long a){root=r;page=p;athlete=a;}
    }

    private final Deque<NavEntry439> back439 = new ArrayDeque<>();
    private String renderedPage439 = null;
    private long renderedAthlete439 = -1;
    private boolean restoring439 = false;

    @Override void base(String title, boolean back){
        final String destination = page == null ? "" : page;
        final LinearLayout previousRoot = root;
        final String previousPage = renderedPage439;
        final long previousAthlete = renderedAthlete439;

        // Only a real page transition belongs in history. Re-drawing the same page
        // after a save/sync must not create a fake extra Back step.
        if(!restoring439 && previousRoot!=null && previousPage!=null &&
                !previousPage.equals(destination)){
            back439.addLast(new NavEntry439(previousRoot,previousPage,previousAthlete));
            while(back439.size()>MAX_BACK_439) back439.removeFirst();
        }

        super.base(title,back);
        renderedPage439=destination;
        renderedAthlete439=currentAthlete;
    }

    /** Home is a navigation root. When it is opened normally (including logo tap),
     * old navigation history is intentionally discarded. */
    @Override void showHome(){
        if(!restoring439) back439.clear();
        super.showHome();
        renderedPage439="HOME";
        renderedAthlete439=-1;
    }

    @Override void goBack(){
        if(!back439.isEmpty()){
            NavEntry439 e=back439.removeLast();
            restoring439=true;
            try{
                // setContentView detaches the current screen and re-attaches the exact
                // previous screen instance. Its ScrollView / filters remain untouched.
                setContentView(e.root);
                root=e.root;
                page=e.page;
                currentAthlete=e.athlete;
                renderedPage439=e.page;
                renderedAthlete439=e.athlete;
            }finally{
                restoring439=false;
            }
            return;
        }

        // If a legacy path reached a non-home page without history, never exit the
        // application unexpectedly: return to Home. Home itself keeps the existing
        // exit-confirmation behavior from the parent class.
        if(page!=null && !"HOME".equals(page)){
            showHome();
            return;
        }
        super.goBack();
    }
}

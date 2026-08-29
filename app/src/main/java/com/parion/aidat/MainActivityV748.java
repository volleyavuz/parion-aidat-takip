package com.parion.aidat;

import android.view.View;

/**
 * v4.2.31 - final dashboard only.
 *
 * V36 is still kept as a functional ancestor because later screens reuse its payment/list
 * helpers, phone actions and other non-HOME behavior. Its original four-card HOME renderer,
 * however, is now a compatibility no-op: no finance dataset is calculated and legacy cards
 * are never rendered. MainActivityV657 remains the single visible dashboard renderer.
 */
public class MainActivityV748 extends MainActivityV747 {
    @Override DashData dashboardData(){
        return new DashData();
    }

    @Override View dashCard(String label,String value,int color,Runnable action){
        View ghost=new View(this);
        ghost.setVisibility(View.GONE);
        return ghost;
    }
}

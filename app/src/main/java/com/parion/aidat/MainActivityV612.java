package com.parion.aidat;

import java.util.*;

/**
 * v4.0.12 - ANR/home hotfix.
 * - Never run periodic full CLOUD->LOCAL restore while local DB already has athletes.
 * - Manual sync still works.
 * - During one home render, reuse dashboard/payment calculations instead of re-querying SQLite.
 */
public class MainActivityV612 extends MainActivityV610 {
    private boolean buildingHome612=false;
    private DashData dashboardCache612=null;
    private final HashMap<Long,HashMap<Integer,PayRec>> paymentCache612=new HashMap<>();

    @Override void syncFromCloud(boolean announce){
        if(!announce){
            try{
                if(db!=null && db.count(null)>0) return;
            }catch(Exception ignored){}
        }
        super.syncFromCloud(announce);
    }

    @Override void showHome(){
        buildingHome612=true;
        dashboardCache612=null;
        paymentCache612.clear();
        try{
            super.showHome();
        }finally{
            buildingHome612=false;
            dashboardCache612=null;
            paymentCache612.clear();
        }
    }

    @Override DashData dashboardData(){
        if(!buildingHome612) return super.dashboardData();
        if(dashboardCache612==null) dashboardCache612=super.dashboardData();
        return dashboardCache612;
    }

    @Override HashMap<Integer,PayRec> paymentMap(long id){
        if(!buildingHome612) return super.paymentMap(id);
        HashMap<Integer,PayRec> x=paymentCache612.get(id);
        if(x==null){
            x=super.paymentMap(id);
            paymentCache612.put(id,x);
        }
        return x;
    }
}

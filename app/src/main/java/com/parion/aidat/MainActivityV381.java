package com.parion.aidat;

public class MainActivityV381 extends MainActivityV38 {
    private long formAthleteId = -1;

    @Override void form(long id) {
        formAthleteId = id;
        super.form(id);
    }

    @Override public void onBackPressed() {
        if ("FORM".equals(page)) {
            long id = formAthleteId;
            formAthleteId = -1;
            if (id > 0) showProfile(id);
            else showAthletes();
            return;
        }
        super.onBackPressed();
    }
}

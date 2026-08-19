package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.*;
import android.view.Gravity;
import android.widget.*;
import java.util.Locale;
import org.json.JSONObject;

/** v3.9.5 - fixed email + raw password capture, cloud writes still blocked by V500. */
public class MainActivityV505 extends MainActivityV501 {
    static final String LOGIN_EMAIL="volleyavuz@gmail.com";
    @Override public void onCreate(Bundle b){ super.onCreate(b); }

    @Override void showLogin(){
        page="LOGIN"; currentAthlete=-1; base("PARION • ONLINE GİRİŞ",false);
        ScrollView sv=scroll(); LinearLayout box=box(sv); box.setPadding(dp(18),dp(24),dp(18),dp(18));
        ImageView logo=new ImageView(this); logo.setImageResource(R.drawable.parion_logo); logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE); box.addView(logo,new LinearLayout.LayoutParams(-1,dp(120)));
        TextView title=tv("PARION SPOR OKULU",22,BLACK,true); title.setGravity(Gravity.CENTER); box.addView(title);
        TextView email=tv(LOGIN_EMAIL,16,BLACK,true); email.setGravity(Gravity.CENTER); email.setAllCaps(false); box.addView(email,new LinearLayout.LayoutParams(-1,dp(52)));

        CaseSafePasswordEditText pass=new CaseSafePasswordEditText(this); pass.setHint("Şifre"); pass.setSingleLine(true); pass.setAllCaps(false); pass.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); pass.setTransformationMethod(PasswordTransformationMethod.getInstance()); box.addView(pass,new LinearLayout.LayoutParams(-1,dp(56)));

        CheckBox show=new CheckBox(this); show.setText("ŞİFREYİ GÖSTER"); show.setOnCheckedChangeListener((b,checked)->{int pos=pass.getSelectionStart();pass.setTransformationMethod(checked?HideReturnsTransformationMethod.getInstance():PasswordTransformationMethod.getInstance());if(pos>=0&&pos<=pass.length())pass.setSelection(pos);}); box.addView(show);

        Button paste=btn("PANODAN ŞİFREYİ YAPIŞTIR"); box.addView(paste,new LinearLayout.LayoutParams(-1,dp(52)));
        paste.setOnClickListener(v->{try{ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);ClipData cd=cm==null?null:cm.getPrimaryClip();if(cd==null||cd.getItemCount()==0){toast("PANODA ŞİFRE YOK.");return;}CharSequence cs=cd.getItemAt(0).coerceToText(this);String raw=cs==null?"":cs.toString();pass.setRawPassword(raw);toast("ŞİFRE HAM HALİYLE ALINDI.");}catch(Exception e){toast("YAPIŞTIRMA HATASI.");}});

        Button login=btn("GİRİŞ YAP"); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58)); lp.setMargins(0,dp(8),0,dp(6)); box.addView(login,lp);
        Button forgot=btn("ŞİFREMİ UNUTTUM"); box.addView(forgot,new LinearLayout.LayoutParams(-1,dp(54)));
        TextView note=tv("E-posta sabittir. Şifre klavye/autofill ham karakterleriyle gönderilir. Buluta veri yazma kapalıdır.",12,Color.DKGRAY,false); note.setPadding(dp(4),dp(12),dp(4),0); box.addView(note);

        login.setOnClickListener(v->auth(LOGIN_EMAIL,pass.getRawPassword(),false));
        forgot.setOnClickListener(v->sendRecovery505());
    }

    private void sendRecovery505(){
        final AlertDialog wait=new AlertDialog.Builder(this).setMessage("Şifre sıfırlama e-postası gönderiliyor...").setCancelable(false).create();wait.show();
        new Thread(()->{try{JSONObject body=new JSONObject().put("email",LOGIN_EMAIL);HttpResult r=super.request("POST",SUPABASE_URL+"/auth/v1/recover",body.toString(),null);if(r.code>=200&&r.code<300)runOnUiThread(()->{wait.dismiss();new AlertDialog.Builder(this).setTitle("E-POSTA GÖNDERİLDİ").setMessage("Şifre sıfırlama bağlantısı gönderildi.").setPositiveButton("TAMAM",null).show();});else runOnUiThread(()->{wait.dismiss();toast("ŞİFRE SIFIRLAMA HATASI • HTTP "+r.code);});}catch(Exception e){runOnUiThread(()->{wait.dismiss();toast("ŞİFRE SIFIRLAMA HATASI • "+shortMsg(e));});}},"parion-password-recovery-505").start();
    }
}
package com.parion.aidat;

import android.app.*;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.TextKeyListener;
import android.view.Gravity;
import android.widget.*;
import org.json.JSONObject;

/** v3.9.3 - login fields are excluded from global uppercase behavior. */
public class MainActivityV503 extends MainActivityV501 {
    @Override public void onCreate(Bundle b){ super.onCreate(b); }

    @Override void showLogin(){
        page="LOGIN"; currentAthlete=-1; base("PARION • ONLINE GİRİŞ",false);
        ScrollView sv=scroll(); LinearLayout box=box(sv); box.setPadding(dp(18),dp(24),dp(18),dp(18));
        ImageView logo=new ImageView(this); logo.setImageResource(R.drawable.parion_logo); logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE); box.addView(logo,new LinearLayout.LayoutParams(-1,dp(120)));
        TextView title=tv("PARION SPOR OKULU",22,BLACK,true); title.setGravity(Gravity.CENTER); box.addView(title);
        TextView info=tv("Merkezi veritabanına bağlanmak için hesabınızla giriş yapın.",13,Color.DKGRAY,false); info.setGravity(Gravity.CENTER); info.setPadding(0,dp(4),0,dp(14)); box.addView(info);

        EditText email=new EditText(this); email.setHint("E-posta"); email.setSingleLine(true); email.setAllCaps(false); email.setFilters(new InputFilter[0]); email.setKeyListener(TextKeyListener.getInstance(false,TextKeyListener.Capitalize.NONE)); email.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS); email.setText("volleyavuz@gmail.com"); box.addView(email,new LinearLayout.LayoutParams(-1,dp(56)));

        EditText pass=new EditText(this); pass.setHint("Şifre"); pass.setSingleLine(true); pass.setAllCaps(false); pass.setFilters(new InputFilter[0]); pass.setKeyListener(TextKeyListener.getInstance(false,TextKeyListener.Capitalize.NONE)); pass.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); pass.setTransformationMethod(PasswordTransformationMethod.getInstance()); box.addView(pass,new LinearLayout.LayoutParams(-1,dp(56)));

        CheckBox show=new CheckBox(this); show.setText("ŞİFREYİ GÖSTER"); show.setOnCheckedChangeListener((b,checked)->{
            int pos=pass.getSelectionStart();
            pass.setTransformationMethod(checked?HideReturnsTransformationMethod.getInstance():PasswordTransformationMethod.getInstance());
            pass.setKeyListener(TextKeyListener.getInstance(false,TextKeyListener.Capitalize.NONE));
            if(pos>=0&&pos<=pass.length())pass.setSelection(pos);
        }); box.addView(show);

        Button login=btn("GİRİŞ YAP"); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58)); lp.setMargins(0,dp(8),0,dp(6)); box.addView(login,lp);
        Button forgot=btn("ŞİFREMİ UNUTTUM"); box.addView(forgot,new LinearLayout.LayoutParams(-1,dp(54)));
        TextView note=tv("Şifre alanında küçük/büyük harfler aynen korunur. Buluta veri yazma kapalıdır.",12,Color.DKGRAY,false); note.setPadding(dp(4),dp(12),dp(4),0); box.addView(note);

        login.setOnClickListener(v->auth(email.getText().toString().trim(),pass.getText().toString(),false));
        forgot.setOnClickListener(v->sendRecovery503(email.getText().toString().trim()));
    }

    private void sendRecovery503(String email){
        if(email==null||email.trim().isEmpty()){toast("E-POSTA ADRESİNİ GİRİN.");return;}
        final AlertDialog wait=new AlertDialog.Builder(this).setMessage("Şifre sıfırlama e-postası gönderiliyor...").setCancelable(false).create();wait.show();
        new Thread(()->{try{
            JSONObject body=new JSONObject().put("email",email.trim());
            HttpResult r=super.request("POST",SUPABASE_URL+"/auth/v1/recover",body.toString(),null);
            if(r.code>=200&&r.code<300)runOnUiThread(()->{wait.dismiss();new AlertDialog.Builder(this).setTitle("E-POSTA GÖNDERİLDİ").setMessage("Şifre sıfırlama bağlantısı gönderildi.").setPositiveButton("TAMAM",null).show();});
            else runOnUiThread(()->{wait.dismiss();toast("ŞİFRE SIFIRLAMA HATASI • HTTP "+r.code);});
        }catch(Exception e){runOnUiThread(()->{wait.dismiss();toast("ŞİFRE SIFIRLAMA HATASI • "+shortMsg(e));});}},"parion-password-recovery-503").start();
    }
}

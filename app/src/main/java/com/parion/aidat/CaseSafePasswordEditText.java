package com.parion.aidat;

import android.content.*;
import android.util.AttributeSet;
import android.view.inputmethod.*;
import android.widget.EditText;
import android.view.autofill.AutofillValue;

/** Keeps the original IME/autofill text separately from any UI uppercase transformation. */
public class CaseSafePasswordEditText extends EditText {
    private final StringBuilder raw = new StringBuilder();
    public CaseSafePasswordEditText(Context c){super(c);} public CaseSafePasswordEditText(Context c, AttributeSet a){super(c,a);} public CaseSafePasswordEditText(Context c,AttributeSet a,int s){super(c,a,s);}
    public String getRawPassword(){ return raw.length()>0 ? raw.toString() : getText().toString(); }
    public void setRawPassword(String s){ raw.setLength(0); if(s!=null)raw.append(s); super.setText(s==null?"":s); setSelection(length()); }
    @Override public void autofill(AutofillValue v){
        if(v!=null && v.isText()){ String s=String.valueOf(v.getTextValue()); raw.setLength(0); raw.append(s); }
        super.autofill(v);
    }
    @Override public InputConnection onCreateInputConnection(EditorInfo outAttrs){
        final InputConnection base=super.onCreateInputConnection(outAttrs); if(base==null)return null;
        return new InputConnectionWrapper(base,true){
            @Override public boolean commitText(CharSequence text,int newCursorPosition){ if(text!=null)raw.append(text); return super.commitText(text,newCursorPosition); }
            @Override public boolean setComposingText(CharSequence text,int newCursorPosition){ return super.setComposingText(text,newCursorPosition); }
            @Override public boolean deleteSurroundingText(int beforeLength,int afterLength){ if(beforeLength>0&&raw.length()>0){int n=Math.min(beforeLength,raw.length());raw.delete(raw.length()-n,raw.length());} return super.deleteSurroundingText(beforeLength,afterLength); }
        };
    }
}
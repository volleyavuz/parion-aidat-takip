package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.*;

/** Read-only provider for files generated under cache/attendance_exports. */
public class AttendanceShareProvider extends ContentProvider {
    private File root;
    @Override public boolean onCreate(){root=new File(getContext().getCacheDir(),"attendance_exports");root.mkdirs();return true;}
    private File resolve(Uri uri) throws FileNotFoundException{
        String name=uri.getLastPathSegment();
        if(name==null||name.contains("/")||name.contains("\\")||name.contains(".."))throw new FileNotFoundException();
        try{
            File f=new File(root,name).getCanonicalFile();
            if(!f.getParentFile().equals(root.getCanonicalFile())||!f.isFile())throw new FileNotFoundException();
            return f;
        }catch(IOException e){throw new FileNotFoundException();}
    }
    @Override public String getType(Uri uri){String n=String.valueOf(uri.getLastPathSegment()).toLowerCase();return n.endsWith(".pdf")?"application/pdf":n.endsWith(".csv")?"text/csv":"application/octet-stream";}
    @Override public ParcelFileDescriptor openFile(Uri uri,String mode) throws FileNotFoundException{
        if(!"r".equals(mode))throw new FileNotFoundException("read only");
        return ParcelFileDescriptor.open(resolve(uri),ParcelFileDescriptor.MODE_READ_ONLY);
    }
    @Override public Cursor query(Uri uri,String[] projection,String selection,String[] selectionArgs,String sortOrder){
        try{File f=resolve(uri);MatrixCursor c=new MatrixCursor(new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE});c.addRow(new Object[]{f.getName(),f.length()});return c;}catch(Exception e){return null;}
    }
    @Override public Uri insert(Uri uri,ContentValues values){throw new UnsupportedOperationException();}
    @Override public int delete(Uri uri,String selection,String[] selectionArgs){throw new UnsupportedOperationException();}
    @Override public int update(Uri uri,ContentValues values,String selection,String[] selectionArgs){throw new UnsupportedOperationException();}
}

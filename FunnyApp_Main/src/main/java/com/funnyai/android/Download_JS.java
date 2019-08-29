package com.funnyai.android;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.faendir.rhino_android.RhinoAndroidHelper;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.InputStream;
import java.net.URL;

class Download_JS extends AsyncTask {

    private BackGroundService mService1;
    private String url="";

    public Download_JS(BackGroundService mService1) {

        this.mService1=mService1;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        this.url=(String)objects[0];
        String js_script=S_File.Read(this.url);

        Context ct = new RhinoAndroidHelper().enterContext();//Context.enter();
        Scriptable scope = ct.initStandardObjects();


        Object sys = Context.javaToJS(this.mService1, scope);
        ScriptableObject.putProperty(scope, "sys", sys);

        ct.evaluateString(scope, js_script, null, 1, null);

        return "";
    }
}

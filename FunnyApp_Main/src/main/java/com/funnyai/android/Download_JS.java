package com.funnyai.android;


import android.os.AsyncTask;

import com.faendir.rhino_android.RhinoAndroidHelper;
import com.funnyai.tools.M_Json;
import com.funnyai.tools.M_Net;
import com.funnyai.tools.M_String;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

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


        Object s_sys = Context.javaToJS(this.mService1, scope);
        ScriptableObject.putProperty(scope, "s_sys", s_sys);
        Object s_net = Context.javaToJS(new M_Net(), scope);
        ScriptableObject.putProperty(scope, "s_net", s_net);
        Object s_string = Context.javaToJS(new M_String(), scope);
        ScriptableObject.putProperty(scope, "s_string", s_string);
        Object s_json = Context.javaToJS(new M_Json(), scope);
        ScriptableObject.putProperty(scope, "s_json", s_json);



        ct.evaluateString(scope, js_script, null, 1, null);

        return "";
    }
}

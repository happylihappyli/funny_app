package com.funnyai.tools;

import com.funnyai.android.BackGroundService;
import com.jayway.jsonpath.JsonPath;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.TreeMap;

public class M_Json {

    public void JSONArray(String key,String strReturn){
        JSONArray token = null;
        try {
            token = new JSONArray(strReturn);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        BackGroundService.pMap.put(key, token);
    }

    public int JSONArray_length(String key){
        JSONArray token = (JSONArray)BackGroundService.pMap.get(key);
        return token.length();
    }

    public void JSONObject_JSONArray(
            String key,String key2,String strPath){
        JSONObject token=(JSONObject) BackGroundService.pMap.get(key);
        JSONArray token2 = null;
        try {
            token2 = token.getJSONArray(strPath);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        BackGroundService.pMap.put(key2, token2);
    }


    public void JSONArray_JSONObject(
            String key,String key2,Integer index){
        JSONArray token=(JSONArray) BackGroundService.pMap.get(key);
        JSONObject token2 = null;
        try {
            token2 = (JSONObject) token.get(index);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        BackGroundService.pMap.put(key2, token2);
    }

    public void JSONObject(
            String key,String strReturn){
        JSONObject token = null;
        try {
            token = new JSONObject(strReturn);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        BackGroundService.pMap.put(key, token);
    }

    /**
     *
     * @param json
     * @param strPaths
     * @return
     */
    public String JSONObject_XPath(
            String json,String strPaths){
        String strLine = JsonPath.read(json,strPaths);
        return strLine;
    }

    public String to_string(
            String key){
        JSONObject token=(JSONObject) BackGroundService.pMap.get(key);
        return token.toString();
    }
}

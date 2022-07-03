package com.tf.routerrecorder.Utils;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class JsonHelper {
    private Context _context = null;

    /**
     * Constructor used only when we need the app context.
     * @param context Context of the app.
     */
    public JsonHelper(Context context) {
        _context = context;
    }

    /**
     * Normal constructor.
     */
    public JsonHelper() {
    }

    /**
     * Get Json Object from an Android Resource Path. This is use in the application.
     * @param path ID of the resource to open.
     * @return JsonObject
     */
    public JSONObject getJsonObjectFromPath(int path) {
        InputStream inputStream = _context.getResources().openRawResource(path);
        return getJsonObject(inputStream);
    }

    /**
     * Convert a InputStream into a JSONObject
     * @param inputStream data to conver to JSONObject
     * @return JSONObject
     */
    public JSONObject getJsonObject(InputStream inputStream) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        JSONObject object = null;
        int ctr;
        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String json = byteArrayOutputStream.toString();
            object = new JSONObject(json);
        } catch (JSONException e){
            e.printStackTrace();
        }
        return object;
    }
}

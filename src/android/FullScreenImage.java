

package com.spikeglobal.cordova.plugin.show.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

import android.annotation.SuppressLint;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;


import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.Base64;
import android.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.webkit.MimeTypeMap;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
	
import com.squareup.picasso.Picasso;

@SuppressLint("DefaultLocale")
public class FullScreenImage extends CordovaPlugin {
    private CallbackContext command;
	private static final String LOG_TAG = "FullScreenImagePlugin";

    /**
     * Executes the request.
     *
     * This method is called from the WebView thread.
     * To do a non-trivial amount of work, use:
     *     cordova.getThreadPool().execute(runnable);
     *
     * To run on the UI thread, use:
     *     cordova.getActivity().runOnUiThread(runnable);
     *
     * @param action   The action to execute.
     * @param args     The exec() arguments in JSON form.
     * @param callback The callback context used when calling
     *                 back into JavaScript.
     * @return         Whether the action was valid.
     */
    @Override
    public boolean execute (String action, JSONArray args,
                            CallbackContext callback) throws JSONException {

        this.command = callback;

        if ("showImageURL".equals(action)) {
            showImageURL(args);

            return true;
        }

        // Returning false results in a "MethodNotFound" error.
        return false;
    }

    private String getJSONProperty(JSONObject json, String property) throws JSONException {
        if (json.has(property)) {
            return json.getString(property);
        }
        return null;
    }

    
    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {  
            this.showImage(this.saveImage(bitmap));
        }

        @Override
        public void onBitmapFailed() {
            Log.v(FullScreenImage.LOG_TAG, "Could not load image");
        }
    };

    /**
     * Show image in full screen from local resources.
     *
     * @param url     File path in local system
     */
    public void showImageURL (JSONArray args) throws JSONException {

        JSONObject json = args.getJSONObject(0);
        String url = getJSONProperty(json, "url");
        Picasso.get().load(url).into(this.target);
    }

    private File saveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");    
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete (); 
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return file;
        } catch (Exception e) {
            Log.v(FullScreenImage.LOG_TAG, e.getMessage());
            return null;
        }
    }

    private void showImage (File file)  {
        
        Uri path = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(path, "image/*");
        this.cordova.getActivity().startActivity(intent);
    }
}
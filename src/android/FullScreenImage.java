

package com.spikeglobal.cordova.plugin.show.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.util.Locale;
import java.util.Random;

import android.annotation.SuppressLint;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Base64;
import android.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.webkit.MimeTypeMap;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

@SuppressLint("DefaultLocale")
public class FullScreenImage extends CordovaPlugin {
	private CallbackContext command;
    private static final String LOG_TAG = "FullScreenImagePlugin";
    
    private static FullScreenImage instance;
    private Handler uiHandler;
    private Runnable runnable;
    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            Log.v(FullScreenImage.LOG_TAG, "bitmap loaded");
            FullScreenImage.instance.showImage(FullScreenImage.instance.saveImage(bitmap));
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable d) {
            Log.v(FullScreenImage.LOG_TAG, "Could not load image");
        }

        @Override
        public void onPrepareLoad(Drawable d) {}
    };

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
        FullScreenImage.instance = this;

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

	/**
	 * Show image in full screen from local resources.
	 *
	 * @param url     File path in local system
	 */
	public void showImageURL (JSONArray args) throws JSONException {
        this.uiHandler = new Handler(Looper.getMainLooper());
        this.runnable = new Runnable() {

			@Override
			public void run() {
				try {
					Log.v(FullScreenImage.LOG_TAG, "show image url");
					JSONObject json = args.getJSONObject(0);
					String imageUrl = getJSONProperty(json, "url");
					Picasso.get()
						.load(imageUrl)
						.into(FullScreenImage.instance.target);
				}
				catch (Exception e) {
					Log.v(FullScreenImage.LOG_TAG, e.getMessage());
				}
			}
		};
		this.uiHandler.post(this.runnable);
	}

	private File saveImage(Bitmap finalBitmap) {

		Log.v(FullScreenImage.LOG_TAG, "save image");
		String root = Environment.getExternalStorageDirectory().toString();
		File myDir = new File(root + "/saved_images");
		myDir.mkdirs();
		Random generator = new Random();
		int n = 10000;
		n = generator.nextInt(n);
		String fname = "Image-" + n + ".jpg";
		File file = new File(myDir, fname);
		if (file.exists()) file.delete();
		Log.v(FullScreenImage.LOG_TAG, fname);
		try {
			FileOutputStream out = new FileOutputStream(file);
			finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
		} catch (java.io.IOException e){
			Log.v(FullScreenImage.LOG_TAG, e.getMessage());
		}
		return file;
	}

	private void showImage (File file)  {

		Log.v(FullScreenImage.LOG_TAG, "show saved image");
		Uri path = Uri.fromFile(file);
		Log.v(FullScreenImage.LOG_TAG, path.getPath());
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(path, "image/*");
		this.cordova.getActivity().startActivity(intent);
	}
}



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


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
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
			new OpenFileFromBitmap(bitmap, FullScreenImage.instance.cordova.getActivity().getApplicationContext()).execute();
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

	public class OpenFileFromBitmap extends AsyncTask<Void, Integer, String> {

		Context context;
		Bitmap bitmap;
		File file;

		public OpenFileFromBitmap(Bitmap bitmap, Context context) {
			this.bitmap = bitmap;
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... params) {

			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
			byte[] byteArray = bytes.toByteArray();
			String filename = this.getMD5(byteArray);
			this.file = new File(this.context.getCacheDir(), filename + ".jpg");
			try {
				FileOutputStream fo = new FileOutputStream(file);
				fo.write(byteArray);
				fo.flush();
				fo.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
			Log.v(FullScreenImage.LOG_TAG, "show saved image: " + this.file.getPath());
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.parse(this.file.getPath()), "image/*");
			FullScreenImage.instance.cordova.getActivity().startActivity(intent);
		}

		private String getMD5(byte[] source) {
			StringBuilder sb = new StringBuilder();
			java.security.MessageDigest md5 = null;
			try {
				md5 = java.security.MessageDigest.getInstance("MD5");
				md5.update(source);
			} catch (java.security.NoSuchAlgorithmException e) {
			}
			if (md5 != null) {
				for (byte b : md5.digest()) {
					sb.append(String.format("%02X", b));
				}
			}
			return sb.toString();
		}
	}
}

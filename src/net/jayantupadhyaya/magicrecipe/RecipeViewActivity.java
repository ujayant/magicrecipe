package net.jayantupadhyaya.magicrecipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/** Detailed view of the recipe */
public class RecipeViewActivity extends Activity {
	private WebView webView;
	String recipeLink;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_view);

		Intent intent = getIntent();
		recipeLink = intent.getStringExtra("RECIPE LINK");

		webView = (WebView) findViewById(R.id.webView);

		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
		settings.setLoadsImagesAutomatically(true);
		settings.setBuiltInZoomControls(true);
		settings.setSupportZoom(false);

		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.setInitialScale(40);
		webView.setScrollbarFadingEnabled(true);

		if (isNetworkAvailable()) {
			new GetRecipes().execute(recipeLink);

		} else {
			Toast.makeText(getApplicationContext(),
					R.string.network_unavailable, Toast.LENGTH_LONG).show();
		}

		webView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (isNetworkAvailable()) {
					new GetRecipes().execute(url);

				} else {
					Toast.makeText(getApplicationContext(),
							R.string.network_unavailable, Toast.LENGTH_LONG)
							.show();
				}
				return true;
			}

			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				final AlertDialog alertDialog = new AlertDialog.Builder(
						RecipeViewActivity.this).create();
				alertDialog.setTitle("Error");
				alertDialog.setMessage(description);
				alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								return;
							}
						});
				alertDialog.show();
			}

		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/** Check if the key event was the Back button and if there's history */
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
			webView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private class GetRecipes extends AsyncTask<String, Void, String> {

		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog
					.show(RecipeViewActivity.this, "", "Loading");
		}

		@Override
		protected String doInBackground(String... recipeURL) {
			try {
				return downloadRecipes(recipeURL[0]);
			} catch (IOException e) {
				return null;
			}
		}

		private String downloadRecipes(String recipeURL) throws IOException {
			InputStream is = null;
			try {
				URL url = new URL(recipeURL);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);
				conn.setRequestMethod("GET");
				conn.connect();
				int response = conn.getResponseCode();
				if (response == 200) {
					is = conn.getInputStream();

					/** Convert the InputStream into a string */
					return inputStreamtoString(is);
				}
			} finally {
				if (is != null) {
					is.close();
				}
			}
			return null;
		}

		/** onPostExecute displays the results of the AsyncTask. */
		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			if (result != null) {
				webView.loadData(result, "text/html", "UTF-8");
			} else {
				final AlertDialog alertDialog = new AlertDialog.Builder(
						RecipeViewActivity.this).create();
				alertDialog.setTitle("Page not found");
				alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								alertDialog.dismiss();
							}
						});
				alertDialog.show();
			}
		}
	}

	/** Convert InputStream to String */
	private String inputStreamtoString(InputStream in) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(in));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	/** Check the network status */
	private boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

}

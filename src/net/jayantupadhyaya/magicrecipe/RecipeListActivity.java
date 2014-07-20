package net.jayantupadhyaya.magicrecipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import net.jayantupadhyaya.magicrecipe.LoadMoreListView.OnLoadMoreListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RecipeListActivity extends ListActivity {

	/** list with the data to show in the listview */
	private ArrayList<RecipeItem> recipeItems;

	private static String ingredients;
	private static int pageNumber = 1;
	private EditText userInput;
	private JSONObject obj;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_screen);

		userInput = (EditText) findViewById(R.id.ingredients);
		/** Filter to restrict the input to letters and whitespaces */
		userInput.setFilters(new InputFilter[] { new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				for (int i = start; i < end; i++) {
					if (!Character.isWhitespace(source.charAt(i))) {
						if (!Character.isLetter(source.charAt(i))) {
							return "";
						}
					}
				}
				return null;
			}
		} });

		userInput
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEARCH) {
							searchRecipes(v);
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
							return true;
						}
						return false;
					}

				});

		recipeItems = new ArrayList<RecipeItem>();

		setListAdapter(new RecipeListAdapter(this, recipeItems));

		((LoadMoreListView) getListView())
				.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						RecipeItem item = recipeItems.get(position);
						Intent intent = new Intent(RecipeListActivity.this,
								RecipeViewActivity.class);
						intent.putExtra("RECIPE LINK", item.getRecipeLink());
						startActivity(intent);
						overridePendingTransition(R.anim.slide_in_right,
								R.anim.slide_out_left);
					}

				});

		/** set a listener to be invoked when the list reaches the end */
		((LoadMoreListView) getListView())
				.setOnLoadMoreListener(new OnLoadMoreListener() {
					public void onLoadMore() {
						/**
						 * Do the work to load more items at the end of list
						 * here
						 */
						if (isNetworkAvailable()) {
							pageNumber++;
							((LoadMoreListView) getListView())
									.getmLabLoadMore().setText(
											"Loading page" + " " + pageNumber);
							new LoadDataTask().execute(ingredients);
						}
					}
				});
	}

	public void searchRecipes(View view) {

		if (userInput.getText().toString().equals("")) {
			Toast.makeText(getApplicationContext(), "Ingredients Please!",
					Toast.LENGTH_LONG).show();
		} else {
			pageNumber = 1;
			recipeItems.clear();
			((BaseAdapter) getListAdapter()).notifyDataSetChanged();
			String ingredientList[] = (userInput.getText().toString().trim())
					.split("\\s+");
			if (ingredientList.length == 1) {
				ingredients = ingredientList[0];
			} else {
				ingredients = TextUtils.join(",", ingredientList);
			}

			if (isNetworkAvailable()) {
				new LoadDataTask().execute(ingredients);

			} else {
				Toast.makeText(getApplicationContext(),
						R.string.network_unavailable, Toast.LENGTH_LONG).show();
			}
		}
	}

	private class LoadDataTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				return downloadRecipeList(params[0]);
			} catch (IOException e) {
				return null;
			}
		}

		private String downloadRecipeList(String ingredients)
				throws IOException {
			InputStream is = null;
			try {
				URL url = new URL("http://www.recipepuppy.com/api/?i="
						+ ingredients + "&p=" + pageNumber);
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

		@Override
		protected void onPostExecute(String result) {
			try {
				if (result != null) {
					obj = (JSONObject) new JSONTokener(result).nextValue();
					JSONArray jsonArray = obj.getJSONArray("results");
					if (jsonArray.length() > 0) {
						for (int i = 0; i < jsonArray.length(); i++) {
							RecipeItem recipe = new RecipeItem();
							recipe.setRecipeTitle(jsonArray.getJSONObject(i)
									.getString("title"));
							recipe.setRecipeLink(jsonArray.getJSONObject(i)
									.getString("href"));
							recipe.setRecipeDescription(jsonArray
									.getJSONObject(i).getString("ingredients"));
							recipe.setThumbnailLink(jsonArray.getJSONObject(i)
									.getString("thumbnail"));
							recipeItems.add(recipe);
						}
					} else {
						Toast.makeText(getApplicationContext(),
								R.string.unavailable, Toast.LENGTH_LONG).show();
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			/** Notify the adapter that the data has changed */
			((BaseAdapter) getListAdapter()).notifyDataSetChanged();

			/** Call onLoadMoreComplete when the LoadMore task has finished */
			((LoadMoreListView) getListView()).onLoadMoreComplete();
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			/** Notify the loading more operation has finished */
			((LoadMoreListView) getListView()).onLoadMoreComplete();
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

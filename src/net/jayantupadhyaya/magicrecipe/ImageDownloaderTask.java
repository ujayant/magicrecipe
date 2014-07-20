package net.jayantupadhyaya.magicrecipe;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

/** Downloads the images in the recipe list */
public class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {

	private ImageView thumbnail;

	public ImageDownloaderTask(ImageView imageView) {
		thumbnail = imageView;
	}

	@Override
	// Actual download method, run in the task thread
	protected Bitmap doInBackground(String... params) {
		// params comes from the execute() call: params[0] is the url.
		try {
			return downloadBitmap(params[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** Downloads the image & converts it into BitMap */
	private Bitmap downloadBitmap(String urlString) throws IOException {
		InputStream is = null;
		try {
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setUseCaches(true);
			// conn.setDoInput(true);

			conn.connect();
			int response = conn.getResponseCode();
			if (response == 200) {
				is = conn.getInputStream();
				// Convert the InputStream into a string
				return BitmapFactory.decodeStream(is);
			}
		} finally {
			if (is != null) {
				is.close();
			}
		}
		return null;
	}

	@Override
	/** Once the image is downloaded, associates it to the imageView */
	protected void onPostExecute(Bitmap bitmap) {
		if (isCancelled()) {
			bitmap = null;
		}
		if (thumbnail != null) {
			if (bitmap != null) {
				thumbnail.setImageBitmap(bitmap);
			}
		}

	}
}

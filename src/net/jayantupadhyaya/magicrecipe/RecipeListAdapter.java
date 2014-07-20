package net.jayantupadhyaya.magicrecipe;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RecipeListAdapter extends ArrayAdapter<RecipeItem> {

	private ArrayList<RecipeItem> items;
	private LayoutInflater inflater;

	public RecipeListAdapter(Context context, ArrayList<RecipeItem> items) {
		super(context, 0, items);
		this.items = items;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = convertView;
		ViewHolder holder;
		final RecipeItem recipeitem = items.get(position);
		if (view == null) {
			view = inflater.inflate(R.layout.recipe_list_row, null);
			holder = new ViewHolder();
			holder.thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
			holder.recipeTitle = (TextView) view.findViewById(R.id.recipeTitle);
			holder.recipeDescription = (TextView) view
					.findViewById(R.id.recipeDescription);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		if (recipeitem != null) {
			holder.recipeTitle.setText(recipeitem.getRecipeTitle());
			holder.recipeDescription.setText(recipeitem.getRecipeDescription());
			if (recipeitem.getThumbnailLink() != null) {
				new ImageDownloaderTask(holder.thumbnail).execute(recipeitem
						.getThumbnailLink());
			}
		}
		return view;

	}

	static class ViewHolder {
		ImageView thumbnail;
		TextView recipeTitle;
		TextView recipeDescription;
	}

}

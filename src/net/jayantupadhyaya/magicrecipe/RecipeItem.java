package net.jayantupadhyaya.magicrecipe;
public class RecipeItem {

	private String recipeTitle;
	private String recipeLink;
	private String thumbnailLink;
	private String recipeDescription;

	public String getRecipeDescription() {
		return recipeDescription;
	}

	public void setRecipeDescription(String recipeDescription) {
		this.recipeDescription = recipeDescription;
	}

	public String getRecipeTitle() {
		return recipeTitle;
	}

	public void setRecipeTitle(String recipeTitle) {
		this.recipeTitle = recipeTitle;
	}

	public String getRecipeLink() {
		return recipeLink;
	}

	public void setRecipeLink(String recipeLink) {
		this.recipeLink = recipeLink;
	}

	public String getThumbnailLink() {
		return thumbnailLink;
	}

	public void setThumbnailLink(String thumbnailLink) {
		this.thumbnailLink = thumbnailLink;
	}

}

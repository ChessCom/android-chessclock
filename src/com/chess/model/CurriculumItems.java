package com.chess.model;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.07.13
 * Time: 6:55
 */
public class CurriculumItems {

	private String[] categories;
	private int[][] ids;
	private String[][] titles;
	private String[][] urls;
	private boolean[][] viewedMarks;


	public String[] getCategories() {
		return categories;
	}

	public void setCategories(String[] categories) {
		this.categories = categories;
	}

	public int[][] getIds() {
		return ids;
	}

	public void setIds(int[][] ids) {
		this.ids = ids;
	}

	public String[][] getTitles() {
		return titles;
	}

	public void setTitles(String[][] titles) {
		this.titles = titles;
	}

	public String[][] getUrls() {
		return urls;
	}

	public void setUrls(String[][] urls) {
		this.urls = urls;
	}

	public boolean[][] getViewedMarks() {
		return viewedMarks;
	}

	public void setViewedMarks(boolean[][] viewedMarks) {
		this.viewedMarks = viewedMarks;
	}
}

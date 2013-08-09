package com.chess.model;

import android.util.SparseArray;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 09.08.13
 * Time: 10:17
 */
public class CurriculumLessonsItems {
	private SparseArray<String> categories;
	private SparseArray<SparseArray<Integer>> ids;
	private SparseArray<SparseArray<String>> titles;
	private SparseArray<SparseArray<String>> urls;
	private boolean[][] viewedMarks;


	public SparseArray<String> getCategories() {
		return categories;
	}

	public void setCategories(SparseArray<String> categories) {
		this.categories = categories;
	}

	public SparseArray<SparseArray<Integer>> getIds() {
		return ids;
	}

	public void setIds(SparseArray<SparseArray<Integer>> ids) {
		this.ids = ids;
	}

	public SparseArray<SparseArray<String>> getTitles() {
		return titles;
	}

	public void setTitles(SparseArray<SparseArray<String>> titles) {
		this.titles = titles;
	}

	public SparseArray<SparseArray<String>> getUrls() {
		return urls;
	}

	public void setUrls(SparseArray<SparseArray<String>> urls) {
		this.urls = urls;
	}

	public boolean[][] getViewedMarks() {
		return viewedMarks;
	}

	public void setViewedMarks(boolean[][] viewedMarks) {
		this.viewedMarks = viewedMarks;
	}
}

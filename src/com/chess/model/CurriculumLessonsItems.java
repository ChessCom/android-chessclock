package com.chess.model;

import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 09.08.13
 * Time: 10:17
 */
public class CurriculumLessonsItems {
	private SparseArray<String> categories;
	private SparseArray<SparseIntArray> ids;
	private SparseArray<SparseArray<String>> titles;
	private SparseArray<SparseArray<String>> urls;
	private SparseIntArray displayOrder;
	private SparseArray<SparseBooleanArray> viewedMarks;


	public SparseArray<String> getCategories() {
		return categories;
	}

	public void setCategories(SparseArray<String> categories) {
		this.categories = categories;
	}

	public SparseArray<SparseIntArray> getIds() {
		return ids;
	}

	public void setIds(SparseArray<SparseIntArray> ids) {
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

	public SparseIntArray getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(SparseIntArray displayOrder) {
		this.displayOrder = displayOrder;
	}

	public SparseArray<SparseBooleanArray> getViewedMarks() {
		return viewedMarks;
	}

	public void setViewedMarks(SparseArray<SparseBooleanArray> viewedMarks) {
		this.viewedMarks = viewedMarks;
	}
}

package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.08.13
 * Time: 22:45
 */
public class LessonSingleItem {

/*
	"id": 2067,
	"name": "Rook versus pawn",
	"completed": false
*/

	private int id;
	private String name;
	private boolean completed;
	/* Local addition */
	private long categoryId;
	private long courseId;
	private String user;
	private boolean started;

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(long categoryId) {
		this.categoryId = categoryId;
	}

	public long getCourseId() {
		return courseId;
	}

	public void setCourseId(long courseId) {
		this.courseId = courseId;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}
}
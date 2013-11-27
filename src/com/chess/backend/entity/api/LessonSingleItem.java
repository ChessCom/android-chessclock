package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.08.13
 * Time: 22:45
 */
public class LessonSingleItem {

/*
	"id": 679,
	"completed": true,
	"name": "Double Attack",
	"initial_score": 1350,
	"last_score": 1400,
	"attempts": 2,
	"rating": 1748
*/

	private int id;
	private String name;
	private boolean completed;
	private int initial_score;
	private int last_score;
	private int attempts;
	private int rating;
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

	public int getInitialScore() {
		return initial_score;
	}

	public int getInitialScoreStr() {
		return initial_score;
	}

	public void setInitialScore(int initial_score) {
		this.initial_score = initial_score;
	}

	public int getLastScore() {
		return last_score;
	}

	public String getLastScoreStr() {
		return String.valueOf(last_score);
	}

	public void setLastScore(int last_score) {
		this.last_score = last_score;
	}

	public int getAttempts() {
		return attempts;
	}

	public String getAttemptsStr() {
		return String.valueOf(attempts);
	}

	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}

	public int getRating() {
		return rating;
	}

	public String getRatingStr() {
		return String.valueOf(rating);
	}

	public void setRating(int rating) {
		this.rating = rating;
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
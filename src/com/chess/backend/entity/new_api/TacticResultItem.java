package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 23.12.12
 * Time: 16:19
 */
public class TacticResultItem {
	private String user;
	private long id;
	private float score;
	private int userRatingChange;
	private int userRating;
	private int problemRatingChange;
	private int problemRating;

	public TacticResultItem() {
	}

	public TacticResultItem(String[] values) {
		score = Float.parseFloat(values[0]);
		userRatingChange = Integer.parseInt(values[1]);
		userRating = Integer.parseInt(values[2]);
		problemRatingChange = Integer.parseInt(values[3]);
		problemRating = Integer.parseInt(values[4]);
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public float getScore() {
		return score;
	}

	public String getScoreStr() {
		return String.valueOf(score);
	}

	public void setScore(float score) {
		this.score = score;
	}

	public void setScore(String score) {
		this.score = Float.parseFloat(score);
	}

	public int getUserRatingChange() {
		return userRatingChange;
	}

	public void setUserRatingChange(int userRatingChange) {
		this.userRatingChange = userRatingChange;
	}

	public int getUserRating() {
		return userRating;
	}

	public void setUserRating(int userRating) {
		this.userRating = userRating;
	}

	public int getProblemRatingChange() {
		return problemRatingChange;
	}

	public void setProblemRatingChange(int problemRatingChange) {
		this.problemRatingChange = problemRatingChange;
	}

	public int getProblemRating() {
		return problemRating;
	}

	public void setProblemRating(int problemRating) {
		this.problemRating = problemRating;
	}
}
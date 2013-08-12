package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 23.12.12
 * Time: 16:19
 */
public class TacticRatingData {
	/*
	"rating_info": {
            "score": 50,
            "user_rating_change": 1,
            "user_rating": 1444,
            "problem_rating_change": -1,
            "problem_rating": 1199
        }
	 */

	private String user;
	private long id;
	private float score;
	private int user_rating_change;
	private int user_rating;
	private int problem_rating_change;
	private int problem_rating;

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
		return user_rating_change;
	}

	public void setUserRatingChange(int userRatingChange) {
		this.user_rating_change = userRatingChange;
	}

	public int getUserRating() {
		return user_rating;
	}

	public void setUserRating(int userRating) {
		this.user_rating = userRating;
	}

	public int getProblemRatingChange() {
		return problem_rating_change;
	}

	public void setProblemRatingChange(int problemRatingChange) {
		this.problem_rating_change = problemRatingChange;
	}

	public int getProblemRating() {
		return problem_rating;
	}

	public void setProblemRating(int problemRating) {
		this.problem_rating = problemRating;
	}
}
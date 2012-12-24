package com.chess.model;


import com.chess.utilities.AppUtils;

public class TacticItemOld {

    private String user;
    private long id;
	private String fen;
	private String moveList;
	private int attemptCnt;
	private int passedCnt;
	private int rating;
	private int avgSeconds;
	private boolean stop;
    private boolean wasShowed;
    private boolean isRetry;
    private long secondsSpent;
    private TacticResultItemOld resultItem;

	public TacticItemOld() {
	}

	public TacticItemOld(String[] values) {
		id = Long.parseLong(values[0]);
		fen = values[1];
		moveList = values[2];
		attemptCnt = Integer.parseInt(values[3]);
		passedCnt = Integer.parseInt(values[4]);
		rating = Integer.parseInt(values[5]);
		avgSeconds = Integer.parseInt(values[6]);
	}



	public int getAttemptCnt() {
		return attemptCnt;
	}

	public int getAvgSeconds() {
		return avgSeconds;
	}

	public String getFen() {
		return fen;
	}

	public long getId() {
		return id;
	}

	public String getMoveList() {
		return moveList;
	}

	public int getPassedCnt() {
		return passedCnt;
	}

	public int getRating() {
		return rating;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public void setAttemptCnt(int attemptCnt) {
		this.attemptCnt = attemptCnt;
	}

	public void setAvgSeconds(int avgSeconds) {
		this.avgSeconds = avgSeconds;
	}

	public void setFen(String fen) {
		this.fen = fen;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setMoveList(String moveList) {
		this.moveList = moveList;
	}

	public void setPassedCnt(int passedCnt) {
		this.passedCnt = passedCnt;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

    public boolean isWasShowed() {
        return wasShowed;
    }

    public void setWasShowed(boolean wasShowed) {
        this.wasShowed = wasShowed;
    }

    public boolean isRetry() {
        return isRetry;
    }

    public void setRetry(boolean retry) {
        isRetry = retry;
    }

    public long getSecondsSpent() {
        return secondsSpent;
    }

    public String getSecondsSpentStr() {
        return AppUtils.getSecondsTimeFromSecondsStr(secondsSpent);
    }

    public void setSecondsSpent(long secondsSpent) {
        this.secondsSpent = secondsSpent;
    }

    public void increaseSecondsPassed() {
        secondsSpent++;
    }

    public TacticResultItemOld getResultItem() {
        return resultItem;
    }

    public void setResultItem(String[] values) {
        this.resultItem = new TacticResultItemOld(values);
    }

    public void setResultItem(TacticResultItemOld resultItem) {
        this.resultItem = resultItem;
    }

    public static class TacticResultItemOld {
        private String user;
        private long id;
        private float score;
        private int userRatingChange;
        private int userRating;
        private int problemRatingChange;
        private int problemRating;

        public TacticResultItemOld() {
        }

        public TacticResultItemOld(String[] values) {
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
}

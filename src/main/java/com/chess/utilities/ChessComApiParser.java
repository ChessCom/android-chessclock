package com.chess.utilities;

import java.util.ArrayList;

import com.chess.model.Game;
import com.chess.model.GameListElement;
import com.chess.model.Message;

public class ChessComApiParser {
	//Challenge a friend
	public static String[] GetFriendsParse(String result){
		if(result.trim().length() > 8)
			return result.substring(9).trim().split("[|]");
		else
			return new String[]{""};
	}
	//online new game
	public static ArrayList<GameListElement> ViewOpenChallengeParse(String result){
		ArrayList<GameListElement> output = new ArrayList<GameListElement>();

		String[] g = result.split("[|]");
		int count = g.length-1;

		int i, a;
		for(i=1;i<=count;i++){
			String[] tmp = new String[10];
			a = 0;
			for(String s: g[i].split(":")){
				tmp[a++] = s;
			}
			GameListElement gle = new GameListElement(0, tmp, false);
			output.add(gle);
		}
		return output;
	}
	//online game
	public static ArrayList<GameListElement> ViewChallengeParse(String result){
		ArrayList<GameListElement> output = new ArrayList<GameListElement>();

		String[] g = result.split("[|]");
		int count = g.length-1;

		int i, a;
		for(i=1;i<=count;i++){
			String[] tmp = new String[10];
			a = 0;
			for(String s: g[i].split(":")){
				tmp[a++] = s;
			}
			output.add(new GameListElement(0, tmp, false));
		}
		return output;
	}
	public static ArrayList<GameListElement> GetCurrentOnlineGamesParse(String result){
	    ArrayList<GameListElement> output = new ArrayList<GameListElement>();

      if (result.contains("|"))
      {
        return output;
      }

	    String[] GamesArray = result.split(":", 2);

	    int gamescount = new Integer(GamesArray[0].substring(8));
	    int i, j, inc=0;
	    String[] tmp = GamesArray[1].split(":");

	    for(i=0; i < gamescount; i++){
	    	String[] tmp2 = new String[17];
	        for(j=0;j<17;j++){
	        	tmp2[j] = tmp[inc++];
	        }
			output.add(new GameListElement(1, tmp2, false));
	    }
	    return output;
	}
	public static ArrayList<GameListElement> GetFinishedOnlineGamesParse(String result){
        ArrayList<GameListElement> output = new ArrayList<GameListElement>();

        String[] GamesArray = result.split(":", 2);
        int gamescount = new Integer(GamesArray[0].substring(8));

        int i, j, inc = 0;
        String[] tmp = GamesArray[1].split(":");
        for(i=0; i < gamescount; i++){
        	String[] tmp2 = new String[15];
        	for(j=0; j < 15; j++){
        		tmp2[j] = tmp[inc++];
        	}
			output.add(new GameListElement(2, tmp2, false));
        }
        return output;
	}
	//online game
	public static Game GetGameParseV3(String result){
		if(result.contains("Success")){
			return new Game(result.split(":"), false);
		}
		return null;
	}
	public static ArrayList<Message> ReciveMessages(String result){
		ArrayList<Message> output = new ArrayList<Message>();
		String[] tmp = result.substring(8).split(":");
		int i;
		for(i=0; i < tmp.length; i++){
			if(tmp[i].length() > 1)
				output.add(new Message(tmp[i].substring(0, 1), tmp[i].substring(1)));
		}
		return output;
	}
}

package com.chess.backend.entity.api;

import com.chess.statics.Symbol;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.08.13
 * Time: 7:42
 */
public class ArticleDetailsItem extends BaseResponseItem<ArticleDetailsItem.Data> {

	public static final String TYPE = "&-diagramtype:";
	public static final String FEN_CODE = "[FEN \"";
	public static final String END_PART = "\"]";

/*
    "id": 224,
    "title": "Testing thing",
    "create_date": 1369863079,
    "body": "<p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. yeah!</p>",
    "url": "testing-thing",
    "user_id": 5543,
    "username": "deepgreene",
    "category_name": "For Beginners",
    "category_id": 11,
    "chess_title": "NM",
    "first_name": "Вовк",
    "last_name": "Андрій",
    "country_id": 3,
    "avatar_url": "//d1lalstwiwz2br.cloudfront.net/images_users/avatars/deepgreene.gif",
    "view_count": 14,
    "comment_count": 3,
    "image_url": "//d1lalstwiwz2br.cloudfront.net/images_users/articles/testing-thing_origin.1.png",
    "is_thumb_in_content": true
*/

	public class Data extends ArticleItem.Data{
		private int view_count;
		private int comment_count;
		private List<Diagram> diagrams;

		public int getViewCount() {
			return view_count;
		}

		public int getCommentCount() {
			return comment_count;
		}

		public List<Diagram> getDiagrams() {
			return diagrams;
		}
	}



/*
	///////////////////////////
	// simpleDiagram Example //
	///////////////////////////

	&-diagramtype: simpleDiagram
	&-colorscheme: wooddark
	&-piecestyle: book
	&-float: left
	&-flip: false
	&-prompt: false
	&-coords: false
	&-size: 45
	&-lastmove:
	&-focusnode:
	&-beginnode:
	&-endnode:
	&-pgnbody:
	[Date "????.??.??"]
	[Result "*"]
	[FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"] *

	//////////////////////////
	// chessProblem Example //
	//////////////////////////
	&-diagramtype: chessProblem
	&-colorscheme: blue
	&-piecestyle: classic
	&-float: left
	&-flip: false
	&-prompt: false
	&-coords: false
	&-size: 45
	&-lastmove:
	&-focusnode:
	&-beginnode: 47
	&-endnode:
	&-pgnbody:
	[Event "Berlin 'Evergreen'"]
	[Site "?"]
	[Date "1852.??.??"]
	[Round "?"]
	[White "Anderssen, A"]
	[Black "Dufresne, J"]
	[Result "1-0"]
	[Plycount "47"]
	[Opening "Evans G"]
	1.e4 e5 2.Nf3 Nc6 3.Bc4 Bc5 4.b4 Bxb4 5.c3 Ba5 6.d4 exd4 7.O-O d3 8.Qb3 Qf6 9.e5
	Qg6 10.Re1 Nge7 11.Ba3 b5 12.Qxb5 Rb8 13.Qa4 Bb6 14.Nbd2 Bb7 15.Ne4 Qf5 16.Bxd3
	Qh5 17.Nf6+ gxf6 18.exf6 Rg8 19.Rad1 Qxf3 20.Rxe7+ Nxe7 21.Qxd7+ Kxd7 22.Bf5+ Ke8 23.Bd7+ Kf8 24.Bxe7# 1-0
	 */

/*
 	"diagrams": [
		{
			"diagram_id": 2421,
			"diagram_code": "&-diagramtype:\nchessProblem\n&-colorscheme:\nblue\n&-piecestyle:\nclassic\n&-float:\nleft\n&-flip:\nfalse\n&-prompt:\nfalse\n&-coords:\nfalse\n&-size:\n45\n&-lastmove:\n\n&-focusnode:\n\n&-beginnode:\n47\n&-endnode:\n\n&-pgnbody:\n[Event \"Berlin 'Evergreen'\"]\n[Site \"?\"]\n[Date \"1852.??.??\"]\n[Round \"?\"]\n[White \"Anderssen, A\"]\n[Black \"Dufresne, J\"]\n[Result \"1-0\"]\n[Plycount \"47\"]\n[Opening \"Evans G\"]\n\n1.e4 e5 2.Nf3 Nc6 3.Bc4 Bc5 4.b4 Bxb4 5.c3 Ba5 6.d4 exd4 7.O-O d3 8.Qb3 Qf6 9.e5 Qg6 10.Re1 Nge7 11.Ba3 b5 12.Qxb5 Rb8 13.Qa4 Bb6 14.Nbd2 Bb7 15.Ne4 Qf5 16.Bxd3 Qh5 17.Nf6+ gxf6 18.exf6 Rg8 19.Rad1 Qxf3 20.Rxe7+ Nxe7 21.Qxd7+ Kxd7 22.Bf5+ Ke8 23.Bd7+ Kf8 24.Bxe7# \n1-0"
		},
		{
			"diagram_id": 2422,
			"diagram_code": "&-diagramtype:\nsimpleDiagram\n&-colorscheme:\nwooddark\n&-piecestyle:\nbook\n&-float:\nleft\n&-flip:\nfalse\n&-prompt:\nfalse\n&-coords:\nfalse\n&-size:\n45\n&-lastmove:\n\n&-focusnode:\n\n&-beginnode:\n\n&-endnode:\n\n&-pgnbody:\n[Date \"????.??.??\"]\n[Result \"*\"]\n[FEN \"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1\"]\n\n\n*"
		}
	]
*/

	public static class Diagram {

		public static final int SIMPLE = 0;
		public static final int PROBLEM = 1;
		public static final int PROBLEM2 = 2;

		private long diagram_id;
		private String diagram_code;
		private int type = -1;

		public long getDiagramId() {
			return diagram_id;
		}

		public String getDiagramCode() {
			return diagram_code;
		}

		public void setDiagramId(long diagram_id) {
			this.diagram_id = diagram_id;
		}

		public void setDiagramCode(String diagram_code) {
			this.diagram_code = diagram_code;
		}

		public int getType() {
			if (type == -1) { // if undefined
				if (diagram_code.contains("simpleDiagram")) {
					type = SIMPLE;
				} else if (diagram_code.contains("chessProblem")) {
					type = PROBLEM;
				} else {
					type = PROBLEM2;
				}

				return type;
			} else {
				return type;
			}
		}

		public String getMoveList() {
			String moves = diagram_code.substring(diagram_code.indexOf("\n1.") + "\n".length());
			moves = moves.substring(0, moves.indexOf(Symbol.NEW_STR));
			return moves;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getFen() {
			String fenStr =diagram_code.substring(diagram_code.indexOf(FEN_CODE) + FEN_CODE.length(), diagram_code.length());
			fenStr = fenStr.substring(0, fenStr.indexOf("\"]"));
			return fenStr;
		}
	}
}

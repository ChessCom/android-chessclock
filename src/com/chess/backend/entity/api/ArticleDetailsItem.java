package com.chess.backend.entity.api;

import android.text.TextUtils;
import com.chess.statics.Symbol;
import com.chess.ui.engine.FenHelper;

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

	public static final String SIMPLE_DIAGRAM = "simpleDiagram";
	public static final String CHESS_GAME = "chessGame";
	public static final String CHESS_PROBLEM = "chessProblem";

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

	public class Data extends ArticleItem.Data {
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


	public static class Diagram {
		public static final String FOCUS_NODE = "&-focusnode:\n";
		public static final String FLIP = "&-flip:";

		public static final int SIMPLE = 0;
		public static final int CHESS_GAME = 1;
		public static final int PUZZLE = 2;
		private static final String WHITE_PLAYER = "White";
		private static final String BLACK_PLAYER = "Black";
		private static final String EVENT = "Event";
		private static final String SITE = "Site";
		private static final String DATE = "Date";
		private static final String RESULT = "Result";
		private static final String ECO = "ECO";
		private static final String PLY_COUNT = "Plycount";
		private static final String DELIMITER = " | ";

		private long diagram_id;
		private String diagram_code;
		private int type = -1;
		private String moveList;

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
				if (diagram_code.contains(SIMPLE_DIAGRAM)) {
					type = SIMPLE;
				} else if (diagram_code.contains(ArticleDetailsItem.CHESS_GAME)) {
					type = CHESS_GAME;
				} else if (diagram_code.contains(CHESS_PROBLEM)) {
					type = PUZZLE;
				}

				return type;
			} else {
				return type;
			}
		}

		public void setMoveList(String moveList) {
			this.moveList = moveList;
		}

		public String getMoveList() {
			if (TextUtils.isEmpty(moveList)) {
				// get [Event ] end
				int startIndex = diagram_code.indexOf("]\n\n");
				String movesPart = diagram_code.substring(startIndex + "]\n\n".length());

				moveList = movesPart.substring(0, movesPart.lastIndexOf(Symbol.NEW_STR));
			}
			return moveList;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getFen() {
			if (diagram_code.contains(FEN_CODE)) {
				String fenStr = diagram_code.substring(diagram_code.indexOf(FEN_CODE) + FEN_CODE.length(), diagram_code.length());
				fenStr = fenStr.substring(0, fenStr.indexOf("\"]"));
				return fenStr;
			} else {
				return FenHelper.DEFAULT_FEN;
			}
		}

		public String getPlayers() {
			String whitePlayer = getTagData(WHITE_PLAYER);
			if (whitePlayer.equals(Symbol.EMPTY)) {
				return Symbol.EMPTY;
			}
			return whitePlayer + " vs " + getTagData(BLACK_PLAYER);
		}

		public String getGameInfo() {
			String event = getTagData(EVENT);
			if (event.equals(Symbol.EMPTY)) {
				return Symbol.EMPTY;
			}
			String dateTag = getTagData(DATE);
			if (dateTag.length() > 4) {
				dateTag = dateTag.substring(0, 4);
			}

			return event + DELIMITER
					+ getTagData(SITE) + DELIMITER
					+ dateTag + DELIMITER
					+ "ECO :" + getTagData(ECO) + DELIMITER
					+ getTagData(RESULT);
		}

		public String getUserToMove() {
			String plyStr = getTagData(PLY_COUNT);
			if (TextUtils.isEmpty(plyStr)) {
				return Symbol.EMPTY;
			}

			int ply = Integer.parseInt(plyStr);
			String userToMove;
			if (ply % 2 == 0) {
				userToMove = "White to move";
			} else {
				userToMove = "Black to move";
			}

			return userToMove;
		}

		private String getTagData(String tag) {
			if (!diagram_code.contains("[" + tag + Symbol.SPACE)) {
				return Symbol.EMPTY;
			}
			int tagContentStartIndex = diagram_code.indexOf("[" + tag + " \"") + ("[" + tag + " \"").length();
			String tagContentStr = diagram_code.substring(tagContentStartIndex);
			tagContentStr = tagContentStr.substring(0, tagContentStr.indexOf("\"]\n"));
			return tagContentStr;
		}

		public boolean getFlip() {
			int index = diagram_code.indexOf(FLIP);
			String flipCode = diagram_code.substring(index + FLIP.length());
			flipCode = flipCode.substring(0, 6);
			return flipCode.contains("true");
		}

		public int getFocusNode() {
			int index = diagram_code.indexOf(FOCUS_NODE);
			String focusNode = diagram_code.substring(index + FOCUS_NODE.length());
			String nodeInt = focusNode.substring(0, focusNode.indexOf(Symbol.NEW_STR));
			if (!TextUtils.isEmpty(nodeInt)) {
				return Integer.valueOf(nodeInt);
			} else {
				return 0;
			}
		}
//		private String getResultTag(String tag) {
//			int tagContentStartIndex = diagram_code.indexOf("[" + tag + " \"") + ("[" + tag + " \"").length();
//			String tagContentStr = diagram_code.substring(tagContentStartIndex);
//			tagContentStr = tagContentStr.substring(0, tagContentStr.indexOf("\"]\n"));
//			return s;
//		}
	}

/*
&-diagramtype: chessGame &-colorscheme: blue &-piecestyle: classic &-float: center &-flip: false &-prompt: false &-coords: true &-size: 45 &-lastmove: &-focusnode: &-beginnode: &-endnode: &-pgnbody: [Event "Topalov-Laznicka m 2013"] [Site "Novy Bor CZE"] [Date "2013.09.19"] [Round "1"] [White "Topalov, V."] [Black "Laznicka, V."] [Result "1-0"] [ECO "D24"] [WhiteElo "2769"] [BlackElo "2677"] [Setup "1"] [FEN "6k1/pb4pp/4q3/2pn1p2/2B1r3/2PQR3/P2B1P1P/6K1 w - - 0 26"] [Plycount "9"] [Eventdate "2013.09.19"] [Eventtype "match"] [Eventrounds "6"] [Eventcountry "CZE"] [Source "Mark Crowther"] [Sourcedate "2013.09.23"] 26.f3 Rxe3 { The white king is more open, hence White wants to trade queens. } ( 26...Re5 27.Rxe5 Qxe5 28.Qe3 ) ( 26...Nxe3 $4 27.Bxe6+ ) 27.Qxe3 Qxe3+ 28.Bxe3 Kh8 { Normally, we don't want our king at the edge of the board, especially in endgames. The computer likes this move very much, however, it seems that the king transfer to e6 was a more human decision. Here it is justified as Black has time to take on c3. } ( 28...Kf7 29.Bxc5 a6 30.Kg2 Ke6 31.Bd4 g5 32.h4 h6 { White is still better here but Black is very close to equalizing. } ) 29.Bxc5 Nxc3 30.Bd4 1-0

"diagrams": [
            {
                "diagram_id": 1649150,
                "diagram_code": "&-diagramtype:\nchessGame\n&-colorscheme:\nblue\n&-piecestyle:\nclassic\n&-float:\ncenter\n&-flip:\nfalse\n&-prompt:\nfalse\n&-coords:\ntrue\n&-size:\n45\n&-lastmove:\n\n&-focusnode:\n\n&-beginnode:\n\n&-endnode:\n\n&-pgnbody:\n[Event \"Topalov-Laznicka m 2013\"]\n[Site \"Novy Bor CZE\"]\n[Date \"2013.09.19\"]\n[Round \"1\"]\n[White \"Topalov, V.\"]\n[Black \"Laznicka, V.\"]\n[Result \"1-0\"]\n[ECO \"D24\"]\n[WhiteElo \"2769\"]\n[BlackElo \"2677\"]\n[Setup \"1\"]\n[FEN \"6k1/pb4pp/4q3/2pn1p2/2B1r3/2PQR3/P2B1P1P/6K1 w - - 0 26\"]\n[Plycount \"9\"]\n[Eventdate \"2013.09.19\"]\n[Eventtype \"match\"]\n[Eventrounds \"6\"]\n[Eventcountry \"CZE\"]\n[Source \"Mark Crowther\"]\n[Sourcedate \"2013.09.23\"]\n\n26.f3 Rxe3 { The white king is more open, hence White wants to trade queens. } ( 26...Re5 27.Rxe5 Qxe5 28.Qe3 ) ( 26...Nxe3 $4 27.Bxe6+ ) 27.Qxe3 Qxe3+ 28.Bxe3 Kh8 { Normally, we don't want our king at the edge of the board,\nespecially in endgames. The computer likes this move very much, however, it seems\nthat the king transfer to e6 was a more human decision. Here it is justified\nas Black has time to take on c3. } ( 28...Kf7 29.Bxc5 a6 30.Kg2 Ke6 31.Bd4 g5 32.h4 h6 { White is still better here but Black is very close to\nequalizing. } ) 29.Bxc5 Nxc3 30.Bd4 \n1-0"
            },
            {
                "diagram_id": 1649154,
                "diagram_code": "&-diagramtype:\nchessGame\n&-colorscheme:\nblue\n&-piecestyle:\nclassic\n&-float:\ncenter\n&-flip:\nfalse\n&-prompt:\nfalse\n&-coords:\ntrue\n&-size:\n45\n&-lastmove:\n\n&-focusnode:\n\n&-beginnode:\n\n&-endnode:\n\n&-pgnbody:\n[Event \"Topalov-Laznicka m 2013\"]\n[Site \"Novy Bor CZE\"]\n[Date \"2013.09.19\"]\n[Round \"1\"]\n[White \"Topalov, V.\"]\n[Black \"Laznicka, V.\"]\n[Result \"1-0\"]\n[ECO \"D24\"]\n[WhiteElo \"2769\"]\n[BlackElo \"2677\"]\n[Setup \"1\"]\n[FEN \"7k/pb4pp/8/5p2/2BB4/2n2P2/P6P/6K1 b - - 0 30\"]\n[Plycount \"19\"]\n[Eventdate \"2013.09.19\"]\n[Eventtype \"match\"]\n[Eventrounds \"6\"]\n[Eventcountry \"CZE\"]\n[Source \"Mark Crowther\"]\n[Sourcedate \"2013.09.23\"]\n\n30...Nxa2 $4 { It seems that black should be able to draw the endgame down a\npiece but for three pawns but white two bishops are too strong. And if white\nexchange a pair of light-squared bishops the h-pawn will win the game. } ( 30...Bd5 $1 31.Bd3 ( 31.Bxd5 Ne2+ $1 ( 31...Nxd5 ) 32.Kf2 Nxd4 ) 31...Nxa2 32.Bxf5 Bxf3 33.Bxa7 ) ( 30...Nd5 31.Bxa7 Nf4 ) ( 30...Nb1 31.Kf2 ( 31.Bxa7 Nd2 32.Be2 Nxf3+ 33.Kf2 Nxh2 ) 31...Nd2 32.Be2 Bd5 33.Ke3 Nc4+ 34.Kf4 Nb6 35.a3 Bc4 ) ( 30...Na4 31.Bxa7 Bxf3 ) 31.Bxa2 Bxf3 32.Bb1 Bg4 ( 32...f4 33.Be5 a5 34.Ba2 h6 35.Bxf4 g5 36.Be5+ Kh7 37.Kf2 ) 33.Kg2 ( 33.Bxa7 { was possible too } ) 33...a5 34.Kg3 Bd1 35.Bxf5 Bb3 36.Kf4 Kg8 37.Ke5 Kf7 38.Bxh7 $1 { Almost getting the bishop trapped but not quite! } 38...g6 39.Kf4 Bc2 \n1-0"
            },
            {
                "diagram_id": 1649160,
                "diagram_code": "&-diagramtype:\nchessGame\n&-colorscheme:\nblue\n&-piecestyle:\nclassic\n&-float:\ncenter\n&-flip:\nfalse\n&-prompt:\nfalse\n&-coords:\ntrue\n&-size:\n45\n&-lastmove:\n\n&-focusnode:\n\n&-beginnode:\n\n&-endnode:\n\n&-pgnbody:\n[Event \"Topalov-Laznicka m 2013\"]\n[Site \"Novy Bor CZE\"]\n[Date \"2013.09.21\"]\n[Round \"3\"]\n[White \"Topalov, V.\"]\n[Black \"Laznicka, V.\"]\n[Result \"1/2-1/2\"]\n[ECO \"D24\"]\n[WhiteElo \"2769\"]\n[BlackElo \"2677\"]\n[Setup \"1\"]\n[FEN \"r1b3rk/p1p5/4p1qp/1p3p2/3P1B1p/2P1R3/P4PPQ/5BK1 b - - 0 23\"]\n[Plycount \"25\"]\n[Eventdate \"2013.09.19\"]\n[Eventtype \"match\"]\n[Eventrounds \"6\"]\n[Eventcountry \"CZE\"]\n[Source \"Mark Crowther\"]\n[Sourcedate \"2013.09.23\"]\n\n23...Kh7 24.Qxh4 e5 $1 { Freeing the space for the queen to go to e6, so the\nrook will have the g6-square to defend the h6-pawn. } 25.dxe5 $4 { It is\nunderstandable that GM Topalov wants to stick to the plan and keep an eye on\nthe h-pawn but being flexible here could have benefited White. } ( 25.Rg3 $1 Qe6 26.Rxg8 { Taking advantage of the fact that the rook is not protected with the\nother rook. } 26...Kxg8 27.Bxe5 Kf7 28.Be2 ) ( 25.Bxe5 Be6 26.Rg3 Qf7 ) 25...Qe6 26.Bd3 Rg6 { The worst is behind for Black as he managed to solidify\non the kingside. } 27.Rg3 Rxg3 28.fxg3 Bb7 29.Bc2 $6 ( 29.g4 $1 { This break\nshould have been done when the f-pawn is not defended. } 29...Be4 30.gxf5 Bxf5 31.Bxb5 Qb6+ 32.Kh2 Qg6 ) 29...Rf8 30.g4 $2 { Bad timing. } ( 30.Bb3 Qb6+ 31.Kh2 ) 30...Qg6 31.e6 $1 { Setting-up a bait. } 31...Qxe6 ( 31...Be4 $1 32.e7 Re8 33.Bd1 c5 34.a3 Qg7 35.Kf2 fxg4 36.Bxg4 c4 ) 32.Bxf5+ Rxf5 33.gxf5 Qxf5 34.Qxh6+ Kg8 35.Qg5+ Qxg5 \n1/2-1/2"
            },
            {
                "diagram_id": 1649162,
                "diagram_code": "&-diagramtype:\nchessGame\n&-colorscheme:\nblue\n&-piecestyle:\nclassic\n&-float:\ncenter\n&-flip:\nfalse\n&-prompt:\nfalse\n&-coords:\ntrue\n&-size:\n45\n&-lastmove:\n\n&-focusnode:\n\n&-beginnode:\n\n&-endnode:\n\n&-pgnbody:\n[Event \"Topalov-Laznicka m 2013\"]\n[Site \"Novy Bor CZE\"]\n[Date \"2013.09.23\"]\n[Round \"4\"]\n[White \"Laznicka, V.\"]\n[Black \"Topalov, V.\"]\n[Result \"0-1\"]\n[ECO \"E21\"]\n[WhiteElo \"2677\"]\n[BlackElo \"2769\"]\n[Setup \"1\"]\n[FEN \"r1r3k1/1b3pp1/1B2p2p/6q1/2P5/1Q3P2/P5PP/R4RK1 w - - 0 23\"]\n[Plycount \"18\"]\n[Eventdate \"2013.09.19\"]\n[Eventtype \"match\"]\n[Eventrounds \"6\"]\n[Eventcountry \"CZE\"]\n[Source \"Mark Crowther\"]\n[Sourcedate \"2013.09.23\"]\n\n23.a4 { Pushing the a- and c-pawns is White's plan, to tie the black rooks. } 23...e5 { Black's only counterplay is on the queenside. } 24.c5 Re8 ( 24...Rc6 { Transfering the rook to f6 or g6 right away is a good plan. } 25.a5 Rg6 26.Rf2 e4 { with counterplay } ) 25.a5 Qg6 { Black is keeping the tension. } ( 25...e4 26.fxe4 Re6 27.Qg3 Qxg3 28.hxg3 Rxe4 { White is capturing the d-file and\nthe seventh rank. } 29.Rad1 ) 26.Rad1 $1 { White can not make progress on the\nqueenside because Black has blocked the light squares. Hence White is taking\nthe d-file and try to get to the sixth or seventh ranks. } 26...e4 27.Rd6 Re6 $1 { There\nis no other alternative and Black opens up the f-file to put more pressure on\nthe f-pawn. } 28.Rxe6 fxe6 29.Qc2 Bc6 { Blocking the c-pawn. } ( 29...Rf8 30.c6 ) 30.Rd1 $2 ( 30.f4 $1 Qg4 31.h3 Qg3 32.Qf2 ( 32.Qa2 Bd5 ) 32...Qd3 33.Re1 Rf8 34.Re3 Qc4 { Although it is not easy White will eventually make\nprogress. } ) 30...Rf8 $1 31.Rd6 ( 31.fxe4 Bxe4 32.Qb2 Bd5 33.h3 ) 31...Qg5 $1 \n0-1"
            },
            {
                "diagram_id": 1649164,
                "diagram_code": "&-diagramtype:\nchessGame\n&-colorscheme:\nblue\n&-piecestyle:\nclassic\n&-float:\ncenter\n&-flip:\nfalse\n&-prompt:\nfalse\n&-coords:\ntrue\n&-size:\n45\n&-lastmove:\n\n&-focusnode:\n\n&-beginnode:\n\n&-endnode:\n\n&-pgnbody:\n[Event \"Topalov-Laznicka m 2013\"]\n[Site \"Novy Bor CZE\"]\n[Date \"2013.09.23\"]\n[Round \"4\"]\n[White \"Laznicka, V.\"]\n[Black \"Topalov, V.\"]\n[Result \"0-1\"]\n[ECO \"E21\"]\n[WhiteElo \"2677\"]\n[BlackElo \"2769\"]\n[Setup \"1\"]\n[FEN \"5rk1/6p1/1BbRp2p/P1P3q1/4p3/5P2/2Q3PP/6K1 w - - 0 32\"]\n[Plycount \"12\"]\n[Eventdate \"2013.09.19\"]\n[Eventtype \"match\"]\n[Eventrounds \"6\"]\n[Eventcountry \"CZE\"]\n[Source \"Mark Crowther\"]\n[Sourcedate \"2013.09.23\"]\n\n32.Rxc6 $4 ( 32.h4 $1 { The only move that leads to an equal position. } 32...Qf6 ( 32...Qxh4 33.Qf2 ) 33.fxe4 Qf1+ 34.Kh2 Rf2 35.Qxf2 Qxf2 36.Rxc6 Qxh4+ 37.Kg1 Qe1+ { Black does not have more than equality here. } ) 32...exf3 { The\nposition is lost for White already. } 33.g3 ( 33.Rxe6 f2+ 34.Kf1 Qxg2+ 35.Kxg2 f1=Q+ 36.Kg3 Qf3+ 37.Kh4 Rf4# ) 33...Qe5 34.Qf2 Qa1+ 35.Qf1 Qd4+ 36.Qf2 Qd1+ 37.Qf1 f2+ \n0-1"
            }
	 */

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
}

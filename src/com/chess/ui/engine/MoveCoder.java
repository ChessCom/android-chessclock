package com.chess.ui.engine;

import com.chess.backend.statics.StaticData;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 23.06.13
 * Time: 13:45
 */
public class MoveCoder {

	private static final String PROMOTE_LEFT = "( [ @ {";
	private static final String PROMOTE_RIGHT = ") ] $ }";
	private static final String PROMOTE_STRAIGHT = "^ _ # ~";
	private static final String[] PROMOTION_PIECES = new String[] {
			"(",
			"^",
			")",
			"[",
			"_",
			"]",
			"@",
			"#",
			"$",
			"{",
			"~",
			"}"
	};

	private static final String[] PROMOTION_PIECES_NAMES = new String[] {
			"N",
			"N",
			"N",
			"R",
			"R",
			"R",
			"B",
			"B",
			"B",
			"Q",
			"Q",
			"Q"
	};

	private static String[] originalMoves = new String[]{
			"a1",
			"b1",
			"c1",
			"d1",
			"e1",
			"f1",
			"g1",
			"h1",
			"a2",
			"b2",
			"c2",
			"d2",
			"e2",
			"f2",
			"g2",
			"h2",
			"a3",
			"b3",
			"c3",
			"d3",
			"e3",
			"f3",
			"g3",
			"h3",
			"a4",
			"b4",
			"c4",
			"d4",
			"e4",
			"f4",
			"g4",
			"h4",
			"a5",
			"b5",
			"c5",
			"d5",
			"e5",
			"f5",
			"g5",
			"h5",
			"a6",
			"b6",
			"c6",
			"d6",
			"e6",
			"f6",
			"g6",
			"h6",
			"a7",
			"b7",
			"c7",
			"d7",
			"e7",
			"f7",
			"g7",
			"h7",
			"a8",
			"b8",
			"c8",
			"d8",
			"e8",
			"f8",
			"g8",
			"h8"
	};

	private static Character[] encodedMoves = new Character[]{
			'a',
			'b',
			'c',
			'd',
			'e',
			'f',
			'g',
			'h',
			'i',
			'j',
			'k',
			'l',
			'm',
			'n',
			'o',
			'p',
			'q',
			'r',
			's',
			't',
			'u',
			'v',
			'w',
			'x',
			'y',
			'z',
			'A',
			'B',
			'C',
			'D',
			'E',
			'F',
			'G',
			'H',
			'I',
			'J',
			'K',
			'L',
			'M',
			'N',
			'O',
			'P',
			'Q',
			'R',
			'S',
			'T',
			'U',
			'V',
			'W',
			'X',
			'Y',
			'Z',
			'0',
			'1',
			'2',
			'3',
			'4',
			'5',
			'6',
			'7',
			'8',
			'9',
			'!',
			'?'
	};

	private static MoveCoder ourInstance;
	private final HashMap<Character, String> movesMap;

	public static MoveCoder getInstance() {
		if (ourInstance == null) {
			ourInstance = new MoveCoder();
		}
		return ourInstance;
	}

	private MoveCoder() {
		movesMap = new HashMap<Character, String>();
		for (int i = 0; i < encodedMoves.length; i++) {
			movesMap.put(encodedMoves[i], originalMoves[i]);
		}
	}

	public String decodeMoveList(String moveList) {
		int length = moveList.length();
		StringBuilder builder = new StringBuilder();
		for(int i=0; i<length; i++) {
			if (i%2 == 0) {
				int number = (i) / 2 + 1;
				builder.append(number).append(StaticData.SYMBOL_DOT);
			}
			builder.append(movesMap.get(moveList.charAt(i))).append(StaticData.SYMBOL_SPACE);
		}

		return builder.toString();
	}
}

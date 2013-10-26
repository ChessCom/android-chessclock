package com.chess.backend.entity.api.themes;

import com.chess.backend.entity.api.BaseResponseItem;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 16.10.13
 * Time: 19:20
 */
public class BoardsItem extends BaseResponseItem<List<BoardSingleItem.Data>> {

	public static final String PATH = "https://chess-redesign.s3.amazonaws.com/Boards/";

}

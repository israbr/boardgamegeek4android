package com.boardgamegeek.provider;

import com.boardgamegeek.provider.BggContract.GameRanks;
import com.boardgamegeek.provider.BggContract.Games;
import com.boardgamegeek.provider.BggDatabase.Tables;
import com.boardgamegeek.util.SelectionBuilder;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class GamesIdRankProvider extends BaseProvider {
	private static final String TABLE = Tables.GAME_RANKS;

	@Override
	protected SelectionBuilder buildSimpleSelection(Uri uri) {
		int gameId = Games.getGameId(uri);
		return new SelectionBuilder().table(TABLE).whereEquals(GameRanks.GAME_ID, gameId);
	}

	@Override
	protected String getPath() {
		return "games/#/ranks";
	}

	@Override
	protected String getType(Uri uri) {
		return GameRanks.CONTENT_TYPE;
	}

	@Override
	protected Uri insert(SQLiteDatabase db, Uri uri, ContentValues values) {
		int gameId = Games.getGameId(uri);
		values.put(GameRanks.GAME_ID, gameId);
		long rowId = insert(db, uri, values, TABLE);
		return GameRanks.buildGameRankUri((int) rowId);
	}
}
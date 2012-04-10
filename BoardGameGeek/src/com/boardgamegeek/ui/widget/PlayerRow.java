package com.boardgamegeek.ui.widget;

import java.text.DecimalFormat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.boardgamegeek.R;
import com.boardgamegeek.model.Player;
import com.boardgamegeek.util.ColorUtils;
import com.boardgamegeek.util.StringUtils;

public class PlayerRow extends LinearLayout {
	private Player mPlayer;
	private DecimalFormat mFormat = new DecimalFormat("0.0######");
	private Typeface mTypeface;

	private TextView mName;
	private TextView mUsername;
	private View mColorSwatch;
	private TextView mTeamColor;
	private TextView mScore;
	private TextView mStartingPosition;
	private TextView mRating;
	private ImageView mDeleteButton;
	private RelativeLayout mEditButton;

	private OnClickListener mEditClickListener;
	private OnClickListener mDeleteClickListener;

	public PlayerRow(Context context) {
		this(context, null);
	}

	public PlayerRow(Context context, AttributeSet attrs) {
		super(context, attrs);
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.row_player, this);
		initializeUi();
	}

	private void initializeUi() {
		mName = (TextView) findViewById(R.id.name);
		mUsername = (TextView) findViewById(R.id.username);
		mColorSwatch = findViewById(R.id.color_swatch);
		mTeamColor = (TextView) findViewById(R.id.team_color);
		mScore = (TextView) findViewById(R.id.score);
		mRating = (TextView) findViewById(R.id.rating);
		mStartingPosition = (TextView) findViewById(R.id.starting_position);

		mTypeface = mName.getTypeface();

		mDeleteButton = (ImageView) findViewById(R.id.log_player_delete);
		mDeleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(PlayerRow.this.getContext());
				builder.setTitle(R.string.are_you_sure_title).setMessage(R.string.are_you_sure_delete_player)
						.setCancelable(false).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (mDeleteClickListener != null) {
									mDeleteClickListener.onClick(PlayerRow.this);
								}
							}
						}).setNegativeButton(R.string.no, null);
				builder.create().show();
			}
		});

		mEditButton = (RelativeLayout) findViewById(R.id.log_player_edit);
		mEditButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEditClickListener != null) {
					mEditClickListener.onClick(PlayerRow.this);
				}
			}
		});
	}

	public void hideButtons() {
		mDeleteButton.setVisibility(View.GONE);
		mEditButton.setEnabled(false);
	}

	public void setOnEditListener(OnClickListener l) {
		mEditClickListener = l;
	}

	public void setOnDeleteListener(OnClickListener l) {
		mDeleteClickListener = l;
	}

	public void setPlayer(Player player) {
		mPlayer = player;
		bindUi();
	}

	public Player getPlayer() {
		return mPlayer;
	}

	private void bindUi() {
		if (mPlayer == null) {
			setText(mName, "");
			setText(mUsername, "");
			setText(mTeamColor, "");
			setText(mScore, "");
			setText(mRating, "");
			setText(mStartingPosition, "");
		} else {
			setText(mUsername, mPlayer.Username);
			mName.setText(mPlayer.Name);
			if (mPlayer.New && mPlayer.Win) {
				mName.setTypeface(mTypeface, Typeface.BOLD_ITALIC);
			} else if (mPlayer.New) {
				mName.setTypeface(mTypeface, Typeface.ITALIC);
			} else if (mPlayer.Win) {
				mName.setTypeface(mTypeface, Typeface.BOLD);
			} else {
				mName.setTypeface(mTypeface, Typeface.NORMAL);
			}

			setText(mTeamColor, mPlayer.TeamColor);
			int color = ColorUtils.parseColor(mPlayer.TeamColor);
			if (color != ColorUtils.TRANSPARENT) {
				mColorSwatch.setBackgroundColor(color);
				mColorSwatch.setVisibility(View.VISIBLE);
				mTeamColor.setVisibility(View.GONE);
			} else {
				mColorSwatch.setVisibility(View.GONE);
			}

			setText(mScore, mPlayer.Score);
			setText(mStartingPosition, (StringUtils.isInteger(mPlayer.StartingPosition)) ? "#"
					+ mPlayer.StartingPosition : mPlayer.StartingPosition);
			setText(mRating, (mPlayer.Rating > 0) ? mFormat.format(mPlayer.Rating) : "");
		}
	}

	private void setText(TextView textView, String text) {
		setText(textView, text, "");
	}

	private void setText(TextView textView, String text, String prefix) {
		if (TextUtils.isEmpty(text)) {
			textView.setVisibility(View.GONE);
		} else {
			textView.setVisibility(View.VISIBLE);
			textView.setText(prefix + text);
		}
	}
}
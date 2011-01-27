package com.chess.activities;

import java.util.ArrayList;

import com.chess.R;
import com.chess.core.CoreActivity;
import com.chess.lcc.android.LccHolder;
import com.chess.model.Selection;
import com.chess.utilities.Notifications;
import com.chess.views.SelectionAdapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class Preferences extends CoreActivity {
	private Button PrefBoard, PrefPices, PrefInvite;
	private Spinner AIM, /*Notif, */Strength;
	private CheckBox PrefSSB, PrefNEnable, PrefVacation, PrefShowCoords, PrefShowHighlights;
	private SelectionAdapter Boards, Pieces;
  private TextView onlineTitle;
  private LinearLayout afterIMoveLayout;
  private TextView computerTitle;
  private LinearLayout prefStrengthLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);

		PrefBoard = (Button)findViewById(R.id.PrefBoard);
		PrefPices = (Button)findViewById(R.id.PrefPices);
		PrefInvite = (Button)findViewById(R.id.PrefInvite);

		AIM = (Spinner)findViewById(R.id.PrefAIM);
		//Notif =  (Spinner)findViewById(R.id.PrefNotif);
		Strength = (Spinner)findViewById(R.id.PrefStrength);

		PrefSSB = (CheckBox)findViewById(R.id.PrefSSB);
		PrefNEnable = (CheckBox)findViewById(R.id.PrefNEnable);
		PrefVacation = (CheckBox)findViewById(R.id.PrefVacation);
		PrefShowCoords = (CheckBox)findViewById(R.id.PrefCoords);
		PrefShowHighlights = (CheckBox)findViewById(R.id.PrefHighlights);

    onlineTitle = (TextView)findViewById(R.id.onlineTitle);
    afterIMoveLayout = (LinearLayout)findViewById(R.id.afterIMoveLayout);
    computerTitle = (TextView)findViewById(R.id.computerTitle);
    prefStrengthLayout = (LinearLayout)findViewById(R.id.prefStrengthLayout);

    if (App.isLiveChess())
    {
      onlineTitle.setText("Live Game");
      afterIMoveLayout.setVisibility(View.GONE);
      PrefNEnable.setVisibility(View.GONE);
      PrefVacation.setVisibility(View.GONE);
      computerTitle.setVisibility(View.GONE);
      prefStrengthLayout.setVisibility(View.GONE);
    }
    else
    {
      onlineTitle.setText("Online Game");
      afterIMoveLayout.setVisibility(View.VISIBLE);
      PrefNEnable.setVisibility(View.VISIBLE);
      PrefVacation.setVisibility(View.VISIBLE);
      computerTitle.setVisibility(View.VISIBLE);
      prefStrengthLayout.setVisibility(View.VISIBLE);
    }

		//spiners
		AIM.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				App.SDeditor.putInt(App.sharedData.getString("username", "")+"aim", pos);
				App.SDeditor.commit();
			}
			@Override
			public void onNothingSelected(AdapterView<?> a) {}
		});
		/*Notif.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				App.SDeditor.putInt(App.sharedData.getString("username", "")+"notif", pos);
				App.SDeditor.commit();
			}
			@Override
			public void onNothingSelected(AdapterView<?> a) {}
		});*/
		Strength.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				App.SDeditor.putInt(App.sharedData.getString("username", "")+"strength", pos);
				App.SDeditor.commit();
			}
			@Override
			public void onNothingSelected(AdapterView<?> a) {}
		});
		//checkboxes
		PrefSSB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton b, boolean res) {
				if (App.isLiveChess())
		        {
		          App.SDeditor.putBoolean(App.sharedData.getString("username", "")+"ssblive", res);
		        }
		        else
		        {
		          App.SDeditor.putBoolean(App.sharedData.getString("username", "")+"ssb", res);
		        }
				App.SDeditor.commit();
			}
		});
		PrefNEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton b, boolean res) {
				App.SDeditor.putBoolean(App.sharedData.getString("username", "")+"notifE", res);
				App.SDeditor.commit();
				if(res)
					startService(new Intent(Preferences.this, Notifications.class));
				else
					stopService(new Intent(Preferences.this, Notifications.class));
			}
		});
		PrefShowCoords.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton b, boolean res) {
				App.SDeditor.putBoolean(App.sharedData.getString("username", "")+"coords", res);
				App.SDeditor.commit();
			}
		});
		PrefShowHighlights.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton b, boolean res) {
				App.SDeditor.putBoolean(App.sharedData.getString("username", "")+"highlights", res);
				App.SDeditor.commit();
			}
		});
		if(App.guest){
			PrefVacation.setVisibility(View.GONE);
		}
		else{
			PrefVacation.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String query = "";
            		if(PrefVacation.isChecked()){
            			query = "http://www." + LccHolder.HOST + "/api/vacation_leave?id="+App.sharedData.getString("user_token", "");
					} else{
						query = "http://www." + LccHolder.HOST + "/api/vacation_return?id="+App.sharedData.getString("user_token", "");
					}
            		if(appService != null){
        				appService.RunSingleTask(1,
        					query,
        					PD = ProgressDialog.show(Preferences.this, null, getString(R.string.loading), true)
        				);
        			}
				}
			});
		}
		//buttons
		PrefBoard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(Preferences.this)
	            .setTitle("Boards:")
	            .setAdapter(Boards, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface d, int pos) {
						App.SDeditor.putInt(App.sharedData.getString("username", "")+"board", pos);
						App.SDeditor.commit();
						PrefBoard.setCompoundDrawables(Boards.items.get(pos).image, null, null, null);
						App.LoadBoard(App.res_boards[pos]);
					}
				}).create().show();
			}
		});
		PrefPices.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(Preferences.this)
	            .setTitle("Pieces:")
	            .setAdapter(Pieces, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface d, int pos) {
						App.SDeditor.putInt(App.sharedData.getString("username", "")+"pieces", pos);
						App.SDeditor.commit();
						PrefPices.setCompoundDrawables(Pieces.items.get(pos).image, null, null, null);
						App.LoadPieces(App.res_pieces[pos]);
					}
				}).create().show();
			}
		});
		PrefInvite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		        emailIntent.setType("plain/text");
		        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Play Chess with me");
		        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "I just signed up to play chess at Chess.com on my Android device. Download the free app here: \n http://chess.com/android or signup at http://www.chess.com/register.html . Then find me - my username is \""+App.sharedData.getString("username", "")+"\". \n \n Sent from my Android");
		        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			}
		});
	}
	@Override
	protected void onResume() {
		super.onResume();

		AIM.post(new Runnable() {
			@Override
			public void run() {
				AIM.setSelection(App.sharedData.getInt(App.sharedData.getString("username", "")+"aim", 0));
			}
		});
		/*Notif.post(new Runnable() {
			@Override
			public void run() {
				Notif.setSelection(App.sharedData.getInt(App.sharedData.getString("username", "")+"notif", 0));
			}
		});*/
		Strength.post(new Runnable() {
			@Override
			public void run() {
				Strength.setSelection(App.sharedData.getInt(App.sharedData.getString("username", "")+"strength", 0));
			}
		});

		Strength.post(new Runnable() {
			@Override
			public void run() {
				if (App.isLiveChess())
		        {
		          PrefSSB.setChecked(App.sharedData.getBoolean(App.sharedData.getString("username", "")+"ssblive", false));
		        }
		        else
		        {
		          PrefSSB.setChecked(App.sharedData.getBoolean(App.sharedData.getString("username", "")+"ssb", false));
		        }
				}
		});
		PrefNEnable.post(new Runnable() {
			@Override
			public void run() {
				PrefNEnable.setChecked(App.sharedData.getBoolean(App.sharedData.getString("username", "")+"notifE", true));
			}
		});
		PrefShowCoords.post(new Runnable() {
			@Override
			public void run() {
				PrefShowCoords.setChecked(App.sharedData.getBoolean(App.sharedData.getString("username", "")+"coords", true));
			}
		});
		PrefShowHighlights.post(new Runnable() {
			@Override
			public void run() {
				PrefShowHighlights.setChecked(App.sharedData.getBoolean(App.sharedData.getString("username", "")+"highlights", true));
			}
		});

		//buttons defaults
		PrefBoard.setCompoundDrawablesWithIntrinsicBounds( getResources().getDrawable(getResources().getIdentifier("board_"+App.res_boards[App.sharedData.getInt(App.sharedData.getString("username", "")+"board", 0)], "drawable", "com.chess")), null, null, null);
		PrefPices.setCompoundDrawablesWithIntrinsicBounds( getResources().getDrawable(getResources().getIdentifier("pieces_"+App.res_pieces[App.sharedData.getInt(App.sharedData.getString("username", "")+"pieces", 0)], "drawable", "com.chess")), null, null, null);

		ArrayList<Selection> pieces = new ArrayList<Selection>(9);
		pieces.add(new Selection(getResources().getDrawable(R.drawable.pieces_alpha), getString(R.string.alpha)));
		pieces.add(new Selection(getResources().getDrawable(R.drawable.pieces_book), getString(R.string.book)));
		pieces.add(new Selection(getResources().getDrawable(R.drawable.pieces_cases), getString(R.string.cases)));
		pieces.add(new Selection(getResources().getDrawable(R.drawable.pieces_classic), getString(R.string.classicP)));
		pieces.add(new Selection(getResources().getDrawable(R.drawable.pieces_club), getString(R.string.club)));
		pieces.add(new Selection(getResources().getDrawable(R.drawable.pieces_condal), getString(R.string.condal)));
		pieces.add(new Selection(getResources().getDrawable(R.drawable.pieces_maya), getString(R.string.maya)));
		pieces.add(new Selection(getResources().getDrawable(R.drawable.pieces_modern), getString(R.string.modern)));
		pieces.add(new Selection(getResources().getDrawable(R.drawable.pieces_vintage), getString(R.string.vintage)));

		ArrayList<Selection> boards = new ArrayList<Selection>(9);
		boards.add(new Selection(getResources().getDrawable(R.drawable.board_blue), getString(R.string.blue)));
		boards.add(new Selection(getResources().getDrawable(R.drawable.board_brown), getString(R.string.brown)));
		boards.add(new Selection(getResources().getDrawable(R.drawable.board_green), getString(R.string.green)));
		boards.add(new Selection(getResources().getDrawable(R.drawable.board_grey), getString(R.string.grey)));
		boards.add(new Selection(getResources().getDrawable(R.drawable.board_marble), getString(R.string.marble)));
		boards.add(new Selection(getResources().getDrawable(R.drawable.board_red), getString(R.string.red)));
		boards.add(new Selection(getResources().getDrawable(R.drawable.board_tan), getString(R.string.tan)));
		boards.add(new Selection(getResources().getDrawable(R.drawable.board_wood_light), getString(R.string.woodlight)));
		boards.add(new Selection(getResources().getDrawable(R.drawable.board_wood_dark), getString(R.string.wooddark)));

		Pieces = new SelectionAdapter(Preferences.this, R.layout.selection_item, pieces);
		Boards = new SelectionAdapter(Preferences.this, R.layout.selection_item, boards);

		if(!App.guest){
			if(appService != null){
				appService.RunSingleTask(0,
					"http://www." + LccHolder.HOST + "/api/get_vacation_status?id="+App.sharedData.getString("user_token", ""),
					PD = ProgressDialog.show(Preferences.this, null, getString(R.string.loading), true)
				);
			}
		}
	}
	@Override
	public void LoadNext(int code) {}
	@Override
	public void LoadPrev(int code) {
		finish();
	}
	@Override
	public void Update(int code) {
		if(code == 0){
			if(!App.guest && response.trim().split("[+]")[1].equals("1")){
				PrefVacation.setChecked(true);
				PrefVacation.setText(getString(R.string.vacationOn));
			}
		} else if(code == 1){
			if(PrefVacation.isChecked())
				PrefVacation.setText(getString(R.string.vacationOn));
			else
				PrefVacation.setText(getString(R.string.vacationOff));
		}
	}
}

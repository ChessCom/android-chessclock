package com.chess.activities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.chess.R;
import com.chess.core.CoreActivity;
import com.chess.core.Tabs;
import com.chess.lcc.android.LccHolder;
import com.chess.utilities.MyProgressDialog;
import com.flurry.android.FlurryAgent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class Register extends CoreActivity {
	private EditText RegUsername, RegEmail, RegPassword, RegRetype;
	private Spinner Country;
	private Button RegSubmit;
	private int CID = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		RegUsername = (EditText)findViewById(R.id.RegUsername);
		RegEmail = (EditText)findViewById(R.id.RegEmail);
		RegPassword = (EditText)findViewById(R.id.RegPassword);
		RegRetype = (EditText)findViewById(R.id.RegRetype);
		RegSubmit = (Button)findViewById(R.id.RegSubmitBtn);
		Country = (Spinner)findViewById(R.id.country);


		String[] tmp = COUNTRIES.clone();
		java.util.Arrays.sort(tmp);
		int i = 0, k = 0;
		for(i = 0;i<tmp.length;i++){
			if(tmp[i].equals("United States")){
				k = i;
				break;
			}
		}
		final String[] tmp2 = new String[tmp.length];
		tmp2[0] = tmp[k];
		for(i = 0; i < tmp2.length; i++){
			if(i < k){
				tmp2[i+1] = tmp[i];
			} else if(i > k){
				tmp2[i] = tmp[i];
			}
		}

		ArrayAdapter<String> adapterF = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tmp2);
		adapterF.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Country.setAdapter(adapterF);

		Country.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				int i = 0;
				for(i = 0;i<COUNTRIES.length;i++){
					if(COUNTRIES[i].equals(tmp2[pos])){
						break;
					}
				}
				CID = Integer.parseInt(COUNTRIES_ID[i]);
			}
			@Override
			public void onNothingSelected(AdapterView<?> a) {

			}
		});

		RegSubmit.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		if(RegUsername.getText().toString().length() < 3){
	    			App.ShowMessage(getString(R.string.wrongusername));
	    			return;
	    		}
	    		if(RegEmail.getText().toString().equals("")){
	    			App.ShowMessage(getString(R.string.wrongemail));
	    			return;
	    		}
	    		if(RegPassword.getText().toString().length() < 6){
	    			App.ShowMessage(getString(R.string.wrongpassword));
	    			return;
	    		}
	    		if(!RegPassword.getText().toString().equals(RegRetype.getText().toString())){
	    			App.ShowMessage(getString(R.string.wrongretype));
	    			return;
	    		}
	    		if(CID == -1){
    				App.ShowMessage(getString(R.string.wrongcountry));
    				return;
    			}

	    		String query = "";
	    		try {
	    			query = "http://www." + LccHolder.HOST + "/api/register?username="+URLEncoder.encode(RegUsername.getText().toString(), "UTF-8")+"&password="+URLEncoder.encode(RegPassword.getText().toString(), "UTF-8")+"&email="+URLEncoder.encode(RegEmail.getText().toString(), "UTF-8")+"&country_id="+CID+"&app_type=android";
				} catch (Exception e) {}

				if(appService != null){
					appService.RunSingleTask(0,
						query,
						PD = new MyProgressDialog(ProgressDialog.show(Register.this, null, getString(R.string.loading), true))
					);
				}
	    	}
	    });
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
			String query = "";
			try {
				query = "http://www." + LccHolder.HOST + "/api/login?username="+URLEncoder.encode(RegUsername.getText().toString(), "UTF-8")+"&password="+URLEncoder.encode(RegPassword.getText().toString(), "UTF-8");
			} catch (Exception e) {}

			if(appService != null){
				appService.RunSingleTask(1,
					query,
					PD = new MyProgressDialog(ProgressDialog.show(Register.this, null, getString(R.string.loading), true))
				);
			}
		} else if(code == 1){
      FlurryAgent.onEvent("New Account Created", null);
			String[] r = response.split(":");
			App.SDeditor.putString("username", RegUsername.getText().toString().toLowerCase());
			App.SDeditor.putString("password", RegPassword.getText().toString());
			App.SDeditor.putString("premium_status", r[0].split("[+]")[1]);
			App.SDeditor.putString("api_version", r[1]);
			try {
				App.SDeditor.putString("user_token", URLEncoder.encode(r[2], "UTF-8"));
			} catch (UnsupportedEncodingException e) {}
			App.SDeditor.putString("user_session_id", r[3]);
			App.SDeditor.commit();
			startActivity(new Intent(Register.this, Tabs.class));
            finish();
			App.ShowMessage(getString(R.string.congratulations));
		}
	}

	static final String[] COUNTRIES_ID = new String[] {
		"2",
		"3",
		"4",
		"5",
		"9",
		"10",
		"11",
		"12",
		"13",
		"14",
		"15",
		"17",
		"18",
		"19",
		"20",
		"21",
		"22",
		"23",
		"24",
		"25",
		"26",
		"27",
		"28",
		"29",
		"30",
		"31",
		"32",
		"33",
		"34",
		"35",
		"36",
		"37",
		"38",
		"39",
		"40",
		"41",
		"42",
		"43",
		"44",
		"45",
		"46",
		"47",
		"48",
		"49",
		"50",
		"51",
		"52",
		"53",
		"54",
		"55",
		"56",
		"57",
		"58",
		"59",
		"60",
		"61",
		"62",
		"63",
		"64",
		"65",
		"66",
		"67",
		"68",
		"69",
		"70",
		"71",
		"72",
		"73",
		"74",
		"75",
		"76",
		"77",
		"78",
		"79",
		"80",
		"81",
		"82",
		"83",
		"84",
		"85",
		"86",
		"87",
		"88",
		"89",
		"90",
		"91",
		"92",
		"93",
		"94",
		"95",
		"96",
		"97",
		"98",
		"99",
		"100",
		"101",
		"102",
		"103",
		"104",
		"105",
		"106",
		"107",
		"108",
		"109",
		"110",
		"111",
		"112",
		"113",
		"114",
		"115",
		"116",
		"117",
		"118",
		"119",
		"120",
		"122",
		"123",
		"124",
		"125",
		"126",
		"127",
		"128",
		"129",
		"130",
		"131",
		"132",
		"133",
		"134",
		"135",
		"136",
		"137",
		"138",
		"139",
		"140",
		"141",
		"142",
		"143",
		"145",
		"146",
		"147",
		"148",
		"149",
		"150",
		"151",
		"152",
		"153",
		"154",
		"156",
		"157",
		"158",
		"159",
		"160",
		"161",
		"162",
		"163",
		"164",
		"165",
		"166",
		"169",
		"171",
		"172",
		"173",
		"174",
		"175",
		"176",
		"177",
		"178",
		"179",
		"180",
		"181",
		"182",
		"183",
		"184",
		"185",
		"186",
		"187",
		"188",
		"189",
		"190",
		"191",
		"192",
		"193",
		"194",
		"195",
		"196",
		"197",
		"198",
		"199",
		"200",
		"201",
		"202",
		"203",
		"204",
		"206",
		"207",
		"208",
		"209",
		"210",
		"211",
		"212",
		"213",
		"214",
		"215",
		"216",
		"217",
		"218",
		"219",
		"220",
		"221",
		"222",
		"223",
		"224",
		"225",
		"231",
		"241",
		"261",
		"271",
		"281",
		"291",
		"301",
		"311",
		"312",
		"313",
		"314",
		"315",
		"316",
		"317",
		"318",
		"319",
		"320",
		"321",
		"322",
		"323",
		"324",
		"325",
		"326",
		"327",
		"328",
		"329"
	};
	static final String[] COUNTRIES = new String[] {
		"United States",
		"Canada",
		"Argentina",
		"Belgium",
		"Afghanistan",
		"Albania",
		"Andorra",
		"Anguilla",
		"Antigua/Barbuda",
		"Armenia",
		"Aruba",
		"Australia",
		"Austria",
		"Bahamas",
		"Bahrain",
		"Barbados",
		"Belarus",
		"Belize",
		"Bermuda",
		"Bolivia",
		"Bosnia-Herzegovina",
		"Brazil",
		"Bulgaria",
		"Canary Islands",
		"Cayman Islands",
		"Channel Islands",
		"Chile",
		"China",
		"Colombia",
		"Costa Rica",
		"Croatia",
		"Cuba",
		"Curacao",
		"Cyprus",
		"Czech Republic",
		"Denmark",
		"Dominica",
		"Dominican Republic",
		"Ecuador",
		"Egypt",
		"El Salvador",
		"Estonia",
		"Falkland Islands",
		"Faroe Islands",
		"Fiji",
		"Finland",
		"France",
		"Georgia",
		"Germany",
		"Gibraltar",
		"Greece",
		"Greenland",
		"Grenada",
		"Guadeloupe",
		"Guam",
		"Guatemala",
		"Guernsey",
		"Guyana",
		"Haiti",
		"Honduras",
		"Hong Kong",
		"Hungary",
		"Iceland",
		"India",
		"Indonesia",
		"Iran",
		"Iraq",
		"Ireland",
		"Isle of Man",
		"Israel",
		"Italy",
		"Jamaica",
		"Japan",
		"Jersey",
		"Jordan",
		"Kazakhstan",
		"Kiribati",
		"Korea, South",
		"Kuwait",
		"Latvia",
		"Lebanon",
		"Liechtenstein",
		"Lithuania",
		"Luxembourg",
		"Macao",
		"Macedonia",
		"Malaysia",
		"Malta",
		"Martinique",
		"Mexico",
		"Moldova",
		"Monaco",
		"Montserrat",
		"Nauru",
		"Nepal",
		"Netherlands",
		"New Zealand",
		"Nicaragua",
		"Norway",
		"Oman",
		"Pakistan",
		"Panama",
		"Papua New Guinea",
		"Paraguay",
		"Peru",
		"Philippines",
		"Poland",
		"Portugal",
		"Puerto Rico",
		"Romania",
		"Russia",
		"Saint Croix",
		"Saint Kitts/Nevis",
		"Saint Lucia",
		"Saint Pierre/Miquelon",
		"San Marino",
		"Saudi Arabia",
		"Serbia-Montenegro",
		"Singapore",
		"Slovakia",
		"Slovenia",
		"Solomon Islands",
		"South Africa",
		"South Georgia",
		"Suriname",
		"Sweden",
		"Switzerland",
		"Taiwan",
		"Thailand",
		"Tonga",
		"Trinidad/Tobago",
		"Turkey",
		"Turkmenistan",
		"Tuvalu",
		"Ukraine",
		"United Arab Emirates",
		"Uruguay",
		"Uzbekistan",
		"Vanuatu",
		"Vatican City",
		"Venezuela",
		"Vietnam",
		"Western Samoa",
		"Yemen",
		"Yugoslavia",
		"American Samoa",
		"Saint Vincent",
		"Azerbaijan",
		"Mongolia",
		"Syria",
		"England",
		"Marshall Islands",
		"Northern Ireland",
		"Scotland",
		"Spain",
		"United Kingdom",
		"US Virgin Islands",
		"Wales",
		"Pago Pago",
		"Saint John",
		"Saint Thomas",
		"Great Britain",
		"Holland",
		"South Korea",
		"Kyrgyzstan",
		"Bangladesh",
		"Sudan",
		"Benin",
		"Bhutan",
		"Botswana",
		"Brunei",
		"Burundi",
		"Cambodia",
		"Cameroon",
		"Cape Verde",
		"Central Africa",
		"Chad",
		"Congo",
		"Ivory Coast",
		"Djibouti",
		"Equatorial Guinea",
		"Gabon",
		"Ghana",
		"Kenya",
		"Laos",
		"Liberia",
		"Madagascar",
		"Morocco",
		"Mozambique",
		"Myanmar",
		"Namibia",
		"Niger",
		"Nigeria",
		"Qatar",
		"Rwanda",
		"Samoa",
		"Sao Tome/Principe",
		"Senegal",
		"Sierra Leone",
		"Somalia",
		"Sri Lanka",
		"Swaziland",
		"Tajikistan",
		"Tanzania",
		"Timor-Leste",
		"Togo",
		"Tunisia",
		"Uganda",
		"Zambia",
		"Zimbabwe",
		"Algeria",
		"Mauritania",
		"International",
		"Serbia",
		"Montenegro",
		"Libya",
		"Gambia",
		"Malawi",
		"Palestine",
		"Ethiopia",
		"Mauritius",
		"Lesotho",
		"Mali",
		"Maldives",
		"Catalonia",
		"Galicia",
		"Kosovo",
		"DR Congo",
		"Angola",
		"Comoros",
		"Eritrea",
		"Guinea",
		"Guinea-Bissau",
		"Micronesia",
		"North Korea",
		"Palau",
		"Seychelles",
		"Western Sahara",
		"British Virgin Islands"
	  };
}

package e.user.ibusdriver.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import e.user.ibusdriver.R;
import e.user.ibusdriver.model.Login_SignupTask;
import e.user.ibusdriver.model.MainActivityModelV1;

public class MainActivity extends AppCompatActivity {

	@InjectView(R.id.spinner)
	Spinner spinner ;
	@InjectView(R.id.et_bus_code)
	EditText et_bus_code;
	@InjectView(R.id.bn_send)
	Button bn_send;
	@InjectView(R.id.toolbar)
	Toolbar toolbar;
	@InjectView(R.id.tv_title)
	TextView tv_title;
	@InjectView(R.id.bn_direction0)
	Button bn_direction0;
	@InjectView(R.id.bn_direction1)
	Button bn_direction1;

	private MainActivityModelV1 presenter;
	private String city;
	private String busCode;
	private int direction;

	private final static String TAG = "MainActivity Test";
//test 的 APPKey
	//public final static String APPID = "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF";
	//public final static String APPKey = "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF";

	// real 的 APPKey
	public final static String APPID = "ec95e32b647c4584ae27590ab720c0a0";
	public final static String APPKey = "fxyejxv8VeRpjPWmTbfQYgx8rcE";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.inject(this);
		initView();
	}

	private void initView(){
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		//new Login_SignupTask().execute("http://140.115.52.194:8000/user/api=register");
		int PERMISSION_ALL = 4;
		String[] PERMISSIONS = {
				android.Manifest.permission.ACCESS_NETWORK_STATE,
				android.Manifest.permission.ACCESS_FINE_LOCATION,
				android.Manifest.permission.INTERNET,
				android.Manifest.permission.ACCESS_COARSE_LOCATION
		};
		if(!hasPermissions(this, PERMISSIONS)){
			ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
		}
		presenter = new MainActivityModelV1(getApplicationContext());
		presenter.setSpinner(spinner);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				city = presenter.getSelectedCityName(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Toast.makeText(getApplicationContext(),"請選擇城市",Toast.LENGTH_SHORT).show();
			}
		});


		// focus sometimes not correct in here
		et_bus_code.setFocusableInTouchMode(true);
		et_bus_code.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean b) {
				busCode = et_bus_code.getText().toString();
				Log.d(TAG,"focus" + busCode);
			}
		});

		et_bus_code.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				busCode = et_bus_code.getText().toString();
				Log.d(TAG,busCode);
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});

		presenter.setTitleText(tv_title,"輸入公車路線");
		bn_direction0.setOnClickListener(v -> {
			direction = presenter.chooseDirection(bn_direction0,bn_direction1,0);
		});
		bn_direction1.setOnClickListener((v)->{
			direction = presenter.chooseDirection(bn_direction0,bn_direction1,1);
		});
		bn_send.setOnClickListener(view -> {
			view.setClickable(false);
			Intent intent = new Intent(MainActivity.this,BusRoute.class);
			intent.putExtra("city",city);
			intent.putExtra("busCode",busCode);
			intent.putExtra("direction",direction);
			startActivity(intent);
			finish();
		});

	}




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	public static boolean hasPermissions(Context context, String... permissions) {
		if (context != null && permissions != null) {
			for (String permission : permissions) {
				if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
					return false;
				}
			}
		}
		return true;
	}
}

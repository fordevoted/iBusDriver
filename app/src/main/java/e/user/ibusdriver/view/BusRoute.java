package e.user.ibusdriver.view;

import android.content.Context;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import e.user.ibusdriver.R;
import e.user.ibusdriver.model.BusRouteModelV1;
import e.user.ibusdriver.model.BusRouteModelV2;

public class BusRoute extends AppCompatActivity {

	@InjectView(R.id.tv_stop_name)
	TextView tv_stop_name;
	@InjectView(R.id.tv_people_waiting)
	TextView tv_people_waiting;
	@InjectView(R.id.tv_people_on_bus)
	TextView tv_people_on_bus;
	@InjectView(R.id.tv_people_service)
	TextView tv_people_service;
	@InjectView(R.id.tv_decision)
	TextView tv_decision;
	@InjectView(R.id.toolbar)
	Toolbar toolbar;
	@InjectView(R.id.tv_title)
	TextView tv_title;
	@InjectView(R.id.bn_report)
	ImageButton bn_report;

	private BusRouteModelV1 presenter;
	private Handler handler;
	private String city;
	private String busCode;
	private String routeID;
	private List<Map<String,Object>> data;
	private List<Map<String,Object>> waitingPeopleData;
	private Runnable runGetWaiting;
	private Runnable runData;
	private int position = 0;
	private int direction;

	private final static String TAG = "BusRoute Test";
	private final static int NEXTSTOPSEC = 8000;
	private final static int INCOMINGSEC = 5000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bus_route_layout);
		ButterKnife.inject(this);
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int amStreamMusicMaxVol = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
		am.setStreamVolume(AudioManager.STREAM_ALARM, amStreamMusicMaxVol, 0);

		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			city = bundle.getString("city");
			busCode = bundle.getString("busCode");
			direction = bundle.getInt("direction");
			Log.d(TAG, "bus and city test: " + city + "  " +busCode);
		}
		initView();
	}

	@Override
	protected void onPause() {
		super.onPause();
		handler.removeCallbacksAndMessages(null);
	}

	private void initView(){
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		presenter = new BusRouteModelV1(getApplicationContext());
		presenter.setTitleText(tv_title, BusRouteModelV1.NEXTSTOP);
		presenter.requestRouteData(city,busCode);
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		presenter.setFontSizeByMobile(new TextView[]{tv_stop_name,tv_decision,tv_people_on_bus,tv_people_service,tv_people_waiting},size);

		bn_report.setOnClickListener((v)->{
			Toast.makeText(getApplicationContext(),"Driver report the number of people in th car",Toast.LENGTH_SHORT).show();
		});

		handler = new Handler();
		runData = new Runnable() {
			@Override
			public void run() {
				position = (++position) % waitingPeopleData.size();
				presenter.updateData( tv_people_waiting, tv_stop_name, tv_people_on_bus, tv_people_service, tv_decision,
						data,waitingPeopleData,position,presenter.decideLeave(waitingPeopleData,position));
				presenter.requestWaitingPeopleNumber(city,busCode,direction);
				handler.postDelayed(runGetWaiting,NEXTSTOPSEC);
				presenter.setTitleText(tv_title,BusRouteModelV1.NEXTSTOP);
				presenter.speak(BusRouteModelV1.NEXTSTOP+"  "+ waitingPeopleData.get(position).get("stopName"));

			}
		};
		runGetWaiting = new Runnable() {
			@Override
			public void run() {
				if(presenter.askWaitingPeopleNumberTask.resultBack()!=null){
					waitingPeopleData = presenter.askWaitingPeopleNumberTask.resultBack();
					presenter.setTitleText(tv_title,BusRouteModelV1.COMINGUPSTOP);
					presenter.updateData( tv_people_waiting, tv_stop_name, tv_people_on_bus, tv_people_service, tv_decision,
							data,waitingPeopleData,position,presenter.decideLeave(waitingPeopleData,position));
					presenter.speak(BusRouteModelV1.COMINGUPSTOP +"  "+ waitingPeopleData.get(position).get("stopName"));
					if(presenter.decideLeave(waitingPeopleData,position)){
						presenter.speak(BusRouteModelV2.STOP);
					}else{
						presenter.speak(BusRouteModelV2.LEAVE);
					}
					handler.postDelayed(runData,INCOMINGSEC);
				}else{
					handler.postDelayed(this,100);
				}
			}
		};

		final Runnable runInitDetail = new Runnable() {
			@Override
			public void run() {
				if(presenter.askBusDetailTask.resultBack()!= null){
					data = presenter.askBusDetailTask.resultBack();
					presenter.requestWaitingPeopleNumber(city,busCode,direction);
					handler.post(runGetWaiting);
				}else{
					handler.postDelayed(this,100);
					Log.d(TAG, "runInitDetail test enter else");
				}
			}
		};
		Runnable runInit = new Runnable() {
			@Override
			public void run() {
				if(presenter.askBusRouteTask.resultBack() != null){
					routeID = presenter.askBusRouteTask.resultBack();
					presenter.requestData(city,busCode,routeID);
					handler.post(runInitDetail);
				}else{
					handler.postDelayed(this,100);
				}
			}
		};
		handler.post(runInit);
	}
}

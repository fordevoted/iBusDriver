package e.user.ibusdriver.model;

import android.content.Context;
import android.graphics.Point;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import e.user.ibusdriver.presenter.BusRoutePresenter;
import e.user.ibusdriver.view.BusRoute;

public class BusRouteModelV2 implements BusRoutePresenter {

	private Context  context;
	public final static String NEXTSTOP = "下一站";
	public final static String COMINGUPSTOP = "即將到站";
	public final static String LEAVE = "過站不停車";
	public final static String STOP = "本站停車";

	private final static String TAG= "BusRouteModelV2";

	private final int MaxNum = 50;
	private int numNow = 0;
	public AskBusRouteTask askBusRouteTask;
	public AskBusDetailTask askBusDetailTask;
	public AskWaitingPeopleNumberTask askWaitingPeopleNumberTask;
	private TextToSpeech textToSpeech;
	private TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
		@Override
		public void onInit(int status) {
			if (status == TextToSpeech.SUCCESS){
				int result = textToSpeech.setLanguage(Locale.CHINESE);
				if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
					Log.d(TAG,"data not correct or has been destroyed");
				}else{
					textToSpeech.setLanguage(Locale.CHINA);
				}
			}
		}
	};


	public BusRouteModelV2(Context context){
		this.context = context;
		this.textToSpeech = new TextToSpeech(context,onInitListener);
	}

	@Override
	public void setTitleText(TextView tv_title, String text) {
		tv_title.setText(text);
	}

	@Override
	public void updateData(TextView tv_people_waiting,TextView tv_stop_name,TextView tv_people_on_bus,TextView tv_people_service,TextView tv_decision,
						   List<Map<String,Object>> data, List<Map<String,Object>> waitingData, int position, boolean needToStop) {
		tv_stop_name.setText(data.get(position).get("StopName").toString());
		tv_people_on_bus.setText((numNow + Integer.parseInt(waitingData.get(position).get("queuing").toString()) + " 人"));
		tv_people_service.setText((waitingData.get(position).get("service").toString() + " 人"));
		tv_people_waiting.setText((waitingData.get(position).get("queuing").toString() + " 人"));
		if(needToStop){
			tv_decision.setText(STOP);
		}else{
			tv_decision.setText(LEAVE);
		}
	}


	@Override
	public void requestData(String city, String busCode, String routeID) {
		askBusDetailTask = new AskBusDetailTask(busCode);
		askBusDetailTask.execute("https://ptx.transportdata.tw/MOTC/v2/Bus/EstimatedTimeOfArrival/City/"
				+ city + "/" + busCode + "?$filter=RouteID%20eq%20'" + routeID + "'&$orderby=StopSequence&$top=300&$format=JSON");
	}

	@Override
	public void requestRouteData(String city, String busCode) {
		askBusRouteTask = new AskBusRouteTask(busCode);
		askBusRouteTask.execute("https://ptx.transportdata.tw/MOTC/v2/Bus/Route/City/"
				+  city + "?$top=300&$format=JSON");
	}

	@Override
	public void requestWaitingPeopleNumber(String city, String busCode, int direction) {
		askWaitingPeopleNumberTask = new AskWaitingPeopleNumberTask(city,busCode,direction);
		String result = "{\n" +
				"\"results\":[{\"stopName\":\"中壢公車站\",\"queuing\":1,\"service\":0},\n" +
				"{\"stopName\":\"第一銀行\",\"queuing\":1,\"service\":0},\n" +
				"{\"stopName\":\"第一市場\",\"queuing\":1,\"service\":1},\n" +
				"{\"stopName\":\"河川教育中心\",\"queuing\":1,\"service\":0},\n" +
				"{\"stopName\":\"舊社\",\"queuing\":1,\"service\":0},\n" +
				"{\"stopName\":\"新明國中\",\"queuing\":1,\"service\":1},\n" +
				"{\"stopName\":\"廣興\",\"queuing\":0,\"service\":0},\n" +
				"{\"stopName\":\"仁愛新村\",\"queuing\":0,\"service\":0},\n" +
				"{\"stopName\":\"青果市場\",\"queuing\":1,\"service\":0},\n" +
				"{\"stopName\":\"五權\",\"queuing\":1,\"service\":0},\n" +
				"{\"stopName\":\"佑民醫院\",\"queuing\":1,\"service\":0}\n" +
				"]\n" +
				"}";
		askWaitingPeopleNumberTask.onPostExecute(result);
		//askWaitingPeopleNumberTask.execute("http://140.115.52.194:8000/bus_info/api=get_queuing");
	}

	@Override
	public void speak(String text) {
		textToSpeech.setPitch(1.1f);
		textToSpeech.setSpeechRate(0.8f);

		textToSpeech.speak(text,TextToSpeech.QUEUE_ADD,null,"driver");
	}

	@Override
	public boolean decideLeave(List<Map<String, Object>> waitingPeopleData, int position) {
		if(numNow + Integer.parseInt(waitingPeopleData.get(position).get("queuing").toString()) > MaxNum){
			return false;
		}else if (Integer.parseInt(waitingPeopleData.get(position).get("queuing").toString()) == 0){
			return false;
		}else{
			return true;
		}
	}
	@Override
	public void setFontSizeByMobile(TextView[] textViews, Point size) {
		int index = 0;
		for(TextView textView :textViews){
			switch (index){
				case 0 :{
					textView.setTextSize((float) (size.y * 0.015));
					break;
				}
				case 1 :{
					textView.setTextSize((float) (size.y * 0.0075));
					break;
				}
				default:{
					textView.setTextSize((float) (size.y * 0.01));
				}
			}
			index++;
		}
	}
}

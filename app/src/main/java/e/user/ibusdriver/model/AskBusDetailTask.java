package e.user.ibusdriver.model;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import e.user.ibusdriver.view.MainActivity;

public class AskBusDetailTask extends AsyncTask<String, Integer, String> {
	private final static String TAG = "ABDTask test ";
	private boolean isLoadingEnd = false;
	private String busCode  ;
	private List<Map<String, Object>> data;
	private List<Map<String, Object>> dataGo;
	private List<Map<String, Object>> dataBack;


	public AskBusDetailTask(String busCode){
		this.busCode = busCode;
	}
	@Override
	protected String doInBackground(String... urls) {
		return GET(urls[0]);
	}

	@Override
	protected void onPostExecute(String result){
		Log.d(TAG,result);
		try {
			JSONArray jsonArray = new JSONArray(result);
			data = new ArrayList<>();
			dataGo = new ArrayList<>();
			dataBack = new ArrayList<>();
			for(int i = 0 ; i < jsonArray.length() ; i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if(!jsonObject.getJSONObject("RouteName").getString("Zh_tw").equals(busCode)){
					Log.d(TAG,"BusCode not match and do continues ");
					continue;
				}
				HashMap<String, Object> m  = new HashMap();
				m.put("Direction",jsonObject.getString("Direction"));
				m.put("StopName",jsonObject.getJSONObject("StopName").getString("Zh_tw"));
				m.put("RouteName",jsonObject.getJSONObject("RouteName").getString("Zh_tw"));
				if(jsonObject.has("PlateNumb")){
					m.put("PlateNumb",jsonObject.getString("PlateNumb"));
				}
				switch (jsonObject.getInt("StopStatus")){
					case 0:{
						m.put("StopStatus","正常");
						if(jsonObject.has("EstimateTime")){
							m.put("EstimateTime",jsonObject.getString("EstimateTime"));
						}
						if (jsonObject.has("NextBusTime")){
							m.put("NextBusTime",jsonObject.getString("NextBusTime"));
						}

						break;
					}
					case 1 :{
						m.put("StopStatus","尚未發車");
						break;
					}
					case 2:{
						m.put("StopStatus","交管不停靠");
						break;
					}
					case 3:{
						m.put("StopStatus","末班駛離");
						break;
					}
					case 4:{
						m.put("StopStatus","今日未營運");
						break;
					}
				}

				switch (jsonObject.getInt("Direction")){
					case 0 :{
						dataGo.add(m);
						Log.d(TAG,"data Go add");
						break;
					}
					case 1 :{
						dataBack.add(m);
						Log.d(TAG,"data Back add");
						break;
					}case 2 | 255:{
						dataGo.add(m);
						break;
					}
				}
				//Log.d(TAG,"data add m and m size is " + m.size());
			}
			data.addAll(dataGo);
			data.addAll(dataBack);
			isLoadingEnd = data.size() != 0 ;
			Log.d(TAG , String.valueOf(data.size()));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	private String GET(String APIUrl) {
		String result = "";
		HttpURLConnection connection = null;
		String xdate = getServerTime();
		String SignDate = "x-date: " + xdate;
		String Signature="";
		//取得加密簽章
		Signature = HMAC_SHA1.Signature(SignDate, MainActivity.APPKey);
		Log.d(TAG,Signature);
		Log.d(TAG,SignDate);
		String sAuth = "hmac username=\"" + MainActivity.APPID + "\", algorithm=\"hmac-sha1\", headers=\"x-date\", signature=\"" + Signature + "\"";
		try {
			URL url = new URL(APIUrl);
			Log.d(TAG,APIUrl);
			if("https".equalsIgnoreCase(url.getProtocol())){
				SslUtils.ignoreSsl();
			}
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Authorization", sAuth);
			connection.setRequestProperty("x-date", xdate);
			//connection.setRequestProperty("Accept","*/*");
			//connection.setRequestProperty("Accept-Encoding", "gzip");
			connection.setDoInput(true);
			//connection.setDoOutput(false);

			// receive response as inputStream
			InputStream inputStream = connection.getInputStream();

			// convert inputstream to string
			if(inputStream != null){
				InputStreamReader reader = new InputStreamReader(inputStream,"UTF-8");
				BufferedReader in = new BufferedReader(reader);

				//讀取回傳資料
				String line="";
				while ((line = in.readLine()) != null) {
					result += (line+"\n");
				}

				Log.d(TAG,result);
				/*Type busRouteStationListType = new TypeToken<ArrayList<BusRouteStationData>>(){}.getType();
				Gson gsonReceiver = new Gson();
				List<BusRouteStationData> obj = gsonReceiver.fromJson(result, busRouteStationListType);
				for(BusRouteStationData b : obj){
					Log.d(TAG,b.RouteName);
				}
				Log.d(TAG,result);*/
				//result = convertInputStreamToString(inputStream);
				//Log.d(TAG, inputStream.toString());
			}
			else
				result = "Did not work!";
			return  result;
		} catch (Exception e) {
			Log.d("ABDTask InputStream", e.getLocalizedMessage());
			e.printStackTrace();
			return result;
		}
	}

	public List<Map<String,Object>> resultBack(){
		if(!isLoadingEnd){
			return null;
		}else{
			Log.d(TAG, "data size" + data.size());
			return data;
		}
	}
	private String convertInputStreamToString(InputStream inputStream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1));
		String line = "";
		String result = "";
		while ((line = bufferedReader.readLine() )!= null) {
			result += line;
		}
		inputStream.close();
		return result;
	}
	public static String getServerTime() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(calendar.getTime());
	}
}

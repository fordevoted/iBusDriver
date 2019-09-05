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

public class AskBusRouteTask extends AsyncTask<String, Integer, String> {
	private final static String TAG = "ABRTask test ";
	private boolean isLoadingEnd = false;
	private String busCode;
	private String routeID;

	public AskBusRouteTask(String busCode){
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
			for(int i = 0 ; i < jsonArray.length() ; i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Log.d(TAG,busCode + " vs " + jsonObject.getJSONArray("SubRoutes").getJSONObject(0).getJSONObject("SubRouteName").getString("Zh_tw"));
				if(busCode.equals(jsonObject.getJSONArray("SubRoutes").getJSONObject(0).getJSONObject("SubRouteName").getString("Zh_tw"))){
					routeID = jsonObject.getString("RouteID");
					isLoadingEnd = true;
					break;
				}
			}
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
			Log.d("ATask InputStream", e.getLocalizedMessage());
			e.printStackTrace();
			return result;
		}
	}

	public String resultBack(){
		if(!isLoadingEnd){
			return null;
		}else{
			Log.d(TAG, "routeID " + routeID);
			return routeID;
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

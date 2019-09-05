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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import e.user.ibusdriver.view.MainActivity;

public class AskWaitingPeopleNumberTask extends AsyncTask<String, Integer, String> {
	private final static String TAG = "AWPNTask test ";
	private boolean isLoadingEnd = false;
	private List<Map<String, Object>> data;
	private String city;
	private String busCode;
	private int direction;

	public AskWaitingPeopleNumberTask(String city, String busCode, int direction){
		this.city = city;
		this.busCode = busCode;
		this.direction = direction;
	}
	@Override
	protected String doInBackground(String... urls) {
		return POST(urls[0]);
	}

	@Override
	protected void onPostExecute(String result){
		Log.d(TAG,result);
		try {
			JSONArray jsonArray = new JSONObject(result).getJSONObject("results").getJSONArray("stations");
			data = new ArrayList<>();
			for(int i = 0 ; i < jsonArray.length() ; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Map<String,Object> m = new HashMap();
				m.put("stopName",jsonObject.getString("StopName"));
				m.put("queuing",jsonObject.getString("queuing"));
				m.put("service",jsonObject.getString("service"));
				data.add(m);
			}

			isLoadingEnd = data.size() != 0 ;
			Log.d(TAG , String.valueOf(data.size()));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	private String POST(String APIUrl) {
		String result = "";
		HttpURLConnection connection = null;
		String xdate = getServerTime();
		String SignDate = "x-date: " + xdate;
		String Signature="";
		//取得加密簽章
		try {
			URL url = new URL(APIUrl);
			if("https".equalsIgnoreCase(url.getProtocol())){
				SslUtils.ignoreSsl();
			}
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("x-date", xdate);
			//connection.setRequestProperty("Accept","*/*");
			//connection.setRequestProperty("Accept-Encoding", "gzip");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("city=").append(URLEncoder.encode(city, "UTF-8")).append("&");
			stringBuilder.append("routeName=").append(URLEncoder.encode(busCode, "UTF-8")).append("&");
			stringBuilder.append("Direction=").append(URLEncoder.encode(String.valueOf(direction), "UTF-8")).append("&");
			outputStream.writeBytes(stringBuilder.toString());
			outputStream.flush();
			outputStream.close();

			// receive response as inputStream
			InputStream inputStream = connection.getInputStream();
			//InputStream inputStream = connection.getErrorStream();
			int status = connection.getResponseCode();
			Log.d(TAG, String.valueOf(status));
			// convert inputstream to string
			if(inputStream != null){
				InputStreamReader reader = new InputStreamReader(inputStream,"UTF-8");
				BufferedReader in = new BufferedReader(reader);

				//讀取回傳資料
				String line="";
				while ((line = in.readLine()) != null) {
					result += (line+"\n");
				}

				//Log.d(TAG,result);
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

	public List<Map<String, Object>> resultBack(){
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

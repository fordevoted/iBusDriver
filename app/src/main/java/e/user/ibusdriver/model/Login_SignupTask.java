package e.user.ibusdriver.model;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import static e.user.ibusdriver.model.AskBusDetailTask.getServerTime;

public class Login_SignupTask extends AsyncTask<String, Void, String> {

	private  static final  String TAG = "LSTask test";
	private String authentication = "empty";
	public String account;
	public String password;
	public String password_again;
	public String response;
	public String error_detail = "No Response";
	public boolean isFinish = false;
	public int response_state = -1 ;

	public Login_SignupTask(){

	}
	public Login_SignupTask(String account, String password){
		this.account = account;
		this.password = password;

	}
	public Login_SignupTask(String account, String password, String password_again){
		this.account = account;
		this.password = password;
		this.password_again = password_again;

	}

	@Override
	protected String doInBackground(String... urls) {
		//Log.d("PTask direction test in background", String.valueOf(direction[0]));

		return POST(urls[0]);
	}

	// onPostExecute displays the results of the AsyncTask.
	@Override
	protected void onPostExecute(String result) {
		//Log.d("PTask End Of Execute", "processing json ");
		Log.d("LSTask End Of Execute", "processing json result: " + result);
		try {
			JSONObject jObject = new JSONObject(result);
			 response = jObject.getString("results");
			Log.d("LTask Results Response State", jObject.getString("results"));
			if(response.equals("Fault")){
				error_detail = jObject.getString("problem");
				response_state = -1;
			}else if (response.equals("Success")||response.equals("Success login")){
				response_state = 0;
				authentication = jObject.getString("token");
			}


		} catch (JSONException e) {
			e.printStackTrace();
		}
		isFinish = true;
	}

	public boolean resultBack(){
		return isFinish;
	}
	public String[] responseBack(){
		return new String[]{String.valueOf(response_state),error_detail,authentication};
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
			stringBuilder.append("username=").append(URLEncoder.encode("TESTACCOUNT02", "UTF-8")).append("&");
			stringBuilder.append("password=").append(URLEncoder.encode("TESTACCOUNT02", "UTF-8")).append("&");
			stringBuilder.append("checkpassword=").append(URLEncoder.encode("TESTACCOUNT02", "UTF-8")).append("&");
			stringBuilder.append("email=").append(URLEncoder.encode("TESTACCOUNT02@ibus.test.tw", "UTF-8"));
			outputStream.writeBytes(stringBuilder.toString());
			outputStream.flush();
			outputStream.close();

			// receive response as inputStream)
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


	private String convertInputStreamToString(InputStream inputStream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while ((line = bufferedReader.readLine()) != null) {
			result += line;
		}
		inputStream.close();
		return result;
	}

}
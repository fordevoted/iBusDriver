package e.user.ibusdriver.presenter;

import android.graphics.Point;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public interface BusRoutePresenter {
	void setTitleText(TextView tv_title, String text);
	void updateData(TextView tv_people_waiting,TextView tv_stop_name,TextView tv_people_on_bus,TextView tv_people_service,TextView tv_decision,
					List<Map<String,Object>> data, List<Map<String,Object>> waitingData, int position, boolean needToStop);
	void requestData(String city, String busCode, String routeID);
	void requestRouteData(String city, String busCode);
	void requestWaitingPeopleNumber(String city, String busCode, int direction);
	void speak(String text);
	boolean decideLeave(List<Map<String,Object>> waitingPeopleData,int position);
	void setFontSizeByMobile(TextView[] textViews, Point size);


}

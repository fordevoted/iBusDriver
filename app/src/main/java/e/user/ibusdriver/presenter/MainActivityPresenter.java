package e.user.ibusdriver.presenter;

import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public interface MainActivityPresenter {
	void setTitleText(TextView tv_title, String text);
	void setSpinner(Spinner spinner);
	String getSelectedCityName(int position);
	int chooseDirection(Button bn0,Button bn1, int choose);
}

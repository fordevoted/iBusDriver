package e.user.ibusdriver.model;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import e.user.ibusdriver.R;
import e.user.ibusdriver.presenter.MainActivityPresenter;

public class MainActivityModelV1 implements MainActivityPresenter {

	private Context context;
	private ArrayAdapter<CharSequence> spinnerAdapter;


	public MainActivityModelV1(Context context){
		this.context = context;
	}


	@Override
	public void setTitleText(TextView tv_title, String text) {
		tv_title.setText(text);
	}

	@Override
	public void setSpinner(Spinner spinner) {
		spinnerAdapter = ArrayAdapter.createFromResource(
				context, R.array.choose_city_array,R.layout.spinner_item_layout );
		spinner.setAdapter(spinnerAdapter);
	}

	@Override
	public String getSelectedCityName(int position) {

		switch(position){
			case 0 :{
				return "";
			}
			case 1 :{
				return "Taipei";
			}
			case 2 :{
				return "TaoYuan";
			}
			case 3 :{
				return "TaiChung";
			}
			case 4 :{
				return "Kaohsiung";
			}
		}
		return "";
	}

	@Override
	public int chooseDirection(Button bn0, Button bn1, int choose) {
		if(choose == 0){
			bn0.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorPrimary));
			bn1.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorPrimaryBackground));
			return choose;
		}else{
			bn0.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorPrimaryBackground));
			bn1.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorPrimary));
			return choose;
		}
	}
}

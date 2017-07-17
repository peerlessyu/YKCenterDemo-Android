package com.ykan.sdk.example;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.yaokan.sdk.api.JsonParser;
import com.yaokan.sdk.api.YkanSDKManager;
import com.yaokan.sdk.ir.YkanIRInterface;
import com.yaokan.sdk.ir.YkanIRInterfaceImpl;
import com.yaokan.sdk.model.Brand;
import com.yaokan.sdk.model.BrandResult;
import com.yaokan.sdk.model.DeviceType;
import com.yaokan.sdk.model.DeviceTypeResult;
import com.yaokan.sdk.model.MatchRemoteControl;
import com.yaokan.sdk.model.MatchRemoteControlResult;
import com.yaokan.sdk.model.RemoteControl;
import com.yaokan.sdk.utils.Logger;
import com.yaokan.sdk.utils.ProgressDialogUtils;
import com.yaokan.sdk.utils.Utility;
public class YKCodeAPIActivity extends Activity implements View.OnClickListener {

	private ProgressDialogUtils dialogUtils;

	private YkanIRInterface ykanInterface;

	private String TAG = YKCodeAPIActivity.class.getSimpleName();

	private TextView showText, tvDevice;

	private GizWifiDevice currGizWifiDevice;

	private String deviceId;

	private List<DeviceType> deviceType = new ArrayList<DeviceType>();// 设备类型

	private List<Brand> brands = new ArrayList<Brand>(); // 品牌

	private List<MatchRemoteControl> remoteControls = new ArrayList<MatchRemoteControl>();// 遥控器列表

	private List<String> nameType = new ArrayList<String>();
	private List<String> nameBrands = new ArrayList<String>();
	private List<String> nameRemote = new ArrayList<String>();

	private MatchRemoteControlResult controlResult = null;// 匹配列表

	private RemoteControl remoteControl = null; // 遥控器对象

	private MatchRemoteControl currRemoteControl = null; // 当前匹配的遥控器对象

	private DeviceType currDeviceType = null; // 当前设备类型

	private Brand currBrand = null; // 当前品牌

	private Spinner spType, spBrands, spRemotes;

	private ArrayAdapter<String> typeAdapter, brandAdapter, remoteAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_codeapi);
		// 遥控云数据接口分装对象对象
		ykanInterface = new YkanIRInterfaceImpl(getApplicationContext());
		// 遥控云数据接口分装对象对象
		ykanInterface = new YkanIRInterfaceImpl(getApplicationContext());
		initView();
		initDevice();
	}

	private void initDevice() {
		currGizWifiDevice = (GizWifiDevice) getIntent().getParcelableExtra(
				"GizWifiDevice");
		if (currGizWifiDevice != null) {
			deviceId = currGizWifiDevice.getDid();
			tvDevice.setText(currGizWifiDevice.getProductName() + "("
					+ currGizWifiDevice.getMacAddress() + ") ");
			// 在下载数据之前需要设置设备ID，用哪个设备去下载
			YkanSDKManager.getInstance().setDeviceId(deviceId);
			if (currGizWifiDevice.isSubscribed() == false) {
				currGizWifiDevice.setSubscribe(true);
			}
		}
	}

	private void initView() {
		dialogUtils = new ProgressDialogUtils(this);
		showText = (TextView) findViewById(R.id.showText);
		tvDevice = (TextView) findViewById(R.id.tv_device);
		spType = (Spinner) findViewById(R.id.spType);
		spBrands = (Spinner) findViewById(R.id.spBrand);
		spRemotes = (Spinner) findViewById(R.id.spData);
		typeAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, nameType);
		brandAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, nameBrands);
		remoteAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, nameRemote);
		typeAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		brandAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		remoteAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spType.setAdapter(typeAdapter);
		spBrands.setAdapter(brandAdapter);
		spRemotes.setAdapter(remoteAdapter);
		spType.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				currDeviceType = deviceType.get(pos);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		spBrands.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				currBrand = brands.get(pos);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		spRemotes.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				currRemoteControl = remoteControls.get(pos);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

	}
	
	private void toYKWifiDeviceControlActivity(Intent intent,JsonParser jsonParser){
		intent.setClass(this, YKWifiDeviceControlActivity.class);
		intent.putExtra("GizWifiDevice", currGizWifiDevice);
		try {
			intent.putExtra("rcCommand",jsonParser.toJson(remoteControl.getRcCommand()));
		} catch (JSONException e) {
			Log.e(TAG, "JSONException:" +e.getMessage());
		}
		startActivity(intent);
	}
	
	private void toAirControlActivity(Intent intent,JsonParser jsonParser){
		intent.setClass(this, AirControlActivity.class);
		intent.putExtra("GizWifiDevice", currGizWifiDevice);
		try {
			intent.putExtra("remoteControl",jsonParser.toJson(remoteControl));
		} catch (JSONException e) {
			Log.e(TAG, "JSONException:" +e.getMessage());
		}
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.wifitest:
			// 进入遥控控制面板
			Log.d(TAG, "remoteControl:" + remoteControl);
			if (remoteControl == null) {
				Toast.makeText(getApplicationContext(), "没有下载遥控器数据",
						Toast.LENGTH_SHORT).show();
				return;
			}
			Intent intent = new Intent();
			JsonParser jsonParser = new JsonParser();
			int airTid = 7;// 空调
			if (remoteControl.gettId() == airTid) {
				toAirControlActivity(intent, jsonParser);
			} else {
				toYKWifiDeviceControlActivity(intent, jsonParser);
			}
		  
			break;
		default:
			new DownloadThread(v.getId()).start();
			break;
		}
	}

	class DownloadThread extends Thread {
		private int viewId;

		public DownloadThread(int viewId) {
			this.viewId = viewId;
		}

		@Override
		public void run() {
			dialogUtils.sendMessage(1);
			String result = "";
			Message message = mHandler.obtainMessage();
			try {
				switch (viewId) {
				case R.id.getDeviceType:
					DeviceTypeResult deviceResult = ykanInterface
							.getDeviceType();
					deviceType = deviceResult.getRs();
					result = deviceResult.toString();
					message.what = 0;
					Log.d(TAG, " getDeviceType result:" + result);
					break;
				case R.id.getBrandByType:
					// int type = 7 ;//1:机顶盒，2：电视机
					if (currDeviceType != null) {
						BrandResult brandResult = ykanInterface
								.getBrandsByType(currDeviceType.getTid());
						brands = brandResult.getRs();
						result = brandResult.toString();
						message.what = 1;
						Log.d(TAG, " getBrandByType result:" + brandResult);
					} else {
						result = "请调用获取设备接口";
					}
					break;
				case R.id.getMatchedDataByBrand:
					if (currBrand != null) {
						controlResult = ykanInterface.getRemoteMatched(
								currBrand.getBid(), currDeviceType.getTid(), 4);
						remoteControls = controlResult.getRs();
						result = controlResult.toString();
						message.what = 2;
						Log.d(TAG, " getMatchedDataByBrand result:" + result);
					} else {
						result = "请调用获取设备接口";
					}
					break;
				case R.id.getDetailByRCID:
					if (!Utility.isEmpty(currRemoteControl)) {
						remoteControl = ykanInterface
								.getRemoteDetails(currRemoteControl.getRid());
						result = remoteControl.toString();
					} else {
						result = "请调用匹配数据接口";
						Log.e(TAG, " getDetailByRCID 没有遥控器设备对象列表");
					}
					Log.d(TAG, " getDetailByRCID result:" + result);
					break;
				case R.id.getFastMatched:
					MatchRemoteControlResult rcFastMatched = ykanInterface
							.getFastMatched(
									87,
									7,
									"1,38000,341,169,24,64,23,22,23,22,23,64,24,21,24,21,24,63,24,21,24,21,24,63,24,21,24,63,24,21,24,21,24,21,24,21,24,21,24,21,24,21,25,21,24,21,24,21,24,21,24,21,24,21,24,21,24,21,24,21,24,63,24,21,24,64,24,21,23,22,23,64,24,21,24");
					result = rcFastMatched.toString();
					Log.d(TAG, " getFastMatched result:" + result);
					break;
				default:
					break;
				}
			} catch (Exception exp) {
				Logger.e(TAG, "error:" + exp.getMessage());
			}
			message.obj = result;
			mHandler.sendMessage(message);
			dialogUtils.sendMessage(0);
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			showText.setText("appID:" + YkanSDKManager.getInstance().getAppId()
					+ "\n" + (String) msg.obj);
			switch (msg.what) {
			case 0:
				if (deviceType != null) {
					nameType.clear();
					for (int i = 0; i < deviceType.size(); i++) {
						nameType.add(deviceType.get(i).getName());
					}
				}
				typeAdapter.notifyDataSetInvalidated();
				spType.setAdapter(typeAdapter);
				break;
			case 1:
				if (brands != null) {
					nameBrands.clear();
					for (int i = 0; i < brands.size(); i++) {
						nameBrands.add(brands.get(i).getName());
					}
				}
				brandAdapter.notifyDataSetInvalidated();
				spBrands.setAdapter(brandAdapter);
				break;
			case 2:
				if (remoteControls != null) {
					nameRemote.clear();
					for (int i = 0; i < remoteControls.size(); i++) {
						nameRemote.add(remoteControls.get(i).getName() + "-"
								+ remoteControls.get(i).getRmodel());
					}
				}
				remoteAdapter.notifyDataSetInvalidated();
				spRemotes.setAdapter(remoteAdapter);
				break;

			default:
				break;
			}
		};
	};

	@Override
	public void onBackPressed() {
		finish();
	};

}

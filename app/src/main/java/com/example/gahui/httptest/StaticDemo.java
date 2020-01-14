package com.example.gahui.httptest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 实现静态画运动轨迹 同时展示如何使用自定义图标绘制并响应点击事件
 */
public class StaticDemo extends Activity implements DatePicker.OnDateChangedListener{

	// 定位相关
	BitmapDescriptor mCurrentMarker;
	MapView mMapView;
	BaiduMap mBaiduMap;
	Polyline mPolyline;
	LatLng target;
	MapStatus.Builder builder;
	List<LatLng> latLngs = new ArrayList<LatLng>();

	BitmapDescriptor startBD = BitmapDescriptorFactory
            .fromResource(R.drawable.ic_me_history_startpoint);
	BitmapDescriptor finishBD = BitmapDescriptorFactory
            .fromResource(R.drawable.ic_me_history_finishpoint);
	
	private Marker mMarkerA;
    private Marker mMarkerB;
    private InfoWindow mInfoWindow;

	private List<String> list = new ArrayList<String>();
	private TextView myTextView;
	private Spinner mySpinner;
	private ArrayAdapter<String> adapter;

	private Calendar cal;
	private int year,month,day;
	DatePicker dp_test;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sta);

		new Thread(new Runnable() {
			@Override
			public void run() {

				String path="http://192.168.0.145:8080/dbmap/wxfindUsers/skip?reservoir="+"塘坑背水库";

				try {
					try{
						URL url = new URL(path); //新建url并实例化
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod("GET");//获取服务器数据
						connection.setReadTimeout(8000);//设置读取超时的毫秒数
						connection.setConnectTimeout(8000);//设置连接超时的毫秒数
						InputStream in = connection.getInputStream();
						BufferedReader reader = new BufferedReader(new InputStreamReader(in));
						String result = reader.readLine();//读取服务器进行逻辑处理后页面显示的数据

						JSONObject json = new JSONObject(result);
						//取数据
						JSONArray str = (JSONArray)json.get("data");
						//第一步：添加一个下拉列表项的list，这里添加的项就是下拉列表的菜单项
						for(int i=0;i<str.length();i++){
							list.add(str.getString(i));
						}

						Log.d("MainActivity","run: "+result);

					}catch (MalformedURLException e){} catch (JSONException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();


		myTextView = (TextView)findViewById(R.id.TextView_city);
		mySpinner = (Spinner)findViewById(R.id.Spinner_city);
		//第二步：为下拉列表定义一个适配器，这里就用到里前面定义的list。
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, list);
		//第三步：为适配器设置下拉列表下拉时的菜单样式。
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//第四步：将适配器添加到下拉列表上
		mySpinner.setAdapter(adapter);
		//第五步：为下拉列表设置各种事件的响应，这个事响应菜单被选中
		mySpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				/* 将所选mySpinner 的值带入myTextView 中*/
				//myTextView.setText("您选择的是："+ adapter.getItem(arg2));
				/* 将mySpinner 显示*/
				arg0.setVisibility(View.VISIBLE);
			}
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				myTextView.setText("NONE");
				arg0.setVisibility(View.VISIBLE);
			}
		});
		/*下拉菜单弹出的内容选项触屏事件处理*/
		mySpinner.setOnTouchListener(new Spinner.OnTouchListener(){
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				/**
				 *
				 */
				return false;
			}
		});
		/*下拉菜单弹出的内容选项焦点改变事件处理*/
		mySpinner.setOnFocusChangeListener(new Spinner.OnFocusChangeListener(){
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub

			}
		});


		//时间选择器
		DatePicker dp_test = (DatePicker) findViewById(R.id.dp_test);
		Calendar calendar = Calendar.getInstance();
		int year=calendar.get(Calendar.YEAR);
		int monthOfYear=calendar.get(Calendar.MONTH);
		int dayOfMonth=calendar.get(Calendar.DAY_OF_MONTH);
		dp_test.init(year,monthOfYear,dayOfMonth, (DatePicker.OnDateChangedListener) this);


		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		
		coordinateConvert();
		
		builder = new MapStatus.Builder();
		builder.target(target).zoom(18f);
		mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

		MarkerOptions oStart = new MarkerOptions();//地图标记覆盖物参数配置类
		oStart.position(latLngs.get(0));//覆盖物位置点，第一个点为起点
		oStart.icon(startBD);//设置覆盖物图片
		oStart.zIndex(1);//设置覆盖物Index
		mMarkerA = (Marker) (mBaiduMap.addOverlay(oStart)); //在地图上添加此图层

		//添加终点图层
		MarkerOptions oFinish = new MarkerOptions().position(latLngs.get(latLngs.size()-1)).icon(finishBD).zIndex(2);
		mMarkerB = (Marker) (mBaiduMap.addOverlay(oFinish));

	    
	    mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            public boolean onMarkerClick(final Marker marker) {
                
                if (marker.getZIndex() == mMarkerA.getZIndex() ) {//如果是起始点图层
                	TextView textView = new TextView(getApplicationContext());
                	textView.setText("起点");
                	textView.setTextColor(Color.BLACK);
                	textView.setGravity(Gravity.CENTER);
                	textView.setBackgroundResource(R.drawable.popup);
                	
                	//设置信息窗口点击回调
                	OnInfoWindowClickListener listener = new OnInfoWindowClickListener() {
                        public void onInfoWindowClick() {
                        	Toast.makeText(getApplicationContext(),"这里是起点", Toast.LENGTH_SHORT).show();
                            mBaiduMap.hideInfoWindow();//隐藏信息窗口
                        }
                    };
                    LatLng latLng = marker.getPosition();//信息窗口显示的位置点
                    /**
                     * 通过传入的 bitmap descriptor 构造一个 InfoWindow
                     * bd - 展示的bitmap
					   position - InfoWindow显示的位置点
					   yOffset - 信息窗口会与图层图标重叠，设置Y轴偏移量可以解决
					   listener - 点击监听者
                     */
                    mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(textView), latLng, -47, listener);
                    mBaiduMap.showInfoWindow(mInfoWindow);//显示信息窗口
                    
                } else if (marker.getZIndex() == mMarkerB.getZIndex()) {//如果是终点图层
                	Button button = new Button(getApplicationContext());
                    button.setText("终点");
                    button.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                        	Toast.makeText(getApplicationContext(),"这里是终点", Toast.LENGTH_SHORT).show();
                            mBaiduMap.hideInfoWindow();
                        }
                    });
                    LatLng latLng = marker.getPosition();
                    /**
                     * 通过传入的 view 构造一个 InfoWindow, 此时只是利用该view生成一个Bitmap绘制在地图中，监听事件由自己实现。
                       view - 展示的 view
					   position - 显示的地理位置
					   yOffset - Y轴偏移量
                     */
                    mInfoWindow = new InfoWindow(button, latLng, -47);
                    mBaiduMap.showInfoWindow(mInfoWindow);
                } 
                return true;
            }
        });
		
		mBaiduMap.setOnPolylineClickListener(new BaiduMap.OnPolylineClickListener() {
			@Override
			public boolean onPolylineClick(Polyline polyline) {
				if (polyline.getZIndex() == mPolyline.getZIndex()) {
					Toast.makeText(getApplicationContext(),"点数：" + polyline.getPoints().size() + ",width:" + polyline.getWidth(), Toast.LENGTH_SHORT).show();
				}
				return false;
			}
		});
		OverlayOptions ooPolyline = new PolylineOptions().width(13).color(0xAAFF0000).points(latLngs);
		mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
		mPolyline.setZIndex(3);
	}
	
	/**
	 * 讲google地图的wgs84坐标转化为百度地图坐标
	 */
	private void  coordinateConvert(){
		CoordinateConverter converter  = new CoordinateConverter();
		converter.from(CoordType.COMMON);
		double lanSum = 0;
		double lonSum = 0;
		for (int i = 0; i < Const.googleWGS84.length; i++) {
			String[] ll = Const.googleWGS84[i].split(",");
			LatLng sourceLatLng = new LatLng(Double.valueOf(ll[0]), Double.valueOf(ll[1]));
			converter.coord(sourceLatLng);  
			LatLng desLatLng = converter.convert();
			latLngs.add(desLatLng);
			lanSum += desLatLng.latitude;
			lonSum += desLatLng.longitude;
		}
		target = new LatLng(lanSum/latLngs.size(), lonSum/latLngs.size());
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
		// 为系统的方向传感器注册监听器
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.getMap().clear();
		mMapView.onDestroy();
		mMapView = null;
		startBD.recycle();
        finishBD.recycle();
		super.onDestroy();
	}
	@Override
	public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		Toast.makeText(StaticDemo.this,"您选择的日期是："+year+"年"+(monthOfYear+1)+"月"+dayOfMonth+"日!",Toast.LENGTH_SHORT).show();
	}

}

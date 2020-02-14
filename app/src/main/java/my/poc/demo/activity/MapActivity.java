package my.poc.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.unionbroad.app.util.Logger;

import my.poc.demo.R;

/**
 * Created by xubin on 17-2-10.
 * <p>
 * Function:
 */
public class MapActivity extends AppCompatActivity {

    public static final String EXTRA_JD = "extra_jd";
    public static final String EXTRA_WD = "extra_wd";
    public static final String EXTRA_TYPE = "extra_type"; //6 location, 5 : altert

    private Logger mLogger = Logger.getLogger("MapActivity");

    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    MKOfflineMap mOffline = null;
    private LatLng mTargetLatLng;
    private double mGis_jd;
    private double mGis_wd;
    private Handler mHandler = new Handler();
    MapView mMapView;

    public static void show(Activity activity, double jd, double wd, int type) {
        Intent intent = new Intent(activity, MapActivity.class);
        intent.putExtra(EXTRA_JD, jd);
        intent.putExtra(EXTRA_WD, wd);
        intent.putExtra(EXTRA_TYPE, type);

        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_layout);
        mGis_jd = 116.3972282409668;
        mGis_wd =  39.90960456049752;
        mLogger.d("Locate to [" + mGis_jd + "," + mGis_wd + "]");

        mMapView = new MapView(this, new BaiduMapOptions());
        ((ViewGroup) findViewById(R.id.root)).addView(mMapView);

        mBaiduMap = mMapView.getMap();
        mLocationClient = new LocationClient(getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(new MyLocationListener());

        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(false);
        mTargetLatLng = new LatLng(mGis_wd, mGis_jd);

        MapStatus targetMapStatus = new MapStatus.Builder().target(mTargetLatLng).zoom(16).build();
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(targetMapStatus));

        CoordinateConverter converter = new CoordinateConverter();
        converter.coord(mTargetLatLng);
        converter.from(CoordinateConverter.CoordType.COMMON);
        LatLng convertLatLng = converter.convert();
        OverlayOptions ooA = new MarkerOptions()
                .position(convertLatLng)
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_launcher)).zIndex(4)
                .draggable(true);
        mBaiduMap.addOverlay(ooA);

        mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
            }
        });
        mBaiduMap.setOnMapRenderCallbadk(new BaiduMap.OnMapRenderCallback() {
            @Override
            public void onMapRenderFinished() {
                mLogger.d("onMapRenderFinished");
                //updateAlermView();
            }
        });
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
                mLogger.d("onMapStatusChangeStart");
            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {
                mLogger.d("onMapStatusChange");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //updateAlermView();
                    }
                });
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                mLogger.d("onMapStatusChangeFinish");
            }
        });
        mOffline = new MKOfflineMap();
        mOffline.init(new MKOfflineMapListener() {
            @Override
            public void onGetOfflineMapState(int i, int i1) {

            }
        });
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null) {
                return;
            }
            LatLng cenpt = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            try {
                mLogger.d("routePlan begin===========");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMapView != null) {
            mMapView.onDestroy();
        }
        if (mLocationClient != null) {
            mLocationClient.stop();
        }
        if (mBaiduMap != null) {
            mBaiduMap.setMyLocationEnabled(false);
        }
        // unregisterReceiver(mReceiver);
    }
}
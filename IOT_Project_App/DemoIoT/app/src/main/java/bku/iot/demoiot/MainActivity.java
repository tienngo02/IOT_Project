package bku.iot.demoiot;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

class Constants {
    public static final String IDLE = "0";
    public static final String MIXER1 = "1";
    public static final String MIXER2 = "2";
    public static final String MIXER3 = "3";
    public static final String PUMP_IN = "4";
    public static final String SELECTOR = "5";
    public static final String PUMP_OUT = "6";
    public static final String NEXT_CYCLE = "7";
    public static final String END = "8";
}


public class MainActivity extends AppCompatActivity {

    MQTTHelper mqttHelper;

    //home_layout
    TextView txtTemp, txtLig, txtHumi;
    TextView txtCycle1, txtCycle2, txtCycle3;
    TextView txtStatus1, txtStatus2, txtStatus3;
    LabeledSwitch btnActive1, btnActive2, btnActive3;
    ImageButton btnSettings, btnError;

    //schedule_layout
    ImageButton btnSched1Show, btnSched2Show, btnSched3Show;
    LinearLayout laySched1Info, laySched2Info, laySched3Info;
    EditText edtSched1Cycle , edtSched1Mix1, edtSched1Mix2, edtSched1Mix3, edtSched1Start, edtSched1Stop, edtSched1Area;
    EditText edtSched2Cycle,  edtSched2Mix1, edtSched2Mix2, edtSched2Mix3, edtSched2Start, edtSched2Stop, edtSched2Area;
    EditText edtSched3Cycle,  edtSched3Mix1, edtSched3Mix2, edtSched3Mix3, edtSched3Start, edtSched3Stop, edtSched3Area;
    LabeledSwitch btnSched1Active, btnSched2Active, btnSched3Active;
    Button btnSchedule1, btnSchedule2, btnSchedule3;
    Boolean isSched1Show = false;
    Boolean isSched2Show = false;
    Boolean isSched3Show = false;


    //statistic_layout
    TextView txtAvgTime, txtCountTimes;
    private LineChart lineChart;


    TabHost myTabHost;
    String check = "";
    JSONObject schedule1 = new JSONObject();
    JSONObject schedule2 = new JSONObject();
    JSONObject schedule3 = new JSONObject();

    private String password = "aio_dAjN47GWlQwyiMtudpF1uVaiTS";

    String[] API_FEED_URLS = {  "https://io.adafruit.com/api/v2/tienngo/feeds/humidity",
                                "https://io.adafruit.com/api/v2/tienngo/feeds/temperature",
                                "https://io.adafruit.com/api/v2/tienngo/feeds/light",
                                "https://io.adafruit.com/api/v2/tienngo/feeds/scheduler1",
                                "https://io.adafruit.com/api/v2/tienngo/feeds/scheduler2",
                                "https://io.adafruit.com/api/v2/tienngo/feeds/scheduler3",
                                "https://io.adafruit.com/api/v2/tienngo/feeds/notification"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //home_layout
        txtTemp = findViewById(R.id.txtTemperature);
        txtLig = findViewById(R.id.txtLight);
        txtHumi = findViewById(R.id.txtHumidity);
        btnActive1 = findViewById(R.id.btnActive1);
        btnActive2 = findViewById(R.id.btnActive2);
        btnActive3 = findViewById(R.id.btnActive3);
        txtCycle1 = findViewById(R.id.txtCycle1);
        txtCycle2 = findViewById(R.id.txtCycle2);
        txtCycle3 = findViewById(R.id.txtCycle3);
        txtStatus1 = findViewById(R.id.txtStatus1);
        txtStatus2 = findViewById(R.id.txtStatus2);
        txtStatus3 = findViewById(R.id.txtStatus3);

        btnSettings = findViewById(R.id.btnSettings);
        btnError = findViewById(R.id.btnError);

        //schedule_layout
        btnSched1Show = findViewById(R.id.btnSched1Show);
        laySched1Info = findViewById(R.id.laySched1Info);
        edtSched1Cycle = findViewById(R.id.edtSched1Cycle);
        edtSched1Mix1 = findViewById(R.id.edtSched1Mix1);
        edtSched1Mix2 = findViewById(R.id.edtSched1Mix2);
        edtSched1Mix3 = findViewById(R.id.edtSched1Mix3);
        edtSched1Start = findViewById(R.id.edtSched1Start);
        edtSched1Stop = findViewById(R.id.edtSched1Stop);
        edtSched1Area = findViewById(R.id.edtSched1Area);
        btnSched1Active = findViewById(R.id.btnSched1Active);
        btnSchedule1 = findViewById(R.id.btnSchedule1);

        btnSched2Show = findViewById(R.id.btnSched2Show);
        laySched2Info = findViewById(R.id.laySched2Info);
        edtSched2Cycle = findViewById(R.id.edtSched2Cycle);
        edtSched2Mix1 = findViewById(R.id.edtSched2Mix1);
        edtSched2Mix2 = findViewById(R.id.edtSched2Mix2);
        edtSched2Mix3 = findViewById(R.id.edtSched2Mix3);
        edtSched2Start = findViewById(R.id.edtSched2Start);
        edtSched2Stop = findViewById(R.id.edtSched2Stop);
        edtSched2Area = findViewById(R.id.edtSched2Area);
        btnSched2Active = findViewById(R.id.btnSched2Active);
        btnSchedule2 = findViewById(R.id.btnSchedule2);

        btnSched3Show = findViewById(R.id.btnSched3Show);
        laySched3Info = findViewById(R.id.laySched3Info);
        edtSched3Cycle = findViewById(R.id.edtSched3Cycle);
        edtSched3Mix1 = findViewById(R.id.edtSched3Mix1);
        edtSched3Mix2 = findViewById(R.id.edtSched3Mix2);
        edtSched3Mix3 = findViewById(R.id.edtSched3Mix3);
        edtSched3Start = findViewById(R.id.edtSched3Start);
        edtSched3Stop = findViewById(R.id.edtSched3Stop);
        edtSched3Area = findViewById(R.id.edtSched3Area);
        btnSched3Active = findViewById(R.id.btnSched3Active);
        btnSchedule3 = findViewById(R.id.btnSchedule3);

        //statistic_layout
        txtAvgTime = findViewById(R.id.txtAvgTime);
        txtCountTimes= findViewById(R.id.txtCountTimes);


        myTabHost = findViewById(R.id.tabHost);
        myTabHost.setup();
        TabHost.TabSpec spec1, spec2, spec3;

        spec1 = myTabHost.newTabSpec("home");
        spec1.setContent(R.id.tab1);
        spec1.setIndicator("Trang chủ");
        myTabHost.addTab(spec1);

        spec2 = myTabHost.newTabSpec("schedule");
        spec2.setContent(R.id.tab2);
        spec2.setIndicator("Lịch tưới");
        myTabHost.addTab(spec2);

        spec3 = myTabHost.newTabSpec("statistic");
        spec3.setContent(R.id.tab3);
        spec3.setIndicator("Lịch sử");
        myTabHost.addTab(spec3);

        SharedPreferences keyPreferences = getSharedPreferences("adafruitKey", MODE_PRIVATE);
        String aioKey = keyPreferences.getString("aio_key","");
        password = aioKey;

        btnActive1.setEnabled(false);
        btnActive2.setEnabled(false);
        btnActive3.setEnabled(false);

        setDataFromAPIs();
        setDataStatistic();


        btnActive1.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
//                JSONConverter jsonConverter = new JSONConverter();
//                String temp =  jsonConverter.run();
//                Log.d("TEST", temp);
//                sendDataMQTT("nvtien/feeds/button1", temp);

                JSONObject oldSchedule = null;
                try {
                    oldSchedule = new JSONObject(schedule1.toString());
                    schedule1.put("isActive", isOn);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                sendDataMQTT("tienngo/feeds/scheduler1",schedule1.toString());
                checkSendData(schedule1, oldSchedule, btnActive1, null);
            }
        });

        btnActive2.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                JSONObject oldSchedule = null;
                try {
                    oldSchedule = new JSONObject(schedule2.toString());
                    schedule2.put("isActive", isOn);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                sendDataMQTT("tienngo/feeds/scheduler2",schedule2.toString());
                checkSendData(schedule2, oldSchedule, btnActive2, null);
            }
        });

        btnActive3.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                JSONObject oldSchedule = null;
                try {
                    oldSchedule = new JSONObject(schedule3.toString());
                    schedule3.put("isActive", isOn);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                sendDataMQTT("tienngo/feeds/scheduler3",schedule3.toString());
                checkSendData(schedule3, oldSchedule, btnActive3, null);
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                settingsIntent.putExtra("isConnect",mqttHelper.isConnect());
                startActivity(settingsIntent);
            }
        });

        btnError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideErrorMessage();
            }
        });

        btnSchedule1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject oldSchedule = null;
                int area;
                if(String.valueOf(edtSched1Area.getEditableText()).equals("1")) area = 0;
                else if(String.valueOf(edtSched1Area.getEditableText()).equals("2")) area = 1;
                else if(String.valueOf(edtSched1Area.getEditableText()).equals("3")) area = 2;
                else area = -1;
                try {
                    oldSchedule = new JSONObject(schedule1.toString());
                    // Add key-value pairs to the JSON object
                    schedule1.put("cycle", Integer.valueOf(String.valueOf(edtSched1Cycle.getEditableText())));
                    schedule1.put("flow1", Integer.valueOf(String.valueOf(edtSched1Mix1.getEditableText())));
                    schedule1.put("flow2", Integer.valueOf(String.valueOf(edtSched1Mix2.getEditableText())));
                    schedule1.put("flow3", Integer.valueOf(String.valueOf(edtSched1Mix3.getEditableText())));
                    schedule1.put("area", area);
                    schedule1.put("isActive", btnSched1Active.isOn());
                    schedule1.put("startTime", String.valueOf(edtSched1Start.getEditableText()));
                    schedule1.put("stopTime", String.valueOf(edtSched1Stop.getEditableText()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendDataMQTT("tienngo/feeds/scheduler1",schedule1.toString());
                checkSendData(schedule1, oldSchedule, null, btnSchedule1);
//                Toast.makeText(MainActivity.this, "HELLO", Toast.LENGTH_SHORT).show();
            }
        });

        btnSchedule2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject oldSchedule = null;
                int area;
                if(String.valueOf(edtSched2Area.getEditableText()).equals("1")) area = 0;
                else if(String.valueOf(edtSched2Area.getEditableText()).equals("2")) area = 1;
                else if(String.valueOf(edtSched2Area.getEditableText()).equals("3")) area = 2;
                else area = -1;
                try {
                    oldSchedule = new JSONObject(schedule2.toString());
                    // Add key-value pairs to the JSON object
                    schedule2.put("cycle", Integer.valueOf(String.valueOf(edtSched2Cycle.getEditableText())));
                    schedule2.put("flow1", Integer.valueOf(String.valueOf(edtSched2Mix1.getEditableText())));
                    schedule2.put("flow2", Integer.valueOf(String.valueOf(edtSched2Mix2.getEditableText())));
                    schedule2.put("flow3", Integer.valueOf(String.valueOf(edtSched2Mix3.getEditableText())));
                    schedule2.put("area", area);
                    schedule2.put("isActive", btnSched2Active.isOn());
                    schedule2.put("startTime", String.valueOf(edtSched2Start.getEditableText()));
                    schedule2.put("stopTime", String.valueOf(edtSched2Stop.getEditableText()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendDataMQTT("tienngo/feeds/scheduler2",schedule2.toString());
                checkSendData(schedule2, oldSchedule, null, btnSchedule2);
//                Toast.makeText(MainActivity.this, "HELLO", Toast.LENGTH_SHORT).show();
            }
        });

        btnSchedule3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject oldSchedule = null;
                int area;
                if(String.valueOf(edtSched3Area.getEditableText()).equals("1")) area = 0;
                else if(String.valueOf(edtSched3Area.getEditableText()).equals("2")) area = 1;
                else if(String.valueOf(edtSched3Area.getEditableText()).equals("3")) area = 2;
                else area = -1;
                try {
                    oldSchedule = new JSONObject(schedule3.toString());
                    // Add key-value pairs to the JSON object
                    schedule3.put("cycle", Integer.valueOf(String.valueOf(edtSched3Cycle.getEditableText())));
                    schedule3.put("flow1", Integer.valueOf(String.valueOf(edtSched3Mix1.getEditableText())));
                    schedule3.put("flow2", Integer.valueOf(String.valueOf(edtSched3Mix2.getEditableText())));
                    schedule3.put("flow3", Integer.valueOf(String.valueOf(edtSched3Mix3.getEditableText())));
                    schedule3.put("area", area);
                    schedule3.put("isActive", btnSched3Active.isOn());
                    schedule3.put("startTime", String.valueOf(edtSched3Start.getEditableText()));
                    schedule3.put("stopTime", String.valueOf(edtSched3Stop.getEditableText()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendDataMQTT("tienngo/feeds/scheduler3",schedule3.toString());
                checkSendData(schedule3, oldSchedule, null, btnSchedule3);
//                Toast.makeText(MainActivity.this, "HELLO", Toast.LENGTH_SHORT).show();
            }
        });

        btnSched1Show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSched1Show){
                    laySched1Info.setVisibility(View.VISIBLE);
                    btnSched1Show.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
                    isSched1Show = true;
                }
                else{
                    laySched1Info.setVisibility(View.GONE);
                    btnSched1Show.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
                    isSched1Show = false;
                }
            }
        });

        btnSched2Show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSched2Show){
                    laySched2Info.setVisibility(View.VISIBLE);
                    btnSched2Show.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
                    isSched2Show = true;
                }
                else{
                    laySched2Info.setVisibility(View.GONE);
                    btnSched2Show.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
                    isSched2Show = false;
                }
            }
        });

        btnSched3Show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSched3Show){
                    laySched3Info.setVisibility(View.VISIBLE);
                    btnSched3Show.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
                    isSched3Show = true;
                }
                else{
                    laySched3Info.setVisibility(View.GONE);
                    btnSched3Show.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
                    isSched3Show = false;
                }
            }
        });


        Intent getNewKey = getIntent();
        String newKey = getNewKey.getStringExtra("newKey");
        if(!TextUtils.isEmpty(newKey)) {
            Log.d("TEST", "setPassword!");
            password = newKey;
        }

//        lineChart = findViewById(R.id.lineChart);
//
//// Tạo dữ liệu cho biểu đồ
//        ArrayList<Entry> entries = new ArrayList<>();
//        for (int i = 0; i < 100; i++) {
//            entries.add(new Entry(i, (float) (Math.random() * 100)));
//        }
//
//        LineDataSet dataSet = new LineDataSet(entries, "Label");
//        dataSet.setColor(Color.BLUE);
//        dataSet.setValueTextColor(Color.BLACK);
//
//        LineData lineData = new LineData(dataSet);
//        lineChart.setData(lineData);
//
//        // Cho phép cuộn và thu phóng
//        lineChart.setDragEnabled(true);
//        lineChart.setScaleEnabled(true);
//        lineChart.setPinchZoom(true);
//
//        // Cấu hình trục X
//        XAxis xAxis = lineChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setGranularity(1f); // one hour
//
//        // Cấu hình trục Y
//        YAxis yAxisLeft = lineChart.getAxisLeft();
//        yAxisLeft.setGranularity(1f);
//        lineChart.getAxisRight().setEnabled(false);
//
//        lineChart.invalidate();

        startMQTT();

//        if(!mqttHelper.isConnect()){
//            btnLED.setEnabled(false);
//            btnPUMP.setEnabled(false);
//            showErrorMessage("Mất kết nối MQTT ");
//        }
    }


    public void sendDataMQTT(String topic, String value){
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(false);

        byte[] b = value.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        try {
            mqttHelper.mqttAndroidClient.publish(topic, msg);
        }
        catch (MqttException e){
        }
    }

    public void startMQTT(){
        mqttHelper = new MQTTHelper(this, this.password);
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                SharedPreferences keyPreferences = getSharedPreferences("adafruitKey", MODE_PRIVATE);
                SharedPreferences.Editor keyEditor = keyPreferences.edit();
                keyEditor.putString("aio_key", password);
                keyEditor.commit();

                btnActive1.setEnabled(true);
                btnActive2.setEnabled(true);
                btnActive3.setEnabled(true);
                hideErrorMessage();
                Log.d("TEST", "connectComplete!");
            }

            @Override
            public void connectionLost(Throwable cause) {
                btnActive1.setEnabled(false);
                btnActive2.setEnabled(false);
                btnActive3.setEnabled(false);
                showErrorMessage("Mất kết nối MQTT ");
                Log.d("TEST", "connectionLost!");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("TEST", topic + "***" + message.toString());
                if(topic.contains("temperature")){
                    txtTemp.setText(message.toString()+ "°C");
                }
                else if(topic.contains("light")){
                    txtLig.setText(message.toString()+ "lux");
                }
                else if(topic.contains("humidity")){
                    txtHumi.setText(message.toString()+ "%");
                }
                else if(topic.contains("notification")){
                    handleNotification(message.toString());
                }
                else if(topic.contains("check")){
                    check = message.toString();
                    notifyDataUpdated();
                }
                else if(topic.contains("scheduler1")){
                    schedule1 = new JSONObject(message.toString());
                    String startTime = schedule1.getString("startTime");
                    txtCycle1.setText(startTime);
                    Boolean isActive = schedule1.getBoolean("isActive");
                    btnActive1.setOn(isActive);
                }
                else if(topic.contains("scheduler2")){
                    schedule2 = new JSONObject(message.toString());
                    String startTime = schedule2.getString("startTime");
                    txtCycle2.setText(startTime);
                    Boolean isActive = schedule2.getBoolean("isActive");
                    btnActive2.setOn(isActive);
                }
                else if(topic.contains("scheduler3")){
                    schedule3 = new JSONObject(message.toString());
                    String startTime = schedule3.getString("startTime");
                    txtCycle3.setText(startTime);
                    Boolean isActive = schedule3.getBoolean("isActive");
                    btnActive3.setOn(isActive);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

    }

    private void showErrorMessage(String message) {
//        LinearLayout errorContainer = findViewById(R.id.errorContainer);
//        TextView textErrorMessage = findViewById(R.id.textErrorMessage);
//
//        textErrorMessage.setText(message);
//        errorContainer.setVisibility(View.VISIBLE);

        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void hideErrorMessage() {
        LinearLayout errorContainer = findViewById(R.id.errorContainer);
        errorContainer.setVisibility(View.GONE);
    }


    private final Object lock = new Object();

    public void checkSendData(JSONObject dataCheck, JSONObject oldData, LabeledSwitch btnDevice, Button btnSchedule) {
        final long TIMEOUT = 5000; // Thời gian chờ tối đa là 5 giây
        final long startTime = System.currentTimeMillis();

        runOnUiThread(() -> {
            if(btnDevice != null) btnDevice.setEnabled(false);
            if(btnSchedule != null) btnSchedule.setEnabled(false);
            showErrorMessage("Đang gửi dữ liệu ");
        });
        new Thread(() -> {
            synchronized (lock) {
                while (!check.equals(dataCheck.toString()) && System.currentTimeMillis() - startTime < TIMEOUT) {
                    try {
                        lock.wait(TIMEOUT - (System.currentTimeMillis() - startTime)); // Chờ đến khi có dữ liệu hoặc hết thời gian
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (!check.equals(dataCheck.toString())) {
                    runOnUiThread(() -> showErrorMessage("Gửi thất bại "));
                    try {
                        dataCheck.put("cycle", oldData.getInt("cycle"));
                        dataCheck.put("flow1", oldData.getInt("flow1"));
                        dataCheck.put("flow2", oldData.getInt("flow2"));
                        dataCheck.put("flow3", oldData.getInt("flow3"));
                        dataCheck.put("area", oldData.getInt("area"));
                        dataCheck.put("isActive", oldData.getBoolean("isActive"));
//                        dataCheck.put("schedulerName", oldData.get("schedulerName"));
                        dataCheck.put("startTime", oldData.getString("startTime"));
                        dataCheck.put("stopTime", oldData.getString("stopTime"));

                        if(dataCheck.getString("schedulerName").equals("LỊCH TƯỚI 1")) {
                            sendDataMQTT("tienngo/feeds/scheduler1", dataCheck.toString());
                        }
                        else if(dataCheck.getString("schedulerName").equals("LỊCH TƯỚI 2")){
                            sendDataMQTT("tienngo/feeds/scheduler2",dataCheck.toString());
                        }
                        else{
                            sendDataMQTT("tienngo/feeds/scheduler3",dataCheck.toString());
                        }
                        runOnUiThread(() -> {
                            if(btnDevice != null) {
                                try {
                                    btnDevice.setOn(oldData.getBoolean("isActive"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    long checkTime = System.currentTimeMillis() - startTime;

                    SharedPreferences totalTimePreferences = getSharedPreferences("totalTime", MODE_PRIVATE);
                    long totalTime = totalTimePreferences.getLong("total_time",0);
                    totalTime += checkTime;
                    SharedPreferences.Editor totalTimeEditor = totalTimePreferences.edit();
                    totalTimeEditor.putLong("total_time", totalTime);
                    totalTimeEditor.commit();

                    SharedPreferences countTimesPreferences = getSharedPreferences("countTimes", MODE_PRIVATE);
                    long countTimes = countTimesPreferences.getLong("count_times",0);
                    countTimes++;
                    SharedPreferences.Editor countTimesEditor = countTimesPreferences.edit();
                    countTimesEditor.putLong("count_times", countTimes);
                    countTimesEditor.commit();

                    txtAvgTime.setText(String.format("Thời gian gửi nhận trung bình: %dms", totalTime/countTimes));
                    txtCountTimes.setText(String.format("Số lần gửi thành công: %d", countTimes));

                    runOnUiThread(() -> {
                        showErrorMessage(String.format("%dms ", checkTime));
                        if(btnDevice != null) {
                            try {
                                btnDevice.setOn(dataCheck.getBoolean("isActive"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
            runOnUiThread(() -> {
                if(btnDevice != null) btnDevice.setEnabled(true);
                if(btnSchedule != null) btnSchedule.setEnabled(true);
            });
        }).start();
    }

    // Hàm này được gọi khi dữ liệu check được cập nhật
    public void notifyDataUpdated() {
        synchronized (lock) {
            lock.notify(); // Kích thích luồng đang chờ
        }
    }

    private void setDataFromAPIs() {
            new FetchDataAsyncTask(this).execute(API_FEED_URLS);
    }

    private static class FetchDataAsyncTask extends AsyncTask<String[], Void, String[]> {

        private MainActivity activity;

        public FetchDataAsyncTask(MainActivity activity) {
            this.activity = activity;
        }

        @Override
        protected String[] doInBackground(String[]... params) {
            String[] apiUrlArray = params[0];
            String[] arrayResult = new String[7];
            int i = 0;
            for (String apiUrl : apiUrlArray) {
                StringBuilder result = new StringBuilder();
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    arrayResult[i] = null;
                }
                arrayResult[i] = result.toString();
                i++;
            }
            return arrayResult;
        }

        @Override
        protected void onPostExecute(String[] arrayResult) {
            for (String result : arrayResult) {
                try {
                    JSONObject json = new JSONObject(result);
                    String nameFeed = json.getString("name");
                    String lastValue = json.getString("last_value");
                    Log.d("TEST", lastValue+" "+nameFeed);

                    if(lastValue.equals("null")) continue;

                    if (nameFeed.equals("temperature")) {
                        activity.txtTemp.setText(lastValue + "°C");
                    }
                    else if (nameFeed.equals("light")) {
                        activity.txtLig.setText(lastValue + "lux");
                    }
                    else if (nameFeed.equals("humidity")) {
                        activity.txtHumi.setText(lastValue + "%");
                    }
                    else if (nameFeed.equals("notification")) {
                        activity.handleNotification(lastValue);
                    }
                    else if (nameFeed.equals("scheduler1")) {
                        activity.schedule1 = new JSONObject(lastValue);
                        String startTime = activity.schedule1.getString("startTime");
                        activity.txtCycle1.setText(startTime);
                        Boolean isActive = activity.schedule1.getBoolean("isActive");
                        activity.btnActive1.setOn(isActive);

                        activity.edtSched1Cycle.setText(activity.schedule1.getString("cycle"));
                        activity.edtSched1Mix1.setText(activity.schedule1.getString("flow1"));
                        activity.edtSched1Mix2.setText(activity.schedule1.getString("flow2"));
                        activity.edtSched1Mix3.setText(activity.schedule1.getString("flow3"));
                        activity.edtSched1Start.setText(activity.schedule1.getString("startTime"));
                        activity.edtSched1Stop.setText(activity.schedule1.getString("stopTime"));
                        int area = activity.schedule1.getInt("area");
                        if(area == 0) activity.edtSched1Area.setText("1");
                        else if(area == 1) activity.edtSched1Area.setText("2");
                        else if(area == 1) activity.edtSched1Area.setText("3");
                        else activity.edtSched1Area.setText("Tất cả");
                        activity.btnSched1Active.setOn(isActive);
                    }
                    else if (nameFeed.equals("scheduler2")) {
                        activity.schedule2 = new JSONObject(lastValue);
                        String startTime = activity.schedule2.getString("startTime");
                        activity.txtCycle2.setText(startTime);
                        Boolean isActive = activity.schedule2.getBoolean("isActive");
                        activity.btnActive2.setOn(isActive);

                        activity.edtSched2Cycle.setText(activity.schedule2.getString("cycle"));
                        activity.edtSched2Mix1.setText(activity.schedule2.getString("flow1"));
                        activity.edtSched2Mix2.setText(activity.schedule2.getString("flow2"));
                        activity.edtSched2Mix3.setText(activity.schedule2.getString("flow3"));
                        activity.edtSched2Start.setText(activity.schedule2.getString("startTime"));
                        activity.edtSched2Stop.setText(activity.schedule2.getString("stopTime"));
                        int area = activity.schedule2.getInt("area");
                        if(area == 0) activity.edtSched2Area.setText("1");
                        else if(area == 1) activity.edtSched2Area.setText("2");
                        else if(area == 1) activity.edtSched2Area.setText("3");
                        else activity.edtSched2Area.setText("Tất cả");
                        activity.btnSched2Active.setOn(isActive);
                    }
                    else if (nameFeed.equals("scheduler3")) {
                        activity.schedule3 = new JSONObject(lastValue);
                        String startTime = activity.schedule3.getString("startTime");
                        activity.txtCycle3.setText(startTime);
                        Boolean isActive = activity.schedule3.getBoolean("isActive");
                        activity.btnActive3.setOn(isActive);

                        activity.edtSched3Cycle.setText(activity.schedule3.getString("cycle"));
                        activity.edtSched3Mix1.setText(activity.schedule3.getString("flow1"));
                        activity.edtSched3Mix2.setText(activity.schedule3.getString("flow2"));
                        activity.edtSched3Mix3.setText(activity.schedule3.getString("flow3"));
                        activity.edtSched3Start.setText(activity.schedule3.getString("startTime"));
                        activity.edtSched3Stop.setText(activity.schedule3.getString("stopTime"));
                        int area = activity.schedule3.getInt("area");
                        if(area == 0) activity.edtSched3Area.setText("1");
                        else if(area == 1) activity.edtSched3Area.setText("2");
                        else if(area == 1) activity.edtSched3Area.setText("3");
                        else activity.edtSched3Area.setText("Tất cả");
                        activity.btnSched3Active.setOn(isActive);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            activity.btnActive1.setEnabled(true);
            activity.btnActive2.setEnabled(true);
            activity.btnActive3.setEnabled(true);

        }
    }


//    public class JSONConverter {
//
//        public String run() {
//            // Create a new JSON object
//            JSONObject jsonObject = new JSONObject();
//            String jsonString = "ERROR";
//            try {
//                // Add key-value pairs to the JSON object
//                jsonObject.put("cycle", 5);
//                jsonObject.put("flow1", 20);
//                jsonObject.put("flow2", 10);
//                jsonObject.put("flow3", 20);
//                jsonObject.put("area", 0);
//                jsonObject.put("isActive", true);
//                jsonObject.put("schedulerName", "LỊCH TƯỚI 1");
//                jsonObject.put("startTime", "18:30");
//                jsonObject.put("stopTime", "18:40");
//
//                // Convert the JSON object to a string
//                jsonString = jsonObject.toString();
//
//                // Print the JSON string
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            return jsonString;
//        }
//    }

    private void handleNotification(String message){
        String[] parts = message.split(",");
        if (parts[0].equals("0")) {
            handleStatus(parts, txtStatus1, txtCycle1);
        }
        else if (parts[0].equals("1")) {
            handleStatus(parts, txtStatus2, txtCycle2);
        }
        else if (parts[0].equals("2")) {
            handleStatus(parts, txtStatus3, txtCycle3);
        }
        else
            Log.d("TEST", "Invalid Schedule");
    }

    @SuppressLint("SetTextI18n")
    String area = "";
    private void handleStatus(String[] parts, TextView txtStatus, TextView txtCycle){
        switch (parts[2]) {
            case Constants.MIXER1:
                txtCycle.setText("Vòng " + (Integer.parseInt(parts[1]) + 1));
                txtStatus.setText("Máy trộn 1");
                break;
            case Constants.MIXER2:
                txtCycle.setText("Vòng " + (Integer.parseInt(parts[1]) + 1));
                txtStatus.setText("Máy trộn 2");
                break;
            case Constants.MIXER3:
                txtCycle.setText("Vòng " + (Integer.parseInt(parts[1]) + 1));
                txtStatus.setText("Máy trộn 3");
                break;
            case Constants.PUMP_IN:
                txtCycle.setText("Vòng " + (Integer.parseInt(parts[1]) + 1));
                txtStatus.setText("Bơm vào");
                break;
            case Constants.SELECTOR:
                txtCycle.setText("Vòng " + (Integer.parseInt(parts[1]) + 1));
                if (parts[3].equals(0)) area = "Vườn 1";
                else if (parts[3].equals(1)) area = "Vườn 2";
                else area = "Vườn 3";
                txtStatus.setText("Chuẩn bị tưới " + area);
                break;
            case Constants.PUMP_OUT:
                txtCycle.setText("Vòng " + (Integer.parseInt(parts[1]) + 1));
                txtStatus.setText("Đang tưới " + area);
                break;
            case Constants.NEXT_CYCLE:
                txtCycle.setText("Vòng " + (Integer.parseInt(parts[1]) + 1));
                txtStatus.setText("Vòng tiếp theo");
                break;
            case Constants.END:
//                txtCycle.setText("Vòng ");
                txtStatus.setText("Đã kết thúc");
                break;
            case Constants.IDLE:
                break;
            default:
                Log.d("TEST", "Invalid Status");
                break;
        }
    }

    private void setDataStatistic() {
        SharedPreferences totalTimePreferences = getSharedPreferences("totalTime", MODE_PRIVATE);
        long totalTime = totalTimePreferences.getLong("total_time",0);

        SharedPreferences countTimesPreferences = getSharedPreferences("countTimes", MODE_PRIVATE);
        long countTimes = countTimesPreferences.getLong("count_times",0);

        if(countTimes != 0) {
            txtAvgTime.setText(String.format("Thời gian gửi nhận trung bình: %dms", totalTime/countTimes));
        }
        txtCountTimes.setText(String.format("Số lần gửi thành công: %d", countTimes));
    }




}
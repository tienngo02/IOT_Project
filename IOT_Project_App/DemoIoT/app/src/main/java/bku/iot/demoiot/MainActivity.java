package bku.iot.demoiot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    MQTTHelper mqttHelper;
    TextView txtTemp, txtLig, txtHumi;
    LabeledSwitch btnLED, btnPUMP;
    ImageButton btnSettings, btnError;
    TabHost myTabHost;
    String check = "0";

    private String password = "aio_dAjN47GWlQwyiMtudpF1uVaiTS";

    String[] API_FEED_URLS = {  "https://io.adafruit.com/api/v2/tienngo/feeds/humidity",
                                "https://io.adafruit.com/api/v2/tienngo/feeds/temperature",
                                "https://io.adafruit.com/api/v2/tienngo/feeds/light",
                                "https://io.adafruit.com/api/v2/tienngo/feeds/notification",
                                "https://io.adafruit.com/api/v2/tienngo/feeds/scheduler1",
                                "https://io.adafruit.com/api/v2/tienngo/feeds/scheduler2",
                                "https://io.adafruit.com/api/v2/tienngo/feeds/scheduler3"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtTemp = findViewById(R.id.txtTemperature);
        txtLig = findViewById(R.id.txtLight);
        txtHumi = findViewById(R.id.txtHumidity);
        btnLED = findViewById(R.id.btnLED);
        btnPUMP = findViewById(R.id.btnPUMP);
        btnSettings = findViewById(R.id.btnSettings);
        btnError = findViewById(R.id.btnError);

        myTabHost = findViewById(R.id.tabHost);
        myTabHost.setup();
        TabHost.TabSpec spec1, spec2, spec3;

        spec1 = myTabHost.newTabSpec("home");
        spec1.setContent(R.id.tab1);
        spec1.setIndicator("HOME");
        myTabHost.addTab(spec1);

        spec2 = myTabHost.newTabSpec("schedule");
        spec2.setContent(R.id.tab2);
        spec2.setIndicator("SCHEDULE");
        myTabHost.addTab(spec2);

        spec3 = myTabHost.newTabSpec("statistic");
        spec3.setContent(R.id.tab3);
        spec3.setIndicator("STATISTIC");
        myTabHost.addTab(spec3);

        SharedPreferences keyPreferences = getSharedPreferences("adafruitKey", MODE_PRIVATE);
        String aioKey = keyPreferences.getString("aio_key","");
        password = aioKey;

        setDataFromAPIs();

        btnLED.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
//                JSONConverter jsonConverter = new JSONConverter();
//                String temp =  jsonConverter.run();
//                Log.d("TEST", temp);
//                sendDataMQTT("nvtien/feeds/button1", temp);

                if(isOn == true){
                    sendDataMQTT("nvtien/feeds/button1","1");
                    checkSendData("2", btnLED);
                }
                else{
                    sendDataMQTT("nvtien/feeds/button1","0");
                    checkSendData("1", btnLED);
                }
            }
        });

        btnPUMP.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if(isOn == true){
                    sendDataMQTT("nvtien/feeds/button2","1");
                    checkSendData("4", btnPUMP);
                }
                else{
                    sendDataMQTT("nvtien/feeds/button2","0");
                    checkSendData("3", btnPUMP);
                }
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

        Intent getNewKey = getIntent();
        String newKey = getNewKey.getStringExtra("newKey");
        if(!TextUtils.isEmpty(newKey)) {
            Log.d("TEST", "setPassword!");
            password = newKey;
        }
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

                btnLED.setEnabled(true);
                btnPUMP.setEnabled(true);
                hideErrorMessage();
                Log.d("TEST", "connectComplete!");
            }

            @Override
            public void connectionLost(Throwable cause) {
                btnLED.setEnabled(false);
                btnPUMP.setEnabled(false);
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
                else if(topic.contains("button1")){
                    if(message.toString().equals("1")){
                        btnLED.setOn(true);
                    }
                    else{
                        btnLED.setOn(false);
                    }
                }
                else if(topic.contains("button2")){
                    if(message.toString().equals("1")){
                        btnPUMP.setOn(true);
                    }
                    else{
                        btnPUMP.setOn(false);
                    }
                }
                else if(topic.contains("check")){
                    check = message.toString();
                    notifyDataUpdated();
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

    }

    private void showErrorMessage(String message) {
        LinearLayout errorContainer = findViewById(R.id.errorContainer);
        TextView textErrorMessage = findViewById(R.id.textErrorMessage);

        textErrorMessage.setText(message);
        errorContainer.setVisibility(View.VISIBLE);
    }

    private void hideErrorMessage() {
        LinearLayout errorContainer = findViewById(R.id.errorContainer);
        errorContainer.setVisibility(View.GONE);
    }


    private final Object lock = new Object();

    public void checkSendData(String dataCheck, LabeledSwitch btnDevice) {
        final long TIMEOUT = 5000; // Thời gian chờ tối đa là 5 giây
        final long startTime = System.currentTimeMillis();

        showErrorMessage("Đang gửi dữ liệu ");
        btnDevice.setEnabled(false);
        new Thread(() -> {
            synchronized (lock) {
                while (!check.equals(dataCheck) && System.currentTimeMillis() - startTime < TIMEOUT) {
                    try {
                        lock.wait(TIMEOUT - (System.currentTimeMillis() - startTime)); // Chờ đến khi có dữ liệu hoặc hết thời gian
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (!check.equals(dataCheck)) {
                    runOnUiThread(() -> showErrorMessage("Gửi thất bại "));
//                    btnDevice.setEnabled(true);
                    if (dataCheck.equals("2")){
                        btnDevice.setOn(false);
                        sendDataMQTT("nvtien/feeds/button1","0");
                    }
                    else if (dataCheck.equals("4")){
                        btnDevice.setOn(false);
                        sendDataMQTT("nvtien/feeds/button2","0");
                    }
                    else if (dataCheck.equals("1")){
                        btnDevice.setOn(true);
                        sendDataMQTT("nvtien/feeds/button1","1");
                    }
                    else {
                        btnDevice.setOn(true);
                        sendDataMQTT("nvtien/feeds/button2","1");
                    }
                    btnDevice.setEnabled(true);

                } else {
                    btnDevice.setEnabled(true);
                    runOnUiThread(this::hideErrorMessage);
                }

            }
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
            String[] arrayResult = new String[5];
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

                    if (nameFeed.equals("temperature")) {
                        activity.txtTemp.setText(lastValue + "°C");
                    } else if (nameFeed.equals("light")) {
                        activity.txtLig.setText(lastValue + "lux");
                    } else if (nameFeed.equals("humidity")) {
                        activity.txtHumi.setText(lastValue + "%");
                    } else if (nameFeed.equals("button1")) {
                        if (lastValue.equals("1")) {
                            activity.btnLED.setOn(true);
                        } else {
                            activity.btnLED.setOn(false);
                        }
                    } else if (nameFeed.equals("button2")) {
                        if (lastValue.equals("1")) {
                            activity.btnPUMP.setOn(true);
                        } else {
                            activity.btnPUMP.setOn(false);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
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


}
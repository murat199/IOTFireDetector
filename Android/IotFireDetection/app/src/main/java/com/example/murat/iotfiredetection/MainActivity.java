package com.example.murat.iotfiredetection;

import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appyvet.materialrangebar.RangeBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ImageView imageViewState;
    TextView txtJsonResponse, txtUpdatedDate;
    LinearLayout linearLayoutWrapper;
    Button btnUpdate;

    int sensorTemperature=0,sensorAirQuality=0,sensorAlcohol=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageViewState=(ImageView)findViewById(R.id.imageViewState);
        txtJsonResponse=(TextView) findViewById(R.id.txtJsonResponse);
        txtUpdatedDate=(TextView) findViewById(R.id.txtUpdatedDate);
        linearLayoutWrapper=(LinearLayout)findViewById(R.id.linearLayoutWrapper);

        String url = "https://api.thingspeak.com/channels/**********/feeds.json?api_key=******************&results=1";
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .build();
        btnUpdate=(Button)findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            final String requestData = response.body().string();
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtJsonResponse.setText(requestData);
                                    try {
                                        JSONObject parentJson = new JSONObject(requestData);
                                        JSONArray feeds = parentJson.getJSONArray("feeds");
                                        for (int i = 0; i < feeds.length(); i++) {
                                            JSONObject c = feeds.getJSONObject(i);
                                            sensorTemperature = Integer.parseInt(c.getString("field1"));
                                            sensorAirQuality = Integer.parseInt(c.getString("field2"));
                                            sensorAlcohol = Integer.parseInt(c.getString("field3"));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    /*Toast.makeText(getApplicationContext(),"sensorTemperature:"+sensorTemperature,Toast.LENGTH_SHORT).show();
                                    Toast.makeText(getApplicationContext(),"sensorAirQuality:"+sensorAirQuality,Toast.LENGTH_SHORT).show();
                                    Toast.makeText(getApplicationContext(),"sensorAlcohol:"+sensorAlcohol,Toast.LENGTH_SHORT).show();*/
                                    setStateScreen();
                                }
                            });
                        }
                    }
                });
            }
        });

        new Timer().scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            final String requestData = response.body().string();
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtJsonResponse.setText(requestData);
                                    try {
                                        JSONObject parentJson = new JSONObject(requestData);
                                        JSONArray feeds = parentJson.getJSONArray("feeds");
                                        for (int i = 0; i < feeds.length(); i++) {
                                            JSONObject c = feeds.getJSONObject(i);
                                            sensorTemperature = Integer.parseInt(c.getString("field1"));
                                            sensorAirQuality = Integer.parseInt(c.getString("field2"));
                                            sensorAlcohol = Integer.parseInt(c.getString("field3"));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    /*Toast.makeText(getApplicationContext(),"sensorTemperature:"+sensorTemperature,Toast.LENGTH_SHORT).show();
                                    Toast.makeText(getApplicationContext(),"sensorAirQuality:"+sensorAirQuality,Toast.LENGTH_SHORT).show();
                                    Toast.makeText(getApplicationContext(),"sensorAlcohol:"+sensorAlcohol,Toast.LENGTH_SHORT).show();*/
                                    setStateScreen();
                                }
                            });
                        }
                    }
                });
                //Toast.makeText(getApplicationContext(),"Değerler Güncellendi!",Toast.LENGTH_SHORT).show();
            }
        },0,20000);
    }

    void setStateScreen(){
        int stateOfRisk=0;
        stateOfRisk+=getStateOfMq135();
        stateOfRisk+=getStateOfMq3();
        stateOfRisk+=getStateOfTemperature();
        if(stateOfRisk==0){
            //State Low
            linearLayoutWrapper.setBackgroundColor(getResources().getColor(R.color.colorStateLow));
            imageViewState.setImageResource(R.drawable.state_fire_low);
        }else if(stateOfRisk<7){
            //State Medium
            linearLayoutWrapper.setBackgroundColor(getResources().getColor(R.color.colorStateMedium));
            imageViewState.setImageResource(R.drawable.state_fire_medium);
        }else if(stateOfRisk<10){
            //State High
            linearLayoutWrapper.setBackgroundColor(getResources().getColor(R.color.colorStateHigh));
            imageViewState.setImageResource(R.drawable.state_fire_high);
        }

        String dateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        txtUpdatedDate.setText("Son güncelleme : "+dateNow);
    }
    int getStateOfMq135(){
        int result=0;
        String resultText="Hava Kalitesi : "+sensorAirQuality+" PPM. ";
        ImageView imageView=(ImageView)findViewById(R.id.imageViewStateAirQuality);
        TextView textView=(TextView)findViewById(R.id.txtStateAirQuality);
        if(sensorAirQuality<=300){
            resultText+="Hava temiz.";
            imageView.setImageResource(R.drawable.ic_state_low_24dp);
            result=0;
        }else if(sensorAirQuality<=500){
            resultText+="Kacak olabilir. Kontrol et!";
            imageView.setImageResource(R.drawable.ic_state_medium_24dp);
            result=1;
        }else if(sensorAirQuality<=700){
            resultText+="Kotu hava. Tehlikedesin!";
            imageView.setImageResource(R.drawable.ic_state_medium_24dp);
            result=2;
        }else if(sensorAirQuality>700){
            resultText+="Acilen ortamdan uzaklas!";
            imageView.setImageResource(R.drawable.ic_state_high_24dp);
            result=3;
        }
        textView.setText(resultText);
        return result;
    }
    int getStateOfMq3(){
        int result=0;
        String resultText="Alkol : "+sensorAlcohol+". ";
        ImageView imageView=(ImageView)findViewById(R.id.imageViewStateAlcohol);
        TextView textView=(TextView)findViewById(R.id.txtStateAlcohol);
        /*if(sensorAlcohol<200){
            resultText+="Alkol Yok";
            imageView.setImageResource(R.drawable.ic_state_low_24dp);
            result=0;
        }else if (sensorAlcohol>=200 && sensorAlcohol<280){
            resultText+="Bir bira";
            imageView.setImageResource(R.drawable.ic_state_medium_24dp);
            result=1;
        }
        else if (sensorAlcohol>=280 && sensorAlcohol<450){
            resultText+="Iki veya daha fazla bira";
            imageView.setImageResource(R.drawable.ic_state_medium_24dp);
            result=2;
        }else if(sensorAlcohol>450){
            resultText+="Tehlikeli derece alkol kokusu";
            imageView.setImageResource(R.drawable.ic_state_high_24dp);
            result=3;
        }*/
        if(sensorAlcohol<600){
            resultText+="Alkol Yok";
            imageView.setImageResource(R.drawable.ic_state_low_24dp);
            result=0;
        }else{
            resultText+="Tehlikeli derece alkol kokusu";
            imageView.setImageResource(R.drawable.ic_state_high_24dp);
            result=3;
        }
        textView.setText(resultText);
        return result;
    }
    int getStateOfTemperature(){
        int result=0;
        String resultText="Sıcaklık : ";
        ImageView imageView=(ImageView)findViewById(R.id.imageViewStateTemperature);
        TextView textView=(TextView)findViewById(R.id.txtStateTemperature);
        resultText+=" "+sensorTemperature+" Derece.";
        if(sensorTemperature<30){
            imageView.setImageResource(R.drawable.ic_state_low_24dp);
            result=0;
        }else if (sensorTemperature<35){
            imageView.setImageResource(R.drawable.ic_state_medium_24dp);
            result=1;
        }else if (sensorTemperature<40){
            imageView.setImageResource(R.drawable.ic_state_medium_24dp);
            result=2;
        }else{
            imageView.setImageResource(R.drawable.ic_state_high_24dp);
            result=3;
        }
        textView.setText(resultText);
        return result;
    }
}

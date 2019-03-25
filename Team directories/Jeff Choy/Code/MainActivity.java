package ca.bcit.gpstracker;

import java.util.*;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    TcpClient myTcpClient;
    private FusedLocationProviderClient locationClient;
    public double latitude;
    public double longitude;
    public JSONObject locationJSON = new JSONObject();

    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... message) {

            //we create a TCPClient object
            myTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            myTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //response received from server
            Log.d("test", "response " + values[0]);
            //process server response here....

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationClient = LocationServices.getFusedLocationProviderClient(this);


        Button sendButton = (Button)findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                getDeviceLocation();
                setLocationJSON();
                EditText ipField = (EditText) findViewById(R.id.ip_field);
                EditText portField = (EditText) findViewById(R.id.port_field);
                myTcpClient.SERVER_IP = ipField.getText().toString();
                myTcpClient.SERVER_PORT = Integer.parseInt(portField.getText().toString());
                new ConnectTask().execute("");
                new Timer().scheduleAtFixedRate(new TimerTask(){
                    @Override
                    public void run(){
                        getDeviceLocation();
                        setLocationJSON();
                        myTcpClient.sendMessage(locationJSON.toString());
                    }
                },0,2000);
            }
        });
    }

    public void getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "First enable LOCATION ACCESS in settings.", Toast.LENGTH_LONG).show();
            return;
        }
        locationClient.getLastLocation()
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                });
    }

    public void setLocationJSON() {
        EditText userName = (EditText) findViewById(R.id.user_name);
        try {
            locationJSON.put("name", userName.getText().toString());
            locationJSON.put("latitude", latitude);
            locationJSON.put("longitude", longitude);
        }
        catch (JSONException e) {};
    }
}

/*------------------------------------------------------------------------------------------------------------------
--	SOURCE FILE:	MainActivity.java - The entry point for the Android GPS Tracker application.
--
--
--	PROGRAM:		GPS Tracker
--
--
--	FUNCTIONS:		protected void onCreate(Bundle savedInstanceState)
--					public void getDeviceLocation()
--					public void setLocationJSON()
--                  private boolean checkConnectionDetails()
--
--	DATE:			Mar 25, 2019
--
--
--	REVISIONS:
--
--
--	DESIGNER:		Jenny Ly, Jeffrey Choy, Ben Zhang
--
--
--	PROGRAMMER:		Jenny Ly, Jeffrey Choy
--
--
--	NOTES:
--
----------------------------------------------------------------------------------------------------------------------*/
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
import android.view.Gravity;
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

    /*------------------------------------------------------------------------------------------------------------------
    --	CLASS:		    ConnectTask
    --
    --
    --	DATE:			March 25, 2019
    --
    --
    --	REVISIONS:
    --
    --
    --	DESIGNER:		Ben Zhang, Jeffrey Choy
    --
    --
    --	PROGRAMMER:		Jeffrey Choy
    --
    --
    --	INTERFACE:		public class ConnectTask extends AsyncTask<String, String, TcpClient>
    --
    --	RETURNS:
    --
    --
    --	NOTES:			Creates an asynchronous task that will execute at a set interval to send
    --                          data to the server.
    ----------------------------------------------------------------------------------------------------------------------*/
    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        /*------------------------------------------------------------------------------------------------------------------
        --	FUNCTION:		doInBackground
        --
        --
        --	DATE:			March 25, 2019
        --
        --
        --	REVISIONS:
        --
        --
        --	DESIGNER:		Ben Zhang, Jeffrey Choy
        --
        --
        --	PROGRAMMER:		Jeffrey Choy
        --
        --
        --	INTERFACE:		 protected TcpClient doInBackground(String... message)
        --
        --	RETURNS:
        --
        --
        --	NOTES:			Specifies the background task for the TCP client.
        ----------------------------------------------------------------------------------------------------------------------*/
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

    /*------------------------------------------------------------------------------------------------------------------
    --	FUNCTION:		onCreate
    --
    --
    --	DATE:			March 25, 2019
    --
    --
    --	REVISIONS:
    --
    --
    --	DESIGNER:		Ben Zhang, Jeffrey Choy
    --
    --
    --	PROGRAMMER:		Jeffrey Choy
    --
    --
    --	INTERFACE:		protected void onCreate(Bundle savedInstanceState)
    --
    --	RETURNS:
    --
    --
    --	NOTES:			Function initially executed at the entry to the Android application. Adds an event listener
    --								to the connect button which parses the user's text input and attempts to create
    --								a TCP connection.
    ----------------------------------------------------------------------------------------------------------------------*/
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
                if (!checkConnectionDetails()) {
                    Toast toast = Toast.makeText(MainActivity.this, "Invalid Connection Details", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0 ,5);
                    toast.show();
                    return;
                }

                myTcpClient.SERVER_IP = ipField.getText().toString();
                myTcpClient.SERVER_PORT = Integer.parseInt(portField.getText().toString());

                Toast toast = Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0 ,0);
                toast.show();
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

    /*------------------------------------------------------------------------------------------------------------------
    --	FUNCTION:		getDeviceLocation
    --
    --
    --	DATE:			March 25, 2019
    --
    --
    --	REVISIONS:
    --
    --
    --	DESIGNER:		Ben Zhang, Jeffrey Choy
    --
    --
    --	PROGRAMMER:		Jeffrey Choy
    --
    --
    --	INTERFACE:		public void getDeviceLocation()
    --
    --	RETURNS:
    --
    --
    --	NOTES:			Checks for location permissions. Retrieves client location and stores them.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
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

    /*------------------------------------------------------------------------------------------------------------------
    --	FUNCTION:		setLocationJSON
    --
    --
    --	DATE:			March 25, 2019
    --
    --
    --	REVISIONS:
    --
    --
    --	DESIGNER:		Ben Zhang, Jeffrey Choy
    --
    --
    --	PROGRAMMER:		Jeffrey Choy
    --
    --
    --	INTERFACE:		public void setLocationJSON()
    --
    --	RETURNS:
    --
    --
    --	NOTES:			Writes the required data to a JSON object for transmission.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public void setLocationJSON() {
        EditText userName = (EditText) findViewById(R.id.user_name);
        try {
            locationJSON.put("name", userName.getText().toString());
            locationJSON.put("latitude", latitude);
            locationJSON.put("longitude", longitude);
        }
        catch (JSONException e) {};
    }

    /*------------------------------------------------------------------------------------------------------------------
    --	FUNCTION:		checkConnectionDetails
    --
    --
    --	DATE:			March 25, 2019
    --
    --
    --	REVISIONS:
    --
    --
    --	DESIGNER:		Jeffrey Choy
    --
    --
    --	PROGRAMMER:		Jeffrey Choy
    --
    --
    --	INTERFACE:		private boolean checkConnectionDetails()
    --
    --	RETURNS:
    --
    --
    --	NOTES:			Validates the IP Address and Port number for a match with the required server details.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    private boolean checkConnectionDetails() {
        EditText ipField = (EditText) findViewById(R.id.ip_field);
        EditText portField = (EditText) findViewById(R.id.port_field);
        myTcpClient.SERVER_IP = ipField.getText().toString();
        if(Integer.parseInt(portField.getText().toString()) != 3000) {
            return false;
        }
        if (!ipField.getText().toString().equals("18.217.49.198")) {
            return false;
        }

        return true;
    }
}

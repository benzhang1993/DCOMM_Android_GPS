/*------------------------------------------------------------------------------------------------------------------
--	SOURCE FILE:	TcpClient.java - The class for creating and handling a TCP connection with the server.
--
--
--	PROGRAM:		GPS Tracker
--
--
--	FUNCTIONS:		public TcpClient(OnMessageReceived listener)
--					public void sendMessage(final String message)
--					public void stopClient()
--                  public void run()
--                  public void messageReceived(String message)
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

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TcpClient {

    public static final String TAG = TcpClient.class.getSimpleName();
    public static String SERVER_IP = "18"; //server IP address
    public static int SERVER_PORT = 0;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;

    /*------------------------------------------------------------------------------------------------------------------
    --	FUNCTION:		TcpClient
    --
    --
    --	DATE:			March 25, 2019
    --
    --
    --	REVISIONS:
    --
    --
    --	DESIGNER:		Ben Zhang, Jenny Ly
    --
    --
    --	PROGRAMMER:		Jenny Ly
    --
    --
    --	INTERFACE:		public TcpClient(OnMessageReceived listener)
    --
    --	RETURNS:
    --
    --
    --	NOTES:			Constructor of the class. OnMessagedReceived listens for the messages received from server
    ----------------------------------------------------------------------------------------------------------------------*/
    public TcpClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /*------------------------------------------------------------------------------------------------------------------
    --	FUNCTION:		sendMessage
    --
    --
    --	DATE:			March 25, 2019
    --
    --
    --	REVISIONS:
    --
    --
    --	DESIGNER:		Ben Zhang, Jenny Ly
    --
    --
    --	PROGRAMMER:		Jenny Ly
    --
    --
    --	INTERFACE:		public void sendMessage(final String message)
                            final String message to be sent to the server
    --
    --	RETURNS:
    --
    --
    --	NOTES:			Sends the message entered by client to the server
    ----------------------------------------------------------------------------------------------------------------------*/
    public void sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null) {
                    Log.d(TAG, "Sending: " + message);
                    mBufferOut.println(message);
                    mBufferOut.flush();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /*------------------------------------------------------------------------------------------------------------------
    --	FUNCTION:		stopClient
    --
    --
    --	DATE:			March 25, 2019
    --
    --
    --	REVISIONS:
    --
    --
    --	DESIGNER:		Ben Zhang, Jenny Ly
    --
    --
    --	PROGRAMMER:		Jenny Ly
    --
    --
    --	INTERFACE:		public void stopClient()
    --
    --	RETURNS:
    --
    --
    --	NOTES:			Close the connection and release the members
    ----------------------------------------------------------------------------------------------------------------------*/
    public void stopClient() {

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    /*------------------------------------------------------------------------------------------------------------------
    --	FUNCTION:		run
    --
    --
    --	DATE:			March 25, 2019
    --
    --
    --	REVISIONS:
    --
    --
    --	DESIGNER:		Ben Zhang, Jenny Ly
    --
    --
    --	PROGRAMMER:		Jenny Ly
    --
    --
    --	INTERFACE:		public void run()
    --
    --	RETURNS:
    --
    --
    --	NOTES:			Creates the socket connection and assigns the input and output buffers. Listens
    --                      for messages from the server and dispenses them to dummy function.
    ----------------------------------------------------------------------------------------------------------------------*/
    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            Log.d("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVER_PORT);

            try {

                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                //in this while the client listens for the messages sent by the server
                while (mRun) {

                    mServerMessage = mBufferIn.readLine();

                    if (mServerMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(mServerMessage);
                    }

                }

                Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }

    }

    /*------------------------------------------------------------------------------------------------------------------
    --	FUNCTION:		sendMessage
    --
    --
    --	DATE:			March 25, 2019
    --
    --
    --	REVISIONS:
    --
    --
    --	DESIGNER:		Ben Zhang, Jenny Ly
    --
    --
    --	PROGRAMMER:		Jenny Ly
    --
    --
    --	INTERFACE:		public interface OnMessageReceived
    --
    --	RETURNS:
    --
    --
    --	NOTES:			Dummy function to handle messages received from the server.
    ----------------------------------------------------------------------------------------------------------------------*/
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

}
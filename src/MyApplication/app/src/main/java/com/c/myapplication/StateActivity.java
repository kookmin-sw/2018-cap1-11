package com.c.myapplication;

import android.app.FragmentManager;
import android.app.Activity;
import android.os.Bundle;
import java.net.InetAddress;
import android.net.wifi.WifiManager;
import android.net.DhcpInfo;
import java.io.IOException;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.view.Window;


import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.widget.*;
import org.json.JSONException;
import org.json.JSONObject;
import android.view.View;



public class StateActivity  extends AppCompatActivity {

    int udpPort = 1025;
    DatagramSocket socket;

    TextView sensorsDataReceivedTimeTextView;
    TextView temperatureValueTextView;
    TextView avgTemperatureValueTextView;
    TextView humidityValueTextView;
    TextView avgHumidityValueTextView;
    TextView voltageValueTextView;
    TextView freeRamValueTextView;

    Button refreshButton;
    Switch preventScreenLockSwitch;

    boolean appInBackground = false;
    boolean doneEditing = true;

    @Override
    protected void onResume() {
        super.onResume();
        appInBackground = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        appInBackground = true;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state);
        // disable auto turn screen off feature
        final Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON); // Turn screen on if off
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep screen on

        sensorsDataReceivedTimeTextView = (TextView) findViewById(R.id.sensorsDataReceivedTimeTextView);
        temperatureValueTextView = (TextView) findViewById(R.id.temperatureValueTextView);
        avgTemperatureValueTextView = (TextView) findViewById(R.id.avgTemperatureValueTextView);
        humidityValueTextView = (TextView) findViewById(R.id.humidityValueTextView);
        avgHumidityValueTextView = (TextView) findViewById(R.id.avgHumidityValueTextView);

        refreshButton = (Button) findViewById(R.id.refreshButton);

        // request sensors data from WoT sensors node
        (new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new DatagramSocket(udpPort);
                    while (true) {
                        if (appInBackground) {
                            continue;
                        }
                        try {
                            sendUdpData(Commands.GET_ALL_SENSORS_DATA, null);
                            Thread.sleep(10000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        })).start();

        // listen for data from requesting data from WoT sensors node
        (new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (appInBackground) {
                        continue;
                    }
                    DatagramPacket udpPacket = receiveUdpData();
                    if (udpPacket == null) {
                        continue;
                    }
                    String udpPacketData =  new String( udpPacket.getData());
                    try {
                        JSONObject jsonObj = new JSONObject(udpPacketData);
                        updateUserInterface( jsonObj);
                    } catch ( JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        })).start();

        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // request data from WoT sensors node
                (new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sendUdpData( Commands.GET_ALL_SENSORS_DATA, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })).start();
            }
        });

        preventScreenLockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        });
    }

    void updateUserInterface( final JSONObject jsonObj) {
        try {
            final double temperature = jsonObj.getDouble("temperature");
            final double avgTemperature = jsonObj.getDouble("avgTemperature");
            final double humidity = jsonObj.getDouble("humidity");
            final double avgHumidity = jsonObj.getDouble("avgHumidity");
            final double voltage = jsonObj.getDouble("voltage");
            final int freeRam = jsonObj.getInt("freeRam");

            sensorsDataReceivedTimeTextView.post(new Runnable() {
                public void run() {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    sensorsDataReceivedTimeTextView.setText(dateFormat.format(new Date()));
                }
            });
            temperatureValueTextView.post(new Runnable() {
                public void run() {
                    temperatureValueTextView.setText(String.valueOf(temperature) + "°C");
                }
            });
            avgTemperatureValueTextView.post(new Runnable() {
                public void run() {
                    avgTemperatureValueTextView.setText(String.valueOf(avgTemperature) + "°C");
                }
            });
            humidityValueTextView.post(new Runnable() {
                public void run() {
                    humidityValueTextView.setText(String.valueOf(humidity) + "%");
                }
            });
            avgHumidityValueTextView.post(new Runnable() {
                public void run() {
                    avgHumidityValueTextView.setText(String.valueOf(avgHumidity) + "%");
                }
            });
            voltageValueTextView.post(new Runnable() {
                public void run() {
                    voltageValueTextView.setText(String.valueOf(voltage) + "V");
                }
            });
            freeRamValueTextView.post(new Runnable() {
                public void run() {
                    freeRamValueTextView.setText(String.valueOf(freeRam) + "Bytes");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager)getSystemService( Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // no DHCP info...can't do nothing more
        if ( dhcp == null) {
            return null;
        }
        int ipAddress = dhcp.gateway;
        byte[] ipQuads = new byte[4];
        ipQuads[0] = (byte)(ipAddress & 0xFF);;
        ipQuads[1] = (byte)((ipAddress >> 8) & 0xFF);
        ipQuads[2] = (byte)((ipAddress >> 16) & 0xFF);
        ipQuads[3] = (byte)((ipAddress >> 24) & 0xFF);

        /* 192.168.4.1 = [ -64, -88, 4, 1] */
        return InetAddress.getByAddress( ipQuads);
    }

    void sendUdpData( Commands cmd, byte[] params) {
        try {
            final DatagramPacket packet;
            int paramsLength = ( params != null ? params.length : 0);
            byte data[] = new byte[paramsLength + 1];
            byte command[] = new byte[1];
            command[0] = cmd.getValue();
            System.arraycopy( command, 0, data, 0, command.length);
            if ( params != null) {
                System.arraycopy(params, 0, data, 1, params.length);
            }
            packet = new DatagramPacket( data, data.length,
                    getBroadcastAddress(), udpPort);
            socket.send( packet);
        } catch( IOException e){
            e.printStackTrace();
        }
    }

    DatagramPacket receiveUdpData() {
        try {
            byte[] data  = new byte[1024];
            DatagramPacket packet = new DatagramPacket( data, data.length);
            if ( socket == null) {
                return null;
            }
            socket.receive(packet);
            Log.i("HD:receiveUdpData", new String( packet.getData()).trim());
            return packet;
        } catch( IOException e){
            Log.e("HD:receiveUdpData", "Error occurred when receiving UDP data on port: " + udpPort);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Define the set of commands which can be send to the WoT sensor(s) node.
     */
    enum Commands {
        GET_ALL_SENSORS_DATA ( (byte)97);
        private final byte id;
        Commands( byte id) { this.id = id; }
        public byte getValue() { return id; }
    }
}

package com.aware.plugin.template;

import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Plugin;

public class Plugin extends Aware_Plugin {
    public static final String ACTION_AWARE_PLUGIN_CHARGING_MONITOR = "ACTION_AWARE_PLUGIN_CHARGING_MONITOR";

    public static final String EXTRA_DATA = "data";

    public static ContextProducer context_producer;

    private static ContentValues data;

    private static String charger_type = ""; //1=solar, 2= pc ,3=ac

    private static String size_of_panel = "";

    private static String lux_inside_box = "";

    private static String type_of_light = "";

    private static String type_of_solar_cell = "";

    private static String solar_current = "";

    private static int percentage_start = -1; // shall be 0 - 100

    private static int percentage_end = -1;

    private static long time_start = -1;

    private static long time_end = -1;

    private static double speed = 0;

    private static long time_discharge = -1;

    private static double speed_discharge = 0;

    private static int percentage_start_discharge = -1; // shall be 0 - 100

    private static int percentage_end_discharge = 101;
    public Thread solar_thread = new Thread() {
        public void run() {
            while (true) {

                data = new ContentValues();
                data.put(Provider.Charging_Monitor_Data.TIMESTAMP, System.currentTimeMillis());
                data.put(Provider.Charging_Monitor_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                data.put(Provider.Charging_Monitor_Data.CHARGER_TYPE, charger_type);
                data.put(Provider.Charging_Monitor_Data.PANEL_SIZE, size_of_panel);
                data.put(Provider.Charging_Monitor_Data.BOX_LUX, lux_inside_box);
                data.put(Provider.Charging_Monitor_Data.LIGHT_TYPE, type_of_light);
                data.put(Provider.Charging_Monitor_Data.CELL_TYPE, type_of_solar_cell);
                data.put(Provider.Charging_Monitor_Data.SOLAR_CURRENT, solar_current);
                data.put(Provider.Charging_Monitor_Data.PERCENTAGE_START, percentage_start);
                data.put(Provider.Charging_Monitor_Data.PERCENTAGE_END, percentage_end);
                data.put(Provider.Charging_Monitor_Data.TIME_START, time_start);
                data.put(Provider.Charging_Monitor_Data.SPEED, speed);
                data.put(Provider.Charging_Monitor_Data.PERCENTAGE_START_DISCHARGE, percentage_start_discharge);
                data.put(Provider.Charging_Monitor_Data.PERCENTAGE_END_DISCHARGE, percentage_end_discharge);
                data.put(Provider.Charging_Monitor_Data.TIME_DISCHARGE, time_discharge);
                data.put(Provider.Charging_Monitor_Data.SPEED_DISCHARGE, speed_discharge);
                data.put(Provider.Charging_Monitor_Data.TIME_END, time_end);

                getContentResolver().insert(Provider.Charging_Monitor_Data.CONTENT_URI, data);

                //Share context
                context_producer.onContext();

                try {
                    Thread.sleep(6000);
                    //detect once every 6 secs
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::"+getResources().getString(R.string.app_name);
        DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

        //Initialize our plugin's settings
        if( Aware.getSetting(this, Settings.STATUS_PLUGIN_TEMPLATE).length() == 0 ) {
            Aware.setSetting(this, Settings.STATUS_PLUGIN_TEMPLATE, true);
        }

        //Activate programmatically any sensors/plugins you need here
        //e.g., Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER,true);
        //NOTE: if using plugin with dashboard, you can specify the sensors you'll use there.

        //Any active plugin/sensor shares its overall context using broadcasts
        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
                //Broadcast your context here
                Intent context_solar_charger = new Intent();
                context_solar_charger.setAction(ACTION_AWARE_PLUGIN_CHARGING_MONITOR);
                context_solar_charger.putExtra(EXTRA_DATA, data);
                sendBroadcast(context_solar_charger);
                if( DEBUG ) {
                    Log.d(TAG, "Inserted: " + data.toString());
                }
            }
        };
        context_producer = CONTEXT_PRODUCER;

        solar_thread.start();
        //Add permissions you need (Support for Android M) e.g.,
        //REQUIRED_PERMISSIONS.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //To sync data to the server, you'll need to set this variables from your ContentProvider
        //DATABASE_TABLES = Provider.DATABASE_TABLES
        //TABLES_FIELDS = Provider.TABLES_FIELDS
        //CONTEXT_URIS = new Uri[]{ Provider.Table_Data.CONTENT_URI }

        //Activate plugin
        Aware.startPlugin(this, "com.aware.plugin.template");
    }

    //This function gets called every 5 minutes by AWARE to make sure this plugin is still running.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Check if the user has toggled the debug messages
        DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Aware.setSetting(this, Settings.STATUS_PLUGIN_TEMPLATE, false);

        //Deactivate any sensors/plugins you activated here
        //e.g., Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER, false);

        //Stop plugin
        Aware.stopPlugin(this, "com.aware.plugin.template");
    }
}

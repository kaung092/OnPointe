package com.example.rene.dancesafe;

/*
  This class includes a small
  subset of standard GATT attributes
  for demonstration purposes.
*/

import java.util.HashMap;


public class SampleGattAttributes {

    private static HashMap<String, String> attributes = new HashMap();

    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    //public static String BEAN_SERVICE = "A495FF10-C5B1-4B44-B512-1370F02D74DE";

    public static String BEAN_SERVICE = "A495FF20-C5B1-4B44-B512-1370F02D74DE";

    //public static String BEAN_CHARACTERISTIC = "A495FF11-C5B1-4B44-B512-1370F02D74DE";

    public static String BEAN_CHARACTERISTIC = "A495FF21-C5B1-4B44-B512-1370F02D74DE";

    public static String MYO_SERVICE = "D5060001-A904-DEB9-4748-2C7F4A124842";

    public static String MYO_CHARACTERISTIC = "D5060101-A904-DEB9-4748-2C7F4A124842";


    // Samples services and sample characteristics
    static {
        // Sample Services.
        attributes.put(BEAN_SERVICE,"Bean Serial Service");
        attributes.put(MYO_SERVICE,"MYO Service");
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");

        // Sample Characteristics.
        attributes.put(BEAN_CHARACTERISTIC, "Bean Transport Characteristic");
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put(MYO_CHARACTERISTIC,"MYO Characteristic");
    }


    // Find device name based on UUID
    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

}
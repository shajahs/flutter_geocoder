package com.aloisdeniel.geocoder;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeocoderPlugin implements FlutterPlugin, MethodCallHandler {

    private MethodChannel channel;
    private Geocoder geocoder;

    @Override
    public void onAttachedToEngine(FlutterPluginBinding binding) {
        channel = new MethodChannel(binding.getBinaryMessenger(), "github.com/aloisdeniel/geocoder");
        channel.setMethodCallHandler(this);
        geocoder = new Geocoder(binding.getApplicationContext());
    }

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("findAddressesFromQuery")) {
            String address = call.argument("address");
            findAddressesFromQuery(address, result);
        } else if (call.method.equals("findAddressesFromCoordinates")) {
            double latitude = call.argument("latitude");
            double longitude = call.argument("longitude");
            findAddressesFromCoordinates(latitude, longitude, result);
        } else {
            result.notImplemented();
        }
    }

    private void findAddressesFromQuery(final String address, final Result result) {
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocationName(address, 20);
                if (addresses.isEmpty()) {
                    result.error("not_available", "Empty", null);
                } else {
                    result.success(createAddressMapList(addresses));
                }
            } catch (IOException e) {
                result.error("failed", "Failed to get address", e.getMessage());
            }
        }).start();
    }

    private void findAddressesFromCoordinates(final double latitude, final double longitude, final Result result) {
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 20);
                if (addresses.isEmpty()) {
                    result.error("not_available", "Empty", null);
                } else {
                    result.success(createAddressMapList(addresses));
                }
            } catch (IOException e) {
                result.error("failed", "Failed to get address", e.getMessage());
            }
        }).start();
    }

    private List<Map<String, Object>> createAddressMapList(List<Address> addresses) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Address address : addresses) {
            Map<String, Object> addressMap = new HashMap<>();
            addressMap.put("latitude", address.getLatitude());
            addressMap.put("longitude", address.getLongitude());
            addressMap.put("featureName", address.getFeatureName());
            addressMap.put("countryName", address.getCountryName());
            addressMap.put("locality", address.getLocality());
            addressMap.put("postalCode", address.getPostalCode());
            result.add(addressMap);
        }
        return result;
    }
}

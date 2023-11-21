/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author muqtadir32903
 */


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class USBHandler implements USBDriver {
  private UsbManager usbManager;
  
  private UsbDevice device;
  
  private final String ACTION_USB_PERMISSION = "com.morpho.msocrossdevicesample.morphosmart.USB_PERMISSION";
  
  String template = null;
  
  private UsbEndpoint endpoint;
  
  private UsbDeviceConnection usbDeviceConnection;
  
  private MorphoSmartLiteCallback callback;
  
  private boolean isRegistered = false;
  
  private int deviceType;
  
  private int length;
  
  volatile boolean isCancelled = true;
  
  volatile boolean isTimeout = false;
  
  String deviceSerialNumber;
  
  String deviceModel;
  
  public byte[] instantReceive(Context context) throws IOException, IllegalArgumentException {
    byte[] rspData = new byte[1024];
    if (this.device != null && 
      this.usbManager.hasPermission(this.device)) {
      this.endpoint = this.device.getInterface(1).getEndpoint(0);
      this.length = this.usbDeviceConnection.bulkTransfer(this.endpoint, rspData, rspData.length, 0);
      if (Utility.byteArrayToHexString(rspData).substring(30, 32).equalsIgnoreCase("E5"))
        this.isCancelled = true; 
      close();
      return rspData;
    } 
    return null;
  }
  
  public void send(byte[] command, Context context, MorphoSmartLiteCallback callback) throws IOException, IllegalArgumentException {
    this.isCancelled = false;
    this.callback = callback;
    HashMap<String, UsbDevice> usbDevices = this.usbManager.getDeviceList();
    if (!usbDevices.isEmpty()) {
      boolean keep = true;
      for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
        this.device = entry.getValue();
        int deviceVID = this.device.getVendorId();
        if (deviceVID == 8797 || deviceVID == 1947) {
          if (this.usbManager.hasPermission(this.device)) {
            this.usbDeviceConnection = this.usbManager.openDevice(this.device);
            this.usbDeviceConnection.claimInterface(this.device.getInterface(1), true);
            this.endpoint = this.device.getInterface(1).getEndpoint(1);
            if (this.endpoint.getType() == 2 && this.endpoint.getDirection() == 0)
              this.length = this.usbDeviceConnection.bulkTransfer(this.endpoint, command, command.length, 0); 
          } 
          keep = false;
        } 
        if (!keep)
          break; 
      } 
    } else {
      callback.onFailure(MorphoSmartErrors.Errors.COMMUNICATION_ERROR);
    } 
  }
  
  public byte[] receive(Context context) throws IOException, IllegalArgumentException {
    byte[] rspData = new byte[1024];
    if (this.device != null && 
      this.usbManager.hasPermission(this.device)) {
      this.endpoint = this.device.getInterface(1).getEndpoint(0);
      do {
        this.length = this.usbDeviceConnection.bulkTransfer(this.endpoint, rspData, rspData.length, 0);
        if (Utility.byteArrayToHexString(rspData).substring(24, 26).equalsIgnoreCase("71"))
          if (Utility.byteArrayToHexString(rspData).substring(30, 32).equalsIgnoreCase("00") && 
            Utility.byteArrayToHexString(rspData).substring(32, 34).equalsIgnoreCase("40")) {
            String quality = Utility.byteArrayToHexString(rspData).substring(38, 40);
            this.callback.onQualityResponse(Integer.parseInt(quality, 16));
          }  
        if (Utility.byteArrayToHexString(rspData).substring(24, 26).equalsIgnoreCase("71"))
          if (Utility.byteArrayToHexString(rspData).substring(30, 32).equalsIgnoreCase("00") && 
            Utility.byteArrayToHexString(rspData).substring(32, 34).equalsIgnoreCase("01")) {
            String message = Utility.byteArrayToHexString(rspData).substring(38, 40);
            if (Integer.parseInt(message, 16) == 0)
              this.callback.onMessages(MorphoSmartErrors.Messages.MOVE_NO_FINGER); 
            if (Integer.parseInt(message, 16) == 1)
              this.callback.onMessages(MorphoSmartErrors.Messages.MOVE_FINGER_UP); 
            if (Integer.parseInt(message, 16) == 2)
              this.callback.onMessages(MorphoSmartErrors.Messages.MOVE_FINGER_DOWN); 
            if (Integer.parseInt(message, 16) == 3)
              this.callback.onMessages(MorphoSmartErrors.Messages.MOVE_FINGER_LEFT); 
            if (Integer.parseInt(message, 16) == 4)
              this.callback.onMessages(MorphoSmartErrors.Messages.MOVE_FINGER_RIGHT); 
            if (Integer.parseInt(message, 16) == 5)
              this.callback.onMessages(MorphoSmartErrors.Messages.PRESS_FINGER_HARDER); 
            if (Integer.parseInt(message, 16) == 6)
              this.callback.onMessages(MorphoSmartErrors.Messages.LATENT); 
            if (Integer.parseInt(message, 16) == 7)
              this.callback.onMessages(MorphoSmartErrors.Messages.REMOVE_FINGER); 
            if (Integer.parseInt(message, 16) == 8)
              this.callback.onMessages(MorphoSmartErrors.Messages.FINGER_OK); 
            if (Integer.parseInt(message, 16) == 9)
              this.callback.onMessages(MorphoSmartErrors.Messages.FINGER_DETECTED); 
            if (Integer.parseInt(message, 16) == 10)
              this.callback.onMessages(MorphoSmartErrors.Messages.FINGER_MISPLACED); 
            if (Integer.parseInt(message, 16) == 11)
              this.callback.onMessages(MorphoSmartErrors.Messages.LIVE_OK); 
          }  
        if (Utility.byteArrayToHexString(rspData).substring(24, 26).equalsIgnoreCase("21")) {
          if (Utility.byteArrayToHexString(rspData).substring(30, 32).equalsIgnoreCase("FA")) {
            this.callback.onFailure(MorphoSmartErrors.Errors.REQUEST_TIMED_OUT);
            this.isTimeout = true;
            close();
            break;
          } 
          if (Utility.byteArrayToHexString(rspData).substring(30, 32).equalsIgnoreCase("00")) {
            boolean ret = parseResponseTemplate(Utility.byteArrayToHexString(rspData));
            if (ret) {
              close();
              return Utility.hexStringToByteArray(this.template);
            } 
          } 
        } 
        if (Utility.byteArrayToHexString(rspData).substring(30, 32).equalsIgnoreCase("E5")) {
          close();
          break;
        } 
        if (getDeviceType() == 0) {
          close();
          break;
        } 
      } while (!Utility.byteArrayToHexString(rspData).substring(24, 26).equalsIgnoreCase("21") && !this.isCancelled);
    } 
    close();
    return null;
  }
  
  void initiate(MorphoSmartLiteCallback callback, Context context) {
    this.callback = callback;
    initializeReceiver(context);
    obtainPermission(context);
  }
  
  private void close() {
    this.usbDeviceConnection.releaseInterface(this.device.getInterface(1));
    this.usbDeviceConnection.close();
  }
  
  private void unregisterReceiver(Context context) {
    if (this.isRegistered)
      context.unregisterReceiver(this.broadcastReceiver); 
    this.isRegistered = false;
  }
  
  private void obtainPermission(Context context) {
    this.usbManager = (UsbManager)context.getSystemService("usb");
    HashMap<String, UsbDevice> usbDevices = this.usbManager.getDeviceList();
    if (!usbDevices.isEmpty()) {
      boolean keep = true;
      for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
        this.device = entry.getValue();
        int deviceVID = this.device.getVendorId();
        if (deviceVID == 8797 || deviceVID == 1947) {
          PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent("com.morpho.msocrossdevicesample.morphosmart.USB_PERMISSION"), 0);
          this.usbManager.requestPermission(this.device, pi);
          keep = false;
        } else {
          this.usbDeviceConnection = null;
          this.device = null;
        } 
        if (!keep)
          break; 
      } 
    } 
  }
  
  private int checkDeviceKey(MorphoSmartLiteCallback callback, Context context) throws IOException {
    send(Utility.hexStringToByteArray("53594E4304000000FBFFFFFFC6010002454E"), context, callback);
    byte[] response = instantReceive(context);
    if (response != null) {
      String responseString = Utility.byteArrayToHexString(response);
      if (responseString.substring(24, 26).equalsIgnoreCase("C6") && responseString.substring(30, 32).equalsIgnoreCase("00"))
        return 1; 
      if (Utility.byteArrayToHexString(response).substring(30, 32).equalsIgnoreCase("B8"))
        return 2; 
      callback.onFailure(MorphoSmartErrors.Errors.COMMUNICATION_ERROR);
      return 0;
    } 
    callback.onFailure(MorphoSmartErrors.Errors.COMMUNICATION_ERROR);
    return 0;
  }
  
  private void initializeReceiver(Context context) {
    IntentFilter filter = new IntentFilter();
    filter.addAction("com.morpho.msocrossdevicesample.morphosmart.USB_PERMISSION");
    filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
    filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
    if (!this.isRegistered)
      context.registerReceiver(this.broadcastReceiver, filter); 
    this.isRegistered = true;
  }
  
  private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
      public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.morpho.msocrossdevicesample.morphosmart.USB_PERMISSION")) {
          boolean granted = intent.getExtras().getBoolean("permission");
          if (granted) {
            USBHandler.this.usbDeviceConnection = USBHandler.this.usbManager.openDevice(USBHandler.this.device);
            USBHandler.this.callback.onPermissionGranted(MorphoSmartErrors.Response.PERMISSION_GRANTED);
            try {
              USBHandler.this.setDeviceType(USBHandler.this.checkDeviceKey(USBHandler.this.callback, context));
              USBHandler.this.getDeviceSN(USBHandler.this.callback, context);
              USBHandler.this.getDeviceMN(USBHandler.this.callback, context);
            } catch (IOException e) {
              USBHandler.this.setDeviceType(0);
              USBHandler.this.callback.onFailure(MorphoSmartErrors.Errors.COMMUNICATION_ERROR);
            } 
          } else {
            USBHandler.this.setDeviceType(0);
            USBHandler.this.callback.onPermissionDenied(MorphoSmartErrors.Errors.PERMISSION_DENIED);
          } 
        } else if (intent.getAction().equals("android.hardware.usb.action.USB_DEVICE_DETACHED")) {
          USBHandler.this.setDeviceType(0);
          USBHandler.this.unregisterReceiver(context);
        } 
      }
    };
  
  private boolean parseResponseTemplate(String response) {
    if (response.substring(30, 32).equalsIgnoreCase("00") && response.substring(32, 34).equalsIgnoreCase("00")) {
      if (response.substring(42, 44).equalsIgnoreCase("6E")) {
        String len = swapEndianness(response.substring(44, 48));
        this.template = response.substring(48, 48 + Utility.hexStringToInt(len) * 2);
        return true;
      } 
      return false;
    } 
    this.callback.onFailure(MorphoSmartErrors.Errors.COMMUNICATION_ERROR);
    return false;
  }
  
  private String swapEndianness(String len) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i <= len.length() - 2; i += 2)
      result.append((new StringBuilder(len.substring(i, i + 2))).reverse()); 
    len = result.reverse().toString();
    return len;
  }
  
  void getDeviceSN(MorphoSmartLiteCallback callback, Context context) throws IOException {
    send(Utility.hexStringToByteArray("53594e4304000000fbffffff050100ba454e"), context, callback);
    byte[] response = instantReceive(context);
    String serial = parseSerialNumber(Utility.byteArrayToHexString(response)).replaceAll("\\s+", "");
    if (serial.isEmpty()) {
      getOemDeviceSN(callback, context);
    } else {
      this.deviceSerialNumber = serial;
    } 
  }
  
  private void getOemDeviceSN(MorphoSmartLiteCallback callback, Context context) throws IOException {
    send(Utility.hexStringToByteArray("53594e4304000000fbffffff050100bc454e"), context, callback);
    byte[] response = instantReceive(context);
    this.deviceSerialNumber = parseSerialNumber(Utility.byteArrayToHexString(response)).replaceAll("\\s+", "");
  }
  
  private String parseSerialNumber(String response) throws IOException {
    String serialNumber = "";
    if (response.substring(30, 32).equalsIgnoreCase("00"))
      if (response.substring(32, 34).equalsIgnoreCase("BA") || response.substring(32, 34).equalsIgnoreCase("BC")) {
        String len = Utility.swapEndianness(response.substring(34, 38));
        serialNumber = response.substring(38, 38 + Utility.hexStringToInt(len) * 2);
        if (serialNumber.length() > 22)
          serialNumber = serialNumber.substring(0, 22); 
      }  
    return Utility.convertHexToString(serialNumber);
  }
  
  void getDeviceMN(MorphoSmartLiteCallback callback, Context context) throws IOException {
    send(Utility.hexStringToByteArray("53594e4304000000fbffffff050100b8454e"), context, callback);
    byte[] response = instantReceive(context);
    this.deviceModel = parseModel(Utility.byteArrayToHexString(response));
  }
  
  private String parseModel(String response) throws IOException {
    String modelNumber = "";
    if (response.substring(30, 32).equalsIgnoreCase("00"))
      if (response.substring(32, 34).equalsIgnoreCase("B8")) {
        String len = Utility.swapEndianness(response.substring(34, 38));
        modelNumber = response.substring(38, 38 + Utility.hexStringToInt(len) * 2);
        if (modelNumber.contains("00"))
          modelNumber = modelNumber.substring(0, modelNumber.indexOf("00")); 
      }  
    return Utility.convertHexToString(modelNumber);
  }
  
  boolean isPermissionObtained(Context context) throws IOException {
    return hasPermission(context);
  }
  
  boolean setDeviceType(MorphoSmartLiteCallback callback, Context context) throws IOException {
    if (getDeviceType() == 0) {
      int type = checkDeviceKey(callback, context);
      if (type != 0) {
        setDeviceType(type);
        return true;
      } 
      return false;
    } 
    return true;
  }
  
  private boolean hasPermission(Context context) {
    this.usbManager = (UsbManager)context.getSystemService("usb");
    HashMap<String, UsbDevice> usbDevices = this.usbManager.getDeviceList();
    if (!usbDevices.isEmpty())
      for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
        this.device = entry.getValue();
        int deviceVID = this.device.getVendorId();
        if (deviceVID == 8797 || deviceVID == 1947)
          return this.usbManager.hasPermission(this.device); 
        this.usbDeviceConnection = null;
        this.device = null;
      }  
    return false;
  }
  
  int getDeviceType() {
    return this.deviceType;
  }
  
  void setDeviceType(int deviceType) {
    this.deviceType = deviceType;
  }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author muqtadir32903
 */


import android.content.Context;
import java.io.IOException;
import java.util.Arrays;
import javax.crypto.NoSuchPaddingException;

public class MSODevice {
  private static final int MAX_TIMEOUT_SECONDS = 42948;
  
  private static MSODevice mMSOInstance;
  
  private final USBHandler mUSBHandler = new USBHandler();
  
  private byte[] decryptionKeyBytes;
  
  public synchronized String sdkVersion() {
    return "Version: 2.1";
  }
  
  public static final synchronized MSODevice getInstance() {
    synchronized (MSODevice.class) {
      if (mMSOInstance == null)
        return mMSOInstance = new MSODevice(); 
      mMSOInstance.decryptionKeyBytes = null;
      return mMSOInstance;
    } 
  }
  
  public static final synchronized MSODevice getInstance(byte[] decryptionKey) {
    synchronized (MSODevice.class) {
      if (mMSOInstance == null)
        mMSOInstance = new MSODevice(); 
      mMSOInstance.decryptionKeyBytes = decryptionKey;
      return mMSOInstance;
    } 
  }
  
  public synchronized void requestPermission(MorphoSmartLiteCallback callback, Context context) throws IOException {
    this.mUSBHandler.initiate(callback, context);
  }
  
  public synchronized String checkDeviceKey(MorphoSmartLiteCallback callback, Context context) throws IOException {
    if (this.mUSBHandler.isPermissionObtained(context))
      return getDeviceKCV(callback, context); 
    this.mUSBHandler.setDeviceType(0);
    callback.onPermissionRequired(MorphoSmartErrors.Errors.USB_PERMISSION_REQUIRED);
    requestPermission(callback, context);
    return null;
  }
  
  public synchronized String getDeviceSerialNumber(MorphoSmartLiteCallback callback, Context context) throws IOException {
    if (this.mUSBHandler.isPermissionObtained(context)) {
      this.mUSBHandler.getDeviceSN(callback, context);
      return this.mUSBHandler.deviceSerialNumber;
    } 
    this.mUSBHandler.setDeviceType(0);
    callback.onPermissionRequired(MorphoSmartErrors.Errors.USB_PERMISSION_REQUIRED);
    requestPermission(callback, context);
    return null;
  }
  
  public synchronized String getDeviceModel(MorphoSmartLiteCallback callback, Context context) throws IOException {
    if (this.mUSBHandler.isPermissionObtained(context)) {
      this.mUSBHandler.getDeviceMN(callback, context);
      return this.mUSBHandler.deviceModel;
    } 
    this.mUSBHandler.setDeviceType(0);
    callback.onPermissionRequired(MorphoSmartErrors.Errors.USB_PERMISSION_REQUIRED);
    requestPermission(callback, context);
    return null;
  }
  
  public synchronized void isRegistered(MorphoSmartLiteCallback callback, Context context) throws IOException, UnsupportedOperationException {
    if (this.mUSBHandler.isPermissionObtained(context)) {
      String deviceKCV = getDeviceKCV(callback, context);
      if (deviceKCV != null)
        if (getDecryptionKeyKCV(callback).equalsIgnoreCase(deviceKCV)) {
          callback.onRegistered(MorphoSmartErrors.Response.REGISTERED);
        } else {
          callback.onRegistered(MorphoSmartErrors.Response.NOT_REGISTERED);
        }  
    } else {
      this.mUSBHandler.setDeviceType(0);
      callback.onPermissionRequired(MorphoSmartErrors.Errors.USB_PERMISSION_REQUIRED);
      requestPermission(callback, context);
    } 
  }
  
  private String getDecryptionKeyKCV(MorphoSmartLiteCallback callback) {
    String cipher = null;
    try {
      cipher = Utility.byteArrayToHexString(Utility.encrypt(Utility.hexStringToByteArray("00000000000000000000000000000000"), this.decryptionKeyBytes));
    } catch (NoSuchPaddingException|java.security.NoSuchAlgorithmException|java.security.InvalidAlgorithmParameterException|javax.crypto.BadPaddingException|java.security.InvalidKeyException|javax.crypto.IllegalBlockSizeException e) {
      callback.onFailure(MorphoSmartErrors.Errors.KCV_FAILURE);
    } 
    if (cipher != null) {
      cipher = cipher.substring(0, 6);
    } else {
      callback.onFailure(MorphoSmartErrors.Errors.KCV_FAILURE);
    } 
    return cipher;
  }
  
  private String getDeviceKCV(MorphoSmartLiteCallback callback, Context context) throws IOException {
    if (this.mUSBHandler.isPermissionObtained(context)) {
      this.mUSBHandler.send(Utility.hexStringToByteArray("53594E4304000000FBFFFFFFC6010002454E"), context, callback);
      byte[] response = this.mUSBHandler.instantReceive(context);
      if (response != null) {
        String responseString = Utility.byteArrayToHexString(response);
        if (responseString.substring(24, 26).equalsIgnoreCase("C6") && responseString.substring(30, 32).equalsIgnoreCase("00")) {
          String kcv = responseString.substring(32, 38);
          return kcv;
        } 
        if (Utility.byteArrayToHexString(response).substring(30, 32).equalsIgnoreCase("B8")) {
          callback.onFailure(MorphoSmartErrors.Errors.KEY_NOT_FOUND);
          return null;
        } 
        callback.onFailure(MorphoSmartErrors.Errors.COMMUNICATION_ERROR);
        return null;
      } 
      callback.onFailure(MorphoSmartErrors.Errors.COMMUNICATION_ERROR);
      return null;
    } 
    this.mUSBHandler.setDeviceType(0);
    callback.onPermissionRequired(MorphoSmartErrors.Errors.USB_PERMISSION_REQUIRED);
    requestPermission(callback, context);
    return null;
  }
  
  public synchronized void unRegsiter(MorphoSmartLiteCallback callback, Context context, byte[] lockPayload) throws IOException {
    if (this.mUSBHandler.isPermissionObtained(context)) {
      if (this.mUSBHandler.setDeviceType(callback, context))
        if (this.mUSBHandler.getDeviceType() == 1) {
          String ilvUnlock = getLoadKsSymmetric(lockPayload);
          this.mUSBHandler.send(Utility.hexStringToByteArray(ilvUnlock), context, callback);
          byte[] response = this.mUSBHandler.instantReceive(context);
          if (response != null) {
            if (Utility.byteArrayToHexString(response).substring(30, 32).equalsIgnoreCase("00")) {
              callback.onLocked(MorphoSmartErrors.Response.DEVICE_UNREGISTERED);
            } else if (Utility.byteArrayToHexString(response).substring(30, 32).equalsIgnoreCase("FF")) {
              callback.onFailure(MorphoSmartErrors.Errors.ERROR);
            } else if (Utility.byteArrayToHexString(response).substring(30, 32).equalsIgnoreCase("FE")) {
              callback.onFailure(MorphoSmartErrors.Errors.BAD_PARAMETER);
            } else if (Utility.byteArrayToHexString(response).substring(30, 32).equalsIgnoreCase("DE")) {
              callback.onFailure(MorphoSmartErrors.Errors.SECURITY_ERROR);
            } else if (Utility.byteArrayToHexString(response).substring(30, 32).equalsIgnoreCase("F0")) {
              callback.onFailure(MorphoSmartErrors.Errors.BAD_SIGNATURE);
            } else if (Utility.byteArrayToHexString(response).substring(30, 32).equalsIgnoreCase("E2")) {
              callback.onFailure(MorphoSmartErrors.Errors.SECU_CERTIFICATE_NOT_EXIST);
            } 
          } else {
            callback.onFailure(MorphoSmartErrors.Errors.COMMUNICATION_ERROR);
          } 
        } else if (this.mUSBHandler.getDeviceType() == 2) {
          callback.onFailure(MorphoSmartErrors.Errors.KEY_NOT_FOUND);
        }  
    } else {
      this.mUSBHandler.setDeviceType(0);
      callback.onPermissionRequired(MorphoSmartErrors.Errors.USB_PERMISSION_REQUIRED);
      requestPermission(callback, context);
    } 
  }
  
  public synchronized void register(MorphoSmartLiteCallback callback, Context context, byte[] unlockPayload) throws IOException {
    if (this.mUSBHandler.isPermissionObtained(context)) {
      if (this.mUSBHandler.setDeviceType(callback, context))
        if (this.mUSBHandler.getDeviceType() == 1) {
          String ILVunlock = getLoadKsSymmetric(unlockPayload);
          this.mUSBHandler.send(Utility.hexStringToByteArray(ILVunlock), context, callback);
          byte[] response = this.mUSBHandler.instantReceive(context);
          if (response != null) {
            if (Utility.byteArrayToHexString(response).substring(30, 32).equalsIgnoreCase("00")) {
              callback.onUnlocked(MorphoSmartErrors.Response.DEVICE_REGISTERED);
            } else if (Utility.byteArrayToHexString(response).substring(30, 32).equalsIgnoreCase("FF")) {
              callback.onFailure(MorphoSmartErrors.Errors.ERROR);
            } else if (Utility.byteArrayToHexString(response).substring(30, 32).equalsIgnoreCase("FE")) {
              callback.onFailure(MorphoSmartErrors.Errors.BAD_PARAMETER);
            } else if (Utility.byteArrayToHexString(response).substring(30, 32).equalsIgnoreCase("DE")) {
              callback.onFailure(MorphoSmartErrors.Errors.SECURITY_ERROR);
            } else if (Utility.byteArrayToHexString(response).substring(30, 32).equalsIgnoreCase("F0")) {
              callback.onFailure(MorphoSmartErrors.Errors.BAD_SIGNATURE);
            } else if (Utility.byteArrayToHexString(response).substring(30, 32).equalsIgnoreCase("E2")) {
              callback.onFailure(MorphoSmartErrors.Errors.SECU_CERTIFICATE_NOT_EXIST);
            } 
          } else {
            callback.onFailure(MorphoSmartErrors.Errors.COMMUNICATION_ERROR);
          } 
        } else if (this.mUSBHandler.getDeviceType() == 2) {
          callback.onFailure(MorphoSmartErrors.Errors.KEY_NOT_FOUND);
        }  
    } else {
      this.mUSBHandler.setDeviceType(0);
      callback.onPermissionRequired(MorphoSmartErrors.Errors.USB_PERMISSION_REQUIRED);
      requestPermission(callback, context);
    } 
  }
  
  public void capture(MorphoSmartLiteCallback callback, Context context) throws IOException {
    capture(0, callback, context);
  }
  
  public synchronized void capture(int timeoutInSeconds, MorphoSmartLiteCallback callback, Context context) throws IOException {
    if (this.mUSBHandler.isPermissionObtained(context)) {
      if (this.mUSBHandler.setDeviceType(callback, context))
        if (timeoutInSeconds <= 42948) {
          if (this.mUSBHandler.getDeviceType() == 1) {
            String ILVWithTimeout = getEnrollILVWithTimeout(timeoutInSeconds, "53594E433E000000C1FFFFFF213B000000000001010001040200310014010000140100003801006E44040012000000A701000034040041000000A5010001A6010000AE010000AF010000454E");
            this.mUSBHandler.send(Utility.hexStringToByteArray(ILVWithTimeout), context, callback);
            byte[] response = this.mUSBHandler.receive(context);
            if (response != null) {
              int secondHalfSize = response.length % 8;
              int firstHalfEnd = response.length - secondHalfSize;
              byte[] paddedBytes = Arrays.copyOf(response, firstHalfEnd);
              byte[] slice = Arrays.copyOfRange(response, firstHalfEnd, firstHalfEnd + secondHalfSize);
              if (this.decryptionKeyBytes != null) {
                byte[] decryptedTemplateData = new byte[0];
                try {
                  decryptedTemplateData = Utility.decryptTemplate(paddedBytes, this.decryptionKeyBytes);
                } catch (NoSuchPaddingException|java.security.NoSuchAlgorithmException|java.security.InvalidKeyException|java.security.InvalidAlgorithmParameterException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException e) {
                  callback.onFailure(MorphoSmartErrors.Errors.TEMPLATE_DECRYPTION_FAILED);
                } 
                byte[] decryptedTemplateWithExtraPadding = new byte[decryptedTemplateData.length + slice.length];
                System.arraycopy(decryptedTemplateData, 0, decryptedTemplateWithExtraPadding, 0, decryptedTemplateData.length);
                System.arraycopy(slice, 0, decryptedTemplateWithExtraPadding, decryptedTemplateData.length, slice.length);
                callback.onResponse(decryptedTemplateWithExtraPadding);
                this.mUSBHandler.template = null;
              } else {
                callback.onFailure(MorphoSmartErrors.Errors.INVALID_KEY);
              } 
            } else if (!this.mUSBHandler.isCancelled && !this.mUSBHandler.isTimeout) {
              callback.onFailure(MorphoSmartErrors.Errors.COMMUNICATION_ERROR);
            } 
          } else if (this.mUSBHandler.getDeviceType() == 2) {
            String ILVWithTimeout = getEnrollILVWithTimeout(timeoutInSeconds, "53594E433A000000C5FFFFFF2137000000000001010001040200310014010000140100003801006E4404001200000034040041000000A5010001A6010000AE010000AF010000454E");
            this.mUSBHandler.send(Utility.hexStringToByteArray(ILVWithTimeout), context, callback);
            byte[] response = this.mUSBHandler.receive(context);
            if (response != null) {
              callback.onResponse(response);
              this.mUSBHandler.template = null;
            } else if (!this.mUSBHandler.isCancelled && !this.mUSBHandler.isTimeout) {
              callback.onFailure(MorphoSmartErrors.Errors.COMMUNICATION_ERROR);
            } 
          } else if (this.mUSBHandler.getDeviceType() == 0) {
            callback.onFailure(MorphoSmartErrors.Errors.COMMUNICATION_ERROR);
          } 
        } else {
          callback.onFailure(MorphoSmartErrors.Errors.INVALID_TIMEOUT);
        }  
    } else {
      this.mUSBHandler.setDeviceType(0);
      callback.onPermissionRequired(MorphoSmartErrors.Errors.USB_PERMISSION_REQUIRED);
      requestPermission(callback, context);
    } 
  }
  
  public void cancelLiveAcquisition(MorphoSmartLiteCallback callback, Context context) throws IOException {
    if (this.mUSBHandler.isPermissionObtained(context)) {
      if (!this.mUSBHandler.isCancelled) {
        this.mUSBHandler.send(Utility.hexStringToByteArray("53594E4303000000FCFFFFFF700000454E"), context, callback);
        byte[] response = this.mUSBHandler.instantReceive(context);
        if (response != null && 
          Utility.byteArrayToHexString(response).substring(30, 32).equalsIgnoreCase("E5"))
          callback.onCancelled(MorphoSmartErrors.Errors.COMMAND_ABORTED); 
      } 
    } else {
      this.mUSBHandler.setDeviceType(0);
      callback.onPermissionRequired(MorphoSmartErrors.Errors.USB_PERMISSION_REQUIRED);
      requestPermission(callback, context);
    } 
  }
  
  public synchronized String getDeviceCertificate(MorphoSmartLiteCallback callback, Context context) throws IOException {
    String deviceCertificate = null;
    if (this.mUSBHandler.isPermissionObtained(context)) {
      if (this.mUSBHandler.setDeviceType(callback, context))
        if (this.mUSBHandler.getDeviceType() == 1 || this.mUSBHandler.getDeviceType() == 2) {
          String ILVDeviceCertificate = "53594E4304000000FBFFFFFF81010000454E";
          this.mUSBHandler.send(Utility.hexStringToByteArray(ILVDeviceCertificate), context, callback);
          byte[] response = this.mUSBHandler.instantReceive(context);
          if (response != null)
            if (Utility.byteArrayToHexString(response).substring(24, 26).equalsIgnoreCase("81") && Utility.byteArrayToHexString(response).substring(30, 32).equalsIgnoreCase("00")) {
              int len = Utility.hexStringToInt(Utility.swapEndianness(Utility.byteArrayToHexString(response).substring(34, 38))) * 2;
              deviceCertificate = Utility.byteArrayToHexString(response).substring(38, 38 + len);
            } else {
              String error = Utility.byteArrayToHexString(response).substring(30, 32);
              if (error.equalsIgnoreCase("FE")) {
                callback.onFailure(MorphoSmartErrors.Errors.ILVERR_BADPARAMETER);
              } else if (error.equalsIgnoreCase("E2")) {
                callback.onFailure(MorphoSmartErrors.Errors.ILVERR_SECU_CERTIF_NOT_EXIST);
              } else if (error.equalsIgnoreCase("F4")) {
                callback.onFailure(MorphoSmartErrors.Errors.ILVERR_CMD_INPROGRESS);
              } 
            }  
        }  
    } else {
      this.mUSBHandler.setDeviceType(0);
      callback.onPermissionRequired(MorphoSmartErrors.Errors.USB_PERMISSION_REQUIRED);
      requestPermission(callback, context);
    } 
    return deviceCertificate;
  }
  
  private String getEnrollILVWithTimeout(int timeout, String ILV) {
    byte[] timeoutBytes = Utility.intToByteArray(timeout);
    String timeoutHexString = Utility.byteArrayToHexString(timeoutBytes);
    String timeoutString = Utility.swapEndianness(timeoutHexString.substring(4, 8));
    StringBuilder stringBuffer = new StringBuilder(ILV);
    StringBuilder ILVWithTimeout = stringBuffer.replace(32, 36, timeoutString);
    return ILVWithTimeout.toString();
  }
  
  private String getLoadKsSymmetric(byte[] unlockPayload) {
    String finalPayload = "C7" + Utility.swapEndianness(String.format("%04X", new Object[] { Integer.valueOf(Utility.byteArrayToHexString(unlockPayload).length() / 2 + 1) })) + "02" + Utility.byteArrayToHexString(unlockPayload);
    finalPayload = "53594E43" + Utility.swapEndianness(String.format("%08X", new Object[] { Integer.valueOf(finalPayload.length() / 2) })) + Utility.swapEndianness(Utility.onesComplement(String.format("%08X", new Object[] { Integer.valueOf(finalPayload.length() / 2) }))) + finalPayload + "454E";
    return finalPayload;
  }
}

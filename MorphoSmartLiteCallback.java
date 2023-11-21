/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author muqtadir32903
 */

public interface MorphoSmartLiteCallback {
  void onResponse(byte[] paramArrayOfbyte);
  
  void onRegistered(MorphoSmartErrors.Response paramResponse);
  
  void onQualityResponse(int paramInt);
  
  void onUnlocked(MorphoSmartErrors.Response paramResponse);
  
  void onLocked(MorphoSmartErrors.Response paramResponse);
  
  void onPermissionRequired(MorphoSmartErrors.Errors paramErrors);
  
  void onPermissionGranted(MorphoSmartErrors.Response paramResponse);
  
  void onPermissionDenied(MorphoSmartErrors.Errors paramErrors);
  
  void onCancelled(MorphoSmartErrors.Errors paramErrors);
  
  void onFailure(MorphoSmartErrors.Errors paramErrors);
  
  void onMessages(MorphoSmartErrors.Messages paramMessages);
}

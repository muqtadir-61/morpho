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

interface USBDriver {
  public static final int DEFAULT_TIMEOUT = 0;
  
  byte[] instantReceive(Context paramContext) throws IOException, IllegalArgumentException;
  
  void send(byte[] paramArrayOfbyte, Context paramContext, MorphoSmartLiteCallback paramMorphoSmartLiteCallback) throws IOException, IllegalArgumentException;
  
  byte[] receive(Context paramContext) throws IOException, IllegalArgumentException;
}

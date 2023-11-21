/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author muqtadir32903
 */
public interface ILVTag {
  public static final String REBOOT_TAG = "04";
  
  public static final String GET_DESCRIPTOR_TAG = "05";
  
  public static final String PING_TAG = "08";
  
  public static final String STATUS_TAG = "09";
  
  public static final String VERIFY_TAG = "20";
  
  public static final String ENROLL_TAG = "21";
  
  public static final String IDENTIFY_TAG = "22";
  
  public static final String VERIFY_MATCH_TAG = "23";
  
  public static final String IDENTIFY_MATCH_TAG = "24";
  
  public static final String CONVERT_PK_FVP_TAG = "25";
  
  public static final String CANCEL_TAG = "70";
  
  public static final String ASYNC_MESSAGE_TAG = "71";
  
  public static final String CREATE_DB_TAG = "30";
  
  public static final String ERASE_BASE_TAG = "32";
  
  public static final String ERASE_ALL_BASE_TAG = "34";
  
  public static final String DESTROY_DB_TAG = "3B";
  
  public static final String DESTROY_ALL_BASE_TAG = "33";
  
  public static final String ADD_RECORD_TAG = "35";
  
  public static final String REMOVE_RECORD_TAG = "36";
  
  public static final String FIND_USER_DB_TAG = "38";
  
  public static final String UPDATE_PUBLIC_DATA_TAG = "3C";
  
  public static final String UPDATE_PRIVATE_DATA_TAG = "3D";
  
  public static final String LIST_PUBLIC_FIELDS_TAG = "3E";
  
  public static final String GET_DATA_DB_TAG = "3F";
  
  public static final String GET_BASE_CONFIG_TAG = "07";
  
  public static final String GET_USER_TEMPLATE_QUALITY_TAG = "46";
  
  public static final String SECU_GET_CONFIG_TAG = "80";
  
  public static final String SECU_READ_CERTIFICATE_TAG = "81";
  
  public static final String SECU_STO_CERTIF_TAG = "82";
  
  public static final String SECU_STO_PKCS12_TAG = "83";
  
  public static final String SECU_MUTUAL_AUTH_INIT1_TAG = "84";
  
  public static final String SECU_MUTUAL_AUTH_INIT2_TAG = "85";
  
  public static final String SECU_PROTOCOL_TAG = "86";
  
  public static final String OTP_ENROLL_USER_TAG = "B0";
  
  public static final String OTP_GENERATE_TAG = "B1";
  
  public static final String OTP_SET_PARAMETERS_TAG = "B2";
  
  public static final String OTP_GET_STATUS_TAG = "B3";
  
  public static final String GET_MSO_CONFIG_TAG = "90";
  
  public static final String MODIFY_MSO_CONFIG_TAG = "91";
  
  public static final String CONFIG_UART_TAG = "EE";
  
  public static final String SET_MOC_APDU_TAG = "C5";
  
  public static final String LOAD_MOC_KEYS_TAG = "C4";
  
  public static final String GET_UNLOCK_SEED_TAG = "8B";
  
  public static final String UNLOCK_TAG = "8C";
  
  public static final String KEY_CHECK_VALUE_TAG = "C6";
  
  public static final String MSO_GET_FFD_LOG_TAG = "65";
  
  public static final String INVALID_TAG = "50";
  
  public static final String LOAD_KS_TAG = "C7";
}
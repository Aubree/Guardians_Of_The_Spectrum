# Available Data Collection
This document lists the data that can be obtained through Android APIs.

##android.telephony

*CellSignalStrengthLte* | LTE signal strength related information.    
--------------------------- | -----------------------------
getAsuLevel | Get the LTE signal level as an asu value between 0..97, 99 is unknown Asu is calculated based on 3GPP RSRP. Refer to 3GPP 27.007 (Ver 10.3.0) Sec 8.69
getDbm | Get signal strength as dBm
getLevel | Get signal level as an int from 0..4
getTimingAdvance | Get the timing advance value for LTE. See 3GPP xxxx

*CellIdentityLte* | CellIdentity is to represent a unique LTE cell  
--------------------------- | -----------------------------
getCi | 28-bit Cell Identity, Integer.MAX_VALUE if unknown
getEarfcn | 18-bit Absolute RF Channel Number, Integer.MAX_VALUE if unknown
getMcc | 3-digit Mobile Country Code, 0..999, Integer.MAX_VALUE if unknown
getMnc | 2 or 3-digit Mobile Network Code, 0..999, Integer.MAX_VALUE if unknown
getPci | Physical Cell Id 0..503, Integer.MAX_VALUE if unknown
getTac | 16-bit Tracking Area Code, Integer.MAX_VALUE if unknown

*CellSignalStrengthWcdma* | Wcdma signal strength related information.   
--------------------------- | -----------------------------
getAsuLevel | Get the signal level as an asu value between 0..31, 99 is unknown Asu is calculated based on 3GPP RSRP. Refer to 3GPP 27.007 (Ver 10.3.0) Sec 8.69
getDbm | Get the signal strength as dBm
getLevel | Get signal level as an int from 0..4

*CellIdentityWcdma* | CellIdentity to represent a unique UMTS cell    
--------------------------- | -----------------------------
getCid | CID 28-bit UMTS Cell Identity described in TS 25.331, 0..268435455, Integer.MAX_VALUE if unknown
getLac | 16-bit Location Area Code, 0..65535, Integer.MAX_VALUE if unknown
getMcc | 3-digit Mobile Country Code, 0..999, Integer.MAX_VALUE if unknown
getMnc | 2 or 3-digit Mobile Network Code, 0..999, Integer.MAX_VALUE if unknown
getPsc | 9-bit UMTS Primary Scrambling Code described in TS 25.331, 0..511, Integer.MAX_VALUE if unknown
getUarfcn | 16-bit UMTS Absolute RF Channel Number, Integer.MAX_VALUE if unknown

*CellSignalStrengthGsm* | GSM signal strength related information   
--------------------------- | -----------------------------
getAsuLevel | Get the signal level as an asu value between 0..31, 99 is unknown Asu is calculated based on 3GPP RSRP. Refer to 3GPP 27.007 (Ver 10.3.0) Sec 8.69
getDbm | Get the signal strength as dBm
getLevel | Get signal level as an int from 0..4

*CellIdentityGsm* | CellIdentity to represent a unique GSM cell  
--------------------------- | -----------------------------
getArfcn | 16-bit GSM Absolute RF Channel Number, Integer.MAX_VALUE if unknown
getBsic | 6-bit Base Station Identity Code, Integer.MAX_VALUE if unknown
getCid | CID Either 16-bit GSM Cell Identity described in TS 27.007, 0..65535, Integer.MAX_VALUE if unknown
getLac | 16-bit Location Area Code, 0..65535, Integer.MAX_VALUE if unknown
getMcc | 3-digit Mobile Country Code, 0..999, Integer.MAX_VALUE if unknown
getMnc | 2 or 3-digit Mobile Network Code, 0..999, Integer.MAX_VALUE if unknown
getPsc | Integer.MAX_VALUE, undefined for GSM

*CellSignalStrengthCdma* | Signal strength related information.   
--------------------------- | -----------------------------
getAsuLevel | Get the signal level as an asu value between 0..97, 99 is unknown
getCdmaDbm | Get the CDMA RSSI value in dBm
getCdmaEcio | Get the CDMA Ec/Io value in dB*10
getCdmaLevel | Get cdma as level 0..4
getDbm | Get the signal strength as dBm
getEvdoDbm | Get the EVDO RSSI value in dBm
getEvdoEcio | Get the EVDO Ec/Io value in dB*10
getEvdoLevel | Get Evdo as level 0..4
getEvdoSnr | Get the signal to noise ratio. Valid values are 0-8. 8 is the highest.
getLevel | Get signal level as an int from 0..4

*CellIdentityCdma* | CellIdentity is to represent a unique CDMA cell  
--------------------------- | -----------------------------
getBasestationId | Base Station Id 0..65535, Integer.MAX_VALUE if unknown
getLatitude | Base station latitude, which is a decimal number as specified in 3GPP2 C.S0005-A v6.0. It is represented in units of 0.25 seconds and ranges from -1296000 to 1296000, both values inclusive (corresponding to a range of -90 to +90 degrees). Integer.MAX_VALUE if unknown.
getLongitude | Base station longitude, which is a decimal number as specified in 3GPP2 C.S0005-A v6.0. It is represented in units of 0.25 seconds and ranges from -2592000 to 2592000, both values inclusive (corresponding to a range of -180 to +180 degrees). Integer.MAX_VALUE if unknown.
getNetworkId | Network Id 0..65535, Integer.MAX_VALUE if unknown
getSystemId | System Id 0..32767, Integer.MAX_VALUE if unknown

*TelephonyManager* |  Provides access to information about the telephony services on the device. 
--------------------------- | -----------------------------
getAllCellInfo | Returns all observed cell information from all radios on the device including the primary and neighboring cells. The list can include one or more CellInfoGsm, CellInfoCdma, CellInfoLte, and CellInfoWcdma objects, in any combination. On devices with multiple radios it is typical to see instances of one or more of any these in the list. In addition, zero, one, or more of the returned objects may be considered registered; that is, their CellInfo.isRegistered() methods may return true. This method returns valid data for registered cells on devices with FEATURE_TELEPHONY. This method is preferred over using getCellLocation(). However, for older devices, getAllCellInfo() may return null. In these cases, you should call getCellLocation() instead.
getCallState | Returns one of the following constants that represents the current state of all phone calls. CALL_STATE_RINGING CALL_STATE_OFFHOOK CALL_STATE_IDLE
getCellLocation | Returns the current location of the device. If there is only one radio in the device and that radio has an LTE connection, this method will return null. The implementation must not to try add LTE identifiers into the existing cdma/gsm classes. In the future this call will be deprecated.
getDataActivity | Returns a constant indicating the type of activity on a data connection (cellular).
getDataNetworkType | Returns a constant indicating the radio technology (network type) currently in use on the device for data transmission.
getDataState | Returns a constant indicating the current data connection state (cellular).
getDeviceId | Returns the unique device ID, for example, the IMEI for GSM and the MEID or ESN for CDMA phones. Return null if device ID is not available.
getDeviceSoftwareVersion | Returns the software version number for the device, for example, the IMEI/SV for GSM phones. Return null if the software version is not available.
getGroupIdLevel1 | Returns the Group Identifier Level1 for a GSM phone. Return null if it is unavailable.
getIccAuthentication | Returns the response of authentication for the default subscription. Returns null if the authentication hasn't been successful
getLine1Number | Returns the phone number string for line 1, for example, the MSISDN for a GSM phone. Return null if it is unavailable.
getMmsUAProfUrl | Returns the MMS user agent profile URL.
getMmsUserAgent | Returns the MMS user agent.
getNetworkCountryIso | Returns the ISO country code equivalent of the current registered operator's MCC (Mobile Country Code).
getNetworkOperator | Returns the numeric name (MCC+MNC) of current registered operator.
getNetworkOperatorName | Returns the alphabetic name of current registered operator.
getNetworkType | the NETWORK_TYPE_xxxx for current data connection.
getPhoneCount | Returns the number of phones available. Returns 0 if none of voice, sms, data is not supported Returns 1 for Single standby mode (Single SIM functionality) Returns 2 for Dual standby mode.(Dual SIM functionality)
getPhoneType | Returns a constant indicating the device phone type. This indicates the type of radio used to transmit voice calls.
getSimCountryIso | Returns the ISO country code equivalent for the SIM provider's country code.
getSimOperator | Returns the MCC+MNC (mobile country code + mobile network code) of the provider of the SIM. 5 or 6 decimal digits.
getSimOperatorName | Returns the Service Provider Name (SPN).
getSimSerialNumber | Returns the serial number of the SIM, if applicable. Return null if it is unavailable.
getSimState | Returns a constant indicating the state of the default SIM card.
getSubscriberId | Returns the unique subscriber ID, for example, the IMSI for a GSM phone. Return null if it is unavailable.
getVoiceNetworkType | Returns the NETWORK_TYPE_xxxx for voice
hasCarrierPrivileges | Has the calling application been granted carrier privileges by the carrier. If any of the packages in the calling UID has carrier privileges, the call will return true. This access is granted by the owner of the UICC card and does not depend on the registered carrier.
hasIccCard | true if a ICC card is present
iccCloseLogicalChannel | Closes a previously opened logical channel to the ICC card. Input parameters equivalent to TS 27.007 AT+CCHC command.
iccExchangeSimIO | Returns the response APDU for a command APDU sent through SIM_IO.
iccOpenLogicalChannel | Opens a logical channel to the ICC card. Input parameters equivalent to TS 27.007 AT+CCHO command.
iccTransmitApduBasicChannel | Transmit an APDU to the ICC card over the basic channel. Input parameters equivalent to TS 27.007 AT+CSIM command.
iccTransmitApduLogicalChannel | Transmit an APDU to the ICC card over a logical channel. Input parameters equivalent to TS 27.007 AT+CGLA command.
isNetworkRoaming | Returns true if the device is considered roaming on the current network, for GSM purposes.
isSmsCapable | true if the current device supports sms service.
isTtyModeSupported | true if the device supports TTY mode, and false otherwise.
isVoiceCapable | "Voice capable" means that this device supports circuit-switched (i.e. voice) phone calls over the telephony network, and is allowed to display the in-call UI while a cellular voice call is active. This will be false on "data only" devices which can't make voice calls and don't support any in-call UI.
isWorldPhone | Whether the device is a world phone.












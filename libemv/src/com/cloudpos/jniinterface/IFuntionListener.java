package com.cloudpos.jniinterface;

public interface IFuntionListener {
    /**
     * This callback method will be called during every step of EMV proce
     * ss. The byte array parameter has following meanings.
     * @param data
     * data[0] means process status: 0-error 1-continue 2-complete
     *      if status is 0, data[1] means error code:
     *          1--No Application Selected when Application Select
     *          2--Card return 6A81 when Application Select
     *          3--Error when Application Select
     *          4--Error when Initialize Application Data
     *          5--Expired card
     *          6--Error when Read Application Data
     *          7--Card returned invalid data
     *          8--Error when data authentication
     *          9--Generate AC error
     *          10-Process Command error
     *          11-Service not Allowed
     *          12-PIN Entry timeout
     *          13-Check Offline PIN Error when Cardholder Verify
     *          14-Communication Error with Host, but the card need advice
     *          15-User cancelled transaction
     *          18-Switch interface
     *          19-Missing mandatory data
     *          20-App blocked
     *      if status is 1, data[1] means the process step:
     *          1--Notify Application show Application Candidate List
     *          2--Application Select Completed
     *          3--Read Application Data Completed
     *          4--Data Authentication Completed
     *          5--Ready to input offline PIN
     *          6--Notify Application prompt CardHolder enter Online PIN
     *          7--Notify Application confirm to Accepted PIN Bypass or not
     *          8--Notify Application to Process Online
     *          9--Notify Application Check Cardholder's Identification
     *          10-Get Process Option Completed
     *          11-Process Restrict Completed
     *          12-Cardholder Verify Completed
     *          13-Terminal Risk Management Completed
     *      if status is 2, data[1] means transaction result:
     *          1--Transaction approved Offline
     *          2--Transaction approved Online
     *          3--Transaction declined Offline
     *          4--Transaction declined Online
     */
    void emvProcessCallback(byte[] data);

    /**
     * Card event callback
     * @param eventType
     *        0-smart card presented.
     *        1-smart card removed.
     *        9-contact card power on error
     *        11-contactless card power on error
     */
    void cardEventOccured(int eventType);
}

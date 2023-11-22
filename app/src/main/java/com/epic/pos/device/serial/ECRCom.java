package com.epic.pos.device.serial;

import com.epic.pos.device.PosDevice;
import com.epic.pos.device.SerialCom;
import com.epic.pos.util.AppLog;
import com.epic.pos.util.Utility;
import com.nexgo.oaf.apiv3.SdkResult;
import com.nexgo.oaf.apiv3.device.serialport.SerialCfgEntity;
import com.nexgo.oaf.apiv3.device.serialport.SerialPortDriver;

/**
 * The ECRCom class is used for serial communication between the POS and Electronic Cash Registers
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-09-08
 */
public class ECRCom {

    private final String ECR_CMD_BIN_REQUEST_STRING = "BINN";
    private final String ECR_CMD_SALE_ONLINE = "SALK";

    public enum EcrCommand {
        ECR_CMD_BIN_REQUEST,
        ECR_CMD_SALE_ONLINE,
    }

    private final String TAG = ECRCom.class.getSimpleName();
  //  private SerialCom serialCom;
    private ECRCommandListener listener = null;
    SerialPortDriver serialCom;

    public ECRCom() {
        log("ECRCom()");
        new Thread() {
            @Override
            public void run() {
                try {


                    //Open Serial Port Nexgo N82
                    int PortNumber = 0;
                    serialCom = PosDevice.getInstance().deviceEngine.getSerialPortDriver(PortNumber);

                    boolean isCDCEnabled  =  PosDevice.getInstance().platform.getUsbCdcStatus();
                    log("CDC status : " + isCDCEnabled);


                    log("Create serial object");

                    SerialCfgEntity  serialportvalue = new SerialCfgEntity();
                    serialportvalue.setBaudRate(115200);
                    serialportvalue.setDataBits(8);
                    serialportvalue.setParity('n');
                    serialportvalue.setStopBits(1);

                    int isconnected =   serialCom.connect(serialportvalue);

                    log("initialize");
                   // Thread.sleep(500);

                    while (true) {
                        log("in loop");
                        if (isconnected==SdkResult.Success) {
                            log("serial com opened");
                            byte[] buffer = new byte[1024];
                            int len = serialCom.recv(buffer, 100, 24 * 60 * 60 * 1000);
                            log("data len. " +len);
                            if (len >= 1) {
                                log("data received.");
                                String data = Utility.asciiToString(Utility.byte2HexStr(buffer, 0, len));
                                log("data : " +data);
                                if (data.contains("#")) {
                                    log("data: " + data);
                                    String command = data.substring(0, data.indexOf("#"));
                                    log("command: " + command);
                                    if (listener != null) {
                                        if (command.equals(ECR_CMD_BIN_REQUEST_STRING)) {
                                            listener.onECRCommandReceived(EcrCommand.ECR_CMD_BIN_REQUEST, null);
                                        } else if (command.startsWith(ECR_CMD_SALE_ONLINE)) {

                                            String amount = command.substring(4);
                                            log("amount: " + amount);
                                            EcrRes res = new EcrRes();
                                            res.setAmount(amount);
                                            listener.onECRCommandReceived(EcrCommand.ECR_CMD_SALE_ONLINE, res);
                                        }
                                    }
                                }
                            }
                        }

                        Thread.sleep(500);
                    }
                } catch (Exception ex) {
                    log("ECR serial communication exception: " + ex.getMessage());
                    try {
                        Thread.sleep(5000);
                        new ECRCom();
                    } catch (Exception ex2) {
                        log("Unable to sleep serial thread: " + ex2.getMessage());
                    }
                }
            }
        }.start();
    }

    /**
     * Write message to serial com
     *
     * @param cmd
     */
    public void writeMsg(String cmd) {
        serialCom.send(cmd.getBytes(), cmd.length());
    }

    public void setListener(ECRCommandListener listener) {
        this.listener = listener;
    }

    /**
     * Use ECRCommandListener to receive ecr commands
     */
    public interface ECRCommandListener {
        void onECRCommandReceived(EcrCommand cmd, EcrRes res);
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }


}

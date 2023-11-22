package com.epic.pos.ui.sale.qr;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.epic.pos.R;
import com.epic.pos.common.TranTypes;
import com.epic.pos.data.db.dbpos.modal.Currency;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Issuer;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbpos.modal.Terminal;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.util.AppUtil;
import com.epic.pos.util.Utility;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.inject.Inject;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @author buddhika_j
 * @version 1.0
 * @since 2023-09-11
 */
public class QrPresenter extends BasePresenter<QRContract.View> implements QRContract.Presenter {

    private final Repository repository;
    private final NetworkConnection networkConnection;

    public String amount;
    private Context context;
    private Issuer issuer;
    private Host host;

    private Terminal terminal;
    private Merchant merchant;
    private Currency currency;

    int qrrequesttrycount=0;
    SSLContext sslContext = null;
    String qrcode="";

    @Inject
    public QrPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public void initData(Context c) {
        amount = repository.getTotalAmount().replace(",","");
        context=c;
        try {
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        TrustManager[] trustAllCertificates = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };


        try {
            sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }




    //    repository.getCardDefinitionById(repository.getSelectedCardDefinitionId(), cardDefinition -> {
          //  QrPresenter.this.cardDefinition = cardDefinition;
            repository.getIssuerById(8, issuer -> {
                QrPresenter.this.issuer = issuer;
                repository.getIssuerContainsHost(issuer.getIssuerNumber(), host -> {
                   QrPresenter.this.host = host;
                    repository.getTerminalById(4, terminal -> {
                        QrPresenter.this.terminal = terminal;
                        repository.getMerchantById(4, merchant -> {
                            QrPresenter.this.merchant = merchant;
                            repository.getCurrencyByMerchantId(merchant.getMerchantNumber(), currency -> {
                                QrPresenter.this.currency = currency;
                                sendQrRequest();
                            });
                       });
                    });
                });
            });
       // });
    }

    void sendQrRequest() {
        QRGenerationRequest qrGenerationRequest = new QRGenerationRequest();
        qrGenerationRequest.execute();
    }

    private void generateqr(String qrstring) {
            mView.loadqrcode(qrstring);
    }

    public void sendqrvalidaterequest() {
        merchant.setInvNumber(String.valueOf(Integer.parseInt(merchant.getInvNumber()) + 1));
        merchant.setSTAN(String.valueOf(Integer.parseInt(merchant.getSTAN()) + 1));
        repository.updateMerchant(merchant, null);
        QRValidaterRequest qrvalidateRequest = new QRValidaterRequest();
        qrvalidateRequest.execute();
    }

    public class QRGenerationRequest extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            mView.showLoader("Requesting QR Code","Please Wait Requesting QR Code");
            try {
          //   UAT
        //        String password ="UL4cywcm4FH+z86LUMLe4X1fD3D2HGiSjq7Qa61+WG7TfEDj5zmBTc2OM5GU0T4ewfmZ9Vq+Ew39PueIXQP1ZkmFCHjJucEPFSkXMH47UUDoooU/hVKiYakoEX1FObQt";
        //        String username ="epic";
         //       String urlst = "https://qrpos.sampath.lk/webservicesRest/api/lankaqr/v2/generateqr";
                //LIVE
                String password ="rXSrKqosc5oFDNSgBerbo1DbCshN1o8O+WZNrt3pQy3bHl1BhvWs6vnIDWMro8a7B9j158PeVmZGuxKC+QNAsahIz1W/83nm03ypirZ5tVx1HfBKT9PNRbbaKgYPWZP/";
               String username ="qrposepic";
                String urlst = "https://posweb.sampath.lk/CapexServicesRest/api/lankaqr/v2/generateqr";
                // URL for the QR code generation endpoint
                URL url = null;
                try {
                    url = new URL(urlst);
                } catch (MalformedURLException e) {
                    Log.e("QRREQUEST",e.getMessage());
                    e.printStackTrace();
                }

                // Create a connection
                HttpsURLConnection connection = null;
                try {
                    connection = (HttpsURLConnection) url.openConnection();
                } catch (IOException e) {
                    Log.e("QRREQUEST",e.getMessage());
                    e.printStackTrace();
                }
                connection.setRequestMethod("POST");

                // Set request headers
                connection.setRequestProperty("ServiceName", "GenerateQR_V2");
                connection.setRequestProperty("TokenID", "0");
                connection.setRequestProperty("Authorization", "Basic " + getBase64Credentials(username, password));
                connection.setRequestProperty("Content-Type", "application/json");
                Log.d("QRREQUEST","doInBackground1");
                // Create the request JSON body


                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
             //   HttpsURLConnection.setDefaultSSLSocketFactory(newSslSocketFactory());



                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });

                String trantime = Utility.getCurrentDateTimeInISO8601();

                Log.d("QRREQUEST","trantime : " +trantime);
                JSONObject requestBody = new JSONObject();
                requestBody.put("RequestTime", trantime);
             //   requestBody.put("hostname", "192.168.129.65");
              //  requestBody.put("port", "1220");
                requestBody.put("hostname", "@hostname");
                requestBody.put("port", "@port");
                requestBody.put("RequestID", "CCARD_123456780");

                String invoiceNo = AppUtil.toInvoiceNumber(Integer.parseInt(merchant.getInvNumber()));
                String  traceNo = AppUtil.toTraceNumber(Integer.parseInt(merchant.getSTAN()));

                JSONObject xmlmsg = new JSONObject();
                JSONObject trxMessage = new JSONObject();
                JSONObject message = new JSONObject();
                Log.d("QRREQUEST","MID : " +merchant.getMerchantID());
                Log.d("AMOUNT : ", amount);

                message.put("MID", merchant.getMerchantID());
                message.put("TX_AMT",amount);
                message.put("PAYMENT_TYPE", "1");
                message.put("MERC_NAME", merchant.getRctHdr1());
                message.put("MERC_CITY", "COLOMBO");
                message.put("OPT_MERC", "0");
                message.put("TX_STATUS", "09");
                message.put("REF_LABEL", "EPICNEXGO"+ traceNo);
                message.put("TX_TYPE", "0");
                message.put("QR_TYPE", "0");
                message.put("MESSAGE_ID", "emvco_generate_qr");
                message.put("TX_CURRENCY", currency.getCurrencyCode());
                message.put("POSTAL_CODE", "0000");
                message.put("COUNTRY_CODE", "LK");
                message.put("RequestTime", trantime);
                message.put("BILL_DATA", "0");
                message.put("MCC", "5999");
                message.put("TX_ORG", "SMB_POS");

                trxMessage.put("MESSAGE", message);
                xmlmsg.put("TRX_MESSAGE", trxMessage);
                Log.d("QRREQUEST","doInBackground2");
                requestBody.put("xmlmsg", xmlmsg);

                // Enable input and output streams
                connection.setDoOutput(true);
                connection.setDoInput(true);
                Log.d("QRREQUEST","doInBackground3");
                // Send the request body

                    DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                    os.writeBytes(requestBody.toString());
                    os.flush();
                    os.close();

                // Get the response code
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    // Read the response
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    return response.toString();
                } else {
                    // Handle the error here
                    return "Error: " + responseCode;
                }

            } catch (IOException | JSONException e) {
                Log.e("QRREQUEST",e.getMessage());
                if(qrrequesttrycount==0){
                    sendQrRequest();
                    qrrequesttrycount++;
                }
                else{
                    mView.onTxnFailed("Connection Error",e.getMessage());
                    }
                    e.printStackTrace();
                    return "Error: " + e.getMessage();
            }
        }
        @Override
        protected void onPostExecute(String result) {
            // Handle the response here
            Log.d("QRREQUEST","result : " +result);
            if(result!=null){
                try {
                    String respocode = getvaluefromresult(result, "RESP_CODE");
                    if(respocode.equals("00")){
                        String qrcode = getvaluefromresult(result,"QR_CODE");
                        setQrcode(qrcode);
                        generateqr(qrcode);
                    }
                    else{
                        String msgcus = getvaluefromresult(result,"RESP_DESC");
                        String msg = getvaluefromresult(result,"RESP_CODE");

                        mView.onTxnFailed(msg,msgcus);
                    }
                }
                catch (Exception e){
                   e.printStackTrace();
                }

            }

            mView.hideLoader();
        }

    }


    public class QRValidaterRequest extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            mView.showLoader("Validating QR","Please Wait Validating Payment Status");
            try {
                Log.d("VALIDATEQR","doInBackground");
            //    String password ="O4grcl4LJ6JnMjni+yP6nxSQ78FPgASYK8aTjRCBx6XENAFJ2GNPHiE7EMWgogt18a9D216amJkCP1K4A8s5Zw9+i387LcPAwrHOLD8Uk022hMIq3FfNUdktDvHbKbcz";
           //     String username = "epic";
            //    String urlst ="https://qrpos.sampath.lk/webservicesRest/api/lankaqr/v2/validateqr";
                String password ="GB288nvshi1P+li5rqwXqGHIA+tIshuncfogDeF0mVVMvIZEkiIxt6L3+GukBVjmPfSe0v8+maKwLA7sKW2+T+FRd4g313WpO+ub5eUCD5qYUTWOLRKwSyQQriz3G9TO";
                String username = "qrposepic";
                String urlst ="https://posweb.sampath.lk/CapexServicesRest/api/lankaqr/v2/validateqr";

                // URL for the QR code generation endpoint
                URL url = null;
                try {
                    url = new URL(urlst);
                } catch (MalformedURLException e) {
                    Log.e("VALIDATEQR",e.getMessage());
                    e.printStackTrace();
                }

                // Create a connection
                HttpsURLConnection connection = null;
                try {
                    connection = (HttpsURLConnection) url.openConnection();
                } catch (IOException e) {
                    Log.e("VALIDATEQR",e.getMessage());
                    e.printStackTrace();
                }
                connection.setRequestMethod("POST");

                // Set request headers
                connection.setRequestProperty("ServiceName", "ValidateQR_V2");
                connection.setRequestProperty("TokenID", "0");
                connection.setRequestProperty("Authorization", "Basic " + getBase64Credentials(username, password));
                connection.setRequestProperty("Content-Type", "application/json");
                Log.d("VALIDATEQR","doInBackground1");
                // Create the request JSON body



                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());


                HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

                String trantime = Utility.getCurrentDateTimeInISO8601();

                Log.d("VALIDATEQR","trantime : " +trantime);
                JSONObject requestBody = new JSONObject();
                requestBody.put("RequestTime", trantime);
             //     requestBody.put("hostname", "192.168.129.65");
              //    requestBody.put("port", "1220");
                requestBody.put("hostname", "@hostname");
                requestBody.put("port", "@port");
                requestBody.put("RequestID", "CCARD_123456780");

                JSONObject xmlmsg = new JSONObject();
                JSONObject trxMessage = new JSONObject();
                JSONObject message = new JSONObject();


                message.put("MESSAGE_ID", "emvco_validate_qr");
                message.put("QR_CODE",getQrcode() );


                trxMessage.put("MESSAGE", message);
                xmlmsg.put("TRX_MESSAGE", trxMessage);
                Log.d("VALIDATEQR","doInBackground2");
                requestBody.put("xmlmsg", xmlmsg);

                // Enable input and output streams
                connection.setDoOutput(true);
                connection.setDoInput(true);
                Log.d("VALIDATEQR","doInBackground3");
                // Send the request body
                DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                os.writeBytes(requestBody.toString());
                os.flush();
                os.close();

                // Get the response code
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    // Read the response
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    return response.toString();
                } else {
                    // Handle the error here
                    return "Error: " + responseCode;
                }

            } catch (IOException | JSONException e) {
                Log.e("VALIDATEQR",e.getMessage());
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }
        @Override
        protected void onPostExecute(String result) {
            // Handle the response here
            Log.d("VALIDATEQR","result : " +result);
            if(result!=null){
                String responcecode = getvaluefromresult(result,"RESP_CODE");
                if(responcecode.equals("00")){

                    String transtatus = getvaluefromresult(result,"TX_STATUS").trim();
                 Log.d("STATUS : ", transtatus);
                    String successstatus = "01";

                    if(transtatus.equals(successstatus)) {
                        com.epic.pos.data.db.dbtxn.modal.Transaction t = createTransactionObj(result);

                        repository.insertTransaction(t, saleId -> {
                            repository.saveCurrentSaleId((int) saleId);
                            mView.gotoReceiptActivity();
                            Log.d("VALIDATEQR", "saleId : " + saleId);
                        });
                    }
                    else{
                        String msgcus = getvaluefromresult(result,"MessageCustomer");
                        String msg = getvaluefromresult(result,"Message");
                        mView.onTxnStillPending(msg,msgcus);

                    }

                  //  String msgcus = getvaluefromresult(result,"MessageCustomer");
                   // String msg = getvaluefromresult(result,"Message");
                 //   mView.onTxnFailed(msg,msgcus);
                }
                else{
                       String msgcus = getvaluefromresult(result,"MessageCustomer");
                       String msg = getvaluefromresult(result,"Message");
                       mView.onTxnFailed(msg,msgcus);
                }
            }
            mView.hideLoader();
        }
    }

    private  String  getvaluefromresult(String result,String getvalue )  {
        try {
            JSONObject jsonObject = new JSONObject(result);
            String value = jsonObject.getString(getvalue);
            System.out.println("Value: " + value);
            return  value;
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }
    // Helper function to encode the username and password for Basic Auth
    private String getBase64Credentials(String username, String password) {
        String credentials = username + ":" + password;
        byte[] data = credentials.getBytes();
        return android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP);
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }


    private com.epic.pos.data.db.dbtxn.modal.Transaction createTransactionObj(String qrresult) {

        com.epic.pos.data.db.dbtxn.modal.Transaction t =
                new com.epic.pos.data.db.dbtxn.modal.Transaction();

       String invoiceNo = AppUtil.toInvoiceNumber(Integer.parseInt(merchant.getInvNumber()));
        String  traceNo = AppUtil.toTraceNumber(Integer.parseInt(merchant.getSTAN()));

        String RequestID = getvaluefromresult(qrresult,"RequestID");
        String pan = getvaluefromresult(qrresult,"FRM_CRD");
        String crd_hldr = getvaluefromresult(qrresult,"CRD_HLDR");
        String trace = getvaluefromresult(qrresult,"TRACE");


        t.setInvoice_no(invoiceNo);
        t.setTrace_no(trace);
        t.setHost(host.getHostID());
        t.setMerchant_no(merchant.getMerchantNumber());
        t.setTransaction_code(TranTypes.QR_SALE);
        t.setTerminal_no(4);
        t.setTerminal_id(terminal.getTerminalID());
        t.setApprove_code(RequestID.split("\\.")[2]);
        t.setRrn(RequestID.split("\\.")[1]);



       String totalAmount = repository.getTotalAmount();
       String baseAmount =  repository.getBaseAmount();

        long totalAmountLong = Long.parseLong(totalAmount.replaceAll(",", "")
                .replaceAll("\\.", ""));
        long baseAmountLong = Long.parseLong(baseAmount.replaceAll(",", "")
                .replaceAll("\\.", ""));

        t.setBase_transaction_amount(String.valueOf(baseAmountLong));
        t.setTotal_amount(String.valueOf(totalAmountLong));



        t.setPan(pan);
        t.setCard_holder_name(crd_hldr);

        t.setMerchant_id(merchant.getMerchantID());
        t.setMerchant_name(merchant.getMerchantName());
//        t.setNii(saleRequest.getNii());
//        t.setSecure_nii(saleRequest.getSecureNii());
//        t.setTpdu(saleRequest.getTpdu());



        t.setResponse_code("00");
     //   t.setCdt_index(cardDefinition.getId());
        t.setIssuer_number(issuer.getIssuerNumber());
        t.setVoided(0);
        t.setCard_label("Lanka QR");
        t.setCurrency_code(Integer.parseInt(currency.getCurrencyCode()));
        t.setCurrency_symbol(currency.getCurrencySymbol());

        t.setTxn_date(Utility.getdateformatedmmdd());
        t.setTxn_time(Utility.gettimeformatedHHmmss());

        return t;
    }

    private SSLSocketFactory newSslSocketFactory() {
        try {

            // Get an instance of the Bouncy Castle KeyStore format
            KeyStore trusted = KeyStore.getInstance("BKS");//put BKS literal
            // Get the raw resource, which contains the keystore with
            // your trusted certificates (root and any intermediate certs)
            InputStream in =context.getResources().openRawResource(R.raw.ca);
            try {
                // Initialize the keystore with the provided trusted certificates
                // Also provide the password of the keystore
                trusted.load(in, "mysecret".toCharArray());
            } finally {
                in.close();
            }
            // Pass the keystore to the SSLSocketFactory. The factory is responsible
            // for the verification of the server certificate.
            SSLSocketFactory sf = new SSLSocketFactory(trusted);
            // Hostname verification from certificate

            sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
            return sf;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

}
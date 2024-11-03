package com.hotifi.payment.processor.razorpay;

import com.hotifi.common.exception.ApplicationException;
import com.hotifi.payment.errors.RazorpayErrorCodes;
import com.hotifi.payment.processor.response.Settlement;
import com.razorpay.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public class RazorpayProcessor {

    RazorpayClient razorpayClient;

    @Value("${razorpay.client}")
    private String razorpayClientId;

    @Value("${razorpay.secret}")
    private String razorpayClientSecret;

    private static final Logger logger = LoggerFactory.getLogger(RazorpayProcessor.class);

    public RazorpayProcessor() {
        try {
            razorpayClient = new RazorpayClient(razorpayClientId, razorpayClientSecret);
        } catch (RazorpayException e) {
            throw new ApplicationException(RazorpayErrorCodes.INVALID_CLIENT_CREDENTIALS);
        }
    }

    //Below methods have json response body above them in comments

    /**
     * {
     * "id": "order_EKwxwAgItmmXdp",
     * "entity": "order",
     * "amount": 50000,
     * "amount_paid": 0,
     * "amount_due": 50000,
     * "currency": "INR",
     * "receipt": "receipt#1",
     * "offer_id": null,
     * "status": "created",
     * "attempts": 0,
     * "notes": [],
     * "created_at": 1582628071
     * }
     */

    public Order createOrder(int amountInPaise, String currency) {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise); // amount in the smallest currency unit
            orderRequest.put("currency", currency);
            return razorpayClient.Orders.create(orderRequest);
        } catch (RazorpayException e) {
            throw new ApplicationException(RazorpayErrorCodes.CREATE_ORDER_FAILED);
        }
    }

    /**
     * {
     * "id":"order_DaaS6LOUAASb7Y",
     * "entity":"order",
     * "amount":2200,
     * "amount_paid":0,
     * "amount_due":2200,
     * "currency":"INR",
     * "receipt":"Receipt #211",
     * "status":"attempted",
     * "attempts":1,
     * "notes":[],
     * "created_at":1572505143
     * }
     */

    public Order fetchOrderById(String orderId) {
        try {
            return razorpayClient.Orders.fetch(orderId);
        } catch (RazorpayException e) {
            throw new ApplicationException(RazorpayErrorCodes.ORDER_NOT_FOUND);
        }
    }

    /**
     *
     * {
     *   "entity":"collection",
     *   "count":1,
     *   "items":[
     *     {
     *       "id":"pay_DaaSOvhgcOfzgR",
     *       "entity":"payment",
     *       "amount":2200,
     *       "currency":"INR",
     *       "status":"captured",
     *       "order_id":"order_DaaS6LOUAASb7Y",
     *       "invoice_id":null,
     *       "international":false,
     *       "method":"card",
     *       "amount_refunded":0,
     *       "refund_status":null,
     *       "captured":true,
     *       "description":"Beans in every imaginable flavour",
     *       "card_id":"card_DZon6fd8J3IcA2",
     *       "bank":null,
     *       "wallet":null,
     *       "vpa":null,
     *       "email":"gaurav.kumar@example.com",
     *       "contact":"+919999999988",
     *       "notes":[],
     *       "fee":44,
     *       "tax":0,
     *       "error_code":null,
     *       "error_description":null,
     *       "created_at":1572505160
     *     }
     *   ]
     * }
     * */

    public List<Payment> fetchPaymentsByOrderId(String orderId) {
        try {
            return razorpayClient.Orders.fetchPayments(orderId);
        } catch (RazorpayException e) {
            throw new ApplicationException(RazorpayErrorCodes.CREATE_ORDER_FAILED);
        }
    }

    /**
     * {
     * "id": "pay_G8VQzjPLoAvm6D",
     * "entity": "payment",
     * "amount": 1000,
     * "currency": "INR",
     * "status": "captured",
     * "order_id": "order_G8VPOayFxWEU28",
     * "invoice_id": null,
     * "international": false,
     * "method": "upi",
     * "amount_refunded": 0,
     * "refund_status": null,
     * "captured": true,
     * "description": "Purchase Shoes",
     * "card_id": null,
     * "bank": null,
     * "wallet": null,
     * "vpa": "gaurav.kumar@exampleupi",
     * "email": "gaurav.kumar@example.com",
     * "contact": "+919999999999",
     * "customer_id": "cust_DitrYCFtCIokBO",
     * "notes": [],
     * "fee": 24,
     * "tax": 4,
     * "error_code": null,
     * "error_description": null,
     * "error_source": null,
     * "error_step": null,
     * "error_reason": null,
     * "acquirer_data": {
     * "rrn": "033814379298"
     * },
     * "created_at": 1606985209
     * }
     */
    public void capturePaymentById(String paymentId, int amountInPaise, String currency) {
        try {
            JSONObject captureRequest = new JSONObject();
            captureRequest.put("amount", amountInPaise);
            captureRequest.put("currency", currency);
            razorpayClient.Payments.capture(paymentId, captureRequest);
        } catch (RazorpayException e) {
            throw new ApplicationException(RazorpayErrorCodes.CAPTURE_PAYMENT_FAILED);
        }
    }

    /**
     * {
     * "id": "pay_G8VQzjPLoAvm6D",
     * "entity": "payment",
     * "amount": 1000,
     * "currency": "INR",
     * "status": "captured",
     * "order_id": "order_G8VPOayFxWEU28",
     * "invoice_id": null,
     * "international": false,
     * "method": "upi",
     * "amount_refunded": 0,
     * "refund_status": null,
     * "captured": true,
     * "description": "Purchase Shoes",
     * "card_id": null,
     * "bank": null,
     * "wallet": null,
     * "vpa": "gaurav.kumar@exampleupi",
     * "email": "gaurav.kumar@example.com",
     * "contact": "+919999999999",
     * "customer_id": "cust_DitrYCFtCIokBO",
     * "notes": [],
     * "fee": 24,
     * "tax": 4,
     * "error_code": null,
     * "error_description": null,
     * "error_source": null,
     * "error_step": null,
     * "error_reason": null,
     * "acquirer_data": {
     * "rrn": "033814379298"
     * },
     * "created_at": 1606985209
     * }
     */
    public Payment getPaymentById(String paymentId) {
        try {
            return razorpayClient.Payments.fetch(paymentId);
        } catch (RazorpayException e) {
            throw new ApplicationException(RazorpayErrorCodes.PAYMENT_NOT_FOUND);
        }
    }

    /**
     * {
     * "entity": "collection",
     * "count": 2,
     * "items": [
     * {
     * "id": "pay_E9uth3WhYbh9QV",
     * "entity": "payment",
     * "amount": 100,
     * "currency": "INR",
     * "status": "captured",
     * "order_id": null,
     * "invoice_id": null,
     * "international": null,
     * "method": "transfer",
     * "amount_refunded": 0,
     * "refund_status": null,
     * "captured": true,
     * "description": null,
     * "card_id": null,
     * "bank": null,
     * "wallet": null,
     * "vpa": null,
     * "email": "",
     * "contact": null,
     * "notes": [],
     * "fee": 0,
     * "tax": 0,
     * "error_code": null,
     * "error_description": null,
     * "created_at": 1580219046
     * },
     * {
     * "id": "pay_E9ulvYl52I27fB",
     * "entity": "payment",
     * "amount": 100,
     * "currency": "INR",
     * "status": "captured",
     * "order_id": null,
     * "invoice_id": null,
     * "international": null,
     * "method": "transfer",
     * "amount_refunded": 0,
     * "refund_status": null,
     * "captured": true,
     * "description": null,
     * "card_id": null,
     * "bank": null,
     * "wallet": null,
     * "vpa": null,
     * "email": "gaurav.kumar@example.com",
     * "contact": "+918888888888",
     * "notes": {
     * "roll_no": "IEC2011025"
     * },
     * "fee": 0,
     * "tax": 0,
     * "error_code": null,
     * "error_description": null,
     * "created_at": 1580218605
     * }
     * ]
     * }
     */

    //TODO
    public List<Payment> getPaymentsByLinkedAccount(String linkedAccountId) {
        try {
            String k = "X-Razorpay-Account";
            Map<String, String> map = new HashMap<>();
            map.put(k, linkedAccountId);
            razorpayClient.addHeaders(map);
            return razorpayClient.Payments.fetchAll();
        } catch (RazorpayException e) {
            throw new ApplicationException(RazorpayErrorCodes.PAYMENTS_OF_LINKED_ACCOUNT_NOT_FOUND);
        }
    }

    /**
     * {
     * "id": "rfnd_FP8QHiV938haTz",
     * "entity": "refund",
     * "amount": 500100,
     * "currency": "INR",
     * "payment_id": "pay_FCXKPFtYfPXJPy",
     * "notes": []
     * "receipt": null,
     * "acquirer_data": {
     * "arn": null
     * },
     * "created_at": 1597078866,
     * "batch_id": null,
     * "status": "processed",
     * "speed_processed": "normal",
     * "speed_requested": "normal"
     * }
     */

    //TODO
    public Refund startNormalFullRefund(String paymentId) {
        try {
            return razorpayClient.Payments.refund(paymentId);
        } catch (RazorpayException e) {
            throw new ApplicationException(RazorpayErrorCodes.NORMAL_FULL_REFUND_FAILED);
        }
    }

    /**
     * {
     * "id": "rfnd_FP8QHiV938haTz",
     * "entity": "refund",
     * "amount": 500100,
     * "currency": "INR",
     * "payment_id": "pay_FCXKPFtYfPXJPy",
     * "notes": []
     * "receipt": null,
     * "acquirer_data": {
     * "arn": null
     * },
     * "created_at": 1597078866,
     * "batch_id": null,
     * "status": "processed",
     * "speed_processed": "normal",
     * "speed_requested": "normal"
     * }
     */
    public Refund startNormalPartialRefund(String paymentId, int amountInPaise) {
        JSONObject refundRequest = new JSONObject();
        refundRequest.put("amount", amountInPaise); // Amount should be in paise
        try {
            return razorpayClient.Payments.refund(paymentId, refundRequest);
        } catch (RazorpayException e) {
            logger.error("An error occurred : {}", e.getMessage(), e);
            throw new ApplicationException(RazorpayErrorCodes.NORMAL_PARTIAL_REFUND_FAILED);
        }
    }

    /**
     * {
     * "id": "rfnd_FP8DDKxqJif6ca",
     * "entity": "refund",
     * "amount": 300100,
     * "currency": "INR",
     * "payment_id": "pay_FIKOnlyii5QGNx",
     * "notes": {
     * "comment": "Comment for refund"
     * },
     * "receipt": null,
     * "acquirer_data": {
     * "arn": "10000000000000"
     * },
     * "created_at": 1597078124,
     * "batch_id": null,
     * "status": "processed",
     * "speed_processed": "normal",
     * "speed_requested": "optimum"
     * }
     */
    public Refund getSpecificRefundById(String paymentId, String refundId) {
        try {
            return razorpayClient.Payments.fetchRefund(paymentId, refundId);
        } catch (RazorpayException e) {
            throw new ApplicationException(RazorpayErrorCodes.SPECIFIC_REFUND_NOT_FOUND);
        }
    }

    /**
     * {
     * "id": "rfnd_EqWThTE7dd7utf",
     * "entity": "refund",
     * "amount": 6000,
     * "currency": "INR",
     * "payment_id": "pay_EpkFDYRirena0f",
     * "notes": {
     * "comment": "Issuing an instant refund"
     * },
     * "receipt": null,
     * "acquirer_data": {
     * "arn": "10000000000000" // or rrn
     * },
     * "created_at": 1589521675,
     * "batch_id": null,
     * "status": "processed",
     * "speed_processed": "optimum",
     * "speed_requested": "optimum"
     * }
     */
    public Refund getRefundById(String refundId) {
        try {
            return razorpayClient.Refunds.fetch(refundId);
        } catch (RazorpayException e) {
            throw new ApplicationException(RazorpayErrorCodes.REFUND_NOT_FOUND);
        }
    }

    /**
     * {
     * "entity": "collection",
     * "count": 1,
     * "items": [
     * {
     * "id": "rfnd_HbVu59xroDSd3L",
     * "entity": "refund",
     * "amount": 200,
     * "currency": "INR",
     * "payment_id": "pay_HbQGJjwR1RqwdO",
     * "notes": [],
     * "receipt": null,
     * "acquirer_data": {},
     * "created_at": 1626855810,
     * "batch_id": null,
     * "status": "processed",
     * "speed_processed": "normal",
     * "speed_requested": "normal"
     * }
     * ]
     * }
     */


    public List<Refund> getRefundsByPaymentId(String paymentId) {
        try {
            return razorpayClient.Payments.fetchAllRefunds(paymentId);
        } catch (RazorpayException e) {
            throw new ApplicationException(RazorpayErrorCodes.PAYMENT_NOT_FOUND);
        }
    }

    /**
     * {
     * "id": "trf_E9utgtfGTcpcmm",
     * "entity": "transfer",
     * "source": "acc_CJoeHMNpi0nC7k",
     * "recipient": "acc_CPRsN1LkFccllA",
     * "amount": 100,
     * "currency": "INR",
     * "amount_reversed": 0,
     * "notes": [],
     * "fees": 1,
     * "tax": 0,
     * "on_hold": false,
     * "on_hold_until": null,
     * "recipient_settlement_id": null,
     * "created_at": 1580219046,
     * "linked_account_notes": [],
     * "processed_at": 1580219046
     * }
     */
    public Transfer startTransfer(String linkedAccountId, int amountInPaise, String currency) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", linkedAccountId);
        jsonObject.put("amount", amountInPaise);
        jsonObject.put("currency", currency);
        try {
            return razorpayClient.Transfers.create(jsonObject);
        } catch (RazorpayException e) {
            logger.error("An error occurred : {}", e.getMessage(), e);
            throw new ApplicationException(RazorpayErrorCodes.TRANSFER_FAILED);
        }
    }

    /**
     * {
     * "id": "trf_E7V62rAxJ3zYMo",
     * "entity": "transfer",
     * "source": "pay_E6j30Iu1R7XbIG",
     * "recipient": "acc_CMaomTz4o0FOFz",
     * "amount": 100,
     * "currency": "INR",
     * "amount_reversed": 0,
     * "notes": [],
     * "fees": 1,
     * "tax": 0,
     * "on_hold": false,
     * "on_hold_until": null,
     * "recipient_settlement_id": null,
     * "created_at": 1579691505,
     * "linked_account_notes": [],
     * "processed_at": 1579691505
     * }
     */
    public Transfer getTransferById(String transferId) {
        try {
            return razorpayClient.Transfers.fetch(transferId);
        } catch (RazorpayException e) {
            throw new ApplicationException(RazorpayErrorCodes.TRANSFER_NOT_FOUND);
        }
    }


    /**
     * {
     * "id": "setl_DGlQ1Rj8os78Ec",
     * "entity": "settlement",
     * "amount": 9973635,
     * "status": "processed",
     * "fees": 471699,
     * "tax": 42070,
     * "utr": "1568176960vxp0rj",
     * "created_at": 1568176960
     * }
     */
    public Settlement getSettlementById(String settlementId) {
        try {
            String url = "https://api.razorpay.com/v1/settlements/" + settlementId;
            URL obj = new URL(url);
            String encoded = Base64.getEncoder()
                    .encodeToString((razorpayClientId + ":" + razorpayClientSecret)
                            .getBytes(StandardCharsets.UTF_8));
            JSONObject jsonObject = getJsonObject(obj, encoded);
            String id = jsonObject.getString("id");
            String entity = jsonObject.getString("entity");
            int amount = jsonObject.getInt("amount");
            String status = jsonObject.getString("status");
            int fees = jsonObject.getInt("fees");
            int tax = jsonObject.getInt("tax");
            String utr = jsonObject.getString("utr");
            long createdAt = jsonObject.getLong("created_at");
            return new Settlement(id, entity, amount, status, fees, tax, utr, createdAt);

        } catch (Exception e) {
            logger.error("An error occurred : {}", e.getMessage(), e);
        }

        return null;
    }

    private static JSONObject getJsonObject(URL obj, String encoded) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + encoded);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null) {
            response.append(inputLine);
        }
        bufferedReader.close();
        //Read JSON response and print
        return new JSONObject(response.toString());
    }
}

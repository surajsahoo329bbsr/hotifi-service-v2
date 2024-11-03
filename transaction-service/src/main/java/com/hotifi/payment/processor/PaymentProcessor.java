package com.hotifi.payment.processor;

import com.hotifi.common.constants.BusinessConstants;
import com.hotifi.payment.entities.Purchase;
import com.hotifi.payment.entities.PurchaseOrder;
import com.hotifi.payment.entities.SellerReceipt;
import com.hotifi.payment.processor.codes.*;
import com.hotifi.payment.processor.razorpay.RazorpayProcessor;
import com.hotifi.payment.processor.razorpay.codes.PaymentStatusCodes;
import com.hotifi.payment.processor.razorpay.codes.RefundStatusCodes;
import com.hotifi.payment.processor.response.Settlement;
import com.hotifi.payment.repositories.PurchaseRepository;
import com.hotifi.payment.repositories.SellerReceiptRepository;
import com.hotifi.payment.utils.PaymentUtils;
import com.hotifi.payment.web.responses.RefundReceiptResponse;
import com.hotifi.payment.web.responses.SellerReceiptResponse;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.Refund;
import com.razorpay.Transfer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Slf4j
public class PaymentProcessor {

    private PaymentMethodCodes paymentMethodCodes;

    private PaymentGatewayCodes paymentGatewayCodes;

    private RazorpayProcessor razorpayProcessor;

    public PaymentProcessor(PaymentGatewayCodes paymentGatewayCodes) {
        this.paymentGatewayCodes = paymentGatewayCodes;
        razorpayProcessor = new RazorpayProcessor();
    }

    public void capturePayment(String paymentId, int amountInr) {
        switch (paymentGatewayCodes) {
            case RAZORPAY:
                int amount = amountInr * BusinessConstants.UNIT_INR_IN_PAISE;
                razorpayProcessor.capturePaymentById(paymentId, amount, "INR");
            case STRIPE:
                log.info("STRIPE PAYMENT");
                break;
            case PAYPAL:
                log.info("PAYPAL PAYMENT");
                break;
        }

    }

    public PurchaseOrder addBuyerOrder(BigDecimal amountOrder, String currency) {
        switch (paymentGatewayCodes) {
            case RAZORPAY:
                Order order = razorpayProcessor
                        .createOrder(amountOrder.multiply(BigDecimal.valueOf(100)).intValue(), currency);

                String orderId = order.get("id");
                BigDecimal amount = PaymentUtils
                        .divideThenMultiplyCeilingTwoScale(new BigDecimal(order.get("amount").toString()), BigDecimal.valueOf(100), BigDecimal.ONE);
                BigDecimal amountPaid = PaymentUtils
                        .divideThenMultiplyCeilingTwoScale(new BigDecimal(order.get("amount_paid").toString()), BigDecimal.valueOf(100), BigDecimal.ONE);
                BigDecimal amountDue = PaymentUtils
                        .divideThenMultiplyCeilingTwoScale(new BigDecimal(order.get("amount_due").toString()), BigDecimal.valueOf(100), BigDecimal.ONE);

                String status = order.get("status");
                int attempts = Integer.parseInt(order.get("attempts").toString());
                Date createdAt = order.get("created_at");
                Date modifiedAt = new Date(System.currentTimeMillis());

                PurchaseOrder purchaseOrderEntity = new PurchaseOrder();
                purchaseOrderEntity.setOrderId(orderId);
                purchaseOrderEntity.setAmount(amount);
                purchaseOrderEntity.setAmountPaid(amountPaid);
                purchaseOrderEntity.setAmountDue(amountDue);
                purchaseOrderEntity.setStatus(OrderStatusCodes.valueOf(status.toUpperCase()).name());
                purchaseOrderEntity.setAttempts(attempts);
                purchaseOrderEntity.setOrderCreatedAt(createdAt);
                purchaseOrderEntity.setOrderModifiedAt(modifiedAt);

                return purchaseOrderEntity;
            case STRIPE:
                log.info("STRIPE PAYMENT");
                break;
            case PAYPAL:
                log.info("PAYPAL PAYMENT");
                break;
        }
        return null;
    }

    public Order getPurchaseOrder(String orderId) {
        switch (paymentGatewayCodes) {
            case RAZORPAY:
                return razorpayProcessor.fetchOrderById(orderId);
            case STRIPE:
                log.info("STRIPE PAYMENT");
                break;
            case PAYPAL:
                log.info("PAYPAL PAYMENT");
                break;
        }
        return null;
    }

    public Purchase getBuyerPurchase(String paymentId, BigDecimal amountPaid) {
        switch (paymentGatewayCodes) {
            case RAZORPAY:
                Payment payment = razorpayProcessor.getPaymentById(paymentId);

                Date paymentDoneAt = payment.get("created_at");
                JSONObject acquirerDataJson = payment.get("acquirer_data");
                PaymentMethodCodes paymentMethod = PaymentMethodCodes.valueOf(payment.get("method").toString().toUpperCase());
                PaymentStatusCodes razorpayStatus = PaymentStatusCodes.valueOf(payment.get("status").toString().toUpperCase());

                String paymentTransactionId = PaymentUtils.getPaymentTransactionId(paymentMethod, acquirerDataJson);

                int amountPaidInPaise = PaymentUtils.getPaiseFromInr(amountPaid);
                if (razorpayStatus == PaymentStatusCodes.REFUNDED) return null;
                //If auto-captured failed do the manual capture
                //Purchase entity model update
                Purchase purchase = new Purchase();
                if (razorpayStatus == PaymentStatusCodes.AUTHORIZED) {
                    try {
                        //calling Api To Capture Payment
                        razorpayProcessor.capturePaymentById(paymentId, amountPaidInPaise, BusinessConstants.CURRENCY_INR);
                        purchase.setStatus(paymentMethod.value() + PaymentStatusCodes.CAPTURED.value());
                    } catch (Exception e) {
                        e.printStackTrace();
                        purchase.setStatus(paymentMethod.value() + PaymentStatusCodes.AUTHORIZED.value());
                    }
                } else
                    purchase.setStatus(paymentMethod.value() + razorpayStatus.value()); //means either status is created or failed
                purchase.setPaymentId(paymentId);
                purchase.setPaymentDoneAt(paymentDoneAt);
                purchase.setPaymentTransactionId(paymentTransactionId);
                return purchase;
            case STRIPE:
                log.info("STRIPE PAYMENT");
                break;
            case PAYPAL:
                log.info("PAYPAL PAYMENT");
                break;
        }
        return null;
    }

    public void getOrUpdateBuyerRefund(Refund refund, Purchase purchase, PurchaseRepository purchaseRepository) {
        String refundId = refund.get("id");
        Date refundCreatedAt = refund.get("created_at");
        RefundStatusCodes refundStatus = RefundStatusCodes.valueOf(refund.get("status").toString().toUpperCase());
        JSONObject acquirerDataJson = refund.get("acquirer_data");
        String refundTransactionId = PaymentUtils.getRefundTransactionId(acquirerDataJson);
        int buyerPaymentStatus = BuyerPaymentCodes.REFUND_PENDING.value() + refundStatus.value();
        //modify purchase entity
        purchase.setStatus((purchase.getStatus() / BusinessConstants.PAYMENT_METHOD_START_VALUE_CODE)
                * BusinessConstants.PAYMENT_METHOD_START_VALUE_CODE
                + buyerPaymentStatus);
        purchase.setRefundStartedAt(refundCreatedAt);
        purchase.setRefundPaymentId(refundId);
        purchase.setRefundTransactionId(refundTransactionId);

        if (purchase.getStatus() % BusinessConstants.PAYMENT_METHOD_START_VALUE_CODE < BuyerPaymentCodes.REFUND_PROCESSED.value())
            purchaseRepository.save(purchase);
    }

    public RefundReceiptResponse getBuyerRefundStatus(PurchaseRepository purchaseRepository, String paymentId) {
        switch (paymentGatewayCodes) {
            case RAZORPAY:
                log.info("RAZORPAY PAYMENT");
                Purchase purchase = purchaseRepository.findByPaymentId(paymentId);
                Refund refund = razorpayProcessor.getRefundById(purchase.getRefundPaymentId()); //To be changed later to support multiple refunds for a single payment
                getOrUpdateBuyerRefund(refund, purchase, purchaseRepository);
                return new RefundReceiptResponse(purchase, BusinessConstants.HOTIFI_BANK_ACCOUNT);
            case STRIPE:
                log.info("STRIPE PAYMENT");
                break;
            case PAYPAL:
                log.info("PAYPAL PAYMENT");
                break;
        }
        return null;
    }

    public SellerReceiptResponse getSellerPaymentStatus(SellerReceiptRepository sellerReceiptRepository, String transferId) {
        switch (paymentGatewayCodes) {
            case RAZORPAY:
                log.info("RAZORPAY PAYMENT");
                SellerReceipt sellerReceipt = sellerReceiptRepository.findByTransferId(transferId);
                String linkedAccountId = null ; //sellerReceipt.getSellerPayment().getSeller().getBankAccount().getLinkedAccountId();
                Transfer transfer = razorpayProcessor.getTransferById(transferId);
                boolean isOnHold = transfer.get("on_hold");
                String settlementId = transfer.get("recipient_settlement_id");

                SellerReceiptResponse sellerReceiptResponse = new SellerReceiptResponse();
                sellerReceipt.setStatus(SellerPaymentCodes.PAYMENT_CREATED.value());
                if (isOnHold) {
                    Date onHoldUntil = transfer.get("on_hold_until");
                    sellerReceiptResponse.setOnHoldUntil(onHoldUntil);
                }
                if (settlementId != null) {
                    Settlement settlement = razorpayProcessor.getSettlementById(settlementId);
                    Date paidAt = PaymentUtils.convertEpochToDate(settlement.getCreatedAt());
                    Date modifiedAt = new Date(System.currentTimeMillis());
                    String transferTransactionId = settlement.getUtr();
                    SellerPaymentCodes sellerPaymentCode = SellerPaymentCodes.valueOf(settlement.getStatus().toUpperCase());
                    sellerReceipt.setPaidAt(paidAt);
                    sellerReceipt.setModifiedAt(modifiedAt);
                    sellerReceipt.setTransferTransactionId(transferTransactionId);
                    sellerReceipt.setStatus(sellerPaymentCode.value());
                }
                //Seller Receipt Response
                sellerReceiptResponse.setOnHold(isOnHold);
                sellerReceiptResponse.setSellerReceipt(sellerReceipt);
                sellerReceiptResponse.setSellerLinkedAccountId(linkedAccountId);
                sellerReceiptResponse.setHotifiBankAccount(BusinessConstants.HOTIFI_BANK_ACCOUNT);

                return sellerReceiptResponse;
            case STRIPE:
                log.info("STRIPE PAYMENT");
                break;
            case PAYPAL:
                log.info("PAYPAL PAYMENT");
                break;
        }
        return null;
    }

    public RefundReceiptResponse startBuyerRefund(PurchaseRepository purchaseRepository, BigDecimal refundAmount, String paymentId) {
        switch (paymentGatewayCodes) {
            case RAZORPAY:
                log.info("TODO RAZORPAY PAYMENT");
                Purchase purchase = purchaseRepository.findByPaymentId(paymentId);
                int amountInPaise = PaymentUtils.getPaiseFromInr(refundAmount);
                //Creating refund entity below
                BigDecimal zeroAmount = new BigDecimal("0.00");
                boolean isRefundStarted = purchase.getStatus() %
                        BusinessConstants.PAYMENT_METHOD_START_VALUE_CODE >= BuyerPaymentCodes.REFUND_PENDING.value();

                if (refundAmount.compareTo(zeroAmount) > 0 && !isRefundStarted) {
                    Refund refund = razorpayProcessor.startNormalPartialRefund(paymentId, amountInPaise);
                    String refundId = refund.get("id");
                    Date refundStartedAt = refund.get("created_at");
                    RefundStatusCodes refundStatus = RefundStatusCodes.valueOf(refund.get("status").toString().toUpperCase());
                    int buyerPaymentStatus = BuyerPaymentCodes.REFUND_PENDING.value() + refundStatus.value();
                    //Purchase entity setup
                    purchase.setRefundPaymentId(refundId);
                    purchase.setRefundStartedAt(refundStartedAt);
                    purchase.setStatus((purchase.getStatus() / BusinessConstants.PAYMENT_METHOD_START_VALUE_CODE)
                            * BusinessConstants.PAYMENT_METHOD_START_VALUE_CODE
                            + buyerPaymentStatus);
                }

                return new RefundReceiptResponse(purchase, BusinessConstants.HOTIFI_BANK_ACCOUNT);
            case STRIPE:
                log.info("STRIPE PAYMENT");
                break;
            case PAYPAL:
                log.info("PAYPAL PAYMENT");
                break;
        }
        return null;
    }

    public SellerReceiptResponse startSellerPayment(BigDecimal sellerPendingAmount, String linkedAccountId, String bankAccountNumber, String bankIfscCode) {
        switch (paymentGatewayCodes) {
            case RAZORPAY:
                log.info("TODO RAZORPAY PAYMENT");
                Transfer transfer = razorpayProcessor.startTransfer(linkedAccountId, PaymentUtils.getPaiseFromInr(sellerPendingAmount),
                        BusinessConstants.CURRENCY_INR);
                SellerReceipt sellerReceipt = new SellerReceipt();
                sellerReceipt.setStatus(SellerPaymentCodes.PAYMENT_CREATED.value());
                Date createdAt = transfer.get("created_at");
                Date modifiedAt = transfer.get("processed_at");
                String transferId = transfer.get("id");
                String settlementId = transfer.get("recipient_settlement_id");
                boolean isOnHold = transfer.get("on_hold");
                Date onHoldUntil = transfer.get("on_hold_until");

                sellerReceipt.setCreatedAt(createdAt);
                sellerReceipt.setModifiedAt(createdAt);
                sellerReceipt.setModifiedAt(modifiedAt);
                sellerReceipt.setTransferId(transferId);
                sellerReceipt.setAmountPaid(sellerPendingAmount);
                sellerReceipt.setBankIfscCode(bankIfscCode);
                sellerReceipt.setBankAccountNumber(bankAccountNumber);
                sellerReceipt.setSettlementId(settlementId);

                //Setup Seller Receipt
                SellerReceiptResponse sellerReceiptResponse = new SellerReceiptResponse();
                sellerReceiptResponse.setHotifiBankAccount(BusinessConstants.HOTIFI_BANK_ACCOUNT);
                sellerReceiptResponse.setSellerLinkedAccountId(linkedAccountId);
                //TODO
                //sellerReceiptResponse.setSellerReceipt(sellerReceipt);
                sellerReceiptResponse.setOnHold(isOnHold);
                sellerReceiptResponse.setOnHoldUntil(onHoldUntil);

                return sellerReceiptResponse;
            case STRIPE:
                log.info("TODO STRIPE PAYMENT");
                break;
            case PAYPAL:
                log.info("TODO PAYPAL PAYMENT");
                break;
        }
        return null;
    }

}

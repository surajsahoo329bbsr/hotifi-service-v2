package com.hotifi.payment.services.interfaces;

import com.hotifi.payment.entities.SellerPayment;
import com.hotifi.payment.models.PendingTransfer;
import com.hotifi.payment.models.TransferUpdate;
import com.hotifi.payment.models.UPITransferUpdate;
import com.hotifi.payment.models.UpiPendingTransfer;
import com.hotifi.payment.web.responses.PendingMoneyResponse;
import com.hotifi.payment.web.responses.RefundReceiptResponse;
import com.hotifi.payment.web.responses.SellerReceiptResponse;
import com.hotifi.user.entitiies.User;

import java.math.BigDecimal;
import java.util.List;

public interface IPaymentService {

    //DO NOT ADD TO CONTROLLER
    void addSellerPayment(User seller, BigDecimal amountEarned);

    //DO NOT ADD TO CONTROLLER
    void updateSellerPayment(User seller, BigDecimal amountEarned, boolean isUpdateTimeOnly);

    //DO NOT ADD TO CONTROLLER
    SellerReceiptResponse addSellerReceipt(User seller, SellerPayment sellerPayment, BigDecimal sellerAmountPaid);

    SellerReceiptResponse getSellerReceipt(Long id);

    List<SellerReceiptResponse> getSortedSellerReceiptsByDateTime(Long sellerPaymentId, int page, int size, boolean isDescending);

    List<SellerReceiptResponse> getSortedSellerReceiptsByAmountPaid(Long sellerPaymentId, int page, int size, boolean isDescending);

    void withdrawBuyerRefunds(Long buyerId);

    List<RefundReceiptResponse> getBuyerRefundReceipts(Long buyerId, int page, int size, boolean isDescending);

    void notifySellerWithdrawalForAdmin(Long sellerId);

    List<PendingTransfer> getAllPendingSellerPaymentsForAdmin();

    List<UpiPendingTransfer> getAllPendingUpiSellerPaymentsForAdmin();

    void updatePendingSellerPaymentsByAdmin(List<TransferUpdate> transferUpdates);

    void updatePendingUpiSellerPaymentsByAdmin(List<UPITransferUpdate> upiTransferUpdates);

    SellerReceiptResponse withdrawSellerPayment(Long sellerId);

    PendingMoneyResponse getPendingPayments(Long sellerId);
}

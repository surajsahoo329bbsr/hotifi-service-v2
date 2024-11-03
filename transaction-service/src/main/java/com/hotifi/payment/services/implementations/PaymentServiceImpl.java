package com.hotifi.payment.services.implementations;

import com.hotifi.payment.entities.SellerPayment;
import com.hotifi.payment.models.PendingTransfer;
import com.hotifi.payment.models.TransferUpdate;
import com.hotifi.payment.models.UPITransferUpdate;
import com.hotifi.payment.models.UpiPendingTransfer;
import com.hotifi.payment.repositories.PurchaseRepository;
import com.hotifi.payment.repositories.SellerPaymentRepository;
import com.hotifi.payment.repositories.SellerReceiptRepository;
import com.hotifi.payment.services.interfaces.IPaymentService;
import com.hotifi.payment.web.responses.PendingMoneyResponse;
import com.hotifi.payment.web.responses.RefundReceiptResponse;
import com.hotifi.payment.web.responses.SellerReceiptResponse;
import com.hotifi.user.entitiies.User;
import com.hotifi.user.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public class PaymentServiceImpl implements IPaymentService {

    private final SellerPaymentRepository sellerPaymentRepository;
    private final SellerReceiptRepository sellerReceiptRepository;
    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;

    public PaymentServiceImpl(SellerPaymentRepository sellerPaymentRepository, SellerReceiptRepository sellerReceiptRepository, UserRepository userRepository, PurchaseRepository purchaseRepository) {
        this.sellerPaymentRepository = sellerPaymentRepository;
        this.sellerReceiptRepository = sellerReceiptRepository;
        this.userRepository = userRepository;
        this.purchaseRepository = purchaseRepository;
    }

    @Override
    public void addSellerPayment(User seller, BigDecimal amountEarned) {

    }

    @Override
    public void updateSellerPayment(User seller, BigDecimal amountEarned, boolean isUpdateTimeOnly) {

    }

    @Override
    public SellerReceiptResponse addSellerReceipt(User seller, SellerPayment sellerPayment, BigDecimal sellerAmountPaid) {
        return null;
    }

    @Override
    public SellerReceiptResponse getSellerReceipt(Long id) {
        return null;
    }

    @Override
    public List<SellerReceiptResponse> getSortedSellerReceiptsByDateTime(Long sellerPaymentId, int page, int size, boolean isDescending) {
        return null;
    }

    @Override
    public List<SellerReceiptResponse> getSortedSellerReceiptsByAmountPaid(Long sellerPaymentId, int page, int size, boolean isDescending) {
        return null;
    }

    @Override
    public void withdrawBuyerRefunds(Long buyerId) {

    }

    @Override
    public List<RefundReceiptResponse> getBuyerRefundReceipts(Long buyerId, int page, int size, boolean isDescending) {
        return null;
    }

    @Override
    public void notifySellerWithdrawalForAdmin(Long sellerId) {

    }

    @Override
    public List<PendingTransfer> getAllPendingSellerPaymentsForAdmin() {
        return null;
    }

    @Override
    public List<UpiPendingTransfer> getAllPendingUpiSellerPaymentsForAdmin() {
        return null;
    }

    @Override
    public void updatePendingSellerPaymentsByAdmin(List<TransferUpdate> transferUpdates) {

    }

    @Override
    public void updatePendingUpiSellerPaymentsByAdmin(List<UPITransferUpdate> upiTransferUpdates) {

    }

    @Override
    public SellerReceiptResponse withdrawSellerPayment(Long sellerId) {
        return null;
    }

    @Override
    public PendingMoneyResponse getPendingPayments(Long sellerId) {
        return null;
    }

    //No need to implement try catch and condition checks since this method will be
    //called by PurchaseServiceImpl
    /*@Transactional
    @Override
    public void addSellerPayment(User seller, BigDecimal amountEarned) {
        SellerPayment sellerPayment = new SellerPayment();
        sellerPayment.setSeller(seller);
        sellerPayment.setAmountEarned(amountEarned);
        sellerPayment.setAmountPaid(BigDecimal.ZERO);
        sellerPaymentRepository.save(sellerPayment);
    }

    //No need to implement try catch and condition checks since this method will be
    //called by PurchaseServiceImpl
    @Transactional
    @Override
    public void updateSellerPayment(User seller, BigDecimal amountEarned, boolean isUpdateTimeOnly) {
        SellerPayment sellerPayment = sellerPaymentRepository.findSellerPaymentBySellerId(seller.getId());
        if (sellerPayment == null)
            throw new ApplicationException(SellerPaymentErrorCodes.SELLER_PAYMENT_NOT_FOUND);
        Date now = new Date(System.currentTimeMillis());
        //If we are not updating time only, then we need to update amount earned
        if (!isUpdateTimeOnly) sellerPayment.setAmountEarned(amountEarned);
        sellerPayment.setModifiedAt(now);
        sellerPaymentRepository.save(sellerPayment);
    }

    //DO NOT ADD TO CONTROLLER
    //No need to implement try catch and condition checks since this method will be
    //called by SellerPaymentServiceImpl
    @Transactional
    @Override
    public SellerReceiptResponse addSellerReceipt(User seller, SellerPayment sellerPayment, BigDecimal sellerAmountPaid) {
        try {
            PaymentProcessor paymentProcessor = new PaymentProcessor(PaymentGatewayCodes.RAZORPAY);
            String bankAccountNumber = seller.getBankAccount().getBankAccountNumber();
            String bankIfscCode = seller.getBankAccount().getBankIfscCode();
            String linkedAccountId = seller.getBankAccount().getLinkedAccountId();
            return paymentProcessor.startSellerPayment(sellerAmountPaid, linkedAccountId, bankAccountNumber, bankIfscCode);
        } catch (Exception e) {
            log.error("Error occurred ", e);
            throw new ApplicationException(SellerPaymentErrorCodes.UNEXPECTED_SELLER_RECEIPT_ERROR);
        }
    }

    @Transactional
    public SellerReceiptResponse addSellerReceiptByAdmin(User seller, TransferUpdate transferUpdate, BigDecimal sellerAmountPaid) {
        try {
            String bankAccountNumber = seller.getBankAccount().getBankAccountNumber();
            String bankIfscCode = seller.getBankAccount().getBankIfscCode();
            String linkedAccountId = seller.getBankAccount().getLinkedAccountId();

            SellerReceipt sellerReceipt = new SellerReceipt();
            sellerReceipt.setStatus(SellerPaymentCodes.PAYMENT_CREATED.value());
            Date createdAt = transferUpdate.getPaidAt();
            Date modifiedAt = new Date(System.currentTimeMillis());
            String transferId = transferUpdate.getTransferId();
            String settlementId = transferUpdate.getSettlementId();

            sellerReceipt.setCreatedAt(createdAt);
            sellerReceipt.setModifiedAt(createdAt);
            sellerReceipt.setModifiedAt(modifiedAt);
            sellerReceipt.setTransferId(transferId);
            sellerReceipt.setAmountPaid(sellerAmountPaid);
            sellerReceipt.setBankIfscCode(bankIfscCode);
            sellerReceipt.setBankAccountNumber(bankAccountNumber);
            sellerReceipt.setSettlementId(settlementId);

            //Setup Seller Receipt
            SellerReceiptResponse sellerReceiptResponse = new SellerReceiptResponse();
            sellerReceiptResponse.setHotifiBankAccount(BusinessConfigurations.HOTIFI_BANK_ACCOUNT);
            sellerReceiptResponse.setSellerLinkedAccountId(linkedAccountId);
            sellerReceiptResponse.setOnHold(transferUpdate.isOnHold());
            sellerReceiptResponse.setSellerReceipt(sellerReceipt);
            sellerReceiptResponse.setOnHoldUntil(transferUpdate.getOnHoldUntil());

            return sellerReceiptResponse;

        } catch (Exception e) {
            log.error("Error occurred ", e);
            throw new ApplicationException(SellerPaymentErrorCodes.UNEXPECTED_SELLER_RECEIPT_ERROR);
        }
    }

    @Transactional
    public SellerReceiptResponse addUpiSellerReceiptByAdmin(User seller, UpiTransferUpdate upiTransferUpdate, BigDecimal sellerAmountPaid) {
        try {
            String utr = upiTransferUpdate.getUtr();
            String upiTransactionId = upiTransferUpdate.getUpiTransactionId();
            String upiId = seller.getUpiId();
            int status = upiTransferUpdate.getStatus();

            SellerReceipt sellerReceipt = new SellerReceipt();
            sellerReceipt.setStatus(status);
            Date createdAt = PaymentUtils.convertUtcToIst(upiTransferUpdate.getPaidAt());
            Date modifiedAt = new Date(System.currentTimeMillis());

            sellerReceipt.setCreatedAt(createdAt);
            sellerReceipt.setModifiedAt(modifiedAt);
            sellerReceipt.setAmountPaid(sellerAmountPaid);
            sellerReceipt.setUpiId(upiId);
            sellerReceipt.setUtr(utr);
            sellerReceipt.setUpiTransactionId(upiTransactionId);

            //Setup Seller Receipt
            SellerReceiptResponse sellerReceiptResponse = new SellerReceiptResponse();
            sellerReceiptResponse.setHotifiBankAccount(BusinessConfigurations.HOTIFI_BANK_ACCOUNT);
            sellerReceiptResponse.setUpiId(upiId);
            sellerReceiptResponse.setSellerReceipt(sellerReceipt);

            return sellerReceiptResponse;

        } catch (Exception e) {
            log.error("Error occurred ", e);
            throw new ApplicationException(SellerPaymentErrorCodes.UNEXPECTED_SELLER_RECEIPT_ERROR);
        }
    }



     //Seller Api Calls


    @Transactional
    @Override
    public SellerReceiptResponse getSellerReceipt(Long id) {
        SellerReceipt sellerReceipt = sellerReceiptRepository.findById(id).orElse(null);
        if (sellerReceipt == null)
            throw new ApplicationException(SellerPaymentErrorCodes.SELLER_RECEIPT_NOT_FOUND);
        try {
            PaymentProcessor paymentProcessor = new PaymentProcessor(PaymentGatewayCodes.RAZORPAY);
            //if (sellerReceipt.getStatus() == SellerPaymentCodes.PAYMENT_PROCESSING.value()) {
            SellerReceiptResponse receiptResponse = paymentProcessor.getSellerPaymentStatus(sellerReceiptRepository, sellerReceipt.getTransferId());
            SellerReceipt latestSellerReceipt = receiptResponse.getSellerReceipt();
            sellerReceiptRepository.save(latestSellerReceipt);
            // }
            SellerPayment sellerPayment = sellerReceipt.getSellerPayment();
            String linkedAccountId = sellerPayment.getSeller().getBankAccount().getLinkedAccountId();
            String hiddenBankAccountNumber = PaymentUtils.hideBankAccountNumber(receiptResponse.getSellerReceipt().getBankAccountNumber());
            receiptResponse.getSellerReceipt().setBankAccountNumber(hiddenBankAccountNumber);
            receiptResponse.setSellerReceipt(sellerReceipt);
            receiptResponse.setSellerLinkedAccountId(linkedAccountId);
            receiptResponse.setHotifiBankAccount(BusinessConfigurations.HOTIFI_BANK_ACCOUNT);
            return receiptResponse;
        } catch (Exception e) {
            log.error("Error occurred ", e);
            throw new ApplicationException(SellerPaymentErrorCodes.UNEXPECTED_SELLER_RECEIPT_ERROR);
        }
    }

    @Transactional
    @Override
    public List<SellerReceiptResponse> getSortedSellerReceiptsByDateTime(Long sellerId, int page, int size, boolean isDescending) {
        SellerPayment sellerPayment = sellerPaymentRepository.findSellerPaymentBySellerId(sellerId);
        if (sellerPayment == null)
            throw new ApplicationException(SellerPaymentErrorCodes.SELLER_PAYMENT_NOT_FOUND);
        try {
            Pageable pageable = isDescending ?
                    PageRequest.of(page, size, Sort.by("created_at").descending()) :
                    PageRequest.of(page, size, Sort.by("created_at"));
            return getSellerReceipts(sellerPayment.getSeller().getId(), pageable, sellerPayment);
        } catch (Exception e) {
            log.error("Error occurred ", e);
            throw new ApplicationException(SellerPaymentErrorCodes.UNEXPECTED_SELLER_RECEIPT_ERROR);
        }
    }

    @Transactional
    @Override
    public List<SellerReceiptResponse> getSortedSellerReceiptsByAmountPaid(Long sellerId, int page, int size, boolean isDescending) {
        try {
            SellerPayment sellerPayment = sellerPaymentRepository.findSellerPaymentBySellerId(sellerId);
            if (sellerPayment == null)
                throw new ApplicationException(SellerPaymentErrorCodes.SELLER_PAYMENT_NOT_FOUND);
            Pageable pageable = isDescending ?
                    PageRequest.of(page, size, Sort.by("amount_paid").descending()) :
                    PageRequest.of(page, size, Sort.by("amount_paid"));
            return getSellerReceipts(sellerPayment.getSeller().getId(), pageable, sellerPayment);
        } catch (Exception e) {
            log.error("Error occurred ", e);
            throw new ApplicationException(SellerPaymentErrorCodes.UNEXPECTED_SELLER_RECEIPT_ERROR);
        }
    }


    @Transactional
    @Override
    public void notifySellerWithdrawalForAdmin(Long sellerId) {
        User seller = userRepository.findById(sellerId).orElse(null);
        boolean isSellerLegit = AppConfigurations.DIRECT_TRANSFER_API_ENABLED ?
                LegitUtils.isSellerLegit(seller, true) : LegitUtils.isSellerUpiLegit(seller, true);
        if (!isSellerLegit)
            throw new ApplicationException(SellerPaymentErrorCodes.SELLER_NOT_LEGIT);
        SellerPayment sellerPayment = sellerPaymentRepository.findSellerPaymentBySellerId(sellerId);
        if (sellerPayment == null)
            throw new ApplicationException(SellerPaymentErrorCodes.SELLER_PAYMENT_NOT_FOUND);

        BigDecimal sellerWithdrawalClaim = sellerPayment.getAmountEarned()
                .multiply(BigDecimal.valueOf((double) (100 - BusinessConfigurations.COMMISSION_PERCENTAGE) / 100))
                .setScale(0, RoundingMode.FLOOR);

        BigDecimal sellerAmountPaid =
                sellerWithdrawalClaim.compareTo(BigDecimal.valueOf(BusinessConfigurations.MAXIMUM_WITHDRAWAL_AMOUNT)) > 0 ?
                        BigDecimal.valueOf(BusinessConfigurations.MAXIMUM_WITHDRAWAL_AMOUNT) : sellerWithdrawalClaim.subtract(sellerPayment.getAmountPaid());

        Date now = new Date(System.currentTimeMillis());

        if (sellerAmountPaid.compareTo(BigDecimal.valueOf(BusinessConfigurations.MINIMUM_WITHDRAWAL_AMOUNT_BEFORE_MATURITY)) < 0) {
            Date lastPaidAt = sellerPayment.getLastPaidAt() != null ? sellerPayment.getLastPaidAt() : sellerPayment.getCreatedAt();
            if (!PaymentUtils.isSellerPaymentDue(now, lastPaidAt))
                throw new ApplicationException(SellerPaymentErrorCodes.WITHDRAW_AMOUNT_PERIOD_ERROR);
            if (sellerAmountPaid.compareTo(BigDecimal.valueOf(BusinessConfigurations.MINIMUM_WITHDRAWAL_AMOUNT)) < 0)
                throw new ApplicationException(SellerPaymentErrorCodes.MINIMUM_WITHDRAWAL_AMOUNT_ERROR);
        }

        try {
            //Following lines will continue after successful-1, processing-2, failure-3 payment
            sellerPayment.setWithdrawalClaimNotified(true);
            sellerPayment.setModifiedAt(now);
            sellerPaymentRepository.save(sellerPayment);
        } catch (Exception e) {
            log.error("Error occurred ", e);
            throw new ApplicationException(SellerPaymentErrorCodes.UNEXPECTED_SELLER_PAYMENT_ERROR);
        }
    }

    //admin api calls
    @Transactional
    @Override
    public List<PendingTransfer> getAllPendingSellerPaymentsForAdmin() {
        List<SellerPayment> sellerPayments = sellerPaymentRepository.findAll();
        List<PendingTransfer> pendingTransfers = new ArrayList<>();
        try {
            sellerPayments.forEach(sellerPayment -> {
                if (sellerPayment.isWithdrawalClaimNotified()) {
                    BigDecimal sellerWithdrawalClaim = sellerPayment.getAmountEarned()
                            .multiply(BigDecimal.valueOf((double) (100 - BusinessConfigurations.COMMISSION_PERCENTAGE) / 100))
                            .setScale(0, RoundingMode.FLOOR);
                    //Pending Transfer Entity
                    Long sellerId = sellerPayment.getSeller().getId();
                    String linkedAccountId = sellerPayment.getSeller().getBankAccount().getLinkedAccountId();
                    Long amountInPaise = sellerWithdrawalClaim.longValue() * 100;
                    String currency = "INR"; //To be changed later
                    PendingTransfer pendingTransfer = new PendingTransfer(sellerId, linkedAccountId, amountInPaise, currency);
                    pendingTransfers.add(pendingTransfer);
                }
            });
        } catch (Exception e) {
            throw new ApplicationException(SellerPaymentErrorCodes.UNEXPECTED_SELLER_PAYMENT_ERROR);
        }

        return pendingTransfers;
    }

    //admin upi call
    @Transactional
    @Override
    public List<UpiPendingTransfer> getAllPendingUpiSellerPaymentsForAdmin() {
        List<SellerPayment> sellerPayments = sellerPaymentRepository.findAll();
        List<UpiPendingTransfer> upiPendingTransfers = new ArrayList<>();
        try {
            sellerPayments.forEach(sellerPayment -> {
                if (sellerPayment.isWithdrawalClaimNotified()) {
                    BigDecimal sellerWithdrawalClaim = sellerPayment.getAmountEarned()
                            .multiply(BigDecimal.valueOf((double) (100 - BusinessConfigurations.COMMISSION_PERCENTAGE) / 100))
                            .setScale(0, RoundingMode.FLOOR);
                    //Pending Transfer Entity
                    Long sellerId = sellerPayment.getSeller().getId();
                    String upiId = sellerPayment.getSeller().getUpiId();
                    String currency = "INR"; //To be changed later
                    UpiPendingTransfer pendingTransfer = new UpiPendingTransfer(sellerId, upiId, sellerWithdrawalClaim, currency);
                    upiPendingTransfers.add(pendingTransfer);
                }
            });
        } catch (Exception e) {
            throw new ApplicationException(SellerPaymentErrorCodes.UNEXPECTED_SELLER_PAYMENT_ERROR);
        }

        return upiPendingTransfers;
    }

    //admin api call
    @Transactional
    @Override
    public void updatePendingSellerPaymentsByAdmin(List<TransferUpdate> transferUpdates) {
        //TODO add error code for PAYMENT_FAILED case --
        // check @updatePendingUpiSellerPaymentsByAdmin(List<TransferUpdate> transferUpdates)
        // for reference
        transferUpdates.forEach(transferUpdate -> {
            Long sellerId = transferUpdate.getSellerId();
            User seller = userRepository.findById(sellerId).orElse(null);
            if (seller == null || seller.getAuthentication().isDeleted())
                throw new ApplicationException(UserErrorCodes.USER_DELETED);
            SellerPayment sellerPayment = sellerPaymentRepository.findSellerPaymentBySellerId(sellerId);
            if (sellerPayment == null)
                throw new ApplicationException(SellerPaymentErrorCodes.SELLER_PAYMENT_NOT_FOUND);

            if (sellerPayment.isWithdrawalClaimNotified()) {
                BigDecimal sellerWithdrawalClaim = sellerPayment.getAmountEarned()
                        .multiply(BigDecimal.valueOf((double) (100 - BusinessConfigurations.COMMISSION_PERCENTAGE) / 100))
                        .setScale(0, RoundingMode.FLOOR);

                BigDecimal sellerAmountPaid =
                        sellerWithdrawalClaim.compareTo(BigDecimal.valueOf(BusinessConfigurations.MAXIMUM_WITHDRAWAL_AMOUNT)) > 0 ?
                                BigDecimal.valueOf(BusinessConfigurations.MAXIMUM_WITHDRAWAL_AMOUNT) : sellerWithdrawalClaim.subtract(sellerPayment.getAmountPaid());

                Date now = new Date(System.currentTimeMillis());

                try {
                    SellerReceiptResponse sellerReceiptResponse = addSellerReceiptByAdmin(seller, transferUpdate, sellerAmountPaid);
                    //Following lines will continue after successful-1, processing-2, failure-3 payment
                    Date lastPaidAt = transferUpdate.getPaidAt();
                    sellerPayment.setAmountPaid(sellerPayment.getAmountPaid().add(sellerAmountPaid));
                    sellerPayment.setLastPaidAt(lastPaidAt);
                    sellerPayment.setModifiedAt(now);
                    sellerPayment.setWithdrawalClaimNotified(false);
                    sellerReceiptResponse.getSellerReceipt().setSellerPayment(sellerPayment);
                    sellerReceiptRepository.save(sellerReceiptResponse.getSellerReceipt());
                    sellerPaymentRepository.save(sellerPayment);
                    return;
                } catch (Exception e) {
                    log.error("Error occurred ", e);
                    throw new ApplicationException(SellerPaymentErrorCodes.UNEXPECTED_SELLER_PAYMENT_ERROR);
                }
            }
            throw new ApplicationException(SellerPaymentErrorCodes.NO_PENDING_TRANSFERS_CLAIMED);
        });
    }

    @Transactional
    @Override
    public void updatePendingUpiSellerPaymentsByAdmin(List<UpiTransferUpdate> upiTransferUpdates) {
        upiTransferUpdates.forEach(upiTransferUpdate -> {
            Long sellerId = upiTransferUpdate.getSellerId();
            User seller = userRepository.findById(sellerId).orElse(null);
            if (seller == null || seller.getAuthentication().isDeleted())
                throw new ApplicationException(UserErrorCodes.USER_DELETED);
            SellerPayment sellerPayment = sellerPaymentRepository.findSellerPaymentBySellerId(sellerId);
            if (sellerPayment == null)
                throw new ApplicationException(SellerPaymentErrorCodes.SELLER_PAYMENT_NOT_FOUND);
            boolean isPaymentFailed = upiTransferUpdate.getStatus() == SellerPaymentCodes.PAYMENT_FAILED.value();
            boolean isErrorDescriptionPresent = upiTransferUpdate.getErrorDescription() != null;
            if (isPaymentFailed && !isErrorDescriptionPresent)
                throw new ApplicationException(SellerPaymentErrorCodes.SELLER_PAYMENT_UPI_FAILED);

            if (sellerPayment.isWithdrawalClaimNotified()) {
                Date now = new Date(System.currentTimeMillis());
                if (isPaymentFailed) {
                    sellerPayment.setModifiedAt(now);
                    sellerPayment.setTransferErrorDescription(upiTransferUpdate.getErrorDescription());
                    sellerPayment.setWithdrawalClaimNotified(false);
                    sellerPaymentRepository.save(sellerPayment);
                    return;
                }

                BigDecimal sellerWithdrawalClaim = sellerPayment.getAmountEarned()
                        .multiply(BigDecimal.valueOf((double) (100 - BusinessConfigurations.COMMISSION_PERCENTAGE) / 100))
                        .setScale(0, RoundingMode.FLOOR);

                BigDecimal sellerAmountPaid =
                        sellerWithdrawalClaim.compareTo(BigDecimal.valueOf(BusinessConfigurations.MAXIMUM_WITHDRAWAL_AMOUNT)) > 0 ?
                                BigDecimal.valueOf(BusinessConfigurations.MAXIMUM_WITHDRAWAL_AMOUNT) : sellerWithdrawalClaim.subtract(sellerPayment.getAmountPaid());

                try {
                    SellerReceiptResponse sellerReceiptResponse = addUpiSellerReceiptByAdmin(seller, upiTransferUpdate, sellerAmountPaid);
                    //Following lines will continue after successful-1, processing-2, failure-3 payment
                    Date lastPaidAt = PaymentUtils.convertUtcToIst(upiTransferUpdate.getPaidAt());
                    sellerPayment.setAmountPaid(sellerPayment.getAmountPaid().add(sellerAmountPaid));
                    sellerPayment.setLastPaidAt(lastPaidAt);
                    sellerPayment.setModifiedAt(now);
                    sellerPayment.setTransferErrorDescription(null);
                    sellerPayment.setWithdrawalClaimNotified(false);
                    sellerReceiptResponse.getSellerReceipt().setSellerPayment(sellerPayment);
                    sellerReceiptRepository.save(sellerReceiptResponse.getSellerReceipt());
                    sellerPaymentRepository.save(sellerPayment);
                    return;
                } catch (Exception e) {
                    log.error("Error occurred ", e);
                    throw new ApplicationException(SellerPaymentErrorCodes.UNEXPECTED_SELLER_PAYMENT_ERROR);
                }
            }
            throw new ApplicationException(SellerPaymentErrorCodes.NO_PENDING_TRANSFERS_CLAIMED);
        });
    }

    @Transactional
    @Override
    public SellerReceiptResponse withdrawSellerPayment(Long sellerId) {
        User seller = userRepository.findById(sellerId).orElse(null);
        boolean isSellerLegit = AppConfigurations.DIRECT_TRANSFER_API_ENABLED ?
                LegitUtils.isSellerLegit(seller, true) : LegitUtils.isSellerUpiLegit(seller, true);
        if (!isSellerLegit)
            throw new ApplicationException(SellerPaymentErrorCodes.SELLER_NOT_LEGIT);
        SellerPayment sellerPayment = sellerPaymentRepository.findSellerPaymentBySellerId(sellerId);
        if (sellerPayment == null)
            throw new ApplicationException(SellerPaymentErrorCodes.SELLER_PAYMENT_NOT_FOUND);

        BigDecimal sellerWithdrawalClaim = sellerPayment.getAmountEarned()
                .multiply(BigDecimal.valueOf((double) (100 - BusinessConfigurations.COMMISSION_PERCENTAGE) / 100))
                .setScale(0, RoundingMode.FLOOR);

        BigDecimal sellerAmountPaid =
                sellerWithdrawalClaim.compareTo(BigDecimal.valueOf(BusinessConfigurations.MAXIMUM_WITHDRAWAL_AMOUNT)) > 0 ?
                        BigDecimal.valueOf(BusinessConfigurations.MAXIMUM_WITHDRAWAL_AMOUNT) : sellerWithdrawalClaim.subtract(sellerPayment.getAmountPaid());

        Date now = new Date(System.currentTimeMillis());

        if (sellerAmountPaid.compareTo(BigDecimal.valueOf(BusinessConfigurations.MINIMUM_WITHDRAWAL_AMOUNT_BEFORE_MATURITY)) < 0) {
            Date lastPaidAt = sellerPayment.getLastPaidAt() != null ? sellerPayment.getLastPaidAt() : sellerPayment.getCreatedAt();
            if (!PaymentUtils.isSellerPaymentDue(now, lastPaidAt))
                throw new ApplicationException(SellerPaymentErrorCodes.WITHDRAW_AMOUNT_PERIOD_ERROR);
            if (sellerAmountPaid.compareTo(BigDecimal.valueOf(BusinessConfigurations.MINIMUM_WITHDRAWAL_AMOUNT)) < 0)
                throw new ApplicationException(SellerPaymentErrorCodes.MINIMUM_WITHDRAWAL_AMOUNT_ERROR);
        }

        try {
            SellerReceiptResponse sellerReceiptResponse = addSellerReceipt(seller, sellerPayment, sellerAmountPaid);
            //Following lines will continue after successful-1, processing-2, failure-3 payment
            Date lastPaidAt = sellerReceiptResponse.getSellerReceipt().getPaidAt();
            sellerPayment.setAmountPaid(sellerPayment.getAmountPaid().add(sellerAmountPaid));
            sellerPayment.setLastPaidAt(lastPaidAt);
            sellerPayment.setModifiedAt(now);
            sellerReceiptResponse.getSellerReceipt().setSellerPayment(sellerPayment);
            sellerReceiptRepository.save(sellerReceiptResponse.getSellerReceipt());
            sellerPaymentRepository.save(sellerPayment);
            return sellerReceiptResponse;
        } catch (Exception e) {
            log.error("Error occurred ", e);
            throw new ApplicationException(SellerPaymentErrorCodes.UNEXPECTED_SELLER_PAYMENT_ERROR);
        }
    }

    //User defined functions
    private List<SellerReceiptResponse> getSellerReceipts(Long sellerPaymentId, Pageable pageable, SellerPayment sellerPayment) {
        List<SellerReceipt> sellerReceipts = sellerReceiptRepository.findSellerReceipts(sellerPaymentId, pageable);
        if (sellerReceipts == null)
            throw new ApplicationException(SellerPaymentErrorCodes.SELLER_RECEIPT_NOT_FOUND);
        try {
            PaymentProcessor paymentProcessor = new PaymentProcessor(PaymentGatewayCodes.RAZORPAY);
            List<SellerReceiptResponse> sellerReceiptResponses = new ArrayList<>();
            for (SellerReceipt sellerReceipt : sellerReceipts) {
                if (sellerReceipt.getStatus() <= SellerPaymentCodes.PAYMENT_CREATED.value()) {
                    SellerReceiptResponse receipt = paymentProcessor.getSellerPaymentStatus(sellerReceiptRepository, sellerReceipt.getTransferId());
                    sellerReceipt = receipt.getSellerReceipt();
                    sellerReceiptRepository.save(sellerReceipt);

                    //updating seller_payment entity after withdrawing money
                    sellerPayment.setAmountPaid(sellerReceipt.getAmountPaid().add(sellerPayment.getAmountPaid()));
                    sellerPayment.setLastPaidAt(sellerReceipt.getPaidAt());
                    sellerPayment.setModifiedAt(sellerReceipt.getModifiedAt());
                    sellerPaymentRepository.save(sellerPayment);

                }
                //TODO Below commented lines may be changed if Direct Transfer API Works
                //String linkedAccountId = sellerPayment.getSeller().getBankAccount().getLinkedAccountId();
                //String hiddenBankAccountNumber = PaymentUtils.hideBankAccountNumber(receiptResponse.getSellerReceipt().getBankAccountNumber());
                //receiptResponse.getSellerReceipt().setBankAccountNumber(hiddenBankAccountNumber);
                //receiptResponse.setSellerLinkedAccountId(linkedAccountId);

                SellerReceiptResponse receiptResponse = new SellerReceiptResponse();
                receiptResponse.setSellerReceipt(sellerReceipt);
                receiptResponse.setHotifiBankAccount(BusinessConfigurations.HOTIFI_BANK_ACCOUNT);
                sellerReceiptResponses.add(receiptResponse);
            }
            return sellerReceiptResponses;
        } catch (Exception e) {
            log.error("Error occurred", e);
            throw new ApplicationException(SellerPaymentErrorCodes.UNEXPECTED_SELLER_RECEIPT_ERROR);
        }
    }

    private PendingMoneyResponse getSellerPayment(Long sellerId) {
        BigDecimal sellerAmount = new BigDecimal("0.00");
        BigDecimal buyerRefund = new BigDecimal("0.00");
        User seller = userRepository.findById(sellerId).orElse(null);
        if (seller == null) {
            return new PendingMoneyResponse(false, sellerAmount, false,
                    null, null,
                    false, buyerRefund, null);
        }
        boolean isTransferIdPresent = AppConfigurations.DIRECT_TRANSFER_API_ENABLED ?
                seller.getBankAccount() != null : seller.getUpiId() != null;

        if (!isTransferIdPresent) {
            return new PendingMoneyResponse(false, sellerAmount, false,
                    null, null,
                    false, buyerRefund, null);
        }

        SellerPayment sellerPayment = sellerPaymentRepository.findSellerPaymentBySellerId(sellerId);
        if (sellerPayment == null) {
            return new PendingMoneyResponse(false, sellerAmount, false,
                    null, null,
                    false, buyerRefund, null);
        }

        //double sellerWithdrawalClaim = Math.floor(sellerPayment.getAmountEarned() * (double) (100 - Constants.COMMISSION_PERCENTAGE) / 100);
        BigDecimal sellerWithdrawalClaim = sellerPayment.getAmountEarned()
                .multiply(BigDecimal.valueOf((double) (100 - BusinessConfigurations.COMMISSION_PERCENTAGE) / 100))
                .setScale(0, RoundingMode.FLOOR);

        BigDecimal sellerAmountPaid =
                sellerWithdrawalClaim.compareTo(BigDecimal.valueOf(BusinessConfigurations.MAXIMUM_WITHDRAWAL_AMOUNT)) > 0 ?
                        BigDecimal.valueOf(BusinessConfigurations.MAXIMUM_WITHDRAWAL_AMOUNT) : sellerWithdrawalClaim.subtract(sellerPayment.getAmountPaid());

        Date now = new Date(System.currentTimeMillis());
        boolean isWithdrawalClaimNotified = sellerPayment.isWithdrawalClaimNotified();
        String transferErrorDescription = sellerPayment.getTransferErrorDescription();

        if (sellerAmountPaid.compareTo(BigDecimal.valueOf(BusinessConfigurations.MINIMUM_WITHDRAWAL_AMOUNT_BEFORE_MATURITY)) < 0) {
            Date lastPaidAt = sellerPayment.getLastPaidAt() != null ? sellerPayment.getLastPaidAt() : sellerPayment.getCreatedAt();
            boolean isSellerPaymentDue = !PaymentUtils.isSellerPaymentDue(now, lastPaidAt);
            boolean isMinimumWithdrawalAmount = sellerAmountPaid.compareTo(BigDecimal.valueOf(BusinessConfigurations.MINIMUM_WITHDRAWAL_AMOUNT)) < 0;
            if (isSellerPaymentDue || isMinimumWithdrawalAmount)
                return new PendingMoneyResponse(false, sellerAmountPaid, isWithdrawalClaimNotified,
                        transferErrorDescription, lastPaidAt, false, buyerRefund, null);
        }

        return new PendingMoneyResponse(true, sellerAmountPaid, isWithdrawalClaimNotified,
                transferErrorDescription, null,
                false, new BigDecimal("0.00"), null);
    }


      //Buyer Api Calls
    @Transactional
    @Override
    public void withdrawBuyerRefunds(Long buyerId) {
        User buyer = userRepository.findById(buyerId).orElse(null);
        if (LegitUtils.isBuyerLegit(buyer)) {
            PaymentProcessor paymentProcessor = new PaymentProcessor(PaymentGatewayCodes.RAZORPAY);
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Date currentTime = new Date(System.currentTimeMillis());
            Supplier<Stream<Purchase>> purchaseStreamSupplier = () -> purchaseRepository.findPurchasesByBuyerId(buyerId, pageable).stream();
            BigDecimal totalRefundAmount = purchaseStreamSupplier.get()
                    .filter(purchase -> purchase.getStatus() % BusinessConfigurations.PAYMENT_METHOD_START_VALUE_CODE < BuyerPaymentCodes.REFUND_PENDING.value() && !PaymentUtils.isBuyerRefundDue(currentTime, purchase.getPaymentDoneAt()))
                    .map(Purchase::getAmountRefund)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalRefundAmount.compareTo(BigDecimal.ZERO) == 0)
                throw new ApplicationException(PurchaseErrorCodes.BUYER_PENDING_REFUNDS_NOT_FOUND);

            List<Purchase> purchases = purchaseStreamSupplier.get()
                    .filter(purchase -> purchase.getStatus() % BusinessConfigurations.PAYMENT_METHOD_START_VALUE_CODE < BuyerPaymentCodes.REFUND_PENDING.value()
                            && !PaymentUtils.isBuyerRefundDue(currentTime, purchase.getPaymentDoneAt()))
                    .collect(Collectors.toList());

            purchases.forEach(purchase -> {
                RefundReceiptResponse buyerRefund = paymentProcessor.startBuyerRefund(purchaseRepository, purchase.getAmountRefund(), purchase.getPaymentId());
                String refundPaymentId = buyerRefund.getPurchase().getRefundPaymentId();
                Date refundStartedAt = buyerRefund.getPurchase().getRefundStartedAt();
                String refundTransactionId = buyerRefund.getPurchase().getRefundTransactionId();
                int buyerPaymentStatus = buyerRefund.getPurchase().getStatus();
                purchaseRepository.updatePurchaseRefundStatus(buyerPaymentStatus, refundPaymentId, refundStartedAt, refundTransactionId, purchase.getId());
            });

        } else
            throw new ApplicationException(PurchaseErrorCodes.PURCHASE_UPDATE_NOT_LEGIT);
    }

    @Transactional
    @Override
    public List<RefundReceiptResponse> getBuyerRefundReceipts(Long buyerId, int page, int size, boolean isDescending) {
        User buyer = userRepository.findById(buyerId).orElse(null);
        if (LegitUtils.isBuyerLegit(buyer)) {
            Pageable pageable = isDescending ? PageRequest.of(page, size, Sort.by("refund_started_at").descending()) :
                    PageRequest.of(page, size, Sort.by("refund_started_at"));
            List<Purchase> purchaseReceipts = purchaseRepository.findPurchasesByBuyerId(buyerId, pageable)
                    .stream()
                    .filter(purchase -> purchase.getStatus() % BusinessConfigurations.PAYMENT_METHOD_START_VALUE_CODE >= BuyerPaymentCodes.REFUND_PENDING.value())
                    .collect(Collectors.toList());
            List<RefundReceiptResponse> refundReceiptResponses = new ArrayList<>();
            PaymentProcessor paymentProcessor = new PaymentProcessor(PaymentGatewayCodes.RAZORPAY);
            purchaseReceipts.forEach(purchase -> {

                RefundReceiptResponse receiptResponse = paymentProcessor.getBuyerRefundStatus(purchaseRepository, purchase.getPaymentId());
                //Setting up values from payment processor
                int status = receiptResponse.getPurchase().getStatus();
                Date refundStartedAt = receiptResponse.getPurchase().getRefundStartedAt();
                String refundPaymentId = receiptResponse.getPurchase().getRefundPaymentId();
                String refundTransactionId = receiptResponse.getPurchase().getRefundTransactionId();
                purchaseRepository.updatePurchaseRefundStatus(status, refundPaymentId, refundStartedAt, refundTransactionId, purchase.getId());

                purchase.setStatus(status);
                purchase.setRefundPaymentId(refundPaymentId);
                purchase.setRefundStartedAt(refundStartedAt);
                purchase.setRefundTransactionId(refundTransactionId);
                RefundReceiptResponse refundReceiptResponse =
                        new RefundReceiptResponse(purchase, BusinessConfigurations.HOTIFI_BANK_ACCOUNT);
                refundReceiptResponses.add(refundReceiptResponse);
            });
            return refundReceiptResponses;
        }

        throw new ApplicationException(PurchaseErrorCodes.BUYER_NOT_LEGIT);
    }

    @Transactional
    @Override
    public PendingMoneyResponse getPendingPayments(Long sellerId) {
        PendingMoneyResponse pendingSellerPayments
                = getSellerPayment(sellerId);
        return getBuyerRefund(sellerId, pendingSellerPayments);
    }

    private PendingMoneyResponse getBuyerRefund(Long buyerId, PendingMoneyResponse pendingMoneyResponse) {
        BigDecimal pendingBuyerRefund = new BigDecimal("0.00");
        Date oldestPurchaseDate = new Date(System.currentTimeMillis());
        User buyer = userRepository.findById(buyerId).orElse(null);
        if (LegitUtils.isBuyerLegit(buyer)) {
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Date currentTime = new Date(System.currentTimeMillis());
            Supplier<Stream<Purchase>> purchaseStreamSupplier = () -> purchaseRepository.findPurchasesByBuyerId(buyerId, pageable).stream();
            BigDecimal totalRefundAmount = purchaseStreamSupplier.get()
                    .filter(purchase -> purchase.getStatus() % BusinessConfigurations.PAYMENT_METHOD_START_VALUE_CODE < BuyerPaymentCodes.REFUND_PENDING.value()
                            && !PaymentUtils.isBuyerRefundDue(currentTime, purchase.getPaymentDoneAt()))
                    .map(Purchase::getAmountRefund)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalRefundAmount.compareTo(BigDecimal.ZERO) == 0) {
                pendingMoneyResponse.setBuyerRefundDue(false);
                pendingMoneyResponse.setBuyerRefund(pendingBuyerRefund);
                pendingMoneyResponse.setBuyerOldestPendingRefundAt(null);
                return pendingMoneyResponse;
            }

            List<Purchase> purchases = purchaseStreamSupplier.get()
                    .filter(purchase -> purchase.getStatus() % BusinessConfigurations.PAYMENT_METHOD_START_VALUE_CODE < BuyerPaymentCodes.REFUND_PENDING.value()
                            && !PaymentUtils.isBuyerRefundDue(currentTime, purchase.getPaymentDoneAt()))
                    .collect(Collectors.toList());

            if (purchases.size() == 0) {
                pendingMoneyResponse.setBuyerRefundDue(false);
                pendingMoneyResponse.setBuyerRefund(pendingBuyerRefund);
                pendingMoneyResponse.setBuyerOldestPendingRefundAt(null);
                return pendingMoneyResponse;
            }

            PaymentProcessor paymentProcessor = new PaymentProcessor(PaymentGatewayCodes.RAZORPAY);

            for (Purchase purchase : purchases) {
                pendingBuyerRefund = pendingBuyerRefund.add(purchase.getAmountRefund());
                if (purchase.getRefundPaymentId() != null)
                    paymentProcessor.getBuyerRefundStatus(purchaseRepository, purchase.getPaymentId());
                if (purchase.getPaymentDoneAt().before(oldestPurchaseDate)) {
                    oldestPurchaseDate = purchase.getPaymentDoneAt();
                }
            }

            pendingMoneyResponse.setBuyerRefundDue(true);
            pendingMoneyResponse.setBuyerRefund(pendingBuyerRefund);
            pendingMoneyResponse.setBuyerOldestPendingRefundAt(oldestPurchaseDate);
            return pendingMoneyResponse;
        }
        pendingMoneyResponse.setBuyerRefundDue(false);
        pendingMoneyResponse.setBuyerRefund(pendingBuyerRefund);
        pendingMoneyResponse.setBuyerOldestPendingRefundAt(null);
        return pendingMoneyResponse;
    }*/


}

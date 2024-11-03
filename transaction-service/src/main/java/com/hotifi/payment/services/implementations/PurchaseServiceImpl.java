package com.hotifi.payment.services.implementations;

import com.google.api.client.util.Value;
import com.hotifi.authentication.entities.Authentication;
import com.hotifi.common.constants.BusinessConstants;
import com.hotifi.common.exception.ApplicationException;
import com.hotifi.common.utils.AESUtils;
import com.hotifi.payment.entities.Purchase;
import com.hotifi.payment.entities.SellerPayment;
import com.hotifi.payment.errors.PurchaseErrorCodes;
import com.hotifi.payment.processor.PaymentProcessor;
import com.hotifi.payment.processor.codes.BuyerPaymentCodes;
import com.hotifi.payment.processor.codes.OrderStatusCodes;
import com.hotifi.payment.processor.codes.PaymentGatewayCodes;
import com.hotifi.payment.processor.razorpay.RazorpayVerificationUtils;
import com.hotifi.payment.utils.PaymentUtils;
import com.hotifi.payment.validators.PaymentValidatorUtils;
import com.hotifi.payment.web.responses.RefundReceiptResponse;
import com.hotifi.session.entities.Session;
import com.hotifi.session.repositories.SessionRepository;
import com.hotifi.speedtest.entities.SpeedTest;
import com.hotifi.speedtest.repositories.SpeedTestRepository;
import com.hotifi.payment.entities.PurchaseOrder;
import com.hotifi.payment.repositories.PurchaseOrderRepository;
import com.hotifi.payment.repositories.PurchaseRepository;
import com.hotifi.payment.repositories.SellerPaymentRepository;
import com.hotifi.payment.services.interfaces.IPaymentService;
import com.hotifi.payment.services.interfaces.IPurchaseService;
import com.hotifi.payment.web.request.OrderRequest;
import com.hotifi.payment.web.request.PurchaseRequest;
import com.hotifi.payment.web.responses.PurchaseReceiptResponse;
import com.hotifi.payment.web.responses.WifiSummaryResponse;
import com.hotifi.user.entitiies.User;
import com.hotifi.user.errors.codes.UserErrorCodes;
import com.hotifi.user.repositories.UserRepository;
import com.hotifi.user.validators.UserValidatorUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class PurchaseServiceImpl implements IPurchaseService {

    private final UserRepository userRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseRepository purchaseRepository;
    private final SellerPaymentRepository sellerPaymentRepository;
    private final IPaymentService sellerPaymentService;

    @Value("${business.aes.secret-key}")
    private String businessAESSecretKey;

    private RestTemplate restTemplate;

    public PurchaseServiceImpl(UserRepository userRepository, PurchaseOrderRepository purchaseOrderRepository, PurchaseRepository purchaseRepository, SellerPaymentRepository sellerPaymentRepository, IPaymentService sellerPaymentService) {
        this.userRepository = userRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseRepository = purchaseRepository;
        this.sellerPaymentRepository = sellerPaymentRepository;
        this.sellerPaymentService = sellerPaymentService;
        restTemplate = new RestTemplate();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isCurrentSessionValid(Long buyerId, Long sessionId, int dataToBeUsed) {
        //check for invalid inputs and invalid razorpay payment id checks
        User buyer = userRepository.findById(buyerId).orElse(null);
        Session session = restTemplate.getForObject("http://localhost:5004/session/" + sessionId, Session.class);

        Authentication authentication = restTemplate.getForObject("http://localhost:5001/authentication/" + buyer.getAuthenticationId(), Authentication.class);

        if (!PaymentValidatorUtils.isBuyerValid(buyer, authentication))
            throw new ApplicationException(PurchaseErrorCodes.BUYER_NOT_LEGIT);
        if (session == null)
            throw new ApplicationException(PurchaseErrorCodes.SESSION_NOT_FOUND);
        if (session.getFinishedAt() != null)
            throw new ApplicationException(PurchaseErrorCodes.SESSION_ALREADY_ENDED);
        if (Double.compare(dataToBeUsed, (double) session.getData() - session.getDataUsed()) > 0)
            throw new ApplicationException(PurchaseErrorCodes.EXCESS_DATA_TO_BUY_ERROR);

        Long sellerId = session.getSpeedTest().getUser().getId();
        if (sellerId.equals(buyerId))
            throw new ApplicationException(PurchaseErrorCodes.BUYER_SELLER_SAME);

        return true;
    }

    @Override
    @Transactional
    public PurchaseOrder addPurchaseOrder(OrderRequest orderRequest) {
        Long sessionId = orderRequest.getSessionId();
        Session session = restTemplate.getForObject("http://localhost:5004/session/" + sessionId, Session.class);
        User buyer = userRepository.findById(orderRequest.getBuyerId()).orElse(null);
        PaymentProcessor paymentProcessor = new PaymentProcessor(PaymentGatewayCodes.RAZORPAY);

        try {
            BigDecimal amountOrder = getBigDecimal(orderRequest, session);

            PurchaseOrder purchaseOrder = paymentProcessor.addBuyerOrder(amountOrder, "INR");
            purchaseOrder.setSessionId(session.getId());
            purchaseOrder.setUser(buyer);
            purchaseOrder.setData(orderRequest.getData());
            purchaseOrderRepository.save(purchaseOrder);
            return purchaseOrder;

        } catch (Exception e) {
            log.error("Error occurred", e);
            throw new ApplicationException(PurchaseErrorCodes.UNEXPECTED_ORDER_ERROR);
        }
    }

    @NotNull
    private static BigDecimal getBigDecimal(OrderRequest orderRequest, Session session) {
        BigDecimal amountOrder = session != null ?
                session.getPrice()
                        .multiply(BigDecimal.valueOf(orderRequest.getData()))
                        .divide(BigDecimal.valueOf(BusinessConstants.UNIT_GB_VALUE_IN_MB), 2, RoundingMode.CEILING)
                        .setScale(0, RoundingMode.CEILING) : BigDecimal.ZERO;

        if (amountOrder.compareTo(orderRequest.getAmount()) != 0) {
            throw new ApplicationException(PurchaseErrorCodes.ORDER_AMOUNT_MODIFIED);
        }
        return amountOrder;
    }

    @Override
    @Transactional
    public PurchaseReceiptResponse addPurchase(PurchaseRequest purchaseRequest) {
        Long sessionId = purchaseRequest.getSessionId();
        String orderId = purchaseRequest.getOrderId();
        String paymentId = purchaseRequest.getPaymentId();
        Session session = restTemplate.getForObject("http://localhost:8080/session/" + sessionId, Session.class);
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findPurchaseOrderByPurchaseOrderId(orderId);
        User buyer = userRepository.findById(purchaseOrder.getUser().getId()).orElse(null);
        PaymentProcessor paymentProcessor = new PaymentProcessor(PaymentGatewayCodes.RAZORPAY);

        try {
            String clientRazorpaySignature = purchaseRequest.getClientRazorpaySignature();
            //TODO
            if(clientRazorpaySignature == null){
                //TODO
            }

            boolean isPaymentAuthentic = RazorpayVerificationUtils.verifyRazorpaySignature(orderId, paymentId, clientRazorpaySignature);
            if (!isPaymentAuthentic)
                throw new ApplicationException(PurchaseErrorCodes.CLIENT_SERVER_PAYMENT_SIGNATURE_MISMATCH);

            com.razorpay.Order razorpayOrder = paymentProcessor.getPurchaseOrder(orderId);
            BigDecimal orderAmount = PaymentUtils
                    .divideThenMultiplyCeilingTwoScale(new BigDecimal(razorpayOrder.get("amount").toString()), BigDecimal.valueOf(100), BigDecimal.ONE);
            BigDecimal orderAmountPaid = PaymentUtils
                    .divideThenMultiplyCeilingTwoScale(new BigDecimal(razorpayOrder.get("amount_paid").toString()), BigDecimal.valueOf(100), BigDecimal.ONE);
            BigDecimal orderAmountDue = PaymentUtils
                    .divideThenMultiplyCeilingTwoScale(new BigDecimal(razorpayOrder.get("amount_due").toString()), BigDecimal.valueOf(100), BigDecimal.ONE);

            String status = razorpayOrder.get("status");
            int attempts = Integer.parseInt(razorpayOrder.get("attempts").toString());
            Date modifiedAt = new Date(System.currentTimeMillis());

            purchaseOrder.setAmount(orderAmount);
            purchaseOrder.setAmountPaid(orderAmountPaid);
            purchaseOrder.setAmountDue(orderAmountDue);
            purchaseOrder.setStatus(OrderStatusCodes.valueOf(status.toUpperCase()).name());
            purchaseOrder.setAttempts(attempts);
            purchaseOrder.setOrderModifiedAt(modifiedAt);

            BigDecimal amountPaid = session != null ?
                    session.getPrice()
                            .multiply(BigDecimal.valueOf(purchaseRequest.getData()))
                            .divide(BigDecimal.valueOf(BusinessConstants.UNIT_GB_VALUE_IN_MB), 2, RoundingMode.CEILING)
                            .setScale(0, RoundingMode.CEILING) : BigDecimal.ZERO;

            Purchase buyerPurchase = paymentProcessor.getBuyerPurchase(purchaseRequest.getPaymentId(), amountPaid);
            Purchase purchase = new Purchase();
            purchase.setPurchaseOrder(purchaseOrder);

            //Getting values from payment processor
            purchase.setPaymentDoneAt(buyerPurchase.getPaymentDoneAt());
            purchase.setPaymentId(buyerPurchase.getPaymentId());
            purchase.setPaymentTransactionId(buyerPurchase.getPaymentTransactionId());
            purchase.setStatus(buyerPurchase.getStatus());
            //Getting values from purchase request
            purchase.setMacAddress(purchaseRequest.getMacAddress());
            purchase.setIpAddress(purchase.getIpAddress());
            purchase.setData(purchaseRequest.getData());
            purchase.setAmountPaid(amountPaid);
            purchase.setAmountRefund(amountPaid);

            //If payment failed or processing do not update session value
            boolean isSessionCompleted = session != null && session.getFinishedAt() != null && buyer != null;
            boolean isBothMacAndIpAddressAbsent = purchaseRequest.getMacAddress() == null && purchaseRequest.getIpAddress() == null && session != null && buyer != null;
            boolean isDataBoughtMoreThanDataSold = session != null && buyer != null && Double.compare(purchaseRequest.getData(), (double) session.getData() - session.getDataUsed()) > 0;
            boolean isPurchaseAlreadySuccessful = buyerPurchase.getStatus() % BusinessConstants.PAYMENT_METHOD_START_VALUE_CODE >= BuyerPaymentCodes.PAYMENT_CAPTURED.value();

            if ((isSessionCompleted || isDataBoughtMoreThanDataSold) && isPurchaseAlreadySuccessful) {
                RefundReceiptResponse receiptResponse = paymentProcessor.startBuyerRefund(purchaseRepository, buyerPurchase.getAmountPaid(), purchaseRequest.getPaymentId());
                purchase.setStatus(receiptResponse.getPurchase().getStatus());
                purchase.setRefundStartedAt(receiptResponse.getPurchase().getRefundStartedAt());
                purchase.setRefundPaymentId(receiptResponse.getPurchase().getRefundPaymentId());
                purchase.setRefundTransactionId(receiptResponse.getPurchase().getRefundTransactionId());
                log.info("Razor Refund Payment");
            }

            Long purchaseId = purchaseRepository.save(purchase).getId();
            boolean updateSessionFlag = (!isSessionCompleted && !isBothMacAndIpAddressAbsent && !isDataBoughtMoreThanDataSold) || !isPurchaseAlreadySuccessful;

            //saving session data used column
            if (session != null && updateSessionFlag) {
                session.setDataUsed(session.getDataUsed() + purchaseRequest.getData());
                restTemplate.put( "http://localhost:5004/session/update/" + session , Session.class);
                purchaseOrderRepository.save(purchaseOrder);
            }

            return getPurchaseReceipt(purchaseId);

        } catch (Exception e) {
            log.error("Error occurred", e);
            throw new ApplicationException(PurchaseErrorCodes.UNEXPECTED_PURCHASE_ERROR);
        }
    }

    @Override
    @Transactional
    public PurchaseReceiptResponse getPurchaseReceipt(Long purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId).orElse(null);
        if (purchase == null)
            throw new ApplicationException(PurchaseErrorCodes.PURCHASE_NOT_FOUND);
        try {
            //TODO add url
            Session session = restTemplate.getForObject("", Session.class);
            String wifiPassword = session != null ? session.getWifiPassword() : null;
            PurchaseReceiptResponse receiptResponse = new PurchaseReceiptResponse();
            receiptResponse.setPurchaseId(purchase.getId());
            receiptResponse.setPaymentId(purchase.getPaymentId());
            receiptResponse.setCreatedAt(purchase.getPaymentDoneAt());
            receiptResponse.setPurchaseStatus(purchase.getStatus());
            receiptResponse.setAmountPaid(purchase.getAmountPaid());
            receiptResponse.setPurchaseTransactionId(purchase.getPaymentTransactionId());
            receiptResponse.setHotifiBankAccount(BusinessConstants.HOTIFI_BANK_ACCOUNT);
            receiptResponse.setWifiPassword(AESUtils.decrypt(wifiPassword, businessAESSecretKey));
            if (purchase.getRefundPaymentId() != null) {
                receiptResponse.setRefundStartedAt(purchase.getRefundStartedAt());
                receiptResponse.setRefundPaymentId(purchase.getRefundPaymentId());
                receiptResponse.setWifiPassword(null);
            }
            if (purchase.getStatus() % BusinessConstants.PAYMENT_METHOD_START_VALUE_CODE < BuyerPaymentCodes.PAYMENT_CAPTURED.value()) {
                receiptResponse.setWifiPassword(null);
            }
            return receiptResponse;
        } catch (Exception e) {
            log.error("Error occurred ", e);
            throw new ApplicationException(PurchaseErrorCodes.UNEXPECTED_PURCHASE_ERROR);
        }
    }

    @Override
    @Transactional
    public Date startBuyerWifiService(Long purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId).orElse(null);
        if (purchase == null)
            throw new ApplicationException(PurchaseErrorCodes.PURCHASE_NOT_FOUND);
        if (purchase.getRefundStartedAt() != null)
            throw new ApplicationException(PurchaseErrorCodes.BUYER_WIFI_SERVICE_ALREADY_FINISHED);
        if (purchase.getSessionCreatedAt() != null)
            throw new ApplicationException(PurchaseErrorCodes.BUYER_WIFI_SERVICE_ALREADY_STARTED);
        if (purchase.getSessionFinishedAt() != null)
            throw new ApplicationException(PurchaseErrorCodes.BUYER_WIFI_SERVICE_ALREADY_FINISHED);
        if (purchase.getStatus() % BusinessConstants.PAYMENT_METHOD_START_VALUE_CODE <= BuyerPaymentCodes.PAYMENT_AUTHORIZED.value())
            throw new ApplicationException(PurchaseErrorCodes.PAYMENT_NOT_SUCCESSFUL);
        try {
            Date sessionStartedAt = new Date(System.currentTimeMillis());
            purchase.setSessionCreatedAt(sessionStartedAt);
            purchase.setSessionModifiedAt(sessionStartedAt);
            purchase.setStatus(purchase.getStatus() - purchase.getStatus() % BusinessConstants.PAYMENT_METHOD_START_VALUE_CODE + BuyerPaymentCodes.START_WIFI_SERVICE.value());
            purchaseRepository.save(purchase);
            saveSessionWithWifiService(purchase);
            return sessionStartedAt;
        } catch (Exception e) {
            log.error("Error occurred ", e);
            throw new ApplicationException(PurchaseErrorCodes.UNEXPECTED_PURCHASE_ERROR);
        }
    }

    @Transactional
    @Override
    public int updateBuyerWifiService(Long purchaseId, double dataUsed) {
        Purchase purchase = purchaseRepository.findById(purchaseId).orElse(null);
        if (PaymentValidatorUtils.isPurchaseUpdateLegit(purchase, dataUsed)) {
            int dataBought = purchase.getData();
            int purchaseStatus = purchase.getStatus() - purchase.getStatus() % BusinessConstants.PAYMENT_METHOD_START_VALUE_CODE + BuyerPaymentCodes.UPDATE_WIFI_SERVICE.value();
            Date now = new Date(System.currentTimeMillis());

            double dataUsedBefore = purchase.getDataUsed();
            BigDecimal calculatedRefundAmount = calculateBuyerRefundAmount(dataBought, dataUsed, purchase.getAmountPaid());
            BigDecimal calculatedSellerAmount = calculateSellerPaymentAmount(dataBought, dataUsed, dataUsedBefore, purchase.getAmountPaid());

            //Updating seller payment each time update is made
            //TODO add url
            Session session = restTemplate.getForObject("", Session.class);
            User seller = session != null ? session.getSpeedTest().getUser() : null;
            SellerPayment sellerPayment = seller != null ? sellerPaymentRepository.findSellerPaymentBySellerId(seller.getId()) : null;
            boolean isUpdateTimeOnly = Double.compare(dataUsed, dataUsedBefore) == 0;

            if (sellerPayment == null)
                sellerPaymentService.addSellerPayment(seller, calculatedSellerAmount);
            else if (Double.compare(dataUsed, purchase.getDataUsed()) >= 0)
                sellerPaymentService.updateSellerPayment(seller, sellerPayment.getAmountEarned()
                        .add(calculatedSellerAmount), isUpdateTimeOnly);

            purchase.setDataUsed(dataUsed);
            purchase.setAmountRefund(calculatedRefundAmount);
            purchase.setSessionModifiedAt(now);
            purchase.setStatus(purchaseStatus);
            purchaseRepository.save(purchase);

            //Comparing if data is going to be exhausted
            if (Double.compare((double) dataBought - dataUsed, BusinessConstants.MINIMUM_DATA_THRESHOLD_MB) < 0 || (session != null ? session.getFinishedAt() : null) != null) {
                log.info("Finish wifi service");
                return 2;
            }

            if (Double.compare(dataUsed, 0.9 * dataBought) > 0) {
                log.info("90% data consumed");
                return 1;
            }

            return 0;
        }

        return -1;
    }

    @Override
    @Transactional
    public WifiSummaryResponse findBuyerWifiSummary(Long purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId).orElse(null);
        if (purchase != null) return getBuyerWifiSummary(purchase);
        throw new ApplicationException(PurchaseErrorCodes.PURCHASE_UPDATE_NOT_LEGIT);
    }

    @Override
    @Transactional
    public WifiSummaryResponse finishBuyerWifiService(Long purchaseId, double dataUsed) {

        Purchase purchase = purchaseRepository.findById(purchaseId).orElse(null);
        PaymentProcessor paymentProcessor = new PaymentProcessor(PaymentGatewayCodes.RAZORPAY);

        if (PaymentValidatorUtils.isPurchaseUpdateLegit(purchase, dataUsed)) {
            int dataBought = purchase.getData();
            Date sessionFinishedAt = new Date(System.currentTimeMillis());
            double dataUsedBefore = purchase.getDataUsed();
            BigDecimal amountRefund = calculateBuyerRefundAmount(dataBought, dataUsed, purchase.getAmountPaid());
            BigDecimal sellerAmount = calculateSellerPaymentAmount(dataBought, dataUsed, dataUsedBefore, purchase.getAmountPaid());
            Session session = restTemplate.getForObject("", Session.class);
            User seller = session != null ? session.getSpeedTest().getUser() : null;
            boolean isUpdateTimeOnly = Double.compare(dataUsed, purchase.getDataUsed()) == 0;

            SellerPayment sellerPayment = seller != null ? sellerPaymentRepository.findSellerPaymentBySellerId(seller.getId()) : null;
            if (Double.compare(dataUsed, purchase.getDataUsed()) >= 0 && sellerPayment != null)
                sellerPaymentService.updateSellerPayment(seller, sellerPayment.getAmountEarned().add(sellerAmount), isUpdateTimeOnly);

            paymentProcessor.startBuyerRefund(purchaseRepository, amountRefund, purchase.getPaymentId());
            purchase.setStatus(purchase.getStatus());
            purchase.setDataUsed(dataUsed);
            purchase.setAmountRefund(amountRefund);
            purchase.setSessionFinishedAt(sessionFinishedAt);
            purchaseRepository.save(purchase);
            saveSessionWithWifiService(purchase);
            return getBuyerWifiSummary(purchase);
        }

        throw new ApplicationException(PurchaseErrorCodes.PURCHASE_UPDATE_NOT_LEGIT);

    }

    @Transactional
    @Override
    public List<WifiSummaryResponse> getSortedWifiUsagesDateTime(Long buyerId, int page, int size, boolean isDescending) {
        try {
            Pageable pageable = isDescending ?
                    PageRequest.of(page, size, Sort.by("payment_done_at").descending())
                    : PageRequest.of(page, size, Sort.by("payment_done_at"));
            return getWifiSummaryResponses(buyerId, pageable);
        } catch (Exception e) {
            log.error("Error occurred ", e);
            throw new ApplicationException(PurchaseErrorCodes.UNEXPECTED_PURCHASE_ERROR);
        }
    }

    @Transactional
    @Override
    public List<WifiSummaryResponse> getSortedWifiUsagesDataUsed(Long buyerId, int page, int size, boolean isDescending) {
        try {
            Pageable pageable = isDescending ?
                    PageRequest.of(page, size, Sort.by("data_used").descending())
                    : PageRequest.of(page, size, Sort.by("data_used"));
            return getWifiSummaryResponses(buyerId, pageable);
        } catch (Exception e) {
            log.error("Error occurred ", e);
            throw new ApplicationException(PurchaseErrorCodes.UNEXPECTED_PURCHASE_ERROR);
        }
    }

    //User defined functions
    private BigDecimal calculateBuyerRefundAmount(double dataBought, double dataUsed, BigDecimal amountPaid) {
        //Do not check for error types, simply calculate and return refund amount
        //return amountPaid - Math.ceil((amountPaid / dataBought) * dataUsed);
        return amountPaid
                .subtract
                        (PaymentUtils
                                .divideThenMultiplyFloorZeroScale(amountPaid, BigDecimal.valueOf(dataBought), BigDecimal.valueOf(dataUsed)));
    }

    private BigDecimal calculateSellerPaymentAmount(double dataBought, double dataUsed, double dataUsedBefore, BigDecimal amountPaid) {
        //Do not check for error types, simply calculate and return refund amount
        //return (amountPaid / dataBought) * (dataUsed - dataUsedBefore);
        return PaymentUtils
                .divideThenMultiplyFloorTwoScale(amountPaid, BigDecimal.valueOf(dataBought), BigDecimal.valueOf(dataUsed - dataUsedBefore));
    }

    private WifiSummaryResponse getBuyerWifiSummary(Purchase purchase) {
        //TODO add url
        Session session = restTemplate.getForObject("", Session.class);
        SpeedTest speedTest = session != null ? restTemplate.getForObject("", SpeedTest.class) : null;
        User seller = speedTest != null ? userRepository.findById(speedTest.getUser().getId()).orElse(null) : null;

        if (seller == null)
            throw new ApplicationException(UserErrorCodes.USER_NOT_FOUND);

        boolean isRefundStarted = purchase.getAmountRefund().compareTo(BigDecimal.ZERO) > 0 && purchase.getRefundPaymentId() != null;

        PaymentProcessor paymentProcessor = new PaymentProcessor(PaymentGatewayCodes.RAZORPAY);
        RefundReceiptResponse refundReceiptResponse = isRefundStarted ?
                paymentProcessor.getBuyerRefundStatus(purchaseRepository, purchase.getPaymentId()) :
                new RefundReceiptResponse(purchase, BusinessConstants.HOTIFI_BANK_ACCOUNT);

        WifiSummaryResponse wifiSummaryResponse = new WifiSummaryResponse();
        wifiSummaryResponse.setSellerUsername(seller.getUsername());
        wifiSummaryResponse.setSellerFullName(seller.getFirstName() + " " + seller.getLastName());
        wifiSummaryResponse.setSellerPhotoUrl(seller.getPhotoUrl());
        wifiSummaryResponse.setAmountPaid(purchase.getAmountPaid());
        wifiSummaryResponse.setAmountRefund(purchase.getAmountRefund());
        wifiSummaryResponse.setSessionStartedAt(purchase.getSessionCreatedAt());
        wifiSummaryResponse.setNetworkProvider(speedTest.getNetworkProvider());
        wifiSummaryResponse.setSessionFinishedAt(purchase.getSessionFinishedAt());
        wifiSummaryResponse.setDataBought(purchase.getData());
        wifiSummaryResponse.setDataUsed(purchase.getDataUsed());
        wifiSummaryResponse.setRefundReceiptResponse(refundReceiptResponse);

        //Below condition will save the entity only if complete payment with refund has not completed yet
        if (purchase.getStatus() % BusinessConstants.PAYMENT_METHOD_START_VALUE_CODE < BuyerPaymentCodes.values().length)
            purchaseRepository.save(purchase);

        return wifiSummaryResponse;
    }

    private void saveSessionWithWifiService(Purchase purchase) {
        //TODO Add url
        Session session = restTemplate.getForObject("", Session.class);
        if (session == null)
            throw new ApplicationException(PurchaseErrorCodes.SESSION_NOT_FOUND);
        purchase.getPurchaseOrder();
        //TODO try a logic for adding purchase order lists
        List<PurchaseOrder> purchaseOrders = null;
        session.setDataUsed(PaymentUtils.getDataUsedSumOfSession(purchaseOrders));
        session.setModifiedAt(purchase.getSessionModifiedAt());
        restTemplate.put("", session); //save session
    }

    private List<WifiSummaryResponse> getWifiSummaryResponses(Long buyerId, Pageable pageable) {
        List<Purchase> purchases = purchaseRepository.findPurchasesByBuyerId(buyerId, pageable);
        List<WifiSummaryResponse> wifiSummaryResponses = new ArrayList<>();
        for (Purchase purchase : purchases) {
            WifiSummaryResponse wifiSummaryResponse = getBuyerWifiSummary(purchase);
            wifiSummaryResponses.add(wifiSummaryResponse);
        }
        return wifiSummaryResponses;
    }

}
package com.hotifi.payment.services.implementations;

import com.hotifi.payment.repositories.PurchaseRepository;
import com.hotifi.payment.repositories.SellerPaymentRepository;
import com.hotifi.payment.services.interfaces.IStatsService;
import com.hotifi.payment.web.responses.BuyerStatsResponse;
import com.hotifi.payment.web.responses.SellerStatsResponse;
import com.hotifi.user.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatsServiceImpl implements IStatsService {

    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;
    //private final SessionRepositor sessionRepository;
    private final SellerPaymentRepository sellerPaymentRepository;

    public StatsServiceImpl(UserRepository userRepository, PurchaseRepository purchaseRepository, SellerPaymentRepository sellerPaymentRepository) {
        this.userRepository = userRepository;
        this.purchaseRepository = purchaseRepository;
        //this.sessionRepository = sessionRepository;
        this.sellerPaymentRepository = sellerPaymentRepository;
    }

    @Override
    public BuyerStatsResponse getBuyerStats(Long buyerId) {
        return null;
    }

    @Override
    public SellerStatsResponse getSellerStats(Long sellerId) {
        return null;
    }

    /*@Transactional(readOnly = true)
    @Override
    public BuyerStatsResponse getBuyerStats(Long buyerId) {
        try {
            User buyer = userRepository.findById(buyerId).orElse(null);
            if (LegitUtils.isUserLegit(buyer)) {
                Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("session_created_at").descending());
                Supplier<Stream<Purchase>> purchaseStreamSupplier = () -> purchaseRepository.findPurchasesByBuyerId(buyerId, pageable).stream();
                Date currentTime = new Date(System.currentTimeMillis());
                BigDecimal totalPendingRefunds =
                        purchaseStreamSupplier.get()
                                .filter(purchase -> purchase.getStatus() % BusinessConfigurations.PAYMENT_METHOD_START_VALUE_CODE < BuyerPaymentCodes.REFUND_PENDING.value() && !PaymentUtils.isBuyerRefundDue(currentTime, purchase.getPaymentDoneAt()))
                                .map(Purchase::getAmountRefund)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalRefundsProcessed =
                        purchaseStreamSupplier.get()
                                .filter(purchase -> purchase.getStatus() % BusinessConfigurations.PAYMENT_METHOD_START_VALUE_CODE == BuyerPaymentCodes.REFUND_PROCESSED.value())
                                .map(Purchase::getAmountRefund)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                double totalDataBought = purchaseStreamSupplier.get().mapToDouble(Purchase::getDataUsed).sum();
                String wifi = NetworkProviderCodes.fromInt(1).name();
                double totalDataBoughtByWifi = purchaseStreamSupplier.get().filter(purchase -> purchase.getPurchaseOrder().getSession().getSpeedTest().getNetworkProvider().equals(wifi))
                        .mapToDouble(Purchase::getDataUsed)
                        .sum();
                double totalDataBoughtByMobile = totalDataBought - totalDataBoughtByWifi;
                return new BuyerStatsResponse(totalPendingRefunds, totalRefundsProcessed, totalDataBought, totalDataBoughtByWifi, totalDataBoughtByMobile);
            } else
                throw new ApplicationException(PurchaseErrorCodes.BUYER_NOT_LEGIT);
        } catch (Exception e) {
            log.error("Error Occurred", e);
            throw new ApplicationException(UserStatusErrorCodes.UNEXPECTED_USER_STATUS_ERROR);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public SellerStatsResponse getSellerStats(Long sellerId) {
        User seller = userRepository.findById(sellerId).orElse(null);
        if (seller == null || seller.getAuthentication().isDeleted())
            throw new ApplicationException(SellerPaymentErrorCodes.SELLER_NOT_LEGIT);
        SellerPayment sellerPayment = sellerPaymentRepository.findSellerPaymentBySellerId(sellerId);
        try {
            if (sellerPayment == null)
                return new SellerStatsResponse(0L, null, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0, 0);

            BigDecimal totalEarnings = sellerPayment
                    .getAmountEarned()
                    .multiply(BigDecimal.valueOf((double) (100 - BusinessConfigurations.COMMISSION_PERCENTAGE) / 100))
                    .setScale(2, RoundingMode.FLOOR);
            BigDecimal totalAmountWithdrawn = sellerPayment.getAmountPaid();
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("created_at").descending());

            List<Long> speedTestIds = seller.getSpeedTests()
                    .stream().map(SpeedTest::getId)
                    .collect(Collectors.toList());
            List<Long> sessionIds = sessionRepository.findSessionsBySpeedTestIds(speedTestIds, pageable)
                    .stream().map(Session::getId)
                    .collect(Collectors.toList());
            Supplier<Stream<Purchase>> purchaseStreamSupplier = () -> purchaseRepository.findPurchasesBySessionIds(sessionIds).stream();

            double totalDataSold = purchaseStreamSupplier.get().mapToDouble(Purchase::getDataUsed).sum();
            String wifi = NetworkProviderCodes.fromInt(1).name();
            double totalDataSoldByWifi = purchaseStreamSupplier.get().filter(purchase -> purchase.getPurchaseOrder().getSession().getSpeedTest().getNetworkProvider().equals(wifi))
                    .mapToDouble(Purchase::getDataUsed)
                    .sum();
            double totalDataSoldByMobile = totalDataSold - totalDataSoldByWifi;
            return new SellerStatsResponse(sellerPayment.getId(), sellerPayment.getLastPaidAt(), totalEarnings, totalAmountWithdrawn, totalDataSold, totalDataSoldByWifi, totalDataSoldByMobile);
        } catch (Exception e) {
            log.error("Error Occurred", e);
            throw new ApplicationException(UserStatusErrorCodes.UNEXPECTED_USER_STATUS_ERROR);
        }
    }*/
}

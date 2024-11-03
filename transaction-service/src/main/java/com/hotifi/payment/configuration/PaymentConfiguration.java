package com.hotifi.payment.configuration;

import com.hotifi.common.services.interfaces.IEmailService;
import com.hotifi.payment.repositories.*;
import com.hotifi.payment.services.implementations.BankAccountServiceImpl;
import com.hotifi.payment.services.implementations.PaymentServiceImpl;
import com.hotifi.payment.services.implementations.PurchaseServiceImpl;
import com.hotifi.payment.services.implementations.StatsServiceImpl;
import com.hotifi.payment.services.interfaces.IBankAccountService;
import com.hotifi.payment.services.interfaces.IPaymentService;
import com.hotifi.payment.services.interfaces.IPurchaseService;
import com.hotifi.payment.services.interfaces.IStatsService;
import com.hotifi.session.entities.Session;
import com.hotifi.session.repositories.SessionRepository;
import com.hotifi.speedtest.repositories.SpeedTestRepository;
import com.hotifi.user.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

@Configuration
@EnableJpaRepositories(basePackages = {"com.hotifi.payment.repositories"})
public class PaymentConfiguration {

    @Bean
    public IPaymentService paymentService(SellerPaymentRepository sellerPaymentRepository, SellerReceiptRepository sellerReceiptRepository, UserRepository userRepository, PurchaseRepository purchaseRepository){
        return new PaymentServiceImpl(sellerPaymentRepository, sellerReceiptRepository, userRepository, purchaseRepository);
    }

    @Bean
    public IBankAccountService bankAccountService(UserRepository userRepository, BankAccountRepository bankAccountRepository, SellerPaymentRepository sellerPaymentRepository, IEmailService emailService){
        return new BankAccountServiceImpl(userRepository, bankAccountRepository, sellerPaymentRepository, emailService);
    }

    @Bean
    public IPurchaseService purchaseService(UserRepository userRepository, PurchaseOrderRepository purchaseOrderRepository, PurchaseRepository purchaseRepository, SellerPaymentRepository sellerPaymentRepository, IPaymentService sellerPaymentService){
        return new PurchaseServiceImpl(userRepository, purchaseOrderRepository, purchaseRepository, sellerPaymentRepository, sellerPaymentService);
    }

    @Bean
    public IStatsService statsService(UserRepository userRepository, PurchaseRepository purchaseRepository, SellerPaymentRepository sellerPaymentRepository){
        return new StatsServiceImpl(userRepository, purchaseRepository, sellerPaymentRepository);
    }
}

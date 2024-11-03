package com.hotifi.payment.services.implementations;

import com.google.api.client.util.Value;
import com.hotifi.common.services.interfaces.IEmailService;
import com.hotifi.payment.entities.BankAccount;
import com.hotifi.payment.repositories.BankAccountRepository;
import com.hotifi.payment.repositories.SellerPaymentRepository;
import com.hotifi.payment.services.interfaces.IBankAccountService;
import com.hotifi.payment.web.request.BankAccountRequest;
import com.hotifi.payment.web.responses.BankAccountAdminResponse;
import com.hotifi.user.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class BankAccountServiceImpl implements IBankAccountService {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final SellerPaymentRepository sellerPaymentRepository;
    private final IEmailService emailService;

    @Value("${email.no-reply-address}")
    private static String noReplyEmailAddress;

    @Value("${email.no-reply-password}")
    private static String noReplyEmailPassword;

    public BankAccountServiceImpl(UserRepository userRepository, BankAccountRepository bankAccountRepository, SellerPaymentRepository sellerPaymentRepository, IEmailService emailService) {
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.sellerPaymentRepository = sellerPaymentRepository;
        this.emailService = emailService;
    }

    @Override
    public void addBankAccount(BankAccountRequest bankAccountRequest) {

    }

    @Override
    public void addUpiId(Long userId, String upiId) {

    }

    @Override
    public void updateBankAccountByCustomer(BankAccountRequest bankAccountRequest) {

    }

    @Override
    public void updateUpiIdByCustomer(Long userId, String upiId) {

    }

    @Override
    public void updateBankAccountByAdmin(Long sellerId, String linkedAccountId, String errorDescription) {

    }

    @Override
    public BankAccount getBankAccountByUserId(Long sellerId) {
        return null;
    }

    @Override
    public List<BankAccountAdminResponse> getUnlinkedBankAccounts() {
        return null;
    }

    /*@Transactional
    @Override
    public void addBankAccount(BankAccountRequest bankAccountRequest) {
        if (bankAccountRequest.getErrorDescription() != null)
            throw new ApplicationException(SellerBankAccountErrorCodes.ERROR_DESCRIPTION_ON_CREATION_BY_SELLER);
        User seller = userRepository.findById(bankAccountRequest.getUserId()).orElse(null);
        if (seller == null)
            throw new ApplicationException(UserErrorCodes.USER_NOT_FOUND);
        if (seller.getAuthentication().isDeleted())
            throw new ApplicationException(UserErrorCodes.USER_DELETED);
        if (!seller.isLoggedIn())
            throw new ApplicationException(UserErrorCodes.USER_NOT_LOGGED_IN);
        try {
            BankAccount bankAccount = new BankAccount();
            bankAccount.setBankAccountType(bankAccountRequest.getBankAccountType());
            bankAccount.setAccountType(bankAccountRequest.getAccountType());
            bankAccount.setBankIfscCode(bankAccountRequest.getBankIfscCode());
            bankAccount.setBankAccountNumber(bankAccountRequest.getBankAccountNumber());
            bankAccount.setBankBeneficiaryName(bankAccountRequest.getBankBeneficiaryName());
            bankAccount.setUser(seller);
            seller.setBankAccount(bankAccount);
            bankAccountRepository.save(bankAccount);
            userRepository.save(seller);
        } catch (DataIntegrityViolationException e) {
            throw new ApplicationException(SellerBankAccountErrorCodes.BANK_ACCOUNT_DETAILS_ALREADY_EXISTS);
        } catch (Exception e) {
            throw new ApplicationException(SellerBankAccountErrorCodes.UNEXPECTED_SELLER_BANK_ACCOUNT_ERROR);
        }
    }

    @Transactional
    @Override
    public void addUpiId(Long userId, String upiId) {
        User seller = userRepository.findById(userId).orElse(null);
        if (seller == null)
            throw new ApplicationException(UserErrorCodes.USER_NOT_FOUND);
        if (seller.getAuthentication().isDeleted())
            throw new ApplicationException(UserErrorCodes.USER_DELETED);
        if (!seller.isLoggedIn())
            throw new ApplicationException(UserErrorCodes.USER_NOT_LOGGED_IN);
        if (!upiId.matches(BusinessConfigurations.VALID_UPI_PATTERN))
            throw new ApplicationException(UserErrorCodes.USER_UPI_ID_INVALID);
        try {
            seller.setUpiId(upiId);
            userRepository.save(seller);
        } catch (DataIntegrityViolationException e) {
            throw new ApplicationException(SellerBankAccountErrorCodes.BANK_ACCOUNT_DETAILS_ALREADY_EXISTS);
        } catch (Exception e) {
            throw new ApplicationException(SellerBankAccountErrorCodes.UNEXPECTED_SELLER_BANK_ACCOUNT_ERROR);
        }
    }

    @Transactional
    @Override
    public void updateUpiIdByCustomer(Long userId, String upiId) {
        User seller = userRepository.findById(userId).orElse(null);
        boolean isSellerLegit = AppConfigurations.DIRECT_TRANSFER_API_ENABLED ?
                LegitUtils.isSellerLegit(seller, false) : LegitUtils.isSellerUpiLegit(seller, false);
        boolean isWithdrawalClaimNotified = sellerPaymentRepository.findSellerPaymentBySellerId(userId).isWithdrawalClaimNotified();
        if (isWithdrawalClaimNotified) {
            throw new ApplicationException(UserErrorCodes.USER_UPI_ID_LOCKED);
        }
        if (isSellerLegit) {
            try {
                seller.setUpiId(upiId);
                userRepository.save(seller);
                return;
            } catch (Exception e) {
                throw new ApplicationException(UserErrorCodes.USER_UPI_ID_UPDATE_FAILED);
            }
        }
        throw new ApplicationException(UserErrorCodes.USER_NOT_LEGIT);
    }

    @Transactional
    @Override
    public void updateBankAccountByCustomer(BankAccountRequest bankAccountRequest) {
        User seller = userRepository.findById(bankAccountRequest.getUserId()).orElse(null);
        boolean isSellerLegit = AppConfigurations.DIRECT_TRANSFER_API_ENABLED ?
                LegitUtils.isSellerLegit(seller, false) : LegitUtils.isSellerUpiLegit(seller, false);
        if (isSellerLegit) {
            try {
                Date modifiedAt = new Date(System.currentTimeMillis());
                BankAccount bankAccount = seller.getBankAccount();
                bankAccount.setUser(seller);
                bankAccount.setBankAccountType(bankAccountRequest.getBankAccountType());
                bankAccount.setModifiedAt(modifiedAt);
                bankAccount.setAccountType(bankAccountRequest.getAccountType());
                bankAccount.setBankIfscCode(bankAccountRequest.getBankIfscCode());
                bankAccount.setBankAccountNumber(bankAccountRequest.getBankAccountNumber());
                bankAccount.setBankBeneficiaryName(bankAccountRequest.getBankBeneficiaryName());
                bankAccount.setLinkedAccountId(null);
                bankAccountRepository.save(bankAccount);
                return;
            } catch (DataIntegrityViolationException e) {
                throw new ApplicationException(SellerBankAccountErrorCodes.BANK_ACCOUNT_DETAILS_ALREADY_EXISTS);
            } catch (Exception e) {
                throw new ApplicationException(SellerBankAccountErrorCodes.UNEXPECTED_SELLER_BANK_ACCOUNT_ERROR);
            }
        }
        throw new ApplicationException(UserErrorCodes.USER_NOT_LEGIT);
    }

    @Transactional
    @Override
    public void updateBankAccountByAdmin(Long userId, String linkedAccountId, String errorDescription) {
        User user = userRepository.findById(userId).orElse(null);
        if (LegitUtils.isSellerLegitByAdmin(user, linkedAccountId, errorDescription)) {
            try {
                Date modifiedAt = new Date(System.currentTimeMillis());
                BankAccount bankAccount = user.getBankAccount();
                bankAccount.setLinkedAccountId(linkedAccountId);
                bankAccount.setModifiedAt(modifiedAt);
                bankAccount.setErrorDescription(errorDescription);
                bankAccount.setUser(user);
                bankAccountRepository.save(bankAccount);

                EmailModel emailModel = new EmailModel();
                emailModel.setToEmail(user.getAuthentication().getEmail());
                emailModel.setFromEmail(noReplyEmailAddress);
                emailModel.setFromEmailPassword(noReplyEmailPassword);

                if (errorDescription != null) {
                    emailService.sendLinkedAccountFailed(user, errorDescription, emailModel);
                    return;
                }
                emailService.sendLinkedAccountSuccessEmail(user, emailModel);
                return;

            } catch (DataIntegrityViolationException e) {
                throw new ApplicationException(SellerBankAccountErrorCodes.BANK_ACCOUNT_DETAILS_ALREADY_EXISTS);
            } catch (Exception e) {
                throw new ApplicationException(SellerBankAccountErrorCodes.UNEXPECTED_SELLER_BANK_ACCOUNT_ERROR);
            }
        }
        throw new ApplicationException(UserErrorCodes.USER_NOT_LEGIT);
    }

    @Transactional
    @Override
    public BankAccount getBankAccountByUserId(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null ? user.getBankAccount() : null;
    }

    @Transactional
    @Override
    public List<BankAccountAdminResponse> getUnlinkedBankAccounts() {
        List<BankAccount> bankAccounts = bankAccountRepository.findUnverifiedBankAccounts();
        return bankAccounts.
                stream()
                .map(bankAccount -> new BankAccountAdminResponse(bankAccount, bankAccount.getUser().getId(),
                        bankAccount.getUser().getAuthentication().getEmail()))
                .collect(Collectors.toList());
    }*/
}
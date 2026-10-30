package com.banking.accountservice.service;


import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountStatus;
import com.banking.accountservice.entity.AccountType;
import com.banking.accountservice.repository.AccountRespository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {

    private final AccountRespository accountRespository;
    private static SecureRandom secureRandom = new SecureRandom();

    public AccountResponse createAccount(CreateAccountRequest request)
    {
        log.info("Creating Account for : {} ",request.getEmail());

        if(accountRespository.existByEmail(request.getEmail()))
        {
            throw new RuntimeException("Account already exists for email : "+request.getEmail());
        }


        Account account = new Account();
        account.setAccountHolderName(request.getAccountHolderName());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setAccountType(account.getAccountType());
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setBalance(request.getInitialDeposite());
        account.setAccountNumber(generateAccountNumber());
        account.setDailyTransactionLimit(
                request.getAccountType() == AccountType.SAVINGS
                        ? new BigDecimal("100000")
                        : new BigDecimal("500000"));

        Account savedAccount = accountRespository.save(account);

        log.info("Account created successfully for : {} ",savedAccount.getAccountNumber());

        return mapToAccountResponse(savedAccount);

    }

    /**
     * Get aaacunt by Account Nunber
     * @param accountNumber
     * @return
     */

   public AccountResponse getAccount(String accountNumber)
    {
    Account account = accountRespository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found for account number : "+accountNumber));
    return mapToAccountResponse(account);
    }

    /**
     * Get account balance by account number
     * @param accountNumber
     * @return
     */
    public BigDecimal getBalance(String accountNumber)
    {
        Account account = accountRespository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found for account number : "+accountNumber));
        return account.getBalance();
    }

    /**
     *Block account -- called by Fraud detection service via Kafka
     * @param accountNumber
     */

    public void blockAccount(String accountNumber)
    {
        log.info("Blocking account : {} ",accountNumber);
        Account account = accountRespository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found for account number : "+accountNumber));
        account.setAccountStatus(AccountStatus.BLOCKED);
        accountRespository.save(account);
        log.info("Account blocked  : {} ",accountNumber);

    }

    /**
     * Deduct Balance from sender account
     * called By Transaction Service
     * @param accountNumber
     * @param amount
     */

    public void deductBalance(String accountNumber, BigDecimal amount) {
        log.info("Deducting balance from account : {} , amount : {} ", accountNumber, amount);
        Account account = accountRespository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found for account number : " + accountNumber));

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active for account number : " + accountNumber);
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance for account number : " + accountNumber);
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRespository.save(account);
        log.info("Balance updated. New Balance : {} , amount : {} ", account.getBalance());
    }

    /**
     * Credit balance
     * Called by Transaction Service Via Kfka
     * @param accountNumber
     * @param amount
     */

    public void creditBalance(String accountNumber, BigDecimal amount)
    {
    log.info("Crediting balance to account : {} , amount : {} ", accountNumber, amount);
    Account account = accountRespository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found for account number : " + accountNumber));
    account.setBalance(account.getBalance().add(amount));
    accountRespository.save(account);
    log.info("Balance Created. New Balance : {} , amount : {} ", account.getBalance());
    }


    //Generate Unique 12 digit account number
    private String generateAccountNumber()
    {
        String accountNumber;
        do {
            long number = secureRandom.nextLong(1_000_000_000_000L);
            accountNumber = String.format("%012d", number);
        }
        while (accountRespository.existByAccountNumber(accountNumber));
        return accountNumber;
    }

    private AccountResponse mapToAccountResponse(Account account)
    {
    AccountResponse response = new AccountResponse();
    response.setId(account.getId());
    response.setAccountNumber(account.getAccountNumber());
    response.setAccountHolderName(account.getAccountHolderName());
    response.setEmail(account.getEmail());
    response.setPhone(account.getPhone());
    response.setAccountType(account.getAccountType());
    response.setAccountStatus(account.getAccountStatus());
    response.setBalance(account.getBalance());
    response.setDailyTransactionLimit(account.getDailyTransactionLimit());
    response.setCreatedAt(account.getCreatedAt());
    return response;
    }

}

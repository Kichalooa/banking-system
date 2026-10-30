package com.banking.accountservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
public class AccountEventConsumer {
    AccountService accountService;


    /**
     *  Consume transaction.completed Event from Kafka
     *  credits receiver account with the amount
     * @param payload
     */

    @KafkaListener(topics = "transaction.completed")
    public void consumeTransactionCompleted(@Payload Map<String, Object> payload)
    {
        try
        {
            String receiverAccount = (String) payload.get("receiverAccountNumber");
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());

            log.info("Crediting account : {} amount: {}", receiverAccount,amount);
            accountService.creditBalance(receiverAccount, amount);
        }
        catch (Exception e)
        {
            log.error("Error while processing transaction completed event: {}", e.getMessage());
        }
    }

    /**
     *  consume fraud.detected event from kafka
     *  blocks the account
     * @param payload
     */
    @KafkaListener(topics ="fraud.detected")
    public void consumeFraudDetected(@Payload Map<String, Object> payload)
    {
        try
        {
            String accountNumber = (String) payload.get("accountNumber");
            log.info("Blocking account : {} due to fraud detection", accountNumber);
            accountService.blockAccount(accountNumber);
        }
        catch (Exception e)
        {
            log.error("Error while processing fraud detected event: {}", e.getMessage());
        }
    }

}

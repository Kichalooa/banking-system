package com.banking.accountservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountRequest {

    @NotBlank(message = "Account holder name is required")
    private String accountHolderName;


    @NotBlank(message = "Email is required")
    private String email;


    @NotBlank(message = "Phone number is required")
    private String phone;


    @NotNull(message ="Account type is required")
    private String accountType;

    @NotNull(message ="Initial deposite is required")
    @Positive(message = "Initial deposit must be a positive value")
    private BigDecimal InitialDeposite;
}

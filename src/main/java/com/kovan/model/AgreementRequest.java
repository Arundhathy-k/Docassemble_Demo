package com.kovan.model;

import lombok.Data;

@Data
public class AgreementRequest {
    private String agreement_date;
    private String first_name;
    private String last_name;
    private String address;
    private String phone;
}


package com.kovan.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AgreementRequest {

    @JsonProperty("agreement_date")
    private String agreementDate;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String address;
    private String phone;

    /* ---------- Getters ---------- */

    public String getAgreementDate() {
        return agreementDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    /* ---------- Optional setters (needed for Jackson) ---------- */

    public void setAgreementDate(String agreementDate) {
        this.agreementDate = agreementDate;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

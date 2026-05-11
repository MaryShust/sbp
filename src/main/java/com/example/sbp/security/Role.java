package com.example.sbp.security;

import lombok.Getter;
import java.util.Arrays;

@Getter
public enum Role {
    /*
     * Privilege.ACCOUNT_READ,
     * Privilege.ACCOUNT_READ_BY_PHONE,
     * Privilege.ACCOUNT_ACTIVATE,
     * Privilege.BILL_CREATE,
     * Privilege.BILL_READ,
     * Privilege.BILL_READ_DEFAULT,
     * Privilege.BILL_REPLENISH,
     * Privilege.PAYMENT_CREATE,
     * Privilege.PAYMENT_READ_STATUS
     */
    USER,

    /*
     * Privilege.ACCOUNT_CREATE,
     * Privilege.ACCOUNT_SUPER_READ,
     * Privilege.BILL_SUPER_READ,
     * Privilege.PAYMENT_SUPER_READ_STATUS
     */
    MANAGER,

    /*
     * Privilege.USER_MANAGE_ROLES
     */
    ADMIN;

    public static Role fromString(String name) {
        return Arrays.stream(values())
                .filter(r -> r.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + name));
    }
}

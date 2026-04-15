package com.example.sbp.security;

import lombok.Getter;
import java.util.Arrays;
import java.util.Set;

@Getter
public enum Role {
//    "Пользователь"
    USER(Set.of(
            Privilege.ACCOUNT_READ,
            Privilege.ACCOUNT_READ_BY_PHONE,
            Privilege.ACCOUNT_ACTIVATE,
            Privilege.BILL_CREATE,
            Privilege.BILL_READ,
            Privilege.BILL_READ_DEFAULT,
            Privilege.BILL_REPLENISH,
            Privilege.PAYMENT_CREATE,
            Privilege.PAYMENT_READ_STATUS
    )),

//    "Менеджер"
    MANAGER(Set.of(
            Privilege.ACCOUNT_CREATE,
            Privilege.ACCOUNT_READ,
            Privilege.ACCOUNT_READ_BY_PHONE,
            Privilege.BILL_READ,
            Privilege.BILL_READ_DEFAULT,
            Privilege.PAYMENT_READ_STATUS
    )),

//    "Администратор", имеет права только на управление ролями, доступ ко всем персональны данным пользователей запрещен
    ADMIN(Set.of(
            Privilege.USER_MANAGE_ROLES
    ));

    private final Set<Privilege> privileges;

    Role(Set<Privilege> privileges) {
        this.privileges = privileges;
    }

    public static Role fromString(String name) {
        return Arrays.stream(values())
                .filter(r -> r.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + name));
    }
}

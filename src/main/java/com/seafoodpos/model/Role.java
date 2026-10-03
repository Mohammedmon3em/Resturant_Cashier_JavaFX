package com.seafoodpos.model;

public enum Role {
    ADMIN, MODERATOR, CASHIER;

    public boolean canManageUsers()   { return this == ADMIN; }
    public boolean canManageMenu()    { return this == ADMIN || this == MODERATOR; }
    public boolean canViewAllReports(){ return this == ADMIN || this == MODERATOR; }
    public boolean canAccessSettings(){ return this == ADMIN || this == MODERATOR; }

    public String getDisplayName() {
        return switch (this) {
            case ADMIN     -> "Admin";
            case MODERATOR -> "Moderator";
            case CASHIER   -> "Cashier";
        };
    }

    public String getArabicName() {
        return switch (this) {
            case ADMIN     -> "\u0645\u062f\u064a\u0631";
            case MODERATOR -> "\u0645\u0634\u0631\u0641";
            case CASHIER   -> "\u0643\u0627\u0634\u064a\u0631";
        };
    }

    public String getBilingualName() {
        return getDisplayName() + " / " + getArabicName();
    }
}

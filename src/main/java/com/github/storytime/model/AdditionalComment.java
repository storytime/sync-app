package com.github.storytime.model;

/**
 * Values is saved in db by name, so names changed not allowed
 */
public final class AdditionalComment {
    public static final String NBU_PREV_MOUTH_LAST_BUSINESS_DAY = "NBU_PREV_MOUTH_LAST_BUSINESS_DAY";
    public static final String PB_CURRENT_BUSINESS_DAY = "PB_CURRENT_BUSINESS_DAY";

    private AdditionalComment() {
        throw new IllegalStateException("Utility class");
    }
}

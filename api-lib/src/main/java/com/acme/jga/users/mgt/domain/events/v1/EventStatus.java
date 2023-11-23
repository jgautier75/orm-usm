package com.acme.jga.users.mgt.domain.events.v1;

public enum EventStatus {
    PENDING(0),
    PROCESSED(1),
    FINISHED(2),
    FAILED(3);

    private Integer value;

    private EventStatus(Integer aValue) {
        this.value = aValue;
    }

    public Integer getValue() {
        return this.value;
    }

    public static EventStatus fromValue(Integer aValue) {
        if (aValue != null && aValue.equals(0)) {
            return PENDING;
        } else if (aValue != null && aValue.equals(1)) {
            return PROCESSED;
        } else if (aValue != null && aValue.equals(2)) {
            return FINISHED;
        } else if (aValue != null && aValue.equals(3)) {
            return FAILED;
        } else {
            throw new IllegalArgumentException("Unknown eventScope value [" + aValue + "]");
        }
    }

}

package com.example.booking.cache;

public interface AvailabilityCacheService {

    long getAvailableUnitsCount();

    void setAvailableUnits(long actualValue);

    void incrementAvailableUnits();

    void decrementAvailableUnits();
}

package com.example.booking.cache;

public interface AvailabilityCacheService {

    long getAvailableUnitsCount();

    void incrementAvailableUnits();

    void decrementAvailableUnits();

    void rebuildAvailableUnits(long actualValue);
}

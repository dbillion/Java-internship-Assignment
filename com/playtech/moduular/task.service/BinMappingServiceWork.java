package com.playtech.assignment.moduular.task.service;

import com.playtech.assignment.moduular.task.model.BinMapping;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public  class
BinMappingServiceWork implements BinMappingService{
    @Override
    public   Optional<BinMapping> findBinMapping(TreeMap<Long, BinMapping> binMappingMap, String accountNumber) {
        try {
            long accountNumberLong = Long.parseLong(accountNumber);
            return Optional.ofNullable(binMappingMap.floorEntry(accountNumberLong)).map(Map.Entry::getValue);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

}
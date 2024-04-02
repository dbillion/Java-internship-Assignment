package com.playtech.assignment.moduular.task.service;

import com.playtech.assignment.moduular.task.model.BinMapping;

import java.util.Optional;
import java.util.TreeMap;

public  interface BinMappingService {
     Optional<BinMapping> findBinMapping(TreeMap<Long, BinMapping> binMappingMap, String accountNumber);
}
package com.metionlabs.stocksimulator.repository;

import com.metionlabs.stocksimulator.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {


}

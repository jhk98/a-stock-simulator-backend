package com.metionlabs.stocksimulator.repository;

import com.metionlabs.stocksimulator.domain.DailyPrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyPriceRepository extends JpaRepository<DailyPrice, Long> {



}

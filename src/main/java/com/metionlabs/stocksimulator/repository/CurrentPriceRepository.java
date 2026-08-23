package com.metionlabs.stocksimulator.repository;

import com.metionlabs.stocksimulator.domain.CurrentPrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrentPriceRepository extends JpaRepository<CurrentPrice, Long> {



}

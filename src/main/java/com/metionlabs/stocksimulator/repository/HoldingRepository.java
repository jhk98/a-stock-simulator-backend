package com.metionlabs.stocksimulator.repository;

import com.metionlabs.stocksimulator.domain.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, Long> {


}

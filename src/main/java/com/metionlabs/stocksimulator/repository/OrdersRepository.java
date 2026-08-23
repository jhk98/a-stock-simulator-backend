package com.metionlabs.stocksimulator.repository;

import com.metionlabs.stocksimulator.domain.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders, Long> {



}

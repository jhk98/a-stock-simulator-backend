package com.metionlabs.stocksimulator.repository;

import com.metionlabs.stocksimulator.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

//    List<Member> findByName(String name);

}

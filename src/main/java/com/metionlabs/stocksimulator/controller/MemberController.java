package com.metionlabs.stocksimulator.controller;

import com.metionlabs.stocksimulator.domain.Member;
import com.metionlabs.stocksimulator.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/members")
public class MemberController {

    @Autowired
    MemberRepository memberRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Member> get(@PathVariable Long id) {
        Member member = memberRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(member);
    }

    @PostMapping("")
    public ResponseEntity<?> post(@RequestBody Member member) {
        LocalDateTime localDateTime = LocalDateTime.now();
        member.setCreatedAt(localDateTime);
        member.setModifiedAt(localDateTime);
        memberRepository.save(member);

//        return new ResponseEntity<>(HttpStatus.OK);
        return ResponseEntity.ok("created");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(@PathVariable Long id) {
//        return new ResponseEntity<>(HttpStatus.OK);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
//        return new ResponseEntity<>(HttpStatus.OK);
        return ResponseEntity.ok(id);
    }
}
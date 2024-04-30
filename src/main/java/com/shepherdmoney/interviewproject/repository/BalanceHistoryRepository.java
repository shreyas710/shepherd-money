package com.shepherdmoney.interviewproject.repository;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("BalanceHistoryRepo")
public interface BalanceHistoryRepository extends JpaRepository<BalanceHistory, Integer> {
    List<BalanceHistory> findAllByCard(CreditCard card);
}

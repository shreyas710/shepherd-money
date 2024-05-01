package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.BalanceHistoryRepository;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@EnableTransactionManagement
public class CreditCardController {

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BalanceHistoryRepository balanceHistoryRepository;

    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        try {
            CreditCard checkExistingCard = creditCardRepository.findByNumber(payload.getCardNumber());
            if (checkExistingCard != null && checkExistingCard.getUser().getId() == payload.getUserId()) {
                // return 400 if credit card number already exists for the given user
                System.out.println("The following card number already exists for given user");
                return ResponseEntity.badRequest().body(400);
            }
            User user = userRepository.findById(payload.getUserId()).orElse(null);
            if (user == null) {
                // return 400 if user does not exist
                System.out.println("The following user does not exist");
                return ResponseEntity.badRequest().body(400);
            }
            CreditCard newCard = new CreditCard();
            newCard.setUser(user);
            newCard.setIssuanceBank(payload.getCardIssuanceBank());
            newCard.setNumber(payload.getCardNumber());
            creditCardRepository.save(newCard);
            return ResponseEntity.ok(newCard.getId());
        } catch(Exception e) {
            // return 500 error if there is an exception
            System.out.println("Error adding credit card: " + e);
            return ResponseEntity.badRequest().body(500);
        }
    }

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        try {
            List<CreditCardView> cards = new ArrayList<>();
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                // returning empty array list if user does not exist
                System.out.println("The following user does not exist");
                return ResponseEntity.badRequest().body(cards);
            }
            creditCardRepository.findAllByUser(user).forEach(card -> {
                CreditCardView cv = new CreditCardView(card.getIssuanceBank(), card.getNumber());
                cards.add(cv);
            });
            return ResponseEntity.ok(cards);
        } catch(Exception e) {
            // return 500 error if there is an exception
            System.out.println("Error getting all credit cards: " + e);
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
    }

    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        try {
            CreditCard card = creditCardRepository.findByNumber(creditCardNumber);
            if (card == null || card.getUser() == null) {
                // returning 400 if card does not exist or card exists but not associated with a user
                System.out.println("Invalid card number");
                return ResponseEntity.badRequest().body(400);
            }
            return ResponseEntity.ok(card.getUser().getId());
        } catch(Exception e) {
            // return 500 error if there is an exception
            System.out.println("Error getting user id for credit card: " + e);
            return ResponseEntity.badRequest().body(500);
        }
        
    }

    static int upper_bound(List<BalanceHistory> arr, LocalDate key)  { 
        int l = 0, h = arr.size();
        while (l < h && l != arr.size()) {
            int mid = l + (h - l) / 2;
            if (arr.get(mid).getDate().isBefore(key)) {
                l = mid + 1;
            } else {
                h = mid;
            }
        }
        return l == arr.size() ? -1 : l;
    } 

    @PostMapping("/credit-card:update-balance")
    @Transactional
    public ResponseEntity<Integer> postMethodName(@RequestBody UpdateBalancePayload[] payload) {

        // this will be a transactional operation
        
        try {
            Map<String, List<BalanceHistory>> currBalHistoryFromPayload = new HashMap<>();
            for (UpdateBalancePayload updateBalancePayload : payload) {
                CreditCard card = creditCardRepository.findByNumber(updateBalancePayload.getCreditCardNumber());
                if (card == null) {
                    // return 400 if card number is not associated with a card
                    System.out.println("Invalid card number");
                    return ResponseEntity.badRequest().body(400);
                }
                BalanceHistory balanceHistory = new BalanceHistory();
                balanceHistory.setCard(card);
                balanceHistory.setDate(updateBalancePayload.getBalanceDate());
                balanceHistory.setBalance(updateBalancePayload.getBalanceAmount());
                if (currBalHistoryFromPayload.containsKey(updateBalancePayload.getCreditCardNumber())) {
                    currBalHistoryFromPayload.get(updateBalancePayload.getCreditCardNumber()).add(balanceHistory);
                } else {
                    List<BalanceHistory> balanceHistories = new ArrayList<>();
                    balanceHistories.add(balanceHistory);
                    currBalHistoryFromPayload.put(updateBalancePayload.getCreditCardNumber(), balanceHistories);
                }
            }

            // currBalHistoryFromPayload STORES ALL THE BALANCE HISTORY OF ALL THE CREDIT CARDS
            for (Map.Entry<String, List<BalanceHistory>> entry : currBalHistoryFromPayload.entrySet()) {

                // Current balance histories for card number
                List<BalanceHistory> balanceHistories = entry.getValue();
                Collections.sort(balanceHistories, Comparator.comparing(BalanceHistory::getDate));

                // Previous balance histories for same card number;
                CreditCard card = creditCardRepository.findByNumber(entry.getKey());
                List<BalanceHistory> prevBalanceHistories = card.getBalanceHistory();
                Collections.sort(prevBalanceHistories, Comparator.comparing(BalanceHistory::getDate));

                // for every credit card in balance history, update the balance history
                for(Iterator<BalanceHistory> iterator = balanceHistories.iterator(); iterator.hasNext();) {
                    BalanceHistory balanceHistory = iterator.next();
                    if(prevBalanceHistories.size() == 0) {
                        prevBalanceHistories.add(balanceHistory);
                        continue;
                    }
                    int index = upper_bound(prevBalanceHistories, balanceHistory.getDate());
                    if(index == 0) {
                        prevBalanceHistories.add(0, balanceHistory);
                        continue;
                    }
                    if(index == -1) {
                        prevBalanceHistories.add(balanceHistory);
                    } else {
                        if(prevBalanceHistories.get(index).getDate().isEqual(balanceHistory.getDate())){
                            int diffAmount = (int) (balanceHistory.getBalance() - prevBalanceHistories.get(index).getBalance());
                            for(int i = index; i < prevBalanceHistories.size(); i++) {
                                prevBalanceHistories.get(i).setBalance(prevBalanceHistories.get(i).getBalance() + diffAmount);
                            }
                            continue;
                        }
                        index--;
                        int diffAmount = (int) (balanceHistory.getBalance() - prevBalanceHistories.get(index).getBalance());
                        for(int i = index + 1; i < prevBalanceHistories.size(); i++) {
                            prevBalanceHistories.get(i).setBalance(prevBalanceHistories.get(i).getBalance() + diffAmount);
                        }
                        prevBalanceHistories.add(index+1, balanceHistory);
                    }
                }

                if(prevBalanceHistories.size() == 0) {
                    card.setBalanceHistory(balanceHistories);
                    creditCardRepository.save(card);
                    continue;
                }

                if(!prevBalanceHistories.get(prevBalanceHistories.size() - 1).getDate().isEqual(LocalDate.now())) {
                    BalanceHistory balanceHistory = new BalanceHistory();
                    balanceHistory.setCard(card);
                    balanceHistory.setDate(LocalDate.now());
                    balanceHistory.setBalance(prevBalanceHistories.get(prevBalanceHistories.size() - 1).getBalance());
                    prevBalanceHistories.add(balanceHistory);
                }

                card.setBalanceHistory(prevBalanceHistories);
                creditCardRepository.save(card);

                List<BalanceHistory> toBeSaved = new ArrayList<>();
                for(Iterator<BalanceHistory> iterator = prevBalanceHistories.iterator(); iterator.hasNext();) {
                    toBeSaved.add(iterator.next());
                }
                balanceHistoryRepository.saveAll(toBeSaved);
            }
            return ResponseEntity.ok(200);
        } catch (Exception e) {
            // return 500 error if there is an exception
            System.out.println("Error updating balance: " + e);
            return ResponseEntity.badRequest().body(500);
        }
    }
}

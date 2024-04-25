package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class CreditCardController {

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
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
    }

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
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
    }

    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        CreditCard card = creditCardRepository.findByNumber(creditCardNumber);
        if (card == null || card.getUser() == null) {
            // returning 400 if card does not exist or card exists but not associated with a user
            System.out.println("Invalid card number");
            return ResponseEntity.badRequest().body(400);
        }
        return ResponseEntity.ok(card.getUser().getId());
    }

    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<Integer> postMethodName(@RequestBody UpdateBalancePayload[] payload) {
//        //TODO: Given a list of transactions, update credit cards' balance history.
//        //      1. For the balance history in the credit card
//        //      2. If there are gaps between two balance dates, fill the empty date with the balance of the previous date
//        //      3. Given the payload `payload`, calculate the balance different between the payload and the actual balance stored in the database
//        //      4. If the different is not 0, update all the following budget with the difference
//        //      For example: if today is 4/12, a credit card's balanceHistory is [{date: 4/12, balance: 110}, {date: 4/10, balance: 100}],
//        //      Given a balance amount of {date: 4/11, amount: 110}, the new balanceHistory is
//        //      [{date: 4/12, balance: 120}, {date: 4/11, balance: 110}, {date: 4/10, balance: 100}]
//        //      Return 200 OK if update is done and successful, 400 Bad Request if the given card number
//        //        is not associated with a card.
//

        return null;
    }
    
}

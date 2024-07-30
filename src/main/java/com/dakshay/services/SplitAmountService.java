package com.dakshay.services;

import com.dakshay.EqualSplit;
import com.dakshay.dto.EqualExpense;
import com.dakshay.dto.ExactExpense;
import com.dakshay.dto.ExactSplit;
import com.dakshay.dto.Expense;
import com.dakshay.dto.PercentSplit;
import com.dakshay.dto.PercentageExpense;
import com.dakshay.dto.Split;
import com.dakshay.dto.RootDetailsRequestDTO;
import com.dakshay.enums.SplitType;
import com.dakshay.utils.NumberUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.dakshay.enums.SplitType.EXACT;

public class SplitAmountService {


    private Map<String, Map<String, BigDecimal>> owingDetails = new ConcurrentHashMap<>();

    public void splitAmount(Expense expense) {
        if(!expense.validate()) throw new RuntimeException("Expense and split mismatch");
        String paidUser = expense.getPaidByUser();
        BigDecimal paidAmount = expense.getAmount();
        var splitType = expense.getSplitType();
        int totalUsers = expense.getSplits().size();
        Map<String, BigDecimal> owePerUser = owingDetails.get(paidUser) != null ? owingDetails.get(paidUser) : new HashMap<>();
        for (Split split : expense.getSplits()) {
            String owesUserId = split.getUserId();
            if(paidUser.equals(owesUserId)) continue;
            BigDecimal newOwe = BigDecimal.ZERO;
            switch (splitType) {
                case EQUAL ->
                    newOwe = NumberUtils.divideByInt(paidAmount, totalUsers).add(
                            owePerUser.getOrDefault(split.getUserId(), BigDecimal.ZERO)
                    );

                case EXACT ->
                    newOwe = split.getAmount().add( owePerUser.getOrDefault(split.getUserId(), BigDecimal.ZERO));

                case PERCENT ->
                    newOwe = NumberUtils.percentage(paidAmount, split.getPercent()).add(
                            owePerUser.getOrDefault(split.getUserId(), BigDecimal.ZERO)
                    );
            }
            owePerUser.put(owesUserId, newOwe);
        }
        owingDetails.put(paidUser, owePerUser);
    }

    public void show(String userId) {
        System.out.println("Showing for user " + userId);
        Map<String, BigDecimal> owePerUser = owingDetails.get(userId);
        owingDetails.forEach((mainUser ,owing)->{
            owing.forEach((user, amount)->{
                if(userId.equals(user))
                    System.out.println("User "+ user + " owes " + " User " + userId + " : " + amount);

            });
        });
        if(owePerUser == null ) return;
        owePerUser.forEach((user,amount) ->{
            BigDecimal amountOwedByAnotherUser = getAmountOwedByAnotherUser(user, userId);
            BigDecimal net = amount.subtract(amountOwedByAnotherUser);
            if(net.compareTo(BigDecimal.ZERO) < 0)
                System.out.println("User "+ userId + " owes " + " User " + user + " : " + net);
            else if(net.compareTo(BigDecimal.ZERO) > 0)
                System.out.println("User "+ user + " owes " + " User " + userId + " : " + net.abs());

        });
    }

    public void show() {
        System.out.println("Showing All");
        owingDetails.forEach((userId, owePerUser) -> {
            owePerUser.forEach((user,amount) ->{
                if(user.equals(userId)) return;
                BigDecimal amountOwedByAnotherUser = getAmountOwedByAnotherUser(user, userId);
                BigDecimal net = amount.subtract(amountOwedByAnotherUser);
                if(net.compareTo(BigDecimal.ZERO) < 0)
                    System.out.println("User "+ userId + " owes " + " User " + user + " : " + net);
                else if(net.compareTo(BigDecimal.ZERO) > 0)
                    System.out.println("User "+ user + " owes " + " User " + userId + " : " + net);

            });
        });
    }

    private BigDecimal getAmountOwedByAnotherUser(String user, String userId) {
        Map<String, BigDecimal> map = (owingDetails.get(user)!=null ? owingDetails.get(user) : new HashMap<>());
        return map.get(userId) != null ? map.get(userId) : BigDecimal.ZERO;
    }


    public static void main(String[] args) {
        SplitAmountService splitAmountService = new SplitAmountService();

        InputStream inputStream = SplitAmountService.class.getResourceAsStream("/input.txt");

        if (inputStream == null) {
            System.err.println("Resource not found!");
            return;
        }

        // Wrap the InputStream with a BufferedReader
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            Expense expense = null;
            while ((line = reader.readLine()) != null) {
                int k=0;
                String[] elements = line.split(" ");
                String type = elements[k++];
                if("EXPENSE".equals(type)) {
                    String paidByUser = elements[k++];
                    BigDecimal amt = new BigDecimal(elements[k++]);
                    int totalUser = Integer.parseInt(elements[k++]);
                    List<String> toSplitInusers = new ArrayList<>();
                    for(int i = 0 ; i<totalUser; i++) {
                        String userName = elements[k++];
                        toSplitInusers.add(userName);
                    }
                    String splitType = elements[k++];
                    if(SplitType.EQUAL.name().equals(splitType)) {
                        List<Split> equalSplits = toSplitInusers.stream()
                                .map(user ->  new EqualSplit(user,NumberUtils.divideByInt(amt, toSplitInusers.size())))
                                .collect(Collectors.toList());
                        expense = new EqualExpense(paidByUser, amt, equalSplits);

                    }
                    if(SplitType.PERCENT.name().equals(splitType)) {
                        List<Split> percentSplits = new ArrayList<>();
                        for (int i=0; i<toSplitInusers.size();i++) {
                            int percent = Integer.parseInt(elements[k++]);
                            PercentSplit percentSplit = new PercentSplit(paidByUser, percent);
                            percentSplits.add(percentSplit);
                        }
                        expense = new PercentageExpense(paidByUser,amt, percentSplits);
                    }
                    if(EXACT.name().equals(splitType)) {
                        List<Split> percentSplits = new ArrayList<>();
                        for (int i=0; i<toSplitInusers.size();i++) {
                            BigDecimal exactAmount = new BigDecimal(elements[k++]);
                            Split percentSplit = new ExactSplit(paidByUser, exactAmount);
                            percentSplits.add(percentSplit);
                        }
                        expense = new ExactExpense(paidByUser,amt, percentSplits);
                    }
                    assert expense != null;
                    splitAmountService.splitAmount(expense);
                } else {
                    if(elements.length == 1) splitAmountService.show();
                    if(elements.length == 2) splitAmountService.show(elements[1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/*

User

1
2
3
4

Event ->
Electricity Bill :
Split type equal 1000 / 4 = 250
1 1000 => Owe(User 2 - 250), Owe(User 3 - 250), Owe(User 4 - 250)
2 Owes(User 1 - 250)
3 Owes(User 1 - 250)
4 Owes(User 1 - 250)


Map
Owe->
1
    2 620
    3 1130
    4 250

4
    1 480
    2 240
    3 240

2
    1 0
    3 0
    4 0


Map Owes
1
    2 0
    3 0
    4 480

2
    1 620
    3 0
    4 480

3
    1 250
    2 0
    4 480

4
    1 1130
    2 0
    3 0

User 1 owe 250 to User 2 OR User 2 Owes User 1 : 250
User 1 owe 250 to User 3 OR User 3 Owes User 1 : 250
User 1 owe 250 to User 4 OR User 4 Owes User 1 : 250

4 ko 1 se 500/- lene

Broadband Bill 1250 :
Split type equal 1250 / 4 = 500
4 2000 => Owe(User 2 - 500), Owe(User 3 - 500), Owe(User 1 - 500)
2 Owes(User 1 - 250) (User 4 - 500)
3 Owes(User 1 - 250) (User 4 - 500)
1 Owes(User 4 - 500)



Food Bill 1200
User 4 -> Split percentage : 40 20 20 20

480
240
240
240

 */
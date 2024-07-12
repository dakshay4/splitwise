package com.dakshay.services;

import com.dakshay.dto.ChildDetailRequestDTO;
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

public class SplitAmountService {


    private Map<String, Map<String, BigDecimal>> owingDetails = new ConcurrentHashMap<>();

    public void splitAmount(RootDetailsRequestDTO rootDetailsRequestDTO) {
        String paidUser = rootDetailsRequestDTO.getUserId();
        BigDecimal paidAmount = rootDetailsRequestDTO.getAmount();
        var splitType = rootDetailsRequestDTO.getSplitType();
        int totalUsers = rootDetailsRequestDTO.getChildDetailRequests().size();
        Map<String, BigDecimal> owePerUser = owingDetails.get(paidUser) != null ? owingDetails.get(paidUser) : new HashMap<>();
        for (ChildDetailRequestDTO childDetailRequest : rootDetailsRequestDTO.getChildDetailRequests()) {
            String owesUserId = childDetailRequest.getUserId();
            if(paidUser.equals(owesUserId)) continue;
            BigDecimal newOwe = BigDecimal.ZERO;
            switch (splitType) {
                case EQUAL ->
                    newOwe = NumberUtils.divideByInt(paidAmount, totalUsers).add(
                            owePerUser.getOrDefault(childDetailRequest.getUserId(), BigDecimal.ZERO)
                    );

                case EXACT ->
                    newOwe = childDetailRequest.getAmount().add( owePerUser.getOrDefault(childDetailRequest.getUserId(), BigDecimal.ZERO));

                case PERCENT ->
                    newOwe = NumberUtils.percentage(paidAmount, childDetailRequest.getPercentage()).add(
                            owePerUser.getOrDefault(childDetailRequest.getUserId(), BigDecimal.ZERO)
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
            RootDetailsRequestDTO rootDetailsRequestDTO = new RootDetailsRequestDTO();
            while ((line = reader.readLine()) != null) {
                int k=0;
                String[] elements = line.split(" ");
                String type = elements[k++];
                List<ChildDetailRequestDTO> childDetailRequestDTOs = new ArrayList<>();
                if("EXPENSE".equals(type)) {
                    String mainUser = elements[k++];
                    String amt = elements[k++];
                    int totalUser = Integer.parseInt(elements[k++]);
                    rootDetailsRequestDTO.setUserId(mainUser);
                    rootDetailsRequestDTO.setAmount(new BigDecimal(amt));
                    for(int i = 0 ; i<totalUser; i++) {
                        String userName = elements[k++];
                        ChildDetailRequestDTO childDetailRequestDTO = new ChildDetailRequestDTO();
                        childDetailRequestDTO.setUserId(userName);
                        childDetailRequestDTOs.add(childDetailRequestDTO);
                    }
                    String splitType = elements[k++];
                    if(SplitType.EQUAL.name().equals(splitType)) rootDetailsRequestDTO.setSplitType(SplitType.EQUAL);
                    if(SplitType.PERCENT.name().equals(splitType)) {
                        rootDetailsRequestDTO.setSplitType(SplitType.PERCENT);
                        for(int i = 0 ; i<totalUser; i++) {
                            int percent = Integer.parseInt(elements[k++]);
                            ChildDetailRequestDTO childDetailRequestDTO = childDetailRequestDTOs.get(i);
                            childDetailRequestDTO.setPercentage(percent);
                        }
                    }
                    if(SplitType.EXACT.name().equals(splitType)) {
                        rootDetailsRequestDTO.setSplitType(SplitType.EXACT);
                        for(int i = 0 ; i<totalUser; i++) {
                            BigDecimal exactAmount = new BigDecimal(elements[k++]);
                            ChildDetailRequestDTO childDetailRequestDTO = childDetailRequestDTOs.get(i);
                            childDetailRequestDTO.setAmount(exactAmount);
                        }
                    }
                    rootDetailsRequestDTO.setChildDetailRequests(childDetailRequestDTOs);
                    splitAmountService.splitAmount(rootDetailsRequestDTO);
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
package com.dakshay.model;


import lombok.Data;

@Data
public class User {

    private final String id;
    private final String name;
    private final String email;
    private final String phoneNo;
}

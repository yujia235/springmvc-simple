package com.yujia.service;

public interface IAccountService {
    void transfer(String fromCardNo, String toCardNo, int money) throws Exception;
}

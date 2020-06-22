package com.yujia.dao;

import com.yujia.model.pojo.Account;

public interface IAccountDao {

    Account queryAccountByCardNo(String cardNo) throws Exception;

    int updateAccountByCardNo(Account account) throws Exception;
}

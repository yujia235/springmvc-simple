package com.yujia.service.impl;

import com.yujia.annotation.Autowired;
import com.yujia.annotation.Service;
import com.yujia.annotation.Transactional;
import com.yujia.dao.IAccountDao;
import com.yujia.model.pojo.Account;
import com.yujia.service.IAccountService;

@Service
public class AccountServiceImpl implements IAccountService {

    @Autowired
    private IAccountDao accountDao;

    @Transactional
    @Override
    public void transfer(String fromCardNo, String toCardNo, int money) throws Exception {
        Account fromAccount = accountDao.queryAccountByCardNo(fromCardNo);
        Account toAccount = accountDao.queryAccountByCardNo(toCardNo);
        fromAccount.setMoney(fromAccount.getMoney()-money);
        toAccount.setMoney(toAccount.getMoney()+ money);
        accountDao.updateAccountByCardNo(fromAccount);
//        int i =  1/0;
        accountDao.updateAccountByCardNo(toAccount);
    }
}

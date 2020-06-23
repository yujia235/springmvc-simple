package com.yujia.controller;

import com.yujia.annotation.Autowired;
import com.yujia.annotation.Controller;
import com.yujia.annotation.RequestMapping;
import com.yujia.annotation.Security;
import com.yujia.model.bo.TransferAccountBO;
import com.yujia.model.pojo.Result;
import com.yujia.service.IAccountService;

@Controller
@Security({"zhangsan","lisi","wangwu"})
@RequestMapping(value = "/account")
public class AccountController {

    @Autowired
    private IAccountService accountService;

    @RequestMapping(value = "/transfer")
    public Result transfer(TransferAccountBO transferAccount) {
        Result result = new Result();
        try {
            // 2.调⽤service层⽅法
            accountService.transfer(transferAccount.getFromCardNo(), transferAccount.getToCardNo(), transferAccount.getMoney());
            result.setStatus("200");
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus("201");
            result.setMessage(e.toString());
        }
//        // 3.响应
//        resp.setContentType("application/json;charset=utf-8");
//        resp.getWriter().print(JsonUtils.object2Json(result));
        return result;
    }

    @Security("zhangsan")
    @RequestMapping(value = "/test1")
    public String test1(String username, String age) {
        return "username=" + username + ", age=" + age;
    }

    @Security("lisi")
    @RequestMapping(value = "/test2")
    public String test2(String username, String sex) {
        return "username=" + username + ", sex=" + sex;
    }

    @RequestMapping(value = "/test3")
    public String test3(String username, String high) {
        return "username=" + username + ", high=" + high;
    }
}

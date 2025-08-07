package cn.y.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 *  用户登录请求体
 *
 * @author yyy
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -4705388187205963142L;

    private String userAccount;

    private String userPassword;

}

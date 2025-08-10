package cn.y.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 修改队伍请求体
 */
@Data
public class TeamJoinRequest implements Serializable {


    private static final long serialVersionUID = 4905685127761792795L;


    /**
     * id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String teamPassword;
}

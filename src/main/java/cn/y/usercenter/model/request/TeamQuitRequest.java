package cn.y.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 退出队伍请求体
 */
@Data
public class TeamQuitRequest implements Serializable {


    private static final long serialVersionUID = 4905685127761792795L;


    /**
     * id
     */
    private Long teamId;


}

package cn.y.usercenter.model.domain;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 用户-队伍表
 * @TableName user_team
 */
@TableName(value ="user_team")
@Data
public class UserTeam {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 队员 id
     */
    private Long userId;

    /**
     * 队伍 id
     */
    private Long teamId;

    /**
     * 加入时间
     */
    private Date joinTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;
}

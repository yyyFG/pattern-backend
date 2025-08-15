package cn.y.usercenter.model.vo;

import cn.y.usercenter.model.domain.User;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 队伍用户信息封装类（脱敏）
 */
@Data
public class TeamUserVO implements Serializable {


    private static final long serialVersionUID = -4847341058065245957L;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 登录账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 用户简介
     */
    private String profile;


    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态  0-正常
     */
    private Integer userStatus;

    /**
     * 用户 0 - 普通用户 1 - 管理员
     */
    private Integer userRole;

    /**
     * 星球用户
     */
    private String planetCode;

    /**
     * 标签列表 json
     */
    private String tags;

    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建人 id
     */
    private Long userId;

    /**
     * 队伍状态  0 - 公开， 1- 私有， 2 - 加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 创建人用户信息
     */
    UserVO currentUser;


    /**
     * 入队用户列表
     */
    List<User> userList;

    /**
     * 是否已加入队伍
     */
    private boolean hasJoin = false;

}

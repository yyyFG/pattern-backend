package cn.y.usercenter.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户包装类（脱敏）
 */
@Data
public class UserVO implements Serializable {


    private static final long serialVersionUID = 3364798468488671974L;
    /**
     * id
     */
    private Long id;

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
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


}

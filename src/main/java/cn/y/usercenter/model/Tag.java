package cn.y.usercenter.model;


import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import lombok.Data;

/**
 * 标签表
 * @TableName tag
 */
@TableName(value ="tag")
@Data
public class Tag {
    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称
     */
    @TableField(value = "tagName")
    private String tagName;

    /**
     * 用户 id
     */
    @TableField(value = "userId")
    private String userId;

    /**
     * 父标签
     */
    @TableField(value = "parentId")
    private Long parentId;

    /**
     * 0-是父标签, 1-不是父标签
     */
    @TableField(value = "isParent")
    private Integer isParent;

    /**
     * 创建时间
     */
    @TableField(value = "createTime")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "updateTime")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField(value = "isDelete")
    private Integer isDelete;
}
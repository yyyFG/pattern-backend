package cn.y.usercenter.model.dto;

import cn.y.usercenter.model.request.PageRequest;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 队伍查询封装类
 */
@Data
public class TeamQuery extends PageRequest implements Serializable {

    private static final long serialVersionUID = 2855013198122982834L;

    /**
     * id
     */
    private Long id;

    /**
     * 搜索关键词（同时对队伍名称和描述搜索）
     */
    private String searchText;

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
     * 创建人 id
     */
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TeamQuery teamQuery = (TeamQuery) o;
        return Objects.equals(id, teamQuery.id) && Objects.equals(searchText, teamQuery.searchText) && Objects.equals(name, teamQuery.name) && Objects.equals(description, teamQuery.description) && Objects.equals(maxNum, teamQuery.maxNum) && Objects.equals(userId, teamQuery.userId) && Objects.equals(status, teamQuery.status);
    }

    /**
     * 队伍状态  0 - 公开， 1- 私有， 2 - 加密
     */
    private Integer status;
}

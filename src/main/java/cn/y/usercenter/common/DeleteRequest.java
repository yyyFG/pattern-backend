package cn.y.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除请求体
 */
@Data
public class DeleteRequest implements Serializable {


    private static final long serialVersionUID = 4905685127761792795L;


    /**
     * id
     */
    private Long id;


}

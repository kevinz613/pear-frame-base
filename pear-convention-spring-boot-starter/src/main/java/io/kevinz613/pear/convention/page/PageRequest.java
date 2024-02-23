package io.kevinz613.pear.convention.page;

import lombok.Data;

/**
 * 分页请求
 *
 * @author kevinz613
 */
@Data
public class PageRequest {

    /**
     * 当前页
     */
    private Long current;

    /**
     * 每页显示数据条数
     */
    private Long size;
}

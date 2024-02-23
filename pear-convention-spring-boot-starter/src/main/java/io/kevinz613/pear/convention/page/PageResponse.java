package io.kevinz613.pear.convention.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页响应
 *
 * @author kevinz613
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponse<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = -6811247549970435121L;

    /**
     * 当前页
     */
    private Long current;

    /**
     * 每页显示数据条数
     */
    private Long size;

    /**
     * 总数
     */
    private Long total;

    /**
     * 查询数据列表
     */
    private List<T> records = Collections.emptyList();

    public PageResponse(Long current, Long size) {
        this(current, size, 0L);
    }

    public PageResponse(Long current, Long size, Long total) {
        if (current > 1) {
            this.current = current;
        }
        this.size = size;
        this.total = total;
    }

    public PageResponse setRecords(List<T> records) {
        this.records = records;
        return this;
    }

    public <R> PageResponse<R> convert(Function<? super T, ? extends R> mapper) {
        List<R> collect = this.getRecords().stream().map(mapper).collect(Collectors.toList());
        return ((PageResponse<R>) this).setRecords(collect);
    }
}

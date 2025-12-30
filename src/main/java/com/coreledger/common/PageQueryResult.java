package com.coreledger.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

@Data
public class PageQueryResult<T> implements Serializable {

    private List<T> content;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 总记录数
     */
    private Long totalElements;

    public PageQueryResult(List<T> content) {
        this(content, null, null);
    }

    public PageQueryResult(List<T> content, Integer totalPages, Long totalElements) {
        this.content = content;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    public static <T> PageQueryResult<T> empty(){
        return new PageQueryResult<>(List.of(), 0,0L);
    }

    public static <T> PageQueryResult<T> of(List<T> data){
        return new PageQueryResult<>(data);
    }

    public static <T, P> PageQueryResult<T> of(PageQueryResult<P> page , Function<List<P>, List<T>> mapper){
        return new PageQueryResult<>(mapper.apply(page.content), page.totalPages, page.totalElements);
    }

    public static <T, P> PageQueryResult<T> of(Page<P> page, Function<List<P>, List<T>> mapper) {
        return new PageQueryResult<>(mapper.apply(page.getContent()),  page.getTotalPages(), page.getTotalElements());
    }

    @JsonIgnore
    public boolean isNotEmpty() {
        return !CollectionUtils.isEmpty(content);
    }

}

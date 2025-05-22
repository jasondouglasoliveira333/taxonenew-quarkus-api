package br.com.lkm.taxone.mapper.dto;

import java.util.Arrays;
import java.util.List;

public class PageResponse<T> {
    private Integer  totalPages;
    private List content;
    public Integer getTotalPages() {
        return totalPages;
    }
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
    public List getContent() {
        return content;
    }
    public void setContent(List content) {
        this.content = content;
    }
    
    public void setContent(Object[] contentArray) {
        this.content = Arrays.asList(contentArray);
    }
    
    
}

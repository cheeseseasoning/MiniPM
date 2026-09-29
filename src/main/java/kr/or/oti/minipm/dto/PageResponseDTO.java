package kr.or.oti.minipm.dto;

import java.util.List;

import lombok.Getter;

@Getter
public class PageResponseDTO<T> {

    private int page;
    private int size;
    private int total;

    private int start;
    private int end;

    private boolean prev;
    private boolean next;

    private List<T> items;

    public PageResponseDTO(PageRequestDTO pageRequestDTO, List<T> items, int total) {

        this.page = pageRequestDTO.getPage();
        this.size = pageRequestDTO.getSize();
        this.items = items;
        this.total = total;

        int temporaryEnd = (int) Math.ceil(page / 10.0) * 10;

        this.start = temporaryEnd - 9;

        int lastPage = (int) Math.ceil(total / (double) size);

        this.end = Math.min(temporaryEnd, lastPage);

        this.prev = start > 1;
        this.next = end < lastPage;
    }
}
package rabbit.flt.rpc.server;

import rabbit.flt.rpc.common.Request;

import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FilterChain {

    /**
     * 过滤器
     */
    private List<Filter> filters;

    /**
     * 当前请求
     */
    private Request request;

    /**
     * selection key
     */
    private SelectionKey selectionKey;

    /**
     * 执行游标
     */
    private int cursor = 0;

    public FilterChain() {
        filters = new ArrayList<>();
    }

    public FilterChain(List<Filter> filters, Request request, SelectionKey selectionKey) {
        this();
        this.filters.addAll(filters);
        this.request = request;
        this.selectionKey = selectionKey;
    }

    /**
     * 执行过滤链
     */
    public void doChain() {
        if (cursor < filters.size()) {
            nextFilter().doFilter(this);
        }
    }

    private Filter nextFilter() {
        Filter filter = filters.get(cursor);
        cursor++;
        return filter;
    }

    /**
     * 新增filter
     * @param filter
     */
    public void add(Filter filter) {
        filters.add(filter);
        Collections.sort(filters, Comparator.comparing(Filter::getPriority));
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public Request getRequest() {
        return request;
    }

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }
}

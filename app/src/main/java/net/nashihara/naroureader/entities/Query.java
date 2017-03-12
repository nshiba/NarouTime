package net.nashihara.naroureader.entities;

import net.nashihara.naroureader.models.EasyParcelable;

public class Query extends EasyParcelable {

    public static final Creator<Query> CREATOR = new EasyCreator<>(Query.class);

    private int limit;
    private int sortOrder;
    private String search;
    private String notSearch;
    private boolean targetTitle;
    private boolean targetStory;
    private boolean targetKeyword;
    private boolean targetWriter;
    private int time;
    private int maxLength;
    private int minLength;
    private boolean end;
    private boolean stop;
    private boolean pickup;

    public Query() {
        this.setTime(0);
        this.setSortOrder(0);
    }

    private String ncode;

    public String getNcode() {
        return ncode;
    }

    public void setNcode(final String ncode) {
        this.ncode = ncode;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(final int limit) {
        this.limit = limit;
    }

    public void setLimit(final String limitStr) {
        setLimit(validateLimit(limitStr));
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(final int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(final String search) {
        this.search = search;
    }

    public String getNotSearch() {
        return notSearch;
    }

    public void setNotSearch(final String notSearch) {
        this.notSearch = notSearch;
    }

    public boolean isTargetTitle() {
        return targetTitle;
    }

    public void setTargetTitle(final boolean targetTitle) {
        this.targetTitle = targetTitle;
    }

    public boolean isTargetStory() {
        return targetStory;
    }

    public void setTargetStory(final boolean targetStory) {
        this.targetStory = targetStory;
    }

    public boolean isTargetKeyword() {
        return targetKeyword;
    }

    public void setTargetKeyword(final boolean targetKeyword) {
        this.targetKeyword = targetKeyword;
    }

    public boolean isTargetWriter() {
        return targetWriter;
    }

    public void setTargetWriter(final boolean targetWriter) {
        this.targetWriter = targetWriter;
    }

    public int getTime() {
        return time;
    }

    public void setTime(final int time) {
        this.time = time;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(final int maxLength) {
        this.maxLength = maxLength;
    }

    public void setMaxLength(final String maxLength) {
        int max = maxLength.equals("") ? 0 : Integer.parseInt(maxLength);
        setMaxLength(max);
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(final int minLength) {
        this.minLength = minLength;
    }

    public void setMinLength(final String minLength) {
        int min = minLength.equals("") ? 0 : Integer.parseInt(minLength);
        setMinLength(min);
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(final boolean end) {
        this.end = end;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(final boolean stop) {
        this.stop = stop;
    }

    public boolean isPickup() {
        return pickup;
    }

    public void setPickup(final boolean pickup) {
        this.pickup = pickup;
    }

    private int validateLimit(String limitStr) {
        return limitStr.equals("") ? 0 : Integer.parseInt(limitStr);
    }
}

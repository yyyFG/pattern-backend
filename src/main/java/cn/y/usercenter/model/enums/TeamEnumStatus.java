package cn.y.usercenter.model.enums;

/**
 * 队伍状态枚举
 */
public enum TeamEnumStatus {

    PUBLIC(0,"公开"),
    PRIVATE(1,"私密"),
    SECRET(2,"加密");

    private int value;

    private String text;

    public static TeamEnumStatus getEnumByValue(Integer value) {
        if(value == null){
            return null;
        }
        TeamEnumStatus[] values = TeamEnumStatus.values();
        for (TeamEnumStatus teamEnumStatus : values) {
            if(teamEnumStatus.getValue() == value){
                return teamEnumStatus;
            }
        }
        return null;
    }

    TeamEnumStatus(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

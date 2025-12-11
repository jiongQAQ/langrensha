package com.werewolf.domain.model;

/**
 * 游戏阶段枚举
 *
 * @author Claude & User
 */
public enum Phase {
    /**
     * 等待开始
     */
    WAITING("等待开始", "Waiting"),

    /**
     * 游戏准备中（分配角色）
     */
    PREPARING("准备中", "Preparing"),

    /**
     * 夜晚阶段
     */
    NIGHT("夜晚", "Night"),

    /**
     * 白天阶段
     */
    DAY("白天", "Day"),

    /**
     * 投票阶段
     */
    VOTING("投票", "Voting"),

    /**
     * 游戏结束
     */
    FINISHED("结束", "Finished");

    private final String chineseName;
    private final String englishName;

    Phase(String chineseName, String englishName) {
        this.chineseName = chineseName;
        this.englishName = englishName;
    }

    public String getChineseName() {
        return chineseName;
    }

    public String getEnglishName() {
        return englishName;
    }
}

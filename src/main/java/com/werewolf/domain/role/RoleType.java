package com.werewolf.domain.role;

/**
 * 角色类型枚举
 *
 * @author Claude & User
 */
public enum RoleType {
    /**
     * 狼人
     */
    WEREWOLF("狼人", "Werewolf"),

    /**
     * 预言家
     */
    SEER("预言家", "Seer"),

    /**
     * 女巫
     */
    WITCH("女巫", "Witch"),

    /**
     * 平民
     */
    VILLAGER("平民", "Villager");

    private final String chineseName;
    private final String englishName;

    RoleType(String chineseName, String englishName) {
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

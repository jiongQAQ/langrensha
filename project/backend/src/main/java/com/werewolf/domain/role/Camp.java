package com.werewolf.domain.role;

/**
 * 阵营枚举
 *
 * @author Claude & User
 */
public enum Camp {
    /**
     * 狼人阵营
     */
    WEREWOLF("狼人阵营", "Werewolf Camp"),

    /**
     * 好人阵营
     */
    GOOD("好人阵营", "Good Camp");

    private final String chineseName;
    private final String englishName;

    Camp(String chineseName, String englishName) {
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

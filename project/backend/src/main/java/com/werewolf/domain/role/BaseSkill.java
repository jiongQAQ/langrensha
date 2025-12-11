package com.werewolf.domain.role;

import lombok.Getter;

/**
 * 技能抽象基类
 * 提供技能的通用实现
 *
 * @author Claude & User
 */
@Getter
public abstract class BaseSkill implements Skill {

    /**
     * 技能名称
     */
    private final String name;

    /**
     * 技能描述
     */
    private final String description;

    /**
     * 使用限制（-1表示无限制）
     */
    private final int usageLimit;

    /**
     * 剩余使用次数
     */
    private int remainingUses;

    protected BaseSkill(String name, String description, int usageLimit) {
        this.name = name;
        this.description = description;
        this.usageLimit = usageLimit;
        this.remainingUses = usageLimit;
    }

    @Override
    public boolean isAvailable() {
        return usageLimit == -1 || remainingUses > 0;
    }

    @Override
    public void use() {
        if (usageLimit != -1 && remainingUses > 0) {
            remainingUses--;
        }
    }

    @Override
    public void reset() {
        this.remainingUses = usageLimit;
    }
}

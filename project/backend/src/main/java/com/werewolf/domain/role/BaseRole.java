package com.werewolf.domain.role;

import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色抽象基类
 * 提供角色的通用实现
 *
 * @author Claude & User
 */
@Getter
public abstract class BaseRole implements Role {

    /**
     * 角色类型
     */
    private final RoleType roleType;

    /**
     * 角色名称
     */
    private final String roleName;

    /**
     * 角色阵营
     */
    private final Camp camp;

    /**
     * 角色描述
     */
    private final String description;

    /**
     * 角色技能列表
     */
    private final List<Skill> skills;

    protected BaseRole(RoleType roleType, Camp camp, String description) {
        this.roleType = roleType;
        this.roleName = roleType.getChineseName();
        this.camp = camp;
        this.description = description;
        this.skills = new ArrayList<>();
    }

    /**
     * 添加技能
     *
     * @param skill 技能
     */
    protected void addSkill(Skill skill) {
        this.skills.add(skill);
    }

    @Override
    public boolean hasNightAction() {
        return !skills.isEmpty();
    }

    @Override
    public boolean isDivine() {
        return roleType == RoleType.SEER || roleType == RoleType.WITCH;
    }

    @Override
    public ActionResult performNightAction(Object context) {
        // 子类需要实现具体的夜晚行动逻辑
        return ActionResult.builder()
                .success(false)
                .actionType(ActionResult.ActionType.NONE)
                .message("此角色无夜晚行动")
                .build();
    }
}

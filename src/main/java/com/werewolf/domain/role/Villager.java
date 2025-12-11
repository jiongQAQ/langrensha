package com.werewolf.domain.role;

import java.util.Collections;
import java.util.List;

/**
 * 平民角色
 *
 * @author Claude & User
 */
public class Villager extends BaseRole {

    public Villager() {
        super(
            RoleType.VILLAGER,
            Camp.GOOD,
            "好人阵营成员，无特殊技能。依靠发言分析和投票帮助好人阵营获胜。"
        );
        // 平民没有技能
    }

    @Override
    public List<Skill> getSkills() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasNightAction() {
        return false;
    }

    @Override
    public ActionResult performNightAction(Object context) {
        return ActionResult.builder()
                .success(false)
                .actionType(ActionResult.ActionType.NONE)
                .message("平民没有夜晚行动")
                .build();
    }
}

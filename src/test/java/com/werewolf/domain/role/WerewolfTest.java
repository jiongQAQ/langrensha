package com.werewolf.domain.role;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 狼人角色测试
 *
 * @author Claude & User
 */
class WerewolfTest {

    @Test
    void testWerewolfBasicInfo() {
        Werewolf werewolf = new Werewolf();

        assertEquals(RoleType.WEREWOLF, werewolf.getRoleType());
        assertEquals("狼人", werewolf.getRoleName());
        assertEquals(Camp.WEREWOLF, werewolf.getCamp());
        assertNotNull(werewolf.getDescription());
    }

    @Test
    void testWerewolfHasNightAction() {
        Werewolf werewolf = new Werewolf();

        assertTrue(werewolf.hasNightAction());
        assertFalse(werewolf.isDivine());
    }

    @Test
    void testWerewolfSkills() {
        Werewolf werewolf = new Werewolf();

        assertFalse(werewolf.getSkills().isEmpty());
        assertEquals(1, werewolf.getSkills().size());

        Skill skill = werewolf.getSkills().get(0);
        assertEquals("击杀", skill.getName());
        assertTrue(skill.isAvailable());
        assertEquals(-1, skill.getUsageLimit());
    }

    @Test
    void testWerewolfKillSkill() {
        Werewolf werewolf = new Werewolf();
        KillSkill killSkill = werewolf.getKillSkill();

        assertNotNull(killSkill);
        assertTrue(killSkill.isAvailable());

        // 测试无限使用
        for (int i = 0; i < 10; i++) {
            killSkill.use();
            assertTrue(killSkill.isAvailable());
        }
    }

    @Test
    void testWerewolfPerformNightAction() {
        Werewolf werewolf = new Werewolf();
        ActionResult result = werewolf.performNightAction(null);

        assertTrue(result.isSuccess());
        assertEquals(ActionResult.ActionType.KILL, result.getActionType());
        assertNotNull(result.getMessage());
    }
}

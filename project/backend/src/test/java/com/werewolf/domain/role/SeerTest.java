package com.werewolf.domain.role;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 预言家角色测试
 *
 * @author Claude & User
 */
class SeerTest {

    @Test
    void testSeerBasicInfo() {
        Seer seer = new Seer();

        assertEquals(RoleType.SEER, seer.getRoleType());
        assertEquals("预言家", seer.getRoleName());
        assertEquals(Camp.GOOD, seer.getCamp());
        assertNotNull(seer.getDescription());
    }

    @Test
    void testSeerIsDivine() {
        Seer seer = new Seer();

        assertTrue(seer.hasNightAction());
        assertTrue(seer.isDivine());
    }

    @Test
    void testSeerSkills() {
        Seer seer = new Seer();

        assertFalse(seer.getSkills().isEmpty());
        assertEquals(1, seer.getSkills().size());

        Skill skill = seer.getSkills().get(0);
        assertEquals("查验", skill.getName());
        assertTrue(skill.isAvailable());
        assertEquals(-1, skill.getUsageLimit());
    }

    @Test
    void testSeerCheckSkill() {
        Seer seer = new Seer();
        CheckSkill checkSkill = seer.getCheckSkill();

        assertNotNull(checkSkill);
        assertTrue(checkSkill.isAvailable());

        // 测试无限使用
        for (int i = 0; i < 10; i++) {
            checkSkill.use();
            assertTrue(checkSkill.isAvailable());
        }
    }

    @Test
    void testSeerPerformNightAction() {
        Seer seer = new Seer();
        ActionResult result = seer.performNightAction(null);

        assertTrue(result.isSuccess());
        assertEquals(ActionResult.ActionType.CHECK, result.getActionType());
        assertNotNull(result.getMessage());
    }
}

package com.werewolf.domain.role;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 平民角色测试
 *
 * @author Claude & User
 */
class VillagerTest {

    @Test
    void testVillagerBasicInfo() {
        Villager villager = new Villager();

        assertEquals(RoleType.VILLAGER, villager.getRoleType());
        assertEquals("平民", villager.getRoleName());
        assertEquals(Camp.GOOD, villager.getCamp());
        assertNotNull(villager.getDescription());
    }

    @Test
    void testVillagerHasNoNightAction() {
        Villager villager = new Villager();

        assertFalse(villager.hasNightAction());
        assertFalse(villager.isDivine());
    }

    @Test
    void testVillagerHasNoSkills() {
        Villager villager = new Villager();

        assertTrue(villager.getSkills().isEmpty());
        assertEquals(0, villager.getSkills().size());
    }

    @Test
    void testVillagerPerformNightAction() {
        Villager villager = new Villager();
        ActionResult result = villager.performNightAction(null);

        assertFalse(result.isSuccess());
        assertEquals(ActionResult.ActionType.NONE, result.getActionType());
        assertNotNull(result.getMessage());
    }
}

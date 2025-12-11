package com.werewolf.domain.role;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 女巫角色测试
 *
 * @author Claude & User
 */
class WitchTest {

    @Test
    void testWitchBasicInfo() {
        Witch witch = new Witch();

        assertEquals(RoleType.WITCH, witch.getRoleType());
        assertEquals("女巫", witch.getRoleName());
        assertEquals(Camp.GOOD, witch.getCamp());
        assertNotNull(witch.getDescription());
    }

    @Test
    void testWitchIsDivine() {
        Witch witch = new Witch();

        assertTrue(witch.hasNightAction());
        assertTrue(witch.isDivine());
    }

    @Test
    void testWitchSkills() {
        Witch witch = new Witch();

        assertFalse(witch.getSkills().isEmpty());
        assertEquals(2, witch.getSkills().size());

        // 检查解药
        AntidoteSkill antidote = witch.getAntidoteSkill();
        assertEquals("解药", antidote.getName());
        assertTrue(antidote.isAvailable());
        assertEquals(1, antidote.getUsageLimit());

        // 检查毒药
        PoisonSkill poison = witch.getPoisonSkill();
        assertEquals("毒药", poison.getName());
        assertTrue(poison.isAvailable());
        assertEquals(1, poison.getUsageLimit());
    }

    @Test
    void testWitchAntidoteUsageLimit() {
        Witch witch = new Witch();
        AntidoteSkill antidote = witch.getAntidoteSkill();

        assertTrue(antidote.isAvailable());
        assertEquals(1, antidote.getRemainingUses());

        // 使用一次后应该不可用
        antidote.use();
        assertFalse(antidote.isAvailable());
        assertEquals(0, antidote.getRemainingUses());

        // 重置后应该恢复
        antidote.reset();
        assertTrue(antidote.isAvailable());
        assertEquals(1, antidote.getRemainingUses());
    }

    @Test
    void testWitchPoisonUsageLimit() {
        Witch witch = new Witch();
        PoisonSkill poison = witch.getPoisonSkill();

        assertTrue(poison.isAvailable());
        assertEquals(1, poison.getRemainingUses());

        // 使用一次后应该不可用
        poison.use();
        assertFalse(poison.isAvailable());
        assertEquals(0, poison.getRemainingUses());

        // 重置后应该恢复
        poison.reset();
        assertTrue(poison.isAvailable());
        assertEquals(1, poison.getRemainingUses());
    }

    @Test
    void testWitchFirstNight() {
        Witch witch = new Witch();

        assertTrue(witch.isFirstNight());
        witch.markFirstNightPassed();
        assertFalse(witch.isFirstNight());
    }

    @Test
    void testWitchCannotUseBothPotionsInSameNight() {
        Witch witch = new Witch();

        assertFalse(witch.canUseBothPotionsInSameNight());
    }

    @Test
    void testWitchPerformNightAction() {
        Witch witch = new Witch();
        ActionResult result = witch.performNightAction(null);

        assertTrue(result.isSuccess());
        assertEquals(ActionResult.ActionType.ANTIDOTE, result.getActionType());
        assertNotNull(result.getMessage());
    }
}

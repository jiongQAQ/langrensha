package com.werewolf.engine.day;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpeechManager 单元测试
 */
@DisplayName("发言管理器测试")
class SpeechManagerTest {

    private List<Long> playerIds;

    @BeforeEach
    void setUp() {
        playerIds = Arrays.asList(1L, 2L, 3L, 4L);
    }

    @Test
    @DisplayName("初始化 - 按顺序")
    void testInitialize_InOrder() {
        SpeechManager manager = new SpeechManager(playerIds, false);

        assertEquals(1L, manager.getCurrentSpeaker());
        assertFalse(manager.isAllSpeechFinished());
        assertEquals("1/4", manager.getProgress());
    }

    @Test
    @DisplayName("初始化 - 随机顺序")
    void testInitialize_Random() {
        SpeechManager manager = new SpeechManager(playerIds, true);

        assertNotNull(manager.getCurrentSpeaker());
        List<Long> order = manager.getSpeechOrder();
        assertEquals(4, order.size());
        assertTrue(order.containsAll(playerIds));
    }

    @Test
    @DisplayName("初始化 - 空列表抛出异常")
    void testInitialize_EmptyList() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SpeechManager(Arrays.asList());
        });
    }

    @Test
    @DisplayName("初始化 - null抛出异常")
    void testInitialize_NullList() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SpeechManager(null);
        });
    }

    @Test
    @DisplayName("记录发言 - 正常")
    void testRecordSpeech_Normal() {
        SpeechManager manager = new SpeechManager(playerIds, false);

        manager.recordSpeech(1L, "我是好人");
        manager.recordSpeech(2L, "我认为3号是狼人");

        assertEquals("我是好人", manager.getSpeech(1L));
        assertEquals("我认为3号是狼人", manager.getSpeech(2L));

        Map<Long, String> allSpeeches = manager.getAllSpeeches();
        assertEquals(2, allSpeeches.size());
    }

    @Test
    @DisplayName("记录发言 - 空内容转为沉默")
    void testRecordSpeech_EmptyContent() {
        SpeechManager manager = new SpeechManager(playerIds, false);

        manager.recordSpeech(1L, "");
        manager.recordSpeech(2L, null);

        assertEquals("[沉默]", manager.getSpeech(1L));
        assertEquals("[沉默]", manager.getSpeech(2L));
    }

    @Test
    @DisplayName("记录发言 - 玩家ID为null抛出异常")
    void testRecordSpeech_NullPlayerId() {
        SpeechManager manager = new SpeechManager(playerIds, false);

        assertThrows(IllegalArgumentException.class, () -> {
            manager.recordSpeech(null, "发言内容");
        });
    }

    @Test
    @DisplayName("记录遗言 - 正常")
    void testRecordLastWords_Normal() {
        SpeechManager manager = new SpeechManager(playerIds, false);

        manager.recordLastWords(1L, "我是好人，请相信我");

        assertEquals("我是好人，请相信我", manager.getLastWords(1L));

        Map<Long, String> allLastWords = manager.getAllLastWords();
        assertEquals(1, allLastWords.size());
    }

    @Test
    @DisplayName("记录遗言 - 空内容转为无遗言")
    void testRecordLastWords_EmptyContent() {
        SpeechManager manager = new SpeechManager(playerIds, false);

        manager.recordLastWords(1L, "");
        manager.recordLastWords(2L, null);

        assertEquals("[无遗言]", manager.getLastWords(1L));
        assertEquals("[无遗言]", manager.getLastWords(2L));
    }

    @Test
    @DisplayName("移动到下一个发言者")
    void testMoveToNextSpeaker() {
        SpeechManager manager = new SpeechManager(playerIds, false);

        assertEquals(1L, manager.getCurrentSpeaker());

        Long next = manager.moveToNextSpeaker();
        assertEquals(2L, next);
        assertEquals(2L, manager.getCurrentSpeaker());

        next = manager.moveToNextSpeaker();
        assertEquals(3L, next);

        next = manager.moveToNextSpeaker();
        assertEquals(4L, next);

        next = manager.moveToNextSpeaker();
        assertNull(next);
        assertTrue(manager.isAllSpeechFinished());
    }

    @Test
    @DisplayName("发言进度显示")
    void testGetProgress() {
        SpeechManager manager = new SpeechManager(playerIds, false);

        assertEquals("1/4", manager.getProgress());

        manager.moveToNextSpeaker();
        assertEquals("2/4", manager.getProgress());

        manager.moveToNextSpeaker();
        assertEquals("3/4", manager.getProgress());

        manager.moveToNextSpeaker();
        assertEquals("4/4", manager.getProgress());

        manager.moveToNextSpeaker();
        assertEquals("4/4（已完成）", manager.getProgress());
    }

    @Test
    @DisplayName("重置发言管理器")
    void testReset() {
        SpeechManager manager = new SpeechManager(playerIds, false);

        manager.recordSpeech(1L, "发言1");
        manager.recordLastWords(2L, "遗言测试");  // 记录遗言
        manager.moveToNextSpeaker();
        manager.moveToNextSpeaker();

        manager.reset();

        assertEquals(1L, manager.getCurrentSpeaker());
        assertFalse(manager.isAllSpeechFinished());
        assertTrue(manager.getAllSpeeches().isEmpty());
        assertFalse(manager.getAllLastWords().isEmpty());  // 遗言不会被清空
    }

    @Test
    @DisplayName("清空所有记录")
    void testClear() {
        SpeechManager manager = new SpeechManager(playerIds, false);

        manager.recordSpeech(1L, "发言1");
        manager.recordLastWords(2L, "遗言");
        manager.moveToNextSpeaker();

        manager.clear();

        assertEquals(1L, manager.getCurrentSpeaker());
        assertTrue(manager.getAllSpeeches().isEmpty());
        assertTrue(manager.getAllLastWords().isEmpty());
    }

    @Test
    @DisplayName("获取发言顺序")
    void testGetSpeechOrder() {
        SpeechManager manager = new SpeechManager(playerIds, false);

        List<Long> order = manager.getSpeechOrder();
        assertEquals(playerIds, order);

        // 确保返回的是副本，修改不影响原列表
        order.clear();
        assertEquals(4, manager.getSpeechOrder().size());
    }

    @Test
    @DisplayName("完整流程测试")
    void testFullFlow() {
        SpeechManager manager = new SpeechManager(playerIds, false);

        // 所有玩家依次发言
        for (Long playerId : playerIds) {
            assertEquals(playerId, manager.getCurrentSpeaker());
            manager.recordSpeech(playerId, "发言内容-" + playerId);

            if (playerId < 4L) {
                manager.moveToNextSpeaker();
            }
        }

        // 完成所有发言
        manager.moveToNextSpeaker();
        assertTrue(manager.isAllSpeechFinished());

        // 验证所有发言都已记录
        assertEquals(4, manager.getAllSpeeches().size());
    }
}

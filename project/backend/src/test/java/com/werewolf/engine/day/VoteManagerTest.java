package com.werewolf.engine.day;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VoteManager 单元测试
 */
@DisplayName("投票管理器测试")
class VoteManagerTest {

    private VoteManager voteManager;

    @BeforeEach
    void setUp() {
        voteManager = new VoteManager();
    }

    @Test
    @DisplayName("添加投票 - 正常投票")
    void testAddVote_Normal() {
        voteManager.addVote(1L, 2L);
        voteManager.addVote(3L, 2L);
        voteManager.addVote(4L, 5L);

        assertEquals(3, voteManager.getTotalVoters());

        Map<Long, Long> voteDetails = voteManager.getVoteDetails();
        assertEquals(2L, voteDetails.get(1L));
        assertEquals(2L, voteDetails.get(3L));
        assertEquals(5L, voteDetails.get(4L));
    }

    @Test
    @DisplayName("添加投票 - 弃票")
    void testAddVote_Abstain() {
        voteManager.addVote(1L, null);  // 弃票
        voteManager.addVote(2L, 3L);

        assertEquals(1, voteManager.getTotalVoters());

        Set<Long> abstained = voteManager.getAbstainedPlayers();
        assertTrue(abstained.contains(1L));
        assertFalse(abstained.contains(2L));
    }

    @Test
    @DisplayName("添加投票 - 改变投票")
    void testAddVote_ChangeVote() {
        voteManager.addVote(1L, 2L);
        voteManager.addVote(1L, 3L);  // 改变投票

        Map<Long, Long> voteDetails = voteManager.getVoteDetails();
        assertEquals(3L, voteDetails.get(1L));
    }

    @Test
    @DisplayName("添加投票 - 从弃票改为投票")
    void testAddVote_FromAbstainToVote() {
        voteManager.addVote(1L, null);  // 先弃票
        voteManager.addVote(1L, 2L);    // 再投票

        assertEquals(1, voteManager.getTotalVoters());
        assertFalse(voteManager.getAbstainedPlayers().contains(1L));
    }

    @Test
    @DisplayName("添加投票 - 投票者ID为空抛出异常")
    void testAddVote_NullVoterId() {
        assertThrows(IllegalArgumentException.class, () -> {
            voteManager.addVote(null, 2L);
        });
    }

    @Test
    @DisplayName("统计投票结果 - 正常情况")
    void testCountVotes_Normal() {
        voteManager.addVote(1L, 3L);
        voteManager.addVote(2L, 3L);
        voteManager.addVote(4L, 5L);

        VoteManager.VoteResult result = voteManager.countVotes();

        assertNotNull(result);
        assertEquals(3L, result.getExiledPlayerId());
        assertEquals(2, result.getMaxVotes());
        assertFalse(result.isTie());
        assertTrue(result.hasExiledPlayer());
    }

    @Test
    @DisplayName("统计投票结果 - 平票情况")
    void testCountVotes_Tie() {
        voteManager.addVote(1L, 3L);
        voteManager.addVote(2L, 4L);

        VoteManager.VoteResult result = voteManager.countVotes();

        assertNotNull(result);
        assertNull(result.getExiledPlayerId());
        assertEquals(1, result.getMaxVotes());
        assertTrue(result.isTie());
        assertFalse(result.hasExiledPlayer());
    }

    @Test
    @DisplayName("统计投票结果 - 无人投票")
    void testCountVotes_NoVotes() {
        VoteManager.VoteResult result = voteManager.countVotes();

        assertNotNull(result);
        assertNull(result.getExiledPlayerId());
        assertEquals(0, result.getMaxVotes());
        assertTrue(result.isTie());
        assertFalse(result.hasExiledPlayer());
    }

    @Test
    @DisplayName("统计投票结果 - 多人平票")
    void testCountVotes_MultiTie() {
        voteManager.addVote(1L, 3L);
        voteManager.addVote(2L, 4L);
        voteManager.addVote(5L, 6L);

        VoteManager.VoteResult result = voteManager.countVotes();

        assertTrue(result.isTie());
        assertNull(result.getExiledPlayerId());
    }

    @Test
    @DisplayName("票数分布正确")
    void testVoteDistribution() {
        voteManager.addVote(1L, 3L);
        voteManager.addVote(2L, 3L);
        voteManager.addVote(4L, 3L);
        voteManager.addVote(5L, 6L);

        VoteManager.VoteResult result = voteManager.countVotes();
        Map<Long, Integer> distribution = result.getVoteDistribution();

        assertEquals(3, distribution.get(3L));
        assertEquals(1, distribution.get(6L));
    }

    @Test
    @DisplayName("清空投票记录")
    void testClear() {
        voteManager.addVote(1L, 2L);
        voteManager.addVote(3L, null);

        voteManager.clear();

        assertEquals(0, voteManager.getTotalVoters());
        assertTrue(voteManager.getAbstainedPlayers().isEmpty());
        assertTrue(voteManager.getVoteDetails().isEmpty());
    }
}

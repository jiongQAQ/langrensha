package com.werewolf.engine.day;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 投票管理器
 * 负责收集和统计白天投票结果
 */
@Slf4j
public class VoteManager {

    // 投票记录：投票者ID -> 目标ID
    private final Map<Long, Long> votes = new HashMap<>();

    // 弃票的玩家
    private final Set<Long> abstainedPlayers = new HashSet<>();

    /**
     * 添加一票
     * @param voterId 投票者ID
     * @param targetId 目标玩家ID（null表示弃票）
     */
    public void addVote(Long voterId, Long targetId) {
        if (voterId == null) {
            throw new IllegalArgumentException("投票者ID不能为空");
        }

        if (targetId == null) {
            // 弃票
            abstainedPlayers.add(voterId);
            votes.remove(voterId);  // 移除之前的投票（如果有）
            log.info("玩家 {} 选择弃票", voterId);
        } else {
            votes.put(voterId, targetId);
            abstainedPlayers.remove(voterId);  // 移除弃票记录（如果有）
            log.info("玩家 {} 投票给玩家 {}", voterId, targetId);
        }
    }

    /**
     * 统计投票结果
     * @return 投票结果
     */
    public VoteResult countVotes() {
        if (votes.isEmpty()) {
            log.warn("没有任何玩家投票");
            return new VoteResult(null, new HashMap<>(), 0, true);
        }

        // 统计每个玩家获得的票数
        Map<Long, Integer> voteCount = new HashMap<>();
        for (Long targetId : votes.values()) {
            voteCount.put(targetId, voteCount.getOrDefault(targetId, 0) + 1);
        }

        // 找出最高票数
        int maxVotes = voteCount.values().stream()
                .max(Integer::compareTo)
                .orElse(0);

        // 找出所有得票最多的玩家
        List<Long> topPlayers = voteCount.entrySet().stream()
                .filter(entry -> entry.getValue() == maxVotes)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 判断是否平票
        boolean isTie = topPlayers.size() > 1;

        Long exiledPlayerId = isTie ? null : topPlayers.get(0);

        if (isTie) {
            log.warn("平票发生！得票最多的玩家: {}, 票数: {}", topPlayers, maxVotes);
            log.warn("根据规则，当日无人出局");
        } else {
            log.info("玩家 {} 获得最高票数 {}, 将被放逐", exiledPlayerId, maxVotes);
        }

        return new VoteResult(exiledPlayerId, voteCount, maxVotes, isTie);
    }

    /**
     * 获取投票详情
     * @return 投票详情映射（投票者ID -> 目标ID）
     */
    public Map<Long, Long> getVoteDetails() {
        return new HashMap<>(votes);
    }

    /**
     * 获取弃票玩家列表
     * @return 弃票玩家ID集合
     */
    public Set<Long> getAbstainedPlayers() {
        return new HashSet<>(abstainedPlayers);
    }

    /**
     * 获取总投票人数（不包括弃票）
     * @return 投票人数
     */
    public int getTotalVoters() {
        return votes.size();
    }

    /**
     * 清空所有投票
     */
    public void clear() {
        votes.clear();
        abstainedPlayers.clear();
        log.debug("投票记录已清空");
    }

    /**
     * 投票结果
     */
    @Getter
    public static class VoteResult {
        // 被放逐的玩家ID（null表示平票，无人出局）
        private final Long exiledPlayerId;

        // 票数分布（玩家ID -> 获得票数）
        private final Map<Long, Integer> voteDistribution;

        // 最高票数
        private final int maxVotes;

        // 是否平票
        private final boolean tie;

        public VoteResult(Long exiledPlayerId,
                         Map<Long, Integer> voteDistribution,
                         int maxVotes,
                         boolean tie) {
            this.exiledPlayerId = exiledPlayerId;
            this.voteDistribution = new HashMap<>(voteDistribution);
            this.maxVotes = maxVotes;
            this.tie = tie;
        }

        /**
         * 是否有人被放逐
         */
        public boolean hasExiledPlayer() {
            return exiledPlayerId != null;
        }
    }
}

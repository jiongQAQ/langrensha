package com.werewolf.engine.night;

import com.werewolf.domain.model.GameState;
import com.werewolf.domain.model.Player;
import com.werewolf.domain.role.*;
import lombok.Data;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 夜晚行动处理器
 * 处理狼人击杀、预言家查验、女巫用药等夜晚行动
 *
 * @author Claude & User
 */
@Slf4j
public class NightActionProcessor {

    /**
     * 处理狼人击杀行动
     *
     * @param gameState 游戏状态
     * @param votes 狼人投票结果 (玩家ID -> 目标玩家ID)
     * @return 击杀结果
     */
    public WerewolfKillResult processWerewolfKill(GameState gameState, Map<Long, Long> votes) {
        log.debug("处理狼人击杀行动，投票数: {}", votes.size());

        // 获取所有狼人玩家
        List<Player> werewolves = gameState.getAlivePlayers().stream()
                .filter(p -> p.getRole() instanceof Werewolf)
                .collect(Collectors.toList());

        // 验证投票
        if (votes.isEmpty()) {
            log.warn("狼人未进行投票");
            return WerewolfKillResult.builder()
                    .success(false)
                    .targetPlayerId(null)
                    .message("狼人未进行投票")
                    .build();
        }

        // 统计票数
        Map<Long, Long> voteCount = new HashMap<>();
        votes.values().forEach(targetId ->
            voteCount.put(targetId, voteCount.getOrDefault(targetId, 0L) + 1)
        );

        // 找出票数最多的目标
        Long targetPlayerId = voteCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (targetPlayerId == null) {
            log.warn("狼人击杀目标为空");
            return WerewolfKillResult.builder()
                    .success(false)
                    .targetPlayerId(null)
                    .message("未能确定击杀目标")
                    .build();
        }

        Player targetPlayer = gameState.getPlayerById(targetPlayerId);
        if (targetPlayer == null || !targetPlayer.isAlive()) {
            log.warn("狼人击杀目标无效: {}", targetPlayerId);
            return WerewolfKillResult.builder()
                    .success(false)
                    .targetPlayerId(targetPlayerId)
                    .message("击杀目标无效")
                    .build();
        }

        log.info("狼人决定击杀玩家: {} ({})", targetPlayer.getName(), targetPlayerId);

        return WerewolfKillResult.builder()
                .success(true)
                .targetPlayerId(targetPlayerId)
                .targetPlayerName(targetPlayer.getName())
                .votes(votes)
                .voteCount(voteCount)
                .message("狼人击杀目标确定")
                .build();
    }

    /**
     * 处理预言家查验行动
     *
     * @param gameState 游戏状态
     * @param seerPlayerId 预言家玩家ID
     * @param targetPlayerId 查验目标玩家ID
     * @return 查验结果
     */
    public SeerCheckResult processSeerCheck(GameState gameState, Long seerPlayerId, Long targetPlayerId) {
        log.debug("处理预言家查验行动: 预言家={}, 目标={}", seerPlayerId, targetPlayerId);

        // 验证预言家
        Player seer = gameState.getPlayerById(seerPlayerId);
        if (seer == null || !seer.isAlive() || !(seer.getRole() instanceof Seer)) {
            log.warn("预言家无效: {}", seerPlayerId);
            return SeerCheckResult.builder()
                    .success(false)
                    .message("预言家无效")
                    .build();
        }

        // 验证目标
        Player target = gameState.getPlayerById(targetPlayerId);
        if (target == null || !target.isAlive()) {
            log.warn("查验目标无效: {}", targetPlayerId);
            return SeerCheckResult.builder()
                    .success(false)
                    .message("查验目标无效")
                    .build();
        }

        // 不能查验自己
        if (seerPlayerId.equals(targetPlayerId)) {
            log.warn("预言家不能查验自己");
            return SeerCheckResult.builder()
                    .success(false)
                    .message("不能查验自己")
                    .build();
        }

        // 获取查验结果
        boolean isWerewolf = target.getRole() instanceof Werewolf;
        String result = isWerewolf ? "狼人" : "好人";

        log.info("预言家查验结果: 玩家{} 是 {}", target.getName(), result);

        return SeerCheckResult.builder()
                .success(true)
                .seerPlayerId(seerPlayerId)
                .targetPlayerId(targetPlayerId)
                .targetPlayerName(target.getName())
                .isWerewolf(isWerewolf)
                .result(result)
                .message("查验完成")
                .build();
    }

    /**
     * 处理女巫用药行动
     *
     * @param gameState 游戏状态
     * @param witchPlayerId 女巫玩家ID
     * @param killedPlayerId 被狼人击杀的玩家ID
     * @param useAntidote 是否使用解药
     * @param usePoisonTargetId 使用毒药的目标ID（null表示不使用）
     * @return 女巫用药结果
     */
    public WitchActionResult processWitchAction(
            GameState gameState,
            Long witchPlayerId,
            Long killedPlayerId,
            boolean useAntidote,
            Long usePoisonTargetId) {

        log.debug("处理女巫用药: 女巫={}, 被杀={}, 解药={}, 毒药目标={}",
                witchPlayerId, killedPlayerId, useAntidote, usePoisonTargetId);

        // 验证女巫
        Player witch = gameState.getPlayerById(witchPlayerId);
        if (witch == null || !witch.isAlive() || !(witch.getRole() instanceof Witch)) {
            log.warn("女巫无效: {}", witchPlayerId);
            return WitchActionResult.builder()
                    .success(false)
                    .message("女巫无效")
                    .build();
        }

        Witch witchRole = (Witch) witch.getRole();

        // 检查是否同时使用解药和毒药
        if (useAntidote && usePoisonTargetId != null) {
            log.warn("女巫不能同时使用解药和毒药");
            return WitchActionResult.builder()
                    .success(false)
                    .message("解药和毒药不能同时使用")
                    .build();
        }

        boolean antidoteUsed = false;
        Long savedPlayerId = null;
        boolean poisonUsed = false;
        Long poisonedPlayerId = null;

        // 处理解药
        if (useAntidote) {
            if (!witchRole.getAntidoteSkill().isAvailable()) {
                log.warn("解药已用完");
                return WitchActionResult.builder()
                        .success(false)
                        .message("解药已用完")
                        .build();
            }

            // 检查首夜自救限制
            if (witchRole.isFirstNight() && killedPlayerId.equals(witchPlayerId)) {
                log.warn("女巫首夜不能自救");
                return WitchActionResult.builder()
                        .success(false)
                        .message("首夜不能自救")
                        .build();
            }

            if (killedPlayerId == null) {
                log.warn("没有人被狼人击杀，无法使用解药");
                return WitchActionResult.builder()
                        .success(false)
                        .message("无人被杀，无法使用解药")
                        .build();
            }

            witchRole.getAntidoteSkill().use();
            antidoteUsed = true;
            savedPlayerId = killedPlayerId;
            log.info("女巫使用解药救活玩家: {}", killedPlayerId);
        }

        // 处理毒药
        if (usePoisonTargetId != null) {
            if (!witchRole.getPoisonSkill().isAvailable()) {
                log.warn("毒药已用完");
                return WitchActionResult.builder()
                        .success(false)
                        .message("毒药已用完")
                        .build();
            }

            Player poisonTarget = gameState.getPlayerById(usePoisonTargetId);
            if (poisonTarget == null || !poisonTarget.isAlive()) {
                log.warn("毒药目标无效: {}", usePoisonTargetId);
                return WitchActionResult.builder()
                        .success(false)
                        .message("毒药目标无效")
                        .build();
            }

            witchRole.getPoisonSkill().use();
            poisonUsed = true;
            poisonedPlayerId = usePoisonTargetId;
            log.info("女巫使用毒药毒死玩家: {}", usePoisonTargetId);
        }

        // 标记首夜已过
        if (witchRole.isFirstNight()) {
            witchRole.markFirstNightPassed();
        }

        return WitchActionResult.builder()
                .success(true)
                .witchPlayerId(witchPlayerId)
                .antidoteUsed(antidoteUsed)
                .savedPlayerId(savedPlayerId)
                .poisonUsed(poisonUsed)
                .poisonedPlayerId(poisonedPlayerId)
                .message("女巫行动完成")
                .build();
    }

    /**
     * 计算夜晚最终死亡名单
     *
     * @param werewolfKillResult 狼人击杀结果
     * @param witchActionResult 女巫用药结果
     * @return 死亡玩家ID列表
     */
    public List<Long> calculateDeaths(WerewolfKillResult werewolfKillResult, WitchActionResult witchActionResult) {
        List<Long> deaths = new ArrayList<>();

        // 狼人击杀的玩家
        if (werewolfKillResult.isSuccess() && werewolfKillResult.getTargetPlayerId() != null) {
            deaths.add(werewolfKillResult.getTargetPlayerId());
        }

        // 女巫救活的玩家从死亡名单移除
        if (witchActionResult.isAntidoteUsed() && witchActionResult.getSavedPlayerId() != null) {
            deaths.remove(witchActionResult.getSavedPlayerId());
        }

        // 女巫毒死的玩家加入死亡名单
        if (witchActionResult.isPoisonUsed() && witchActionResult.getPoisonedPlayerId() != null) {
            deaths.add(witchActionResult.getPoisonedPlayerId());
        }

        log.info("夜晚最终死亡名单: {}", deaths);
        return deaths;
    }

    // ========== 结果类定义 ==========

    @Data
    @Builder
    public static class WerewolfKillResult {
        private boolean success;
        private Long targetPlayerId;
        private String targetPlayerName;
        private Map<Long, Long> votes;
        private Map<Long, Long> voteCount;
        private String message;
    }

    @Data
    @Builder
    public static class SeerCheckResult {
        private boolean success;
        private Long seerPlayerId;
        private Long targetPlayerId;
        private String targetPlayerName;
        private boolean isWerewolf;
        private String result;
        private String message;
    }

    @Data
    @Builder
    public static class WitchActionResult {
        private boolean success;
        private Long witchPlayerId;
        private boolean antidoteUsed;
        private Long savedPlayerId;
        private boolean poisonUsed;
        private Long poisonedPlayerId;
        private String message;
    }
}

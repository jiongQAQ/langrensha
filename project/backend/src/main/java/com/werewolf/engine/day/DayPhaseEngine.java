package com.werewolf.engine.day;

import com.werewolf.domain.model.GameEvent;
import com.werewolf.domain.model.GameState;
import com.werewolf.domain.model.Player;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 白天阶段引擎
 * 负责执行完整的白天流程
 */
@Slf4j
public class DayPhaseEngine {

    /**
     * 执行完整的白天流程
     *
     * @param gameState 游戏状态
     * @param lastNightDeaths 昨晚死亡的玩家ID列表
     * @param lastWords 遗言映射（玩家ID -> 遗言内容）
     * @param speeches 发言映射（玩家ID -> 发言内容）
     * @param votes 投票映射（玩家ID -> 目标ID）
     * @return 白天流程执行结果
     */
    public DayPhaseResult executeDayPhase(
            GameState gameState,
            List<Long> lastNightDeaths,
            java.util.Map<Long, String> lastWords,
            java.util.Map<Long, String> speeches,
            java.util.Map<Long, Long> votes) {

        log.info("=== 开始执行白天流程 (回合{}) ===", gameState.getCurrentRound());

        DayPhaseResult result = DayPhaseResult.builder()
                .lastNightDeaths(new ArrayList<>(lastNightDeaths))
                .build();

        // 1. 公布死讯
        log.info("--- 阶段1: 公布死讯 ---");
        announceDeaths(gameState, lastNightDeaths, result);

        // 2. 遗言环节
        log.info("--- 阶段2: 遗言环节 ---");
        processLastWords(gameState, lastNightDeaths, lastWords, result);

        // 3. 发言环节
        log.info("--- 阶段3: 发言环节 ---");
        processSpeeches(gameState, speeches, result);

        // 4. 投票环节
        log.info("--- 阶段4: 投票环节 ---");
        VoteManager voteManager = processVoting(gameState, votes, result);

        // 5. 放逐结果处理
        log.info("--- 阶段5: 放逐结果处理 ---");
        processExileResult(gameState, voteManager, result);

        log.info("=== 白天流程执行完毕，放逐人数: {} ===",
                result.getExiledPlayerId() != null ? 1 : 0);

        return result;
    }

    /**
     * 公布死讯
     */
    private void announceDeaths(GameState gameState, List<Long> deaths, DayPhaseResult result) {
        if (deaths == null || deaths.isEmpty()) {
            log.info("昨晚是平安夜，无人死亡");
            // 记录事件
            gameState.addEvent(GameEvent.builder()
                    .type(GameEvent.EventType.SYSTEM_MESSAGE)
                    .description("昨晚是平安夜，无人死亡")
                    .isPublic(true)
                    .build());
            return;
        }

        // 获取死亡玩家信息
        List<Player> deadPlayers = deaths.stream()
                .map(id -> gameState.getPlayerById(id))
                .filter(p -> p != null)
                .collect(Collectors.toList());

        if (deadPlayers.isEmpty()) {
            log.warn("昨晚死亡名单中的玩家未找到: {}", deaths);
            return;
        }

        // 公布死讯（不公布死亡原因）
        String deathAnnouncement = String.format("昨晚死亡的玩家有：%s",
                deadPlayers.stream()
                        .map(p -> String.format("%s (%d号位)", p.getName(), p.getSeatNumber()))
                        .collect(Collectors.joining("、")));

        log.info(deathAnnouncement);

        // 记录事件
        gameState.addEvent(GameEvent.builder()
                .type(GameEvent.EventType.DEATH_ANNOUNCEMENT)
                .description(deathAnnouncement)
                .isPublic(true)
                .build());
    }

    /**
     * 处理遗言环节
     */
    private void processLastWords(GameState gameState, List<Long> deaths,
                                  java.util.Map<Long, String> lastWords, DayPhaseResult result) {
        if (deaths == null || deaths.isEmpty()) {
            log.info("无人死亡，跳过遗言环节");
            return;
        }

        SpeechManager lastWordsManager = new SpeechManager(deaths);

        for (Long deadPlayerId : deaths) {
            Player deadPlayer = gameState.getPlayerById(deadPlayerId);
            if (deadPlayer == null) {
                log.warn("死亡玩家ID {} 未找到", deadPlayerId);
                continue;
            }

            String lastWord = lastWords != null ? lastWords.get(deadPlayerId) : null;
            if (lastWord == null || lastWord.trim().isEmpty()) {
                lastWord = "[无遗言]";
            }

            lastWordsManager.recordLastWords(deadPlayerId, lastWord);

            log.info("玩家 {} ({}) 的遗言: {}",
                    deadPlayer.getName(), deadPlayer.getSeatNumber(),
                    lastWord.length() > 50 ? lastWord.substring(0, 50) + "..." : lastWord);

            // 记录遗言事件
            gameState.addEvent(GameEvent.builder()
                    .type(GameEvent.EventType.LAST_WORDS)
                    .playerId(deadPlayerId)
                    .description(String.format("%s的遗言：%s", deadPlayer.getName(), lastWord))
                    .isPublic(true)
                    .build());
        }

        result.setLastWordsRecords(lastWordsManager.getAllLastWords());
    }

    /**
     * 处理发言环节
     */
    private void processSpeeches(GameState gameState, java.util.Map<Long, String> speeches,
                                DayPhaseResult result) {
        // 获取所有存活玩家
        List<Long> alivePlayers = gameState.getPlayers().stream()
                .filter(Player::isAlive)
                .map(Player::getId)
                .collect(Collectors.toList());

        if (alivePlayers.isEmpty()) {
            log.warn("没有存活玩家，跳过发言环节");
            return;
        }

        // 按座位号顺序发言
        SpeechManager speechManager = new SpeechManager(alivePlayers, false);

        for (Long playerId : alivePlayers) {
            Player player = gameState.getPlayerById(playerId);
            if (player == null || !player.isAlive()) {
                continue;
            }

            String speech = speeches != null ? speeches.get(playerId) : null;
            if (speech == null || speech.trim().isEmpty()) {
                speech = "[沉默]";
            }

            speechManager.recordSpeech(playerId, speech);

            log.info("玩家 {} ({}) 发言: {}",
                    player.getName(), player.getSeatNumber(),
                    speech.length() > 50 ? speech.substring(0, 50) + "..." : speech);

            // 记录发言事件
            gameState.addEvent(GameEvent.builder()
                    .type(GameEvent.EventType.PLAYER_SPEECH)
                    .playerId(playerId)
                    .description(String.format("%s: %s", player.getName(), speech))
                    .isPublic(true)
                    .build());

            // 更新玩家发言状态
            player.setHasSpoken(true);
        }

        result.setSpeechRecords(speechManager.getAllSpeeches());
        log.info("发言环节完成，共 {} 位玩家发言", alivePlayers.size());
    }

    /**
     * 处理投票环节
     */
    private VoteManager processVoting(GameState gameState, java.util.Map<Long, Long> votes,
                                     DayPhaseResult result) {
        VoteManager voteManager = new VoteManager();

        // 获取所有存活玩家
        List<Player> alivePlayers = gameState.getPlayers().stream()
                .filter(Player::isAlive)
                .collect(Collectors.toList());

        if (alivePlayers.isEmpty()) {
            log.warn("没有存活玩家，跳过投票环节");
            return voteManager;
        }

        // 收集投票
        if (votes != null && !votes.isEmpty()) {
            for (java.util.Map.Entry<Long, Long> entry : votes.entrySet()) {
                Long voterId = entry.getKey();
                Long targetId = entry.getValue();

                // 验证投票者是否存活
                Player voter = gameState.getPlayerById(voterId);
                if (voter == null || !voter.isAlive()) {
                    log.warn("玩家 {} 不存在或已死亡，忽略其投票", voterId);
                    continue;
                }

                voteManager.addVote(voterId, targetId);

                // 更新玩家投票状态
                voter.setHasVoted(true);
                voter.setVoteTargetId(targetId);

                // 记录投票日志
                if (targetId != null) {
                    Player target = gameState.getPlayerById(targetId);
                    log.info("玩家 {} 投票给 {}",
                            voter.getName(),
                            target != null ? target.getName() : "未知玩家");
                } else {
                    log.info("玩家 {} 选择弃票", voter.getName());
                }
            }
        } else {
            log.warn("没有收到任何投票");
        }

        // 统计投票结果
        VoteManager.VoteResult voteResult = voteManager.countVotes();
        result.setVoteResult(voteResult);

        // 记录投票结果事件
        StringBuilder voteInfo = new StringBuilder("投票结果：\n");
        for (java.util.Map.Entry<Long, Integer> entry : voteResult.getVoteDistribution().entrySet()) {
            Player target = gameState.getPlayerById(entry.getKey());
            if (target != null) {
                voteInfo.append(String.format("- %s: %d票\n", target.getName(), entry.getValue()));
            }
        }

        gameState.addEvent(GameEvent.builder()
                .type(GameEvent.EventType.VOTE_RESULT)
                .description(voteInfo.toString())
                .isPublic(true)
                .build());

        return voteManager;
    }

    /**
     * 处理放逐结果
     */
    private void processExileResult(GameState gameState, VoteManager voteManager,
                                   DayPhaseResult result) {
        VoteManager.VoteResult voteResult = result.getVoteResult();

        if (voteResult == null) {
            log.warn("投票结果为空");
            return;
        }

        if (voteResult.isTie()) {
            // 平票，无人出局
            log.info("发生平票，当日无人出局");
            gameState.addEvent(GameEvent.builder()
                    .type(GameEvent.EventType.SYSTEM_MESSAGE)
                    .description("发生平票，当日无人出局")
                    .isPublic(true)
                    .build());
            return;
        }

        if (!voteResult.hasExiledPlayer()) {
            log.info("无人被放逐");
            return;
        }

        // 处理放逐玩家
        Long exiledPlayerId = voteResult.getExiledPlayerId();
        Player exiledPlayer = gameState.getPlayerById(exiledPlayerId);

        if (exiledPlayer == null) {
            log.error("被放逐的玩家ID {} 未找到", exiledPlayerId);
            return;
        }

        // 标记玩家死亡
        exiledPlayer.markDead(Player.DeathReason.VOTED_OUT);

        result.setExiledPlayerId(exiledPlayerId);

        log.info("玩家 {} ({}) 被放逐出局，获得 {} 票",
                exiledPlayer.getName(),
                exiledPlayer.getSeatNumber(),
                voteResult.getMaxVotes());

        // 记录放逐事件
        gameState.addEvent(GameEvent.builder()
                .type(GameEvent.EventType.PLAYER_EXILED)
                .playerId(exiledPlayerId)
                .description(String.format("%s被投票放逐，获得%d票",
                        exiledPlayer.getName(), voteResult.getMaxVotes()))
                .isPublic(true)
                .build());

        // 记录最后遗言（30秒）
        log.info("玩家 {} 可以发表最后遗言（30秒）", exiledPlayer.getName());
    }

    /**
     * 白天流程执行结果
     */
    @Data
    @Builder
    public static class DayPhaseResult {
        // 昨晚死亡的玩家ID列表
        private List<Long> lastNightDeaths;

        // 遗言记录（玩家ID -> 遗言内容）
        private java.util.Map<Long, String> lastWordsRecords;

        // 发言记录（玩家ID -> 发言内容）
        private java.util.Map<Long, String> speechRecords;

        // 投票结果
        private VoteManager.VoteResult voteResult;

        // 被放逐的玩家ID（null表示无人被放逐）
        private Long exiledPlayerId;
    }
}

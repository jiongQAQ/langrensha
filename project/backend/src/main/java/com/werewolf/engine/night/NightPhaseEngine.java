package com.werewolf.engine.night;

import com.werewolf.domain.model.GameEvent;
import com.werewolf.domain.model.GameState;
import com.werewolf.domain.model.Player;
import com.werewolf.domain.role.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 夜晚阶段引擎
 * 协调整个夜晚流程的执行
 *
 * @author Claude & User
 */
@Slf4j
@Component
public class NightPhaseEngine {

    private final NightActionProcessor actionProcessor;

    public NightPhaseEngine() {
        this.actionProcessor = new NightActionProcessor();
    }

    /**
     * 执行完整的夜晚流程
     *
     * @param gameState 游戏状态
     * @param werewolfVotes 狼人投票
     * @param seerCheckTarget 预言家查验目标
     * @param witchUseAntidote 女巫是否使用解药
     * @param witchPoisonTarget 女巫毒药目标
     * @return 夜晚结果
     */
    public NightResult executeNightPhase(
            GameState gameState,
            Map<Long, Long> werewolfVotes,
            Long seerCheckTarget,
            boolean witchUseAntidote,
            Long witchPoisonTarget) {

        log.info("=== 开始执行夜晚流程 (回合{}) ===", gameState.getCurrentRound());

        NightResult result = new NightResult();

        // 阶段1: 狼人击杀
        log.info("--- 阶段1: 狼人行动 ---");
        NightActionProcessor.WerewolfKillResult killResult =
                actionProcessor.processWerewolfKill(gameState, werewolfVotes);
        result.setWerewolfKillResult(killResult);

        // 记录狼人击杀事件
        if (killResult.isSuccess()) {
            GameEvent killEvent = GameEvent.createPrivateEvent(
                    GameEvent.EventType.WEREWOLF_KILL,
                    killResult.getTargetPlayerId(),
                    String.format("狼人决定击杀%s", killResult.getTargetPlayerName())
            );
            gameState.addEvent(killEvent);
        }

        // 阶段2: 预言家查验
        log.info("--- 阶段2: 预言家行动 ---");
        Player seerPlayer = findSeer(gameState);
        if (seerPlayer != null && seerCheckTarget != null) {
            NightActionProcessor.SeerCheckResult checkResult =
                    actionProcessor.processSeerCheck(gameState, seerPlayer.getId(), seerCheckTarget);
            result.setSeerCheckResult(checkResult);

            // 记录预言家查验事件（私有）
            if (checkResult.isSuccess()) {
                GameEvent checkEvent = GameEvent.createPrivateEvent(
                        GameEvent.EventType.SEER_CHECK,
                        seerPlayer.getId(),
                        String.format("预言家查验了%s，结果是%s",
                                checkResult.getTargetPlayerName(),
                                checkResult.getResult())
                );
                checkEvent.setTargetPlayerId(seerCheckTarget);
                gameState.addEvent(checkEvent);
            }
        } else {
            log.info("预言家未行动");
        }

        // 阶段3: 女巫用药
        log.info("--- 阶段3: 女巫行动 ---");
        Player witchPlayer = findWitch(gameState);
        if (witchPlayer != null) {
            Long killedPlayerId = killResult.isSuccess() ? killResult.getTargetPlayerId() : null;

            NightActionProcessor.WitchActionResult witchResult =
                    actionProcessor.processWitchAction(
                            gameState,
                            witchPlayer.getId(),
                            killedPlayerId,
                            witchUseAntidote,
                            witchPoisonTarget
                    );
            result.setWitchActionResult(witchResult);

            // 记录女巫用药事件（私有）
            if (witchResult.isSuccess()) {
                if (witchResult.isAntidoteUsed()) {
                    GameEvent antidoteEvent = GameEvent.createPrivateEvent(
                            GameEvent.EventType.WITCH_ANTIDOTE,
                            witchPlayer.getId(),
                            String.format("女巫使用解药救活了%d号玩家", witchResult.getSavedPlayerId())
                    );
                    gameState.addEvent(antidoteEvent);
                }

                if (witchResult.isPoisonUsed()) {
                    GameEvent poisonEvent = GameEvent.createPrivateEvent(
                            GameEvent.EventType.WITCH_POISON,
                            witchPlayer.getId(),
                            String.format("女巫使用毒药毒死了%d号玩家", witchResult.getPoisonedPlayerId())
                    );
                    gameState.addEvent(poisonEvent);
                }
            }
        } else {
            log.info("女巫未行动");
        }

        // 阶段4: 计算最终死亡名单
        log.info("--- 阶段4: 计算死亡名单 ---");
        List<Long> deaths = actionProcessor.calculateDeaths(
                killResult,
                result.getWitchActionResult() != null ? result.getWitchActionResult()
                        : NightActionProcessor.WitchActionResult.builder()
                        .success(false)
                        .antidoteUsed(false)
                        .poisonUsed(false)
                        .build()
        );

        result.setDeaths(deaths);

        // 更新玩家死亡状态
        for (Long playerId : deaths) {
            Player player = gameState.getPlayerById(playerId);
            if (player != null) {
                // 判断死亡原因
                Player.DeathReason deathReason;
                if (result.getWitchActionResult() != null &&
                        result.getWitchActionResult().isPoisonUsed() &&
                        playerId.equals(result.getWitchActionResult().getPoisonedPlayerId())) {
                    deathReason = Player.DeathReason.POISONED;
                } else {
                    deathReason = Player.DeathReason.KILLED_BY_WEREWOLF;
                }

                player.markDead(deathReason);
                log.info("玩家{} ({}) 死亡，原因: {}", player.getName(), playerId, deathReason);
            }
        }

        // 更新游戏状态的昨晚死亡列表
        gameState.getLastNightDeaths().clear();
        gameState.getLastNightDeaths().addAll(deaths);

        // 记录夜晚结束事件
        GameEvent nightEndEvent = GameEvent.createPublicEvent(
                GameEvent.EventType.NIGHT_END,
                String.format("夜晚结束，共有%d名玩家死亡", deaths.size())
        );
        gameState.addEvent(nightEndEvent);

        log.info("=== 夜晚流程执行完毕，死亡人数: {} ===", deaths.size());

        return result;
    }

    /**
     * 查找存活的预言家
     *
     * @param gameState 游戏状态
     * @return 预言家玩家，不存在则返回null
     */
    private Player findSeer(GameState gameState) {
        return gameState.getAlivePlayers().stream()
                .filter(p -> p.getRole() instanceof Seer)
                .findFirst()
                .orElse(null);
    }

    /**
     * 查找存活的女巫
     *
     * @param gameState 游戏状态
     * @return 女巫玩家，不存在则返回null
     */
    private Player findWitch(GameState gameState) {
        return gameState.getAlivePlayers().stream()
                .filter(p -> p.getRole() instanceof Witch)
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取存活的狼人列表
     *
     * @param gameState 游戏状态
     * @return 狼人玩家列表
     */
    public List<Player> getAliveWerewolves(GameState gameState) {
        return gameState.getAlivePlayers().stream()
                .filter(p -> p.getRole() instanceof Werewolf)
                .collect(Collectors.toList());
    }

    /**
     * 夜晚结果类
     */
    public static class NightResult {
        private NightActionProcessor.WerewolfKillResult werewolfKillResult;
        private NightActionProcessor.SeerCheckResult seerCheckResult;
        private NightActionProcessor.WitchActionResult witchActionResult;
        private List<Long> deaths;

        public NightActionProcessor.WerewolfKillResult getWerewolfKillResult() {
            return werewolfKillResult;
        }

        public void setWerewolfKillResult(NightActionProcessor.WerewolfKillResult werewolfKillResult) {
            this.werewolfKillResult = werewolfKillResult;
        }

        public NightActionProcessor.SeerCheckResult getSeerCheckResult() {
            return seerCheckResult;
        }

        public void setSeerCheckResult(NightActionProcessor.SeerCheckResult seerCheckResult) {
            this.seerCheckResult = seerCheckResult;
        }

        public NightActionProcessor.WitchActionResult getWitchActionResult() {
            return witchActionResult;
        }

        public void setWitchActionResult(NightActionProcessor.WitchActionResult witchActionResult) {
            this.witchActionResult = witchActionResult;
        }

        public List<Long> getDeaths() {
            return deaths;
        }

        public void setDeaths(List<Long> deaths) {
            this.deaths = deaths;
        }
    }
}

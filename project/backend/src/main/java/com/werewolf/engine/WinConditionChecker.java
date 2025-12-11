package com.werewolf.engine;

import com.werewolf.domain.model.GameState;
import com.werewolf.domain.model.Player;
import com.werewolf.domain.role.Camp;
import com.werewolf.domain.role.RoleType;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 胜利条件检查器
 * 负责判断游戏是否结束以及哪方获胜
 */
@Slf4j
public class WinConditionChecker {

    /**
     * 检查游戏胜利条件
     *
     * @param gameState 游戏状态
     * @return 胜利结果，如果游戏未结束返回null
     */
    public WinResult checkWinCondition(GameState gameState) {
        if (gameState == null || gameState.getPlayers() == null) {
            log.error("游戏状态为空，无法检查胜利条件");
            return null;
        }

        List<Player> alivePlayers = gameState.getPlayers().stream()
                .filter(Player::isAlive)
                .collect(Collectors.toList());

        if (alivePlayers.isEmpty()) {
            log.warn("没有存活玩家，游戏异常结束");
            return WinResult.builder()
                    .gameEnded(true)
                    .winningCamp(null)
                    .reason("所有玩家死亡，游戏异常结束")
                    .build();
        }

        // 统计存活玩家
        long aliveWerewolves = countAlivePlayers(alivePlayers, RoleType.WEREWOLF);
        long aliveSeers = countAlivePlayers(alivePlayers, RoleType.SEER);
        long aliveWitches = countAlivePlayers(alivePlayers, RoleType.WITCH);
        long aliveVillagers = countAlivePlayers(alivePlayers, RoleType.VILLAGER);

        log.debug("存活统计 - 狼人:{}, 预言家:{}, 女巫:{}, 平民:{}",
                aliveWerewolves, aliveSeers, aliveWitches, aliveVillagers);

        // 检查好人胜利：所有狼人死亡
        if (aliveWerewolves == 0) {
            log.info("好人胜利！所有狼人已被消灭");
            return WinResult.builder()
                    .gameEnded(true)
                    .winningCamp(Camp.GOOD)
                    .reason("所有狼人已被消灭")
                    .aliveWerewolves(0)
                    .aliveSeers((int) aliveSeers)
                    .aliveWitches((int) aliveWitches)
                    .aliveVillagers((int) aliveVillagers)
                    .build();
        }

        // 检查狼人胜利条件1：所有神职死亡
        long aliveGodRoles = aliveSeers + aliveWitches;
        if (aliveGodRoles == 0) {
            log.info("狼人胜利！所有神职已死亡");
            return WinResult.builder()
                    .gameEnded(true)
                    .winningCamp(Camp.WEREWOLF)
                    .reason("所有神职（预言家、女巫）已死亡")
                    .aliveWerewolves((int) aliveWerewolves)
                    .aliveSeers(0)
                    .aliveWitches(0)
                    .aliveVillagers((int) aliveVillagers)
                    .build();
        }

        // 检查狼人胜利条件2：所有平民死亡
        if (aliveVillagers == 0) {
            log.info("狼人胜利！所有平民已死亡");
            return WinResult.builder()
                    .gameEnded(true)
                    .winningCamp(Camp.WEREWOLF)
                    .reason("所有平民已死亡")
                    .aliveWerewolves((int) aliveWerewolves)
                    .aliveSeers((int) aliveSeers)
                    .aliveWitches((int) aliveWitches)
                    .aliveVillagers(0)
                    .build();
        }

        // 游戏继续
        log.debug("游戏继续，尚未达成胜利条件");
        return WinResult.builder()
                .gameEnded(false)
                .winningCamp(null)
                .reason("游戏进行中")
                .aliveWerewolves((int) aliveWerewolves)
                .aliveSeers((int) aliveSeers)
                .aliveWitches((int) aliveWitches)
                .aliveVillagers((int) aliveVillagers)
                .build();
    }

    /**
     * 统计指定角色类型的存活玩家数量
     *
     * @param alivePlayers 存活玩家列表
     * @param roleType 角色类型
     * @return 存活数量
     */
    private long countAlivePlayers(List<Player> alivePlayers, RoleType roleType) {
        return alivePlayers.stream()
                .filter(p -> p.getRole() != null)
                .filter(p -> p.getRole().getRoleType() == roleType)
                .count();
    }

    /**
     * 胜利结果
     */
    @Data
    @Builder
    public static class WinResult {
        /**
         * 游戏是否结束
         */
        private boolean gameEnded;

        /**
         * 获胜阵营（null表示游戏未结束）
         */
        private Camp winningCamp;

        /**
         * 胜利/游戏状态原因
         */
        private String reason;

        /**
         * 存活狼人数量
         */
        private int aliveWerewolves;

        /**
         * 存活预言家数量
         */
        private int aliveSeers;

        /**
         * 存活女巫数量
         */
        private int aliveWitches;

        /**
         * 存活平民数量
         */
        private int aliveVillagers;

        /**
         * 是否好人胜利
         */
        public boolean isGoodWin() {
            return gameEnded && winningCamp == Camp.GOOD;
        }

        /**
         * 是否狼人胜利
         */
        public boolean isWerewolfWin() {
            return gameEnded && winningCamp == Camp.WEREWOLF;
        }

        /**
         * 获取存活玩家总数
         */
        public int getTotalAlivePlayers() {
            return aliveWerewolves + aliveSeers + aliveWitches + aliveVillagers;
        }
    }
}

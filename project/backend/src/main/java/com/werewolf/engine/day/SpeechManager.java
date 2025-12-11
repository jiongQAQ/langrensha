package com.werewolf.engine.day;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 发言管理器
 * 负责管理白天发言顺序和发言内容记录
 */
@Slf4j
public class SpeechManager {

    // 发言顺序队列（玩家ID列表）
    private final List<Long> speechOrder;

    // 当前发言者索引
    private int currentSpeakerIndex = 0;

    // 发言记录（玩家ID -> 发言内容）
    private final Map<Long, String> speeches = new LinkedHashMap<>();

    // 遗言记录（玩家ID -> 遗言内容）
    private final Map<Long, String> lastWords = new LinkedHashMap<>();

    /**
     * 构造函数：按指定顺序初始化发言队列
     * @param playerIds 玩家ID列表（发言顺序）
     */
    public SpeechManager(List<Long> playerIds) {
        if (playerIds == null || playerIds.isEmpty()) {
            throw new IllegalArgumentException("玩家列表不能为空");
        }
        this.speechOrder = new ArrayList<>(playerIds);
        log.info("初始化发言管理器，发言顺序: {}", speechOrder);
    }

    /**
     * 构造函数：随机初始化发言顺序
     * @param playerIds 玩家ID集合
     * @param random 是否随机顺序
     */
    public SpeechManager(List<Long> playerIds, boolean random) {
        if (playerIds == null || playerIds.isEmpty()) {
            throw new IllegalArgumentException("玩家列表不能为空");
        }
        this.speechOrder = new ArrayList<>(playerIds);
        if (random) {
            Collections.shuffle(this.speechOrder);
            log.info("初始化发言管理器（随机顺序）: {}", speechOrder);
        } else {
            log.info("初始化发言管理器（座位顺序）: {}", speechOrder);
        }
    }

    /**
     * 获取当前发言玩家ID
     * @return 当前发言玩家ID，如果发言结束返回null
     */
    public Long getCurrentSpeaker() {
        if (isAllSpeechFinished()) {
            return null;
        }
        return speechOrder.get(currentSpeakerIndex);
    }

    /**
     * 记录发言内容
     * @param playerId 玩家ID
     * @param speech 发言内容
     */
    public void recordSpeech(Long playerId, String speech) {
        if (playerId == null) {
            throw new IllegalArgumentException("玩家ID不能为空");
        }
        if (speech == null || speech.trim().isEmpty()) {
            speech = "[沉默]";
        }
        speeches.put(playerId, speech);
        log.info("记录玩家 {} 的发言: {}", playerId,
                speech.length() > 50 ? speech.substring(0, 50) + "..." : speech);
    }

    /**
     * 记录遗言
     * @param playerId 玩家ID
     * @param lastWord 遗言内容
     */
    public void recordLastWords(Long playerId, String lastWord) {
        if (playerId == null) {
            throw new IllegalArgumentException("玩家ID不能为空");
        }
        if (lastWord == null || lastWord.trim().isEmpty()) {
            lastWord = "[无遗言]";
        }
        lastWords.put(playerId, lastWord);
        log.info("记录玩家 {} 的遗言: {}", playerId,
                lastWord.length() > 50 ? lastWord.substring(0, 50) + "..." : lastWord);
    }

    /**
     * 移动到下一个发言者
     * @return 下一个发言玩家ID，如果已经是最后一个则返回null
     */
    public Long moveToNextSpeaker() {
        if (isAllSpeechFinished()) {
            log.warn("所有玩家已完成发言");
            return null;
        }

        currentSpeakerIndex++;

        if (isAllSpeechFinished()) {
            log.info("发言环节结束");
            return null;
        }

        Long nextSpeaker = speechOrder.get(currentSpeakerIndex);
        log.info("轮到玩家 {} 发言 (第{}/{}位)", nextSpeaker, currentSpeakerIndex + 1, speechOrder.size());
        return nextSpeaker;
    }

    /**
     * 判断所有玩家是否已完成发言
     * @return true表示发言环节结束
     */
    public boolean isAllSpeechFinished() {
        return currentSpeakerIndex >= speechOrder.size();
    }

    /**
     * 获取发言顺序
     * @return 玩家ID列表（按发言顺序）
     */
    public List<Long> getSpeechOrder() {
        return new ArrayList<>(speechOrder);
    }

    /**
     * 获取所有发言记录
     * @return 发言记录映射（玩家ID -> 发言内容）
     */
    public Map<Long, String> getAllSpeeches() {
        return new LinkedHashMap<>(speeches);
    }

    /**
     * 获取所有遗言记录
     * @return 遗言记录映射（玩家ID -> 遗言内容）
     */
    public Map<Long, String> getAllLastWords() {
        return new LinkedHashMap<>(lastWords);
    }

    /**
     * 获取指定玩家的发言
     * @param playerId 玩家ID
     * @return 发言内容，如果未发言则返回null
     */
    public String getSpeech(Long playerId) {
        return speeches.get(playerId);
    }

    /**
     * 获取指定玩家的遗言
     * @param playerId 玩家ID
     * @return 遗言内容，如果未留遗言则返回null
     */
    public String getLastWords(Long playerId) {
        return lastWords.get(playerId);
    }

    /**
     * 获取当前发言进度
     * @return 进度描述（如 "3/6"）
     */
    public String getProgress() {
        if (isAllSpeechFinished()) {
            return String.format("%d/%d（已完成）", speechOrder.size(), speechOrder.size());
        }
        return String.format("%d/%d", currentSpeakerIndex + 1, speechOrder.size());
    }

    /**
     * 重置发言管理器（保留顺序，清空记录）
     */
    public void reset() {
        currentSpeakerIndex = 0;
        speeches.clear();
        log.info("发言管理器已重置");
    }

    /**
     * 清空所有记录（包括遗言）
     */
    public void clear() {
        currentSpeakerIndex = 0;
        speeches.clear();
        lastWords.clear();
        log.info("发言管理器已清空");
    }
}

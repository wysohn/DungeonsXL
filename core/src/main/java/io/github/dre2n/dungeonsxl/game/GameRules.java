/*
 * Copyright (C) 2012-2016 Frank Baumann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.dre2n.dungeonsxl.game;

import io.github.dre2n.dungeonsxl.requirement.Requirement;
import io.github.dre2n.dungeonsxl.reward.Reward;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;

/**
 * @author Daniel Saukel
 */
public class GameRules {

    public static final GameRules DEFAULT_VALUES = new GameRules();

    static {
        /* keepInventory */
        DEFAULT_VALUES.keepInventoryOnEnter = false;
        DEFAULT_VALUES.keepInventoryOnEscape = false;
        DEFAULT_VALUES.keepInventoryOnFinish = false;
        DEFAULT_VALUES.keepInventoryOnDeath = true;
        DEFAULT_VALUES.lobbyDisabled = false;

        /* World interaction */
        DEFAULT_VALUES.gameMode = GameMode.SURVIVAL;
        DEFAULT_VALUES.build = false;

        /* Fighting */
        DEFAULT_VALUES.playerVersusPlayer = false;
        DEFAULT_VALUES.friendlyFire = false;
        DEFAULT_VALUES.initialLives = 3;

        /* Timer */
        DEFAULT_VALUES.timeLastPlayed = 0;
        DEFAULT_VALUES.timeToNextPlay = 0;
        DEFAULT_VALUES.timeToNextLoot = 0;
        DEFAULT_VALUES.timeToNextWave = 10;
        DEFAULT_VALUES.timeToFinish = -1;
        DEFAULT_VALUES.timeUntilKickOfflinePlayer = -1;

        /* Requirements and rewards */
        DEFAULT_VALUES.requirements = new ArrayList<>();
        DEFAULT_VALUES.finishedOne = null;
        DEFAULT_VALUES.finishedAll = null;
        DEFAULT_VALUES.rewards = new ArrayList<>();

        /* Commands and permissions */
        DEFAULT_VALUES.gameCommandWhitelist = new ArrayList<>();
        DEFAULT_VALUES.gamePermissions = new ArrayList<>();

        /* Misc */
        DEFAULT_VALUES.msgs = new HashMap<>();
        DEFAULT_VALUES.secureObjects = new ArrayList<>();
    }

    /* keepInventory */
    protected Boolean keepInventoryOnEnter;
    protected Boolean keepInventoryOnEscape;
    protected Boolean keepInventoryOnFinish;
    protected Boolean keepInventoryOnDeath;
    protected Boolean lobbyDisabled;

    /* World interaction */
    protected GameMode gameMode;
    protected Boolean build;

    /* Fighting */
    protected Boolean playerVersusPlayer;
    protected Boolean friendlyFire;
    protected Integer initialLives;

    /* Timer */
    protected Integer timeLastPlayed;
    protected Integer timeToNextPlay;
    protected Integer timeToNextLoot;
    protected Integer timeToNextWave;
    protected Integer timeToFinish;
    protected Integer timeUntilKickOfflinePlayer;

    /* Requirements and rewards */
    protected List<Requirement> requirements;
    protected List<String> finishedOne;
    protected List<String> finishedAll;
    protected List<Reward> rewards;

    /* Commands and permissions */
    protected List<String> gameCommandWhitelist;
    protected List<String> gamePermissions;

    /* Misc */
    protected Map<Integer, String> msgs;
    protected List<ItemStack> secureObjects;

    /* Getters and setters */
    // keepInventory
    /**
     * @return if the inventory shall be kept when the player enters the dungeon
     */
    public boolean getKeepInventoryOnEnter() {
        return keepInventoryOnEnter;
    }

    /**
     * @return if the inventory shall be kept when the player leaves the dungeon successlessly
     */
    public boolean getKeepInventoryOnEscape() {
        return keepInventoryOnEscape;
    }

    /**
     * @return if the inventory shall be kept when the player finishs the dungeon
     */
    public boolean getKeepInventoryOnFinish() {
        return keepInventoryOnFinish;
    }

    /**
     * @return if the inventory shall be kept on death
     */
    public boolean getKeepInventoryOnDeath() {
        return keepInventoryOnDeath;
    }

    /**
     * @return if the lobby is disabled
     */
    public boolean isLobbyDisabled() {
        return lobbyDisabled;
    }

    // World interaction
    /**
     * @return the gameMode
     */
    public GameMode getGameMode() {
        return gameMode;
    }

    /**
     * @return if players may build
     */
    public boolean canBuild() {
        return build;
    }

    // Fight
    /**
     * @return if players may attack each other
     */
    public boolean isPlayerVersusPlayer() {
        return playerVersusPlayer;
    }

    /**
     * @return if players may attack group members
     */
    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    /**
     * @return the initial amount of lives
     */
    public int getInitialLives() {
        return initialLives;
    }

    // Timer
    /**
     * @return the timeLastPlayed
     */
    public int getTimeLastPlayed() {
        return timeLastPlayed;
    }

    /**
     * @return the time until a player can play again
     */
    public int getTimeToNextPlay() {
        return timeToNextPlay;
    }

    /**
     * @return the time until a player can get loot again
     */
    public int getTimeToNextLoot() {
        return timeToNextLoot;
    }

    /**
     * @return the break between two waves
     */
    public int getTimeToNextWave() {
        return timeToNextWave;
    }

    /**
     * @return the time until a player gets kicked from his group
     */
    public int getTimeToFinish() {
        return timeToFinish;
    }

    /**
     * @return the time until a player gets kicked from his group if he is offline
     */
    public int getTimeUntilKickOfflinePlayer() {
        return timeUntilKickOfflinePlayer;
    }

    /**
     * @return if the game is "time is running"
     */
    public boolean isTimeIsRunning() {
        return timeToFinish != -1;
    }

    // Requirements and rewards
    /**
     * @return the requirements
     */
    public List<Requirement> getRequirements() {
        if (requirements == null) {
            requirements = new ArrayList<>();
        }
        return requirements;
    }

    /**
     * @return all maps needed to be finished to play this map
     */
    public List<String> getFinishedAll() {
        if (finishedAll == null) {
            finishedAll = new ArrayList<>();
        }
        return finishedAll;
    }

    /**
     * @return all maps needed to be finished to play this map and a collection of maps of which at
     * least one has to be finished
     */
    public List<String> getFinished() {
        if (finishedAll == null) {
            finishedAll = new ArrayList<>();
        }
        if (finishedOne == null) {
            finishedOne = new ArrayList<>();
        }

        List<String> merge = new ArrayList<>();
        merge.addAll(finishedAll);
        merge.addAll(finishedOne);
        return merge;
    }

    /**
     * @return the rewards
     */
    public List<Reward> getRewards() {
        if (rewards == null) {
            rewards = new ArrayList<>();
        }
        return rewards;
    }

    // Commands and permissions
    /**
     * @return the gameCommandWhitelist
     */
    public List<String> getGameCommandWhitelist() {
        if (gameCommandWhitelist == null) {
            gameCommandWhitelist = new ArrayList<>();
        }
        return gameCommandWhitelist;
    }

    /**
     * @return the gamePermissions
     */
    public List<String> getGamePermissions() {
        if (gamePermissions == null) {
            gamePermissions = new ArrayList<>();
        }
        return gamePermissions;
    }

    // Misc
    /**
     * @param id
     * the id of the message
     */
    public String getMessage(int id) {
        if (msgs == null) {
            msgs = new HashMap<>();
        }
        return msgs.get(id);
    }

    /**
     * @param id
     * the ID of the message
     * @param msg
     * the message to set
     */
    public void setMessage(int id, String msg) {
        if (msgs == null) {
            msgs = new HashMap<>();
        }
        msgs.put(id, msg);
    }

    /**
     * @return the objects to get passed to another player of the group when this player leaves
     */
    public List<ItemStack> getSecureObjects() {
        if (secureObjects == null) {
            secureObjects = new ArrayList<>();
        }
        return secureObjects;
    }

    /* Actions */
    /**
     * @param defaultValues
     * the GameType that overrides the values that are null.
     */
    public void apply(GameType defaultValues) {
        if (playerVersusPlayer == null) {
            playerVersusPlayer = defaultValues.isPlayerVersusPlayer();
        }

        if (friendlyFire == null) {
            friendlyFire = defaultValues.isFriendlyFire();
        }

        if (timeToFinish == null) {
            timeToFinish = defaultValues.getShowTime() ? null : -1;
        }

        if (build == null) {
            build = defaultValues.canBuild();
        }

        if (gameMode == null) {
            gameMode = defaultValues.getGameMode();
        }
    }

    /**
     * @param defaultValues
     * the GameRules that override the values that are null.
     */
    public void apply(GameRules defaultValues) {
        /* keepInventory */
        if (keepInventoryOnEnter == null) {
            keepInventoryOnEnter = defaultValues.keepInventoryOnEnter;
        }

        if (keepInventoryOnEscape == null) {
            keepInventoryOnEscape = defaultValues.keepInventoryOnEscape;
        }

        if (keepInventoryOnFinish == null) {
            keepInventoryOnFinish = defaultValues.keepInventoryOnFinish;
        }

        if (keepInventoryOnDeath == null) {
            keepInventoryOnDeath = defaultValues.keepInventoryOnDeath;
        }

        if (lobbyDisabled == null) {
            lobbyDisabled = defaultValues.lobbyDisabled;
        }

        /* World interaction */
        if (gameMode == null) {
            gameMode = defaultValues.gameMode;
        }

        if (build == null) {
            build = defaultValues.build;
        }

        /* Fighting */
        if (playerVersusPlayer == null) {
            playerVersusPlayer = defaultValues.playerVersusPlayer;
        }

        if (friendlyFire == null) {
            friendlyFire = defaultValues.friendlyFire;
        }

        if (initialLives == null) {
            initialLives = defaultValues.initialLives;
        }

        /* Timer */
        if (timeLastPlayed == null) {
            timeLastPlayed = defaultValues.timeLastPlayed;
        }

        if (timeToNextPlay == null) {
            timeToNextPlay = defaultValues.timeToNextPlay;
        }

        if (timeToNextLoot == null) {
            timeToNextLoot = defaultValues.timeToNextLoot;
        }

        if (timeToNextWave == null) {
            timeToNextWave = defaultValues.timeToNextWave;
        }

        if (timeToFinish == null) {
            timeToFinish = defaultValues.timeToFinish;
        }

        if (timeUntilKickOfflinePlayer == null) {
            timeUntilKickOfflinePlayer = defaultValues.timeUntilKickOfflinePlayer;
        }

        /* Requirements and rewards */
        if (requirements == null) {
            requirements = defaultValues.requirements;
        }

        if (finishedOne == null) {
            finishedOne = defaultValues.finishedOne;
        }

        if (finishedAll == null) {
            finishedAll = defaultValues.finishedAll;
        }

        if (rewards == null) {
            rewards = defaultValues.rewards;
        }

        /* Commands and permissions */
        if (gameCommandWhitelist == null) {
            gameCommandWhitelist = defaultValues.gameCommandWhitelist;
        } else if (defaultValues.gameCommandWhitelist != null) {
            gameCommandWhitelist.addAll(defaultValues.gameCommandWhitelist);
        }

        if (gamePermissions == null) {
            gamePermissions = defaultValues.gamePermissions;
        } else if (defaultValues.gamePermissions != null) {
            gamePermissions.addAll(defaultValues.gamePermissions);
        }

        /* Misc */
        if (msgs == null) {
            msgs = defaultValues.msgs;
        } else if (defaultValues.msgs != null) {
            msgs.putAll(defaultValues.msgs);
        }

        if (secureObjects == null) {
            secureObjects = defaultValues.secureObjects;
        } else if (defaultValues.secureObjects != null) {
            secureObjects.addAll(defaultValues.secureObjects);
        }
    }

}

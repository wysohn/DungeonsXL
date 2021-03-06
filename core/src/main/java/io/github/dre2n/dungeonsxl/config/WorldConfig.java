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
package io.github.dre2n.dungeonsxl.config;

import io.github.dre2n.caliburn.CaliburnAPI;
import io.github.dre2n.caliburn.item.UniversalItemStack;
import io.github.dre2n.commons.compatibility.CompatibilityHandler;
import io.github.dre2n.commons.compatibility.Version;
import io.github.dre2n.commons.util.EnumUtil;
import io.github.dre2n.commons.util.NumberUtil;
import io.github.dre2n.dungeonsxl.DungeonsXL;
import io.github.dre2n.dungeonsxl.game.GameRules;
import io.github.dre2n.dungeonsxl.game.GameType;
import io.github.dre2n.dungeonsxl.requirement.FeeLevelRequirement;
import io.github.dre2n.dungeonsxl.requirement.FeeMoneyRequirement;
import io.github.dre2n.dungeonsxl.requirement.GroupSizeRequirement;
import io.github.dre2n.dungeonsxl.requirement.PermissionRequirement;
import io.github.dre2n.dungeonsxl.requirement.Requirement;
import io.github.dre2n.dungeonsxl.requirement.RequirementTypeDefault;
import io.github.dre2n.dungeonsxl.util.DeserializationUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * @author Frank Baumann, Milan Albrecht, Daniel Saukel
 */
public class WorldConfig extends GameRules {

    DungeonsXL plugin = DungeonsXL.getInstance();
    CompatibilityHandler compat = CompatibilityHandler.getInstance();

    private File file;

    private List<String> invitedPlayers = new ArrayList<>();
    private GameType forcedGameType;

    public WorldConfig() {
    }

    public WorldConfig(File file) {
        this.file = file;
        FileConfiguration configFile = YamlConfiguration.loadConfiguration(file);
        load(configFile);
    }

    public WorldConfig(ConfigurationSection configFile) {
        load(configFile);
    }

    // Load & Save
    public void load(ConfigurationSection configFile) {
        /* Messages */
        ConfigurationSection configSectionMessages = configFile.getConfigurationSection("message");
        if (configSectionMessages != null) {
            Set<String> list = configSectionMessages.getKeys(false);
            for (String messagePath : list) {
                int messageId = NumberUtil.parseInt(messagePath);
                setMessage(messageId, configSectionMessages.getString(messagePath));
            }
        }

        /* Secure Objects */
        if (configFile.contains("secureObjects")) {
            if (Version.andHigher(Version.MC1_9).contains(compat.getVersion())) {
                secureObjects = UniversalItemStack.deserializeList(configFile.getList("secureObjects"));
            } else {
                secureObjects = DeserializationUtil.deserializeStackList(configFile.getStringList("secureObjects"));
            }
        }

        /* Invited Players */
        if (configFile.contains("invitedPlayers")) {
            invitedPlayers = configFile.getStringList("invitedPlayers");

        }

        /* Keep Inventory */
        if (configFile.contains("keepInventory")) {
            if (!configFile.contains("keepInventoryOnEnter")) {
                keepInventoryOnEnter = configFile.getBoolean("keepInventory");
            }
            if (!configFile.contains("keepInventoryOnEscape")) {
                keepInventoryOnEscape = configFile.getBoolean("keepInventory");
            }
            if (!configFile.contains("keepInventoryOnFinish")) {
                keepInventoryOnFinish = configFile.getBoolean("keepInventory");
            }
        }

        if (configFile.contains("keepInventoryOnEnter")) {
            keepInventoryOnEnter = configFile.getBoolean("keepInventoryOnEnter");
        }

        if (configFile.contains("keepInventoryOnEscape")) {
            keepInventoryOnEscape = configFile.getBoolean("keepInventoryOnEscape");
        }

        if (configFile.contains("keepInventoryOnFinish")) {
            keepInventoryOnFinish = configFile.getBoolean("keepInventoryOnFinish");
        }

        if (configFile.contains("keepInventoryOnDeath")) {
            keepInventoryOnDeath = configFile.getBoolean("keepInventoryOnDeath");
        }

        /* Build */
        if (configFile.contains("build")) {
            build = configFile.getBoolean("build");
        }

        /* GameMode */
        if (configFile.contains("gameMode")) {
            if (EnumUtil.isValidEnum(GameMode.class, configFile.getString("gameMode").toUpperCase())) {
                gameMode = GameMode.valueOf(configFile.getString("gameMode"));
            } else {
                gameMode = GameMode.getByValue(configFile.getInt("gameMode"));
            }
        }

        /* PvP */
        if (configFile.contains("playerVersusPlayer")) {
            playerVersusPlayer = configFile.getBoolean("playerVersusPlayer");
        }

        /* Friendly fire */
        if (configFile.contains("friendlyFire")) {
            friendlyFire = configFile.getBoolean("friendlyFire");
        }

        /* Lives */
        if (configFile.contains("initialLives")) {
            initialLives = configFile.getInt("initialLives");
        }

        /* Lobby */
        if (configFile.contains("isLobbyDisabled")) {
            lobbyDisabled = configFile.getBoolean("isLobbyDisabled");
        }

        /* Times */
        if (configFile.contains("timeToNextPlay")) {
            timeToNextPlay = configFile.getInt("timeToNextPlay");
        }

        if (configFile.contains("timeToNextLoot")) {
            timeToNextLoot = configFile.getInt("timeToNextLoot");
        }

        if (configFile.contains("timeToNextWave")) {
            timeToNextWave = configFile.getInt("timeToNextWave");
        }

        if (configFile.contains("timeUntilKickOfflinePlayer")) {
            timeUntilKickOfflinePlayer = configFile.getInt("timeUntilKickOfflinePlayer");
        }

        if (configFile.contains("timeToFinish")) {
            timeToFinish = configFile.getInt("timeToFinish");
        }

        /* Dungeon Requirements */
        if (configFile.contains("requirements")) {
            if (requirements == null) {
                requirements = new ArrayList<>();
            }

            for (String identifier : configFile.getConfigurationSection("requirements").getKeys(false)) {
                Requirement requirement = Requirement.create(plugin.getRequirementTypes().getByIdentifier(identifier));

                // Check for built-in requirements
                if (requirement.getType() == RequirementTypeDefault.FEE_MONEY) {
                    ((FeeMoneyRequirement) requirement).setFee(configFile.getDouble("requirements.feeMoney"));

                } else if (requirement.getType() == RequirementTypeDefault.FEE_LEVEL) {
                    ((FeeLevelRequirement) requirement).setFee(configFile.getInt("requirements.feeLevel"));

                } else if (requirement.getType() == RequirementTypeDefault.GROUP_SIZE) {
                    ((GroupSizeRequirement) requirement).setMinimum(configFile.getInt("requirements.groupSize.minimum"));
                    ((GroupSizeRequirement) requirement).setMaximum(configFile.getInt("requirements.groupSize.maximum"));

                } else if (requirement.getType() == RequirementTypeDefault.PERMISSION) {
                    ((PermissionRequirement) requirement).setPermissions(configFile.getStringList("requirements.permission"));
                }

                requirements.add(requirement);
            }
        }

        if (configFile.contains("mustFinishOne")) {
            finishedOne = configFile.getStringList("mustFinishOne");
        } else {
            finishedOne = new ArrayList<>();
        }

        if (configFile.contains("mustFinishAll")) {
            finishedAll = configFile.getStringList("mustFinishAll");
        } else {
            finishedAll = new ArrayList<>();
        }

        if (configFile.contains("timeLastPlayed")) {
            timeLastPlayed = configFile.getInt("timeLastPlayed");
        }

        if (configFile.contains("gameCommandWhitelist")) {
            gameCommandWhitelist = configFile.getStringList("gameCommandWhitelist");
        }

        if (configFile.contains("gamePermissions")) {
            gamePermissions = configFile.getStringList("gamePermissions");
        }

        if (configFile.contains("forcedGameType")) {
            forcedGameType = plugin.getGameTypes().getByName(configFile.getString("forcedGameType"));
        }
    }

    public void save() {
        if (file == null) {
            return;
        }
        FileConfiguration configFile = YamlConfiguration.loadConfiguration(file);

        // Messages
        for (int msgs : this.msgs.keySet()) {
            configFile.set("message." + msgs, this.msgs.get(msgs));
        }

        List<String> secureObjectIds = new ArrayList<>();

        for (ItemStack item : getSecureObjects()) {
            secureObjectIds.add(CaliburnAPI.getInstance().getItems().getCustomItemId(item));
        }

        configFile.set("secureObjects", secureObjects);

        // Invited Players
        configFile.set("invitedPlayers", invitedPlayers);

        try {
            configFile.save(file);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * @return the UUIDs or names of the players invited to edit the map
     */
    public CopyOnWriteArrayList<String> getInvitedPlayers() {
        CopyOnWriteArrayList<String> tmpInvitedPlayers = new CopyOnWriteArrayList<>();
        tmpInvitedPlayers.addAll(invitedPlayers);
        return tmpInvitedPlayers;
    }

    /**
     * @param uuid
     * the player's unique ID
     */
    public void addInvitedPlayer(String uuid) {
        invitedPlayers.add(uuid);
    }

    /**
     * @param uuid
     * the player's unique ID
     * @param name
     * the player's name
     */
    public void removeInvitedPlayers(String uuid, String name) {
        invitedPlayers.remove(uuid);
        // remove player from a 0.9.1 and lower file
        invitedPlayers.remove(name);
    }

    /**
     * @return the forcedGameType
     */
    public GameType getForcedGameType() {
        return forcedGameType;
    }

    /**
     * @param forcedGameType
     * the forcedGameType to set
     */
    public void setForcedGameType(GameType forcedGameType) {
        this.forcedGameType = forcedGameType;
    }

}

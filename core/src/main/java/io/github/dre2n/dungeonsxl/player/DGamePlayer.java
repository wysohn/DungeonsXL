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
package io.github.dre2n.dungeonsxl.player;

import io.github.dre2n.commons.util.NumberUtil;
import io.github.dre2n.commons.util.messageutil.MessageUtil;
import io.github.dre2n.commons.util.playerutil.PlayerUtil;
import io.github.dre2n.dungeonsxl.DungeonsXL;
import io.github.dre2n.dungeonsxl.config.DMessages;
import io.github.dre2n.dungeonsxl.config.DungeonConfig;
import io.github.dre2n.dungeonsxl.event.dgroup.DGroupFinishDungeonEvent;
import io.github.dre2n.dungeonsxl.event.dgroup.DGroupRewardEvent;
import io.github.dre2n.dungeonsxl.event.dplayer.DPlayerFinishEvent;
import io.github.dre2n.dungeonsxl.event.dplayer.DPlayerKickEvent;
import io.github.dre2n.dungeonsxl.event.dplayer.DPlayerUpdateEvent;
import io.github.dre2n.dungeonsxl.event.requirement.RequirementCheckEvent;
import io.github.dre2n.dungeonsxl.game.Game;
import io.github.dre2n.dungeonsxl.game.GameRules;
import io.github.dre2n.dungeonsxl.game.GameType;
import io.github.dre2n.dungeonsxl.game.GameTypeDefault;
import io.github.dre2n.dungeonsxl.mob.DMob;
import io.github.dre2n.dungeonsxl.requirement.Requirement;
import io.github.dre2n.dungeonsxl.reward.Reward;
import io.github.dre2n.dungeonsxl.task.CreateDInstancePlayerTask;
import io.github.dre2n.dungeonsxl.trigger.DistanceTrigger;
import io.github.dre2n.dungeonsxl.world.DGameWorld;
import io.github.dre2n.dungeonsxl.world.DResourceWorld;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

/**
 * Represents a player in a DGameWorld.
 *
 * @author Frank Baumann, Tobias Schmitz, Milan Albrecht, Daniel Saukel
 */
public class DGamePlayer extends DInstancePlayer {

    // Variables
    private boolean ready = false;
    private boolean finished = false;

    private DClass dClass;
    private Location checkpoint;
    private Wolf wolf;
    private int wolfRespawnTime = 30;
    private long offlineTime;

    private int initialLives = -1;
    private int lives;

    public DGamePlayer(Player player, DGameWorld world) {
        super(player, world.getWorld());

        plugin.debug.start("DGamePlayer#init");
        Game game = Game.getByGameWorld(world);
        if (game == null) {
            game = new Game(DGroup.getByPlayer(player));
        }

        GameRules rules = game.getRules();
        player.setGameMode(GameMode.SURVIVAL);

        if (!rules.getKeepInventoryOnEnter()) {
            clearPlayerData();
        }

        if (rules.isLobbyDisabled()) {
            ready();
        }

        initialLives = rules.getInitialLives();
        lives = initialLives;

        Location teleport = world.getLobbyLocation();
        if (teleport == null) {
            PlayerUtil.secureTeleport(player, world.getWorld().getSpawnLocation());
        } else {
            PlayerUtil.secureTeleport(player, teleport);
        }
        plugin.debug.end("DGamePlayer#init", true);
    }

    /**
     * @param player
     * the represented Player
     * @param gameWorld
     * the player's GameWorld
     */
    public static void create(Player player, DGameWorld gameWorld) {
        create(player, gameWorld, false);
    }

    /**
     * @param player
     * the represented Player
     * @param gameWorld
     * the player's GameWorld
     * @param ready
     * if the player will be ready from the beginning
     */
    public static void create(Player player, DGameWorld gameWorld, boolean ready) {
        new CreateDInstancePlayerTask(player, gameWorld, ready).runTaskTimer(plugin, 0L, 5L);
    }

    /* Getters and setters */
    /**
     * @param player
     * the player to set
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * @return if the player is in test mode
     */
    public boolean isInTestMode() {
        DGroup dGroup = DGroup.getByPlayer(getPlayer());
        if (dGroup == null) {
            return false;
        }

        DGameWorld gameWorld = dGroup.getGameWorld();
        if (gameWorld == null) {
            return false;
        }

        Game game = gameWorld.getGame();
        if (game == null) {
            return false;
        }

        GameType gameType = game.getType();
        if (gameType == GameTypeDefault.TEST) {
            return true;
        }

        return false;
    }

    /**
     * @return the isReady
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * @param ready
     * If the player is ready to play the dungeon
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    /**
     * @return the finished
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * @param finished
     * the finished to set
     */
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    /**
     * @return the dClass
     */
    public DClass getDClass() {
        return dClass;
    }

    /**
     * @param dClass
     * the dClass to set
     */
    public void setDClass(String className) {
        plugin.debug.start("DGamePlayer#setDClass");
        Game game = Game.getByWorld(getPlayer().getWorld());
        if (game == null) {
            plugin.debug.end("DGamePlayer#setDClass", true);
            return;
        }

        DClass dClass = plugin.getDClasses().getByName(className);
        if (dClass != null) {
            if (this.dClass != dClass) {
                this.dClass = dClass;

                /* Set Dog */
                if (wolf != null) {
                    wolf.remove();
                    wolf = null;
                }

                if (dClass.hasDog()) {
                    wolf = (Wolf) getWorld().spawnEntity(getPlayer().getLocation(), EntityType.WOLF);
                    wolf.setTamed(true);
                    wolf.setOwner(getPlayer());

                    double maxHealth = ((Damageable) wolf).getMaxHealth();
                    wolf.setHealth(maxHealth);
                }

                /* Delete Inventory */
                getPlayer().getInventory().clear();
                getPlayer().getInventory().setArmorContents(null);
                getPlayer().getInventory().setItemInHand(new ItemStack(Material.AIR));

                // Remove Potion Effects
                for (PotionEffect effect : getPlayer().getActivePotionEffects()) {
                    getPlayer().removePotionEffect(effect.getType());
                }

                // Reset lvl
                getPlayer().setTotalExperience(0);
                getPlayer().setLevel(0);

                /* Set Inventory */
                for (ItemStack istack : dClass.getItems()) {

                    // Leggings
                    if (istack.getType() == Material.LEATHER_LEGGINGS || istack.getType() == Material.CHAINMAIL_LEGGINGS || istack.getType() == Material.IRON_LEGGINGS
                            || istack.getType() == Material.DIAMOND_LEGGINGS || istack.getType() == Material.GOLD_LEGGINGS) {
                        getPlayer().getInventory().setLeggings(istack);
                    } // Helmet
                    else if (istack.getType() == Material.LEATHER_HELMET || istack.getType() == Material.CHAINMAIL_HELMET || istack.getType() == Material.IRON_HELMET
                            || istack.getType() == Material.DIAMOND_HELMET || istack.getType() == Material.GOLD_HELMET) {
                        getPlayer().getInventory().setHelmet(istack);
                    } // Chestplate
                    else if (istack.getType() == Material.LEATHER_CHESTPLATE || istack.getType() == Material.CHAINMAIL_CHESTPLATE || istack.getType() == Material.IRON_CHESTPLATE
                            || istack.getType() == Material.DIAMOND_CHESTPLATE || istack.getType() == Material.GOLD_CHESTPLATE) {
                        getPlayer().getInventory().setChestplate(istack);
                    } // Boots
                    else if (istack.getType() == Material.LEATHER_BOOTS || istack.getType() == Material.CHAINMAIL_BOOTS || istack.getType() == Material.IRON_BOOTS
                            || istack.getType() == Material.DIAMOND_BOOTS || istack.getType() == Material.GOLD_BOOTS) {
                        getPlayer().getInventory().setBoots(istack);
                    } else {
                        getPlayer().getInventory().addItem(istack);
                    }
                }
            }
        }
        plugin.debug.end("DGamePlayer#setDClass", true);
    }

    /**
     * @return the checkpoint
     */
    public Location getCheckpoint() {
        return checkpoint;
    }

    /**
     * @param checkpoint
     * the checkpoint to set
     */
    public void setCheckpoint(Location checkpoint) {
        this.checkpoint = checkpoint;
    }

    /**
     * @return the wolf
     */
    public Wolf getWolf() {
        return wolf;
    }

    /**
     * @param wolf
     * the wolf to set
     */
    public void setWolf(Wolf wolf) {
        this.wolf = wolf;
    }

    /**
     * @return the wolfRespawnTime
     */
    public int getWolfRespawnTime() {
        return wolfRespawnTime;
    }

    /**
     * @param wolfRespawnTime
     * the wolfRespawnTime to set
     */
    public void setWolfRespawnTime(int wolfRespawnTime) {
        this.wolfRespawnTime = wolfRespawnTime;
    }

    /**
     * @return the offlineTime
     */
    public long getOfflineTime() {
        return offlineTime;
    }

    /**
     * @param offlineTime
     * the offlineTime to set
     */
    public void setOfflineTime(long offlineTime) {
        this.offlineTime = offlineTime;
    }

    /**
     * @return the initialLives
     */
    public int getInitialLives() {
        return initialLives;
    }

    /**
     * @param initialLives
     * the initialLives to set
     */
    public void setInitialLives(int initialLives) {
        this.initialLives = initialLives;
    }

    /**
     * @return the lives
     */
    public int getLives() {
        return lives;
    }

    /**
     * @param lives
     * the lives to set
     */
    public void setLives(int lives) {
        this.lives = lives;
    }

    /* Actions */
    @Override
    public void leave() {
        leave(true);
    }

    /**
     * @param message
     * if messages should be sent
     */
    public void leave(boolean message) {
        plugin.debug.start("DGamePlayer#leave");
        GameRules rules = Game.getByWorld(getWorld()).getRules();
        delete();

        if (finished) {
            getSavePlayer().reset(rules.getKeepInventoryOnFinish());
        } else {
            getSavePlayer().reset(rules.getKeepInventoryOnEscape());
        }

        // Permission bridge
        if (plugin.getPermissionProvider() != null) {
            for (String permission : rules.getGamePermissions()) {
                plugin.getPermissionProvider().playerRemoveTransient(getWorld().getName(), player, permission);
            }
        }

        DGroup dGroup = DGroup.getByPlayer(getPlayer());
        if (dGroup != null) {
            dGroup.removePlayer(getPlayer(), message);
        }

        DGameWorld gameWorld = DGameWorld.getByWorld(getWorld());
        Game game = Game.getByGameWorld(gameWorld);
        if (game != null) {
            if (finished) {
                if (game.getType().hasRewards()) {
                    for (Reward reward : rules.getRewards()) {
                        reward.giveTo(getPlayer());
                    }

                    getData().logTimeLastPlayed(dGroup.getDungeon().getName());

                    // Tutorial Permissions
                    if (gameWorld.isTutorial() && plugin.getPermissionProvider() != null && plugin.getPermissionProvider().hasGroupSupport()) {
                        String endGroup = plugin.getMainConfig().getTutorialEndGroup();
                        if (plugin.isGroupEnabled(endGroup)) {
                            plugin.getPermissionProvider().playerAddGroup(getPlayer(), endGroup);
                        }

                        String startGroup = plugin.getMainConfig().getTutorialStartGroup();
                        if (plugin.isGroupEnabled(startGroup)) {
                            plugin.getPermissionProvider().playerRemoveGroup(getPlayer(), startGroup);
                        }
                    }
                }
            }
        }

        if (dGroup != null) {
            if (!dGroup.isEmpty()) {
                if (dGroup.finishIfMembersFinished()) {
                    return;
                }

                // Give secure objects to other players
                int i = 0;
                Player groupPlayer;
                do {
                    groupPlayer = dGroup.getPlayers().get(i);
                    if (groupPlayer != null) {
                        for (ItemStack itemStack : getPlayer().getInventory()) {
                            if (itemStack != null) {
                                if (gameWorld.getSecureObjects().contains(itemStack)) {
                                    groupPlayer.getInventory().addItem(itemStack);
                                }
                            }
                        }
                    }
                    i++;
                } while (groupPlayer == null);
            }

            if (dGroup.getCaptain().equals(getPlayer()) && dGroup.getPlayers().size() > 0) {
                // Captain here!
                Player newCaptain = dGroup.getPlayers().get(0);
                dGroup.setCaptain(newCaptain);
                if (message) {
                    MessageUtil.sendMessage(newCaptain, DMessages.PLAYER_NEW_CAPTAIN.getMessage());
                }
                // ...*flies away*
            }
        }
        plugin.debug.end("DGamePlayer#leave", true);
    }

    public void kill() {
        DPlayerKickEvent dPlayerKickEvent = new DPlayerKickEvent(this, DPlayerKickEvent.Cause.DEATH);
        plugin.getServer().getPluginManager().callEvent(dPlayerKickEvent);

        if (!dPlayerKickEvent.isCancelled()) {
            MessageUtil.broadcastMessage(DMessages.PLAYER_DEATH_KICK.getMessage(player.getName()));
            GameRules rules = Game.getByPlayer(player).getRules();
            leave();
            if (rules.getKeepInventoryOnEscape() && rules.getKeepInventoryOnDeath()) {
                applyRespawnInventory();
            }
        }
    }

    public boolean checkRequirements(Game game) {
        if (DPermissions.hasPermission(player, DPermissions.IGNORE_REQUIREMENTS)) {
            return true;
        }

        GameRules rules = game.getRules();

        if (!checkTime(game)) {
            MessageUtil.sendMessage(player, DMessages.ERROR_COOLDOWN.getMessage(String.valueOf(rules.getTimeToNextPlay())));
            return false;
        }

        for (Requirement requirement : rules.getRequirements()) {
            RequirementCheckEvent event = new RequirementCheckEvent(requirement, player);
            plugin.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                continue;
            }

            if (!requirement.check(player)) {
                return false;
            }
        }

        if (rules.getFinished() != null && rules.getFinishedAll() != null) {
            if (!rules.getFinished().isEmpty()) {

                long bestTime = 0;
                int numOfNeeded = 0;
                boolean doneTheOne = false;

                if (rules.getFinished().size() == rules.getFinishedAll().size()) {
                    doneTheOne = true;
                }

                for (String played : rules.getFinished()) {
                    for (String dungeonName : DungeonsXL.MAPS.list()) {
                        if (new File(DungeonsXL.MAPS, dungeonName).isDirectory()) {
                            if (played.equalsIgnoreCase(dungeonName) || played.equalsIgnoreCase("any")) {

                                Long time = getData().getTimeLastPlayed(dungeonName);
                                if (time != -1) {
                                    if (rules.getFinishedAll().contains(played)) {
                                        numOfNeeded++;
                                    } else {
                                        doneTheOne = true;
                                    }
                                    if (bestTime < time) {
                                        bestTime = time;
                                    }
                                }
                                break;

                            }
                        }
                    }
                }

                if (bestTime == 0) {
                    return false;

                } else if (rules.getTimeLastPlayed() != 0) {
                    if (System.currentTimeMillis() - bestTime > rules.getTimeLastPlayed() * (long) 3600000) {
                        return false;
                    }
                }

                if (numOfNeeded < rules.getFinishedAll().size() || !doneTheOne) {
                    return false;
                }

            }
        }

        return true;
    }

    public boolean checkTime(Game game) {
        if (DPermissions.hasPermission(player, DPermissions.IGNORE_TIME_LIMIT)) {
            return true;
        }

        GameRules rules = game.getRules();

        if (rules.getTimeToNextPlay() != 0) {
            // read PlayerConfig
            long time = getData().getTimeLastPlayed(game.getDungeon().getName());
            if (time != -1) {
                if (time + rules.getTimeToNextPlay() * 1000 * 60 * 60 > System.currentTimeMillis()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void ready() {
        ready(GameTypeDefault.DEFAULT);
    }

    public void ready(GameType gameType) {
        DGroup dGroup = DGroup.getByPlayer(getPlayer());

        if (dGroup == null) {
            return;
        }

        Game game = Game.getByGameWorld(dGroup.getGameWorld());
        if (game == null) {
            game = new Game(dGroup, gameType, dGroup.getGameWorld());

        } else {
            game.setType(gameType);
        }
        game.fetchRules();

        if (!checkRequirements(game)) {
            MessageUtil.sendMessage(player, DMessages.ERROR_REQUIREMENTS.getMessage());
            return;
        }

        ready = true;

        for (DGroup gameGroup : game.getDGroups()) {
            if (!gameGroup.isPlaying()) {
                gameGroup.startGame(game);

            } else {
                respawn();
            }
        }
    }

    public void respawn() {
        DGroup dGroup = DGroup.getByPlayer(getPlayer());

        Location respawn = checkpoint;

        if (respawn == null) {
            respawn = dGroup.getGameWorld().getStartLocation(dGroup);
        }

        if (respawn == null) {
            respawn = getWorld().getSpawnLocation();
        }

        PlayerUtil.secureTeleport(getPlayer(), respawn);

        // Don't forget Doge!
        if (wolf != null) {
            wolf.teleport(getPlayer());
        }

        // Respawn Items
        Game game = Game.getByWorld(getWorld());

        if (game != null && game.getRules().getKeepInventoryOnDeath()) {
            applyRespawnInventory();
        }
    }

    /**
     * The DGamePlayer finishs the current floor.
     *
     * @param specifiedFloor
     * the name of the next floor
     */
    public void finishFloor(DResourceWorld specifiedFloor) {
        plugin.debug.start("DGamePlayer#finishFloor");
        MessageUtil.sendMessage(getPlayer(), DMessages.PLAYER_FINISHED_DUNGEON.getMessage());
        finished = true;

        DGroup dGroup = DGroup.getByPlayer(getPlayer());
        if (dGroup == null) {
            plugin.debug.end("DGamePlayer#finishFloor", true);
            return;
        }

        if (!dGroup.isPlaying()) {
            plugin.debug.end("DGamePlayer#finishFloor", true);
            return;
        }

        dGroup.setNextFloor(specifiedFloor);

        for (Player player : dGroup.getPlayers()) {
            DGamePlayer dPlayer = getByPlayer(player);
            if (!dPlayer.finished) {
                MessageUtil.sendMessage(this.getPlayer(), DMessages.PLAYER_WAIT_FOR_OTHER_PLAYERS.getMessage());
                plugin.debug.end("DGamePlayer#finishFloor", true);
                return;
            }
        }

        boolean invalid = !dGroup.getDungeon().isMultiFloor();

        for (Player player : dGroup.getPlayers()) {
            DGamePlayer dPlayer = getByPlayer(player);

            if (invalid) {
                dPlayer.finish(false);

            } else {
                dPlayer.finished = false;
            }
        }

        if (invalid) {
            plugin.debug.end("DGamePlayer#finishFloor", true);
            return;
        }

        DungeonConfig dConfig = dGroup.getDungeon().getConfig();
        int random = NumberUtil.generateRandomInt(0, dConfig.getFloors().size());
        DResourceWorld newFloor = dGroup.getUnplayedFloors().get(random);
        if (dConfig.getFloorCount() == dGroup.getFloorCount() - 1) {
            newFloor = dConfig.getEndFloor();

        } else if (specifiedFloor != null) {
            newFloor = specifiedFloor;
        }

        /*DGroupFinishFloorEvent event = new DGroupFinishFloorEvent(dGroup, dGroup.getGameWorld(), newFloor);

        if (event.isCancelled()) {
            return;
        }
         */
        Game game = dGroup.getGameWorld().getGame();

        dGroup.removeUnplayedFloor(dGroup.getGameWorld().getResource(), false);
        dGroup.setMapName(newFloor.getName());

        DGameWorld gameWorld = null;
        if (newFloor != null) {
            gameWorld = newFloor.instantiateAsGameWorld();
        }
        dGroup.setGameWorld(gameWorld);

        for (Player player : dGroup.getPlayers()) {
            DGamePlayer dPlayer = getByPlayer(player);
            dPlayer.setWorld(gameWorld.getWorld());
            dPlayer.setCheckpoint(dGroup.getGameWorld().getStartLocation(dGroup));
            if (dPlayer.getWolf() != null) {
                dPlayer.getWolf().teleport(dPlayer.getCheckpoint());
            }
        }
        dGroup.startGame(game);
        plugin.debug.end("DGamePlayer#finishFloor", true);
    }

    /**
     * The DGamePlayer finishs the current game.
     */
    public void finish() {
        finish(true);
    }

    /**
     * @param message
     * if messages should be sent
     */
    public void finish(boolean message) {
        plugin.debug.start("DGamePlayer#finish");
        if (message) {
            MessageUtil.sendMessage(getPlayer(), DMessages.PLAYER_FINISHED_DUNGEON.getMessage());
        }
        finished = true;

        DGroup dGroup = DGroup.getByPlayer(getPlayer());
        if (dGroup == null) {
            plugin.debug.end("DGamePlayer#finish", true);
            return;
        }

        if (!dGroup.isPlaying()) {
            plugin.debug.end("DGamePlayer#finish", true);
            return;
        }

        boolean first = true;
        boolean hasToWait = false;

        for (Player player : dGroup.getPlayers()) {
            DGamePlayer dPlayer = getByPlayer(player);
            if (!dPlayer.finished) {
                if (message) {
                    MessageUtil.sendMessage(this.getPlayer(), DMessages.PLAYER_WAIT_FOR_OTHER_PLAYERS.getMessage());
                }
                hasToWait = true;

            } else if (dPlayer != this) {
                first = false;
            }
        }

        DPlayerFinishEvent dPlayerFinishEvent = new DPlayerFinishEvent(this, first, hasToWait);

        if (dPlayerFinishEvent.isCancelled()) {
            finished = false;
            plugin.debug.end("DGamePlayer#finish", true);
            return;
        }

        if (hasToWait) {
            plugin.debug.end("DGamePlayer#finish", true);
            return;
        }

        DGroupFinishDungeonEvent dGroupFinishDungeonEvent = new DGroupFinishDungeonEvent(dGroup);

        if (dGroupFinishDungeonEvent.isCancelled()) {
            plugin.debug.end("DGamePlayer#finish", true);
            return;
        }

        Game.getByDGroup(dGroup).resetWaveKills();

        DGroupRewardEvent dGroupRewardEvent = new DGroupRewardEvent(dGroup);
        plugin.getServer().getPluginManager().callEvent(dGroupRewardEvent);
        for (Player player : dGroup.getPlayers()) {
            DGamePlayer dPlayer = getByPlayer(player);
            dPlayer.leave(false);

            if (!dGroupRewardEvent.isCancelled()) {
                for (Reward reward : dGroup.getRewards()) {
                    reward.giveTo(player);
                }
            }
        }
    }

    @Override
    public void sendMessage(String message) {
        DGameWorld gameWorld = DGameWorld.getByWorld(getWorld());
        gameWorld.sendMessage(message);

        for (DGlobalPlayer player : plugin.getDPlayers().getDGlobalPlayers()) {
            if (player.isInChatSpyMode()) {
                if (!gameWorld.getWorld().getPlayers().contains(player.getPlayer())) {
                    MessageUtil.sendMessage(player.getPlayer(), ChatColor.GREEN + "[Chatspy] " + ChatColor.WHITE + message);
                }
            }
        }
    }

    @Override
    public void update(boolean updateSecond) {
        boolean locationValid = true;
        Location teleportLocation = player.getLocation();
        boolean teleportWolf = false;
        boolean respawnInventory = false;
        boolean offline = false;
        boolean kick = false;
        boolean triggerAllInDistance = false;

        DGameWorld gameWorld = DGameWorld.getByWorld(getWorld());

        if (!updateSecond) {
            if (!getPlayer().getWorld().equals(getWorld())) {
                locationValid = false;

                if (gameWorld != null) {
                    DGroup dGroup = DGroup.getByPlayer(getPlayer());

                    teleportLocation = getCheckpoint();

                    if (teleportLocation == null) {
                        teleportLocation = dGroup.getGameWorld().getStartLocation(dGroup);
                    }

                    // Don't forget Doge!
                    if (getWolf() != null) {
                        teleportWolf = true;
                    }

                    // Respawn Items
                    if (getRespawnInventory() != null || getRespawnArmor() != null) {
                        respawnInventory = true;
                    }
                }
            }

        } else if (gameWorld != null) {
            // Update Wolf
            if (getWolf() != null) {
                if (getWolf().isDead()) {
                    if (getWolfRespawnTime() <= 0) {
                        setWolf((Wolf) getWorld().spawnEntity(getPlayer().getLocation(), EntityType.WOLF));
                        getWolf().setTamed(true);
                        getWolf().setOwner(getPlayer());
                        setWolfRespawnTime(30);
                    }
                    wolfRespawnTime--;
                }

                DMob dMob = DMob.getByEntity(getWolf());
                if (dMob != null) {
                    gameWorld.removeDMob(dMob);
                }
            }

            // Kick offline players
            if (getOfflineTime() > 0) {
                offline = true;

                if (getOfflineTime() < System.currentTimeMillis()) {
                    kick = true;
                }
            }

            triggerAllInDistance = true;
        }

        DPlayerUpdateEvent event = new DPlayerUpdateEvent(this, locationValid, teleportWolf, respawnInventory, offline, kick, triggerAllInDistance);
        plugin.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        if (!locationValid) {
            PlayerUtil.secureTeleport(getPlayer(), teleportLocation);
        }

        if (teleportWolf) {
            getWolf().teleport(teleportLocation);
        }

        if (respawnInventory) {
            applyRespawnInventory();
        }

        if (kick) {
            DPlayerKickEvent dPlayerKickEvent = new DPlayerKickEvent(this, DPlayerKickEvent.Cause.OFFLINE);

            if (!dPlayerKickEvent.isCancelled()) {
                leave();
            }
        }

        if (triggerAllInDistance) {
            DistanceTrigger.triggerAllInDistance(getPlayer(), gameWorld);
        }
    }

    /* Statics */
    public static DGamePlayer getByPlayer(Player player) {
        for (DGamePlayer dPlayer : plugin.getDPlayers().getDGamePlayers()) {
            if (dPlayer.getPlayer().equals(player)) {
                return dPlayer;
            }
        }
        return null;
    }

    public static DGamePlayer getByName(String name) {
        for (DGamePlayer dPlayer : plugin.getDPlayers().getDGamePlayers()) {
            if (dPlayer.getPlayer().getName().equalsIgnoreCase(name)) {
                return dPlayer;
            }
        }
        return null;
    }

    public static List<DGamePlayer> getByWorld(World world) {
        List<DGamePlayer> dPlayers = new ArrayList<>();

        for (DGamePlayer dPlayer : plugin.getDPlayers().getDGamePlayers()) {
            if (dPlayer.getWorld() == world) {
                dPlayers.add(dPlayer);
            }
        }

        return dPlayers;
    }

}

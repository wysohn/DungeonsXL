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
package io.github.dre2n.dungeonsxl.task;

import io.github.dre2n.dungeonsxl.DungeonsXL;
import io.github.dre2n.dungeonsxl.trigger.RedstoneTrigger;
import io.github.dre2n.dungeonsxl.world.DGameWorld;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Frank Baumann, Daniel Saukel
 */
public class RedstoneEventTask extends BukkitRunnable {

    private Block block;

    public RedstoneEventTask(final Block block) {
        this.block = block;
    }

    @Override
    public void run() {
        for (DGameWorld gameWorld : DungeonsXL.getInstance().getDWorlds().getGameWorlds()) {
            if (block.getWorld() == gameWorld.getWorld()) {
                RedstoneTrigger.updateAll(gameWorld);
            }
        }
    }

}
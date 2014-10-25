package util.save.thread;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import util.save.SaveBlock;
import util.save.SaveStatus;

import java.io.File;
import java.sql.Array;
import java.util.ArrayList;

/*
 * PMS Community
 * This is a Minecraft plugin, where you can manage your plots on many ways.
 * Copyright (C) 2014  Florian Ludewig
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
 *
 * Contact: florianludewig@gmx.de
 */

public class ReadPlotThread extends Thread{

    private ArrayList<ProtectedRegion> regions;
    private World world;
    private String path;
    private Player player;

    public ReadPlotThread(ArrayList<ProtectedRegion> regions, World world, String path, Player player){
        this.regions = regions;
        this.world = world;
        this.path = path;
        this.player = player;
    }

    public void run(){
        SaveStatus saveStatus = new SaveStatus();
        saveStatus.from = regions.size();

        File worldSave = new File(path + "/" + player.getWorld().getName());
        if(!worldSave.exists()){
            worldSave.mkdir();
        }

        for(int i = 0; i < regions.size(); i++){
            this.path = worldSave.getAbsolutePath() + "/" + regions.get(i).getId() + ".plotsave";
            if(!new File(path).exists()){
                ArrayList<SaveBlock> saveBlocks = new ArrayList<>();

                int minX = regions.get(i).getMinimumPoint().getBlockX();
                int minZ = regions.get(i).getMinimumPoint().getBlockZ();

                int maxX = regions.get(i).getMaximumPoint().getBlockX();
                int maxZ = regions.get(i).getMaximumPoint().getBlockZ();

                for(int y = 0; y < 256; y++){
                    for(int x = minX; x <= maxX; x++){
                        for(int z = minZ; z <= maxZ; z++){
                            Block block = this.world.getBlockAt(x, y, z);
                            String[] lines = null;
                            SaveBlock saveBlock = new SaveBlock(block.getX(), block.getY(), block.getZ(), lines, block.getType());
                            saveBlocks.add(saveBlock);
                        }
                    }
                }

                SaveThread saveThread = new SaveThread(saveBlocks, path, regions.get(i).getId(), saveStatus);
                saveThread.start();
            }
        }
    }
}

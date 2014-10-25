package util;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import util.save.SaveBlock;

import java.io.*;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Random;

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

public class SellGSThread extends Thread{
    private World world;
    private Player player;
    private File dataFolder;
    private String plotID;
    private int buffer;

    public SellGSThread(World world, Player player, File dataFolder, String plotID, int buffer){
        this.world = world;
        this.player = player;
        this.dataFolder = dataFolder;
        this.plotID = plotID;
        this.buffer = buffer;
    }

    @Override
    public void run(){
        System.out.println("Plot Recreate Thread started!");
        if(!plotID.equalsIgnoreCase("null")){

            System.out.println("PlotID != null");
            long millis = 0;
            millis = System.currentTimeMillis();

            File plotSave = new File(dataFolder + "/saves/" + player.getWorld().getName() + "/" + plotID + ".plotsave");
            if(plotSave != null){
                System.out.println("PlotSave != null");
                InputStream is;
                try{
                    is = new FileInputStream(plotSave.getAbsolutePath());
                    ObjectInputStream ois = new ObjectInputStream(is);
                    SaveBlock[] saveBlocksArray = (SaveBlock[]) ois.readObject();
                    /*ArrayList<SaveBlock> saveBlocks = new ArrayList();
                    for(int i = 0; i < saveBlocksArray.length; i++){
                        saveBlocks.add(saveBlocksArray[i]);
                    }*/


                    /*Random random = new Random();
                    while(saveBlocks.size() > 0){
                        int randomNextInt = random.nextInt(saveBlocks.size());
                        SaveBlock saveBlock = saveBlocks.get(randomNextInt);
                        Block block = player.getWorld().getBlockAt(saveBlock.getPosX(), saveBlock.getPosY(), saveBlock.getPosZ());
                        if(block.getType() != saveBlock.getMaterial()){
                            if(saveBlock.getMaterial() != Material.DEAD_BUSH || saveBlock.getMaterial() != Material.LONG_GRASS){
                                player.getWorld().getBlockAt(saveBlock.getPosX(), saveBlock.getPosY(), saveBlock.getPosZ()).setType(saveBlock.getMaterial());
                            }
                        }
                        saveBlocks.remove(randomNextInt);
                    }*/

                    for(int i = 0; i < saveBlocksArray.length; i++){
                        for(int j = 0; j < buffer; j++){
                            SaveBlock saveBlock = saveBlocksArray[i];
                            Block block = player.getWorld().getBlockAt(saveBlock.getPosX(), saveBlock.getPosY(), saveBlock.getPosZ());
                            if(block.getType() != saveBlock.getMaterial()){
                                if(saveBlock.getMaterial() != Material.DEAD_BUSH || saveBlock.getMaterial() != Material.LONG_GRASS){
                                    player.getWorld().getBlockAt(saveBlock.getPosX(), saveBlock.getPosY(), saveBlock.getPosZ()).setType(saveBlock.getMaterial());
                                }
                            }
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }catch (ClassNotFoundException e2){
                    e2.printStackTrace();
                }
                millis = millis - System.currentTimeMillis();
                System.out.println("BENCHMARK: " + millis);

            }else{
                System.out.println("PlotSave is null");
            }
        }else{
            System.out.println("PlotID is null");
        }
        System.out.println("Plot Recreate Thread done!");
    }
}

package util;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import util.lists.ListManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

public class Utils {
    public Utils(){

    }

    public static void copy(InputStream in, File file){
        try{
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len = in.read(buf)) > 0){
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static boolean beginsWithNumber(String string){
        char c = string.charAt(0);
        if(Character.isDigit(c)){
            return true;
        }
        return false;
    }

    public String checkPlotType(String plotID, ListManager listManager){
        for(int i = 0; i < listManager.getLists().size(); i++){
            String listType = listManager.getLists().get(i).getListType();
            String listAlias = listManager.getLists().get(i).alias;
            if(plotID.length() > listAlias.length()){
                if(!Character.isLetter(plotID.charAt(listAlias.length()))){
                    if(plotID.contains(listAlias)){
                        return listType;
                    }
                }
            }
        }
        return null;
    }

    private ArrayList<ProtectedRegion> getPlots(WorldGuardPlugin worldGuard, Server server){
        List<World> worlds = server.getWorlds();
        ArrayList<ProtectedRegion> protectedRegions = new ArrayList<>();
        for(int i = 0; i < worlds.size(); i++){
            Map<String, ProtectedRegion> regionStringMap = worldGuard.getRegionManager(worlds.get(i)).getRegions();
            for(Map.Entry<String, ProtectedRegion> entry : regionStringMap.entrySet()){
                protectedRegions.add(entry.getValue());
            }
        }
        return protectedRegions;
    }
}

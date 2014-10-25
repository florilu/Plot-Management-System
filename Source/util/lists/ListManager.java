package util.lists;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

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

public class ListManager {

    public ArrayList<PlotListsList> lists = new ArrayList<>();

    private ArrayList<ProtectedRegion> testRegions = new ArrayList<>();

    private ArrayList<ProtectedRegion> protectedRegions = new ArrayList<>();
    private WorldGuardPlugin worldGuard = null;

    private Logger logger;

    public ListManager(WorldGuardPlugin worldGuard, Logger logger){
        this.worldGuard = worldGuard;
        this.logger = logger;
    }

    public void addListType(String type, String alias){
        lists.add(new PlotListsList(type, alias));
    }

    public void runIdentification(World world){
        protectedRegions = new ArrayList<>();
        for(int i = 0; i < lists.size(); i++){
            lists.get(i).resetList();
        }
        Map<String, ProtectedRegion> regionStringMap = worldGuard.getRegionManager(world).getRegions();
        for(Map.Entry<String, ProtectedRegion> entry : regionStringMap.entrySet()){
            protectedRegions.add(entry.getValue());
        }
        for(int i = 0; i < lists.size(); i++){
            for(int j = 0; j < protectedRegions.size(); j++){
                PlotListsList plotList = lists.get(i);
                ProtectedRegion region = protectedRegions.get(j);
                if(!region.getId().equals("stadt")){
                    if(region.getId().length() - 1 > plotList.alias.length() - 1){
                        if(region.getId().contains(plotList.alias) && !Character.isLetter(region.getId().charAt(plotList.alias.length()))){
                            DefaultDomain domain = region.getOwners();
                            testRegions.add(region);
                            if(domain.size() > 0){
                                plotList.mainList.add("§4" + region.getId() +  "§f, ");
                                plotList.saveList.add(region);
                            }else{
                                plotList.mainList.add("§a" + region.getId() + "§f, ");
                                plotList.saveList.add(region);
                            }
                        }
                    }
                }
            }
        }

        for(int i = 0; i < lists.size(); i++){
            PlotListsList newList = lists.get(i);
            Collections.sort(newList.mainList);
        }
    }

    public ArrayList<PlotListsList> getLists(){
        return this.lists;
    }

    public ArrayList<ProtectedRegion> getProtectedRegions(){
        if(this.protectedRegions != null){
            return this.protectedRegions;
        }
        return null;
    }
}

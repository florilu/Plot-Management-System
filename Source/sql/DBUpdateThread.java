package sql;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import util.Utils;
import util.lists.ListManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

public class DBUpdateThread extends Thread{

    private Server server;
    private SQLibrary sql;
    private Logger logger;
    private WorldGuardPlugin worldGuard;
    private ListManager listManager;
    private Utils utils;
    private int ownPlots, onPlots;

    public DBUpdateThread(Server server, SQLibrary sql, Logger logger, WorldGuardPlugin worldGuard, ListManager listManager, int ownPlots, int onPlots){
        this.server = server;
        this.sql = sql;
        this.logger = logger;
        this.worldGuard = worldGuard;
        this.listManager = listManager;
        this.ownPlots = ownPlots;
        this.onPlots = onPlots;
    }

    public void run() {
        try{
            utils = new Utils();

            /*Connection c = sql.getConnection();
            Statement stmt = c.createStatement();
            String sql = "DROP TABLE players";
            stmt.execute(sql);
            stmt.close();
            stmt = c.createStatement();
            sql = "CREATE TABLE IF NOT EXISTS players (playername TEXT, uuid TEXT, plot1 TEXT, plot2 TEXT, plotGuest TEXT);";
            stmt.executeUpdate(sql);
            stmt.close();*/

            OfflinePlayer[] offlinePlayers = this.server.getOfflinePlayers();
            Player[] onlinePlayers = this.server.getOnlinePlayers();

            //Spielerliste anlegen und am Ende auf doppelte Sätze überprüfen.
            ArrayList<PlayerNameUUIDHolder> playerNames = new ArrayList<>();

            for(int i = 0; i < offlinePlayers.length; i++){
                playerNames.add(new PlayerNameUUIDHolder(offlinePlayers[i].getName(), "null"));
            }
            for(int i = 0; i < onlinePlayers.length; i++){
                playerNames.add(new PlayerNameUUIDHolder(onlinePlayers[i].getName(), onlinePlayers[i].getUniqueId().toString()));
            }
            for(int i = 0; i < playerNames.size(); i++){
                String current = playerNames.get(i).uuid;
                for(int j = 0; j < playerNames.size(); j++){
                    if(j != i){
                        if(playerNames.get(j).name.equals(current)){
                            playerNames.remove(j);
                        }
                    }
                }
            }
            //Ende.
            logger.info("Start reading worlds!");
            List<World> worlds = server.getWorlds();
            for(int i = 0; i < worlds.size(); i++){
                Map<String, ProtectedRegion> regionStringMap = worldGuard.getRegionManager(worlds.get(i)).getRegions();
                for(int j = 0; j < playerNames.size(); j++){
                    ArrayList<String> plotIDs = new ArrayList<>();
                    ArrayList<String> plotIDsGuest = new ArrayList<>();
                    for(Map.Entry<String, ProtectedRegion> entry : regionStringMap.entrySet()){
                        ProtectedRegion region = entry.getValue();
                        if(region.getOwners().size() > 0 && region.getOwners().contains(playerNames.get(j).name) && this.utils.checkPlotType(region.getId(), this.listManager) != null){
                            plotIDs.add(region.getId());
                        }
                        if(region.getMembers().size() > 0 && region.getMembers().contains(playerNames.get(j).name) && this.utils.checkPlotType(region.getId(), this.listManager) != null){
                            plotIDsGuest.add(region.getId());
                        }
                    }

                    //Build table structure
                    String[] table = new String[2 + this.ownPlots + this.onPlots];
                    table[0] = "playername";
                    table[1] = "uuid";
                    int count = 1;
                    for(int a = 2; a < (2 + this.ownPlots); a++){
                        table[a] = "plot" + count;
                        count++;
                    }
                    count = 1;
                    for(int a = 2 + this.ownPlots; a < table.length; a++){
                        table[a] = "plotGuest" + count;
                        count++;
                    }
                    count = 1;
                    //End Build table structure

                    //Build values
                    String[] values = new String[2 + this.ownPlots + this.onPlots];
                    values[0] = playerNames.get(j).name;
                    values[1] = playerNames.get(j).uuid;
                    for(int a = 2; a < this.ownPlots + 2; a++){
                        if((a - 2) < plotIDs.size() && (a - 2) <= this.ownPlots){
                            values[a] = plotIDs.get(a - 2);
                        }else{
                            values[a] = "null";
                        }
                    }
                    for(int a = this.ownPlots + 2; a < values.length; a++){
                        if(count < plotIDsGuest.size()){
                            values[a] = plotIDsGuest.get(count);
                        }else{
                            values[a] = "null";
                        }
                        count++;
                    }
                    //Build values End

                    if(this.sql.selectWhere(worlds.get(i).getName(), "playername", "playername", playerNames.get(j).name) != null){
                        this.sql.update(worlds.get(i).getName(), table, values, "playername", playerNames.get(j).name);
                    }else{
                        this.sql.insert(worlds.get(i).getName(), table, values);
                    }
                }
                logger.info("World " + worlds.get(i).getName() + " done!");
            }
            logger.info("Reading worlds done!");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

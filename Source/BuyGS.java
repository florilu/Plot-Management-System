import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import language.LanguagePack;
import sql.DBUpdateThread;
import sql.SQLibrary;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import util.*;
import util.config.ConfigHolder;
import util.lists.ListManager;
import util.lists.Plot;
import util.lists.PlotListsList;
import util.save.PlotIdentifier;
import util.save.thread.ReadPlotThread;

import java.io.*;
import java.sql.*;
import java.util.*;

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


//TODO Updatedatabases, BuyPlot, und SellPlot überarbeiten und auf konfigurierbare Datenbanken umstellen.

public class BuyGS extends JavaPlugin implements Listener{

    public final String PLUGIN_NAME = "§6[§2Plot Management System§6]§f ";

    private final int rev = 74;
    private final String ver = "1.7.0";

    private Economy econ;

    private FileConfiguration config = null;
    private File configFile;
    private File plotNames;
    private File languagePackFileEn;
    private File languagePackFileDe;
    private String currentLanguagePack;
    private File currentLanguagePackFile;
    private LanguagePack lp;
    private FileConfiguration languagePackConfig;
    private ConfigHolder configHolder;

    private int ownPlots;
    private int onPlots;

    private int recreateBuffer = 1;

    private Utils utils;

    private ArrayList<String> plotNamesList = new ArrayList<>();
    private ArrayList<Plot> plotsFromConfig = new ArrayList<>();

    private ListManager listManager;

    private SQLibrary sql;

    public void onEnable(){
        Bukkit.getPluginManager().registerEvents(this, this);

        this.utils = new Utils();
        listManager = new ListManager(this.getWorldGuard(), this.getLogger());

        this.configFile = new File(getDataFolder(), "config.yml");
        this.plotNames = new File(getDataFolder(), "plotNames.txt");

        File languagePackFolder = new File(getDataFolder() + "/languagepacks");
        if(!languagePackFolder.exists()){
            languagePackFolder.mkdir();
        }
        this.languagePackFileEn = new File(languagePackFolder + "/en.lp");
        this.languagePackFileDe = new File(languagePackFolder + "/de.lp");

        /*SQLLogger logger = new SQLLogger(this.getDataFolder());
        logger.writeLine(languagePackFileEn.getAbsolutePath());
        logger.writeLine(languagePackFileDe.getAbsolutePath());*/

        if(!configFile.exists()){
            configFile.getParentFile().mkdirs();
            utils.copy(getResource("config.yml"), configFile);
        }
        if(!plotNames.exists()){
            plotNames.getParentFile().mkdirs();
            utils.copy(getResource("plotNames.txt"), plotNames);
        }
        if(!languagePackFileEn.exists()){
            languagePackFileEn.getParentFile().mkdirs();
            utils.copy(getResource("languagepacks/en.lp"), languagePackFileEn);
        }
        if(!languagePackFileDe.exists()){
            languagePackFileDe.getParentFile().mkdirs();
            utils.copy(getResource("languagepacks/de.lp"), languagePackFileDe);
        }

        config = new YamlConfiguration();
        loadYamls();

        currentLanguagePack = config.get("general.languagePack").toString();


        readLanguagePack(currentLanguagePack);

        this.getLogger().info("Initializing Databases!");

        //Add Players to the database

        sql = new SQLibrary();
        this.sql.setWriteInConsole(config.getBoolean("general.sqlOutput"));
        /*if(!new File(this.getDataFolder() + "/sql").exists()){
            new File(this.getDataFolder() + "/sql").mkdir();
        }*/
        this.ownPlots = config.getInt("general.ownPlots");
        this.onPlots = config.getInt("general.onPlots");

        sql.connectToDatabase(this.getDataFolder().getAbsolutePath() + "/sql", "database.db"); //Original

        if((this.ownPlots >= 20 || this.ownPlots >= 20) || (this.ownPlots >= 20 && this.onPlots >= 20)){
            if(this.ownPlots >= 20){
                this.getLogger().warning("Config ownPlots is not allowed to be bigger or equal 20!");
            }
            if(this.onPlots >= 20){
                this.getLogger().warning("Config onPlots is not allowed to be bigger or equal 20!");
            }
            this.onDisable();
        }

        //Create table structure.
        String[] table = new String[2 + this.ownPlots + this.onPlots];
        table[0] = "playername";
        table[1] = "uuid";
        int count = 1;
        for(int i = 2; i < ownPlots + 2; i++){
            table[i] = "plot" + count;
            count++;
        }
        count = 1;
        for(int i = (2 + this.ownPlots); i < table.length; i++){
            table[i] = "plotGuest" + count;
            count++;
        }
        //End table structure.
        for(int i = 0; i < Bukkit.getServer().getWorlds().size(); i++){
            World world = (World)Bukkit.getServer().getWorlds().get(i);
            sql.createTable(world.getName(), table, sql.TEXT_TABLE);
        }
        sql.setWriteInConsole(true);

        this.getLogger().info("Databases initialized!");

        setupEconomy();

        try{
            BufferedReader reader = new BufferedReader(new FileReader(plotNames));
            String line;
            while((line = reader.readLine()) != null){
                plotNamesList.add(line);
            }

            for(int i = 0; i < plotNamesList.size(); i++){
                this.getLogger().info(plotNamesList.get(i));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        for(int i = 0; i < plotNamesList.size(); i++){
            Plot plot = new Plot(plotNamesList.get(i),
                    config.get("plots." + plotNamesList.get(i) + ".identification").toString(),
                    config.get("plots." + plotNamesList.get(i) + ".costs").toString(),
                    config.get("plots." + plotNamesList.get(i) + ".permission").toString(),
                    config.getInt("plots." + plotNamesList.get(i) + ".pps"));
            plotsFromConfig.add(plot);

            this.listManager.addListType(plotNamesList.get(i), config.get("plots." + plotNamesList.get(i) + ".identification").toString());
        }

        this.recreateBuffer = config.getInt("general.plotRecreateBuffer");

        this.listManager.runIdentification(Bukkit.getWorld("world"));

        this.getLogger().info(PLUGIN_NAME + " (PMS) started!");
    }

    public void onDisable(){
        if(sql != null){
            sql.disconnect();
        }
        this.getLogger().info(PLUGIN_NAME + " (PMS) stopped!");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]){

        Player player = (Player) sender;

        long firstMillis = System.currentTimeMillis();

        if(cmd.getName().equalsIgnoreCase("listplots")){
            if(args.length == 1){
                if(args[0].equalsIgnoreCase("all") && player.hasPermission("fcgsbuy.listall")){
                    RegionManager regionManager = this.getWorldGuard().getRegionManager(player.getWorld());
                    String list = PLUGIN_NAME;
                    Map<String, ProtectedRegion> regionList = regionManager.getRegions();
                    for(Map.Entry<String, ProtectedRegion> entry : regionList.entrySet()){
                        list = list + entry.getValue().getId() + ", ";
                    }
                    sender.sendMessage(list);

                    return true;
                }else{
                    this.listManager.runIdentification(player.getWorld());

                    for(int i = 0; i < listManager.getLists().size(); i++){
                        if(args[0].equalsIgnoreCase(listManager.getLists().get(i).plotType)){
                            String list = PLUGIN_NAME;
                            for(int j = 0; j < listManager.getLists().get(i).mainList.size(); j++){
                                list = list + listManager.getLists().get(i).mainList.get(j);
                            }
                            sender.sendMessage(list);
                            long secondMillis = System.currentTimeMillis();
                            this.getLogger().info(PLUGIN_NAME + "Needed " + (secondMillis - firstMillis) + " Milliseconds to create the list.");
                        }
                    }
                    return true;
                }
            }else if(args.length > 1){
                sender.sendMessage(PLUGIN_NAME + lp.tooFewArguments);
                return false;
            }else{
                sender.sendMessage(PLUGIN_NAME + lp.tooManyArguments);
                return false;
            }
        }

        if(cmd.getName().equalsIgnoreCase("buyplot")){
            if(args.length == 1 || args.length == 0){
                String plotID = null;
                String plotType = null;
                if(args.length == 0){
                    ArrayList<ProtectedRegion> standingInPlots = getStandingInPlot(sender);
                    if(standingInPlots != null){
                        for(int i = 0; i < standingInPlots.size(); i++){
                            this.getLogger().info(standingInPlots.get(i).getId());
                            if(this.utils.checkPlotType(standingInPlots.get(i).getId(), this.listManager) != null){
                                plotType = this.utils.checkPlotType(standingInPlots.get(i).getId(), this.listManager);
                                plotID = standingInPlots.get(i).getId();
                                i = standingInPlots.size() - 1;
                            }
                        }
                    }
                }else{
                    plotID = args[0];
                    plotType = this.utils.checkPlotType(plotID, this.listManager);
                }
                DefaultDomain domain = new DefaultDomain();
                domain.addPlayer(player.getName());
                if(plotType != null){
                    for(int i = 0; i < plotsFromConfig.size(); i++){
                        Plot plotFC = plotsFromConfig.get(i);
                        if(plotType.equalsIgnoreCase(plotFC.getPlotType())){
                            if(player.hasPermission(plotFC.getPermission())){
                                int number = 1;
                                boolean canHave = false;
                                for(int j = 0; j < this.ownPlots; j++){
                                    String plotPlot = sql.selectWhere(player.getWorld().getName(), "plot" + (j + 1), "playername", sender.getName());
                                    if(!plotPlot.equalsIgnoreCase("null")){
                                        number += 1;
                                    }
                                }
                                if(number == this.ownPlots + 1){
                                    sender.sendMessage(PLUGIN_NAME + this.lp.alreadyOwnOneOrMore);
                                    return true;
                                }else{
                                    canHave = true;
                                }
                                if(canHave){
                                    if(!hasOwner(plotID, player)){
                                        if(plotFC.getPlotCost() > 0 || plotFC.getPricePerSquare() > 0){
                                            if(econ.getBalance(player.getName()) >= plotFC.getPlotCost()){
                                                EconomyResponse r;
                                                int pay = 0;
                                                if(plotFC.getPlotCost() > 0){
                                                    pay += plotFC.getPlotCost();
                                                }
                                                if(plotFC.getPricePerSquare() > 0){
                                                    ProtectedRegion region = this.getWorldGuard().getRegionManager(player.getWorld()).getRegion(plotID);

                                                    int minX = region.getMinimumPoint().getBlockX();
                                                    int maxX = region.getMaximumPoint().getBlockX();
                                                    int minZ = region.getMinimumPoint().getBlockZ();
                                                    int maxZ = region.getMaximumPoint().getBlockZ();

                                                    int width = (maxX - minX) + 1;
                                                    int length = (maxZ - minZ) + 1;

                                                    int result = plotFC.getPricePerSquare() * (width * length);

                                                    pay += result;
                                                }
                                                r = econ.withdrawPlayer(player.getName(), pay);
                                                if(r.transactionSuccess()){
                                                    sql.update(player.getWorld().getName(), new String[]{"plot" + number, plotID}, "uuid", player.getUniqueId().toString(), true);
                                                    this.getWorldGuard().getRegionManager(player.getWorld()).getRegion(plotID).setOwners(domain);
                                                    String from = lp.plotFrom;
                                                    from = from.replace("$player", player.getName());
                                                    this.getWorldGuard().getRegionManager(player.getWorld()).getRegion(plotID).setFlag(DefaultFlag.GREET_MESSAGE, PLUGIN_NAME + from);
                                                    saveWorld(player);
                                                    String message = lp.successBuy;
                                                    message = message.replace("$plotID", plotID);
                                                    message = message.replace("$amount", String.valueOf(r.amount));
                                                    message = message.replace("$econSingular", econ.currencyNameSingular());
                                                    sender.sendMessage(PLUGIN_NAME + message);
                                                    return true;
                                                }else{
                                                    sender.sendMessage(PLUGIN_NAME + lp.failedBuy);
                                                    this.getLogger().info(PLUGIN_NAME + r.errorMessage);
                                                    return true;
                                                }
                                            }else{
                                                double currentBalance = econ.getBalance(player.getName());
                                                int plotCost = plotFC.getPlotCost();
                                                double need = (currentBalance - plotCost) * -1;
                                                String message = lp.notEnoughMoney;
                                                message = message.replace("$amount", String.valueOf(need));
                                                message = message.replace("$econSingular", econ.currencyNameSingular());
                                                sender.sendMessage(PLUGIN_NAME + message);
                                            }
                                        }else{
                                            sql.update(player.getWorld().getName(), new String[]{"plot" + number, plotID}, "uuid", player.getUniqueId().toString(), true);
                                            this.getWorldGuard().getRegionManager(player.getWorld()).getRegion(plotID).setOwners(domain);
                                            String from = lp.plotFrom;
                                            from = from.replace("$player", player.getName());
                                            this.getWorldGuard().getRegionManager(player.getWorld()).getRegion(plotID).setFlag(DefaultFlag.GREET_MESSAGE, PLUGIN_NAME + from);
                                            saveWorld(player);
                                            String message = lp.successGetPlot;
                                            message = message.replace("$plotID", plotID);
                                            sender.sendMessage(PLUGIN_NAME + message);
                                            return true;
                                        }
                                    }else{
                                        sender.sendMessage(PLUGIN_NAME + lp.belongsToSomeone);
                                        return true;
                                    }
                                }
                            }else{
                                sender.sendMessage(PLUGIN_NAME + lp.plotBuyNotEnoughPermissions);
                                return true;
                            }
                        }
                    }
                }else{
                    sender.sendMessage(PLUGIN_NAME + lp.notStandingOnPlot);
                }
                return true;
            }else {
                sender.sendMessage(PLUGIN_NAME + lp.tooManyArguments);
                return false;
            }
        }

        if(cmd.getName().equalsIgnoreCase("sellplot")){
            if(args.length < 1){
                sender.sendMessage(lp.tooFewArguments);
            }else if(args.length > 1){
                sender.sendMessage(lp.tooManyArguments);
            }else{
                String plotID = null;
                int columnNumber = 0;
                for(int i = 0; i < this.ownPlots; i++){
                    String plot = sql.selectWhere(player.getWorld().getName(), "plot" + (i + 1), "playername", player.getName());
                    if(plot != null){
                        if(plot.equalsIgnoreCase(args[0])){
                            plotID = plot;
                            columnNumber = i + 1;
                        }
                    }
                }
                if(plotID != null){
                    String plotType = this.utils.checkPlotType(plotID, this.listManager);

                    double plotCost = 0;

                    for(int i = 0; i < plotsFromConfig.size(); i++){
                        Plot plotFC = plotsFromConfig.get(i);
                        if(plotFC.getPlotType().equalsIgnoreCase(this.utils.checkPlotType(plotID, this.listManager))){
                            if(plotFC.getPlotCost() > 0){
                                plotCost += plotFC.getPlotCost();
                            }
                            if(plotFC.getPricePerSquare() > 0){
                                ProtectedRegion region = this.getWorldGuard().getRegionManager(player.getWorld()).getRegion(plotID);
                                int minX = region.getMinimumPoint().getBlockX();
                                int maxX = region.getMaximumPoint().getBlockX();
                                int minZ = region.getMinimumPoint().getBlockZ();
                                int maxZ = region.getMaximumPoint().getBlockZ();

                                int width = (maxX - minX) + 1;
                                int length = (maxZ - minZ) + 1;

                                plotCost += plotFC.getPricePerSquare() * (width * length);
                            }
                            plotCost = plotFC.getPlotCost();
                            break;
                        }
                    }

                    ProtectedRegion region = this.getWorldGuard().getRegionManager(player.getWorld()).getRegion(plotID);
                    sql.update(player.getWorld().getName(), new String[]{"plot" + columnNumber, "null"}, "uuid", player.getUniqueId().toString(), true);

                    DefaultDomain domain = region.getOwners();
                    domain.getGroups().clear();
                    this.getWorldGuard().getRegionManager(player.getWorld()).getRegion(plotID).setOwners(domain);
                    this.getWorldGuard().getRegionManager(player.getWorld()).getRegion(plotID).getFlags().clear();
                    this.saveWorld(player);

                    if(plotCost > 0){
                        econ.depositPlayer(player.getName(), plotCost);
                        String message = lp.successSoldPlot;
                        message = message.replace("$plotType", plotType);
                        message = message.replace("$amount", String.valueOf(plotCost));
                        message = message.replace("$econSingular", econ.currencyNameSingular());
                        sender.sendMessage(PLUGIN_NAME + message);
                        return true;
                    }else{
                        String message = lp.successLeftPlot;
                        message = message.replace("$plotType", plotType);
                        sender.sendMessage(PLUGIN_NAME + message);
                    }
                    SellGSThread sellGSThread = new SellGSThread(player.getWorld(), player, this.getDataFolder(), plotID, this.recreateBuffer);
                    sellGSThread.start();
                }
            }
            return true;
        }

        if(cmd.getName().equalsIgnoreCase("adduser")){
            // /adduser <plotID> <playerName>
            if(args.length < 2){
                sender.sendMessage(PLUGIN_NAME + lp.tooFewArguments);
                return false;
            }else if(args.length > 2){
                sender.sendMessage(PLUGIN_NAME + lp.tooManyArguments);
                return false;
            }else{
                int count = 0;
                String sqlSelectResult = null;
                for(int i = 1; i <= this.onPlots; i++){
                    sqlSelectResult = sql.selectWhere(player.getWorld().getName(), "plotGuest" + i, "playerName", args[1]);
                    if(sqlSelectResult != null){
                        if(!sqlSelectResult.equalsIgnoreCase("null")){
                            count++;
                        }
                    }else{
                        sender.sendMessage(this.PLUGIN_NAME + this.lp.playerDoesntExist);
                        return true;
                    }
                }
                if(count < this.onPlots){
                    sql.update(player.getWorld().getName(), new String[]{"plotGuest" + (count + 1), args[0]}, "playername", args[1], true);
                    ProtectedRegion region = this.getWorldGuard().getRegionManager(player.getWorld()).getRegion(args[0]);
                    DefaultDomain domain = region.getMembers();
                    domain.addPlayer(args[1]);
                    region.setMembers(domain);
                    saveWorld(player);
                    String message = lp.userIsAbleToBuildOnPlot;
                    message = message.replace("$player", args[1]);
                    message = message.replace("$plotID", region.getId());
                    sender.sendMessage(PLUGIN_NAME + message);
                }else{
                    sender.sendMessage(PLUGIN_NAME + lp.userIsAlreadyAbleToBuildOnPlot);
                }
                return true;
            }
        }

        if(cmd.getName().equalsIgnoreCase("removeuser")){
            // /removeuser <plotID> <playerName>

            if(args.length < 2){
                sender.sendMessage(PLUGIN_NAME + lp.tooFewArguments);
                return false;
            }else if(args.length > 2){
                sender.sendMessage(PLUGIN_NAME + lp.tooManyArguments);
                return false;
            }else{
                ProtectedRegion region = this.getWorldGuard().getRegionManager(player.getWorld()).getRegion(args[0]);
                DefaultDomain domain = region.getMembers();
                if(domain.contains(args[1])){
                    for(int i = 0; i < this.onPlots; i++){
                        String sqlResult = sql.selectWhere(player.getWorld().getName(), "plotGuest" + (i + 1), "playername", args[1]);
                        System.out.println(sqlResult);
                        if(sqlResult != null){
                            if(!sqlResult.equalsIgnoreCase("null")){
                                if(sqlResult.equalsIgnoreCase(args[0])){
                                    sql.update(player.getWorld().getName(), new String[]{"plotGuest" + (i + 1), "null"}, "playername", args[1], true);
                                }
                            }
                        }
                    }
                    domain.removePlayer(args[1]);
                    saveWorld(player);
                    String message = lp.noLongerCanBuildOnPlot;
                    message = message.replace("$player", args[1]);
                    sender.sendMessage(PLUGIN_NAME + message);
                    return true;
                }else{
                    String message = lp.playerDoesntExist;
                    message = message.replace("$player", args[1]);
                    sender.sendMessage(PLUGIN_NAME + message);
                    return true;
                }
            }
        }

        if(cmd.getName().equalsIgnoreCase("removeowner")){
            if(args.length < 1){
                sender.sendMessage(PLUGIN_NAME + lp.tooFewArguments);
                return false;
            }else if(args.length > 1){
                sender.sendMessage(PLUGIN_NAME + lp.tooManyArguments);
                return false;
            }else{
                PlotIdentifier plotIdentifier = new PlotIdentifier(this.plotsFromConfig);
                if(plotIdentifier.isPlot(args[0])){
                    DefaultDomain owners =  this.getWorldGuard().getRegionManager(player.getWorld()).getRegion(args[0]).getOwners();
                    DefaultDomain members = this.getWorldGuard().getRegionManager(player.getWorld()).getRegion(args[0]).getOwners();

                    Set<String> ownerSet = owners.getPlayers();
                    Set<String> memberSet = members.getPlayers();

                    for(String ownerString : ownerSet){
                        for(int i = 0; i < this.ownPlots; i++){
                            if(sql.selectWhere(player.getWorld().getName(), "plot" + (i + 1), "playername", ownerString).equalsIgnoreCase(args[0])){
                                sql.update(player.getWorld().getName(), new String[]{"plot" + (i + 1), "null"}, "playername", ownerString, true);
                            }
                        }
                    }
                    for(String memberString : memberSet){
                        for(int i = 0; i < this.onPlots; i++){
                            if(sql.selectWhere(player.getWorld().getName(), "plotGuest" + (i + 1), "playername", memberString).equalsIgnoreCase(args[0])){
                                sql.update(player.getWorld().getName(), new String[]{"plotGuest", "null"}, "playername", memberString, true);
                            }
                        }
                    }
                    owners.getPlayers().clear();
                    members.getPlayers().clear();
                    this.getWorldGuard().getRegionManager(player.getWorld()).getRegion(args[0]).setOwners(owners);
                    this.getWorldGuard().getRegionManager(player.getWorld()).getRegion(args[0]).setMembers(members);
                    this.getWorldGuard().getRegionManager(player.getWorld()).getRegion(args[0]).setFlag(DefaultFlag.GREET_MESSAGE, null);
                }else{
                    return true;
                }
                try{
                    this.getWorldGuard().getRegionManager(player.getWorld()).save();
                }catch (ProtectionDatabaseException e){
                    e.printStackTrace();
                }
                String message = lp.allMembersAndOwnersHaveBeenRemoved;
                message = message.replace("$plotID", args[0]);
                sender.sendMessage(PLUGIN_NAME + message);
                return true;
            }
        }

        if(cmd.getName().equalsIgnoreCase("updatedatabases")){
            DBUpdateThread dbUpdateThread = new DBUpdateThread(Bukkit.getServer(), this.sql, this.getLogger(), this.getWorldGuard(), this.listManager, this.ownPlots, this.onPlots);
            dbUpdateThread.start();
            return true;
        }

        if(cmd.getName().equalsIgnoreCase("ownedby")){
            if(args.length > 1){
                player.sendMessage(PLUGIN_NAME + lp.tooFewArguments);
                return false;
            }else if(args.length < 1){
                player.sendMessage(PLUGIN_NAME + lp.tooManyArguments);
                return false;
            }else{
                ArrayList<String> results = new ArrayList<>();
                for(int i = 0; i < this.ownPlots; i++) {
                    String result = "";
                    result = sql.selectWhere(player.getWorld().getName(), "playername", "plot" + (i + 1), args[0]);
                    if(result != null){
                        if(!result.equalsIgnoreCase("null")){
                            results.add(result);
                        }
                    }
                }

                if(results.size() > 0){
                    String list = "";
                    for(int i = 0; i < results.size(); i++){
                        if(i != (results.size() - 1)){
                            list = list + results.get(i) + ", ";
                        }else{
                            list = list + results.get(i);
                        }
                    }

                    String message = lp.ownedByPlayer;
                    message = message.replace("$player", list);
                    player.sendMessage(PLUGIN_NAME + message);
                }else {
                    player.sendMessage(PLUGIN_NAME + lp.ownedByNobody);
                }
                return true;
            }
        }

        if(cmd.getName().equalsIgnoreCase("listplotsfromplayer")){
            if(args.length < 1){
                player.sendMessage(PLUGIN_NAME + lp.tooFewArguments);
                return false;
            }else if(args.length > 1){
                player.sendMessage(PLUGIN_NAME + lp.tooManyArguments);
                return false;
            }else{
                ArrayList<String> plots = new ArrayList<>();
                for(int i = 0; i < this.ownPlots; i++){
                    String plot = sql.selectWhere(player.getWorld().getName(), "plot" + (i + 1), "playername", args[0]);
                    if(plot != null){
                        if(!plot.equalsIgnoreCase("null")){
                            plots.add(plot);
                        }
                    }
                }
                String complete = "";
                for(int i = 0; i < plots.size(); i++){
                    if(i < plots.size() - 1){
                        complete = complete + plots.get(i) + ", ";
                    }else{
                        complete = complete + plots.get(i);
                    }
                }
                String message = lp.thePlayerOwnsList;
                message = message.replace("$player", args[0]);
                message = message.replace("$list", complete);
                player.sendMessage(PLUGIN_NAME + message);

                return true;
            }
        }

        if(cmd.getName().equalsIgnoreCase("saveplots")){
            if(args.length > 0){
                player.sendMessage(PLUGIN_NAME + lp.tooManyArguments);
                return false;
            }else{
                File plotSave = new File(this.getDataFolder() + "/saves");
                if(!plotSave.exists()){
                    plotSave.mkdir();
                }
                PlotIdentifier plotIdentifier = new PlotIdentifier(this.plotsFromConfig);
                ArrayList<ProtectedRegion> regions = plotIdentifier.getWorldPlotList(this.getPlots(player));
                World world = getServer().getWorld(player.getWorld().getName());
                ReadPlotThread plotThread = new ReadPlotThread(regions, world, plotSave.getAbsolutePath(), player);
                plotThread.start();
                return true;
            }
        }

        //Debug
        if(cmd.getName().equalsIgnoreCase("isplot")){
            if(args.length < 1){
                sender.sendMessage(lp.tooFewArguments);
                return false;
            }else if(args.length > 1){
                sender.sendMessage(lp.tooManyArguments);
                return false;
            }else{
                PlotIdentifier plotIdentifier = new PlotIdentifier(this.plotsFromConfig);
                sender.sendMessage(PLUGIN_NAME + "ID: " + args[0] + " PLOT: " + plotIdentifier.isPlot(args[0]));
                return true;
            }
        }

        if(cmd.getName().equalsIgnoreCase("pmsinfo")){
            String info = "";
            info = info + PLUGIN_NAME + "PMS made by florilu \n";
            for(int i = 0; i < PLUGIN_NAME.length(); i++){
                info = info + " ";
            }
            info = info + "Ver " + this.ver + " Rev " + this.rev;
            sender.sendMessage(info);
            return true;
        }

        if(cmd.getName().equalsIgnoreCase("changepack")){
            if(args.length > 1){
                sender.sendMessage(PLUGIN_NAME + lp.tooManyArguments);
                return false;
            }else if(args.length < 1){
                sender.sendMessage(PLUGIN_NAME + lp.tooFewArguments);
                return false;
            }else{
                if(readLanguagePack(args[0])){
                    sender.sendMessage(PLUGIN_NAME + lp.successLoadLP);
                }else{
                    sender.sendMessage(PLUGIN_NAME + lp.couldntLoadLP);
                }
                return true;
            }
        }

        //TODO hier ist ein bug in der liste.
        if(cmd.getName().equalsIgnoreCase("myplots")){
            if(args.length > 0){
                sender.sendMessage(PLUGIN_NAME + lp.tooManyArguments);
                return false;
            }else{
                ArrayList<String> plotIDs = new ArrayList<>();
                for(int i = 0; i < this.ownPlots; i++){
                    String result = this.sql.selectWhere(player.getWorld().getName(), "plot" + (i + 1), "playername", player.getName());
                    if(!result.equalsIgnoreCase("null")){
                        plotIDs.add(result);
                    }
                }
                if(plotIDs.size() > 0){
                    String list = "";
                    for(int i = 0; i < plotIDs.size(); i++){
                        if(i != (plotIDs.size() - 1)){
                            list = list + plotIDs.get(i) + ", ";
                        }else{
                            list = list + plotIDs.get(i);
                        }
                    }
                    sender.sendMessage(PLUGIN_NAME + this.lp.myPlotsSuccess + list);
                }else{
                    sender.sendMessage(PLUGIN_NAME + this.lp.myPlotsNoPlots);
                }
                return true;
            }
        }

        if(cmd.getName().equalsIgnoreCase("defineplot")){
            if(args.length > 5){
                sender.sendMessage(PLUGIN_NAME + this.lp.tooManyArguments);
                return false;
            }else if(args.length < 5){
                sender.sendMessage(PLUGIN_NAME + this.lp.tooFewArguments);
                return false;
            }else{
                try{
                    ArrayList<String> lines = new ArrayList<>();
                    BufferedReader reader = new BufferedReader(new FileReader(this.plotNames));
                    String line = null;
                    while((line = reader.readLine()) != null){
                        lines.add(line);
                    }
                    if(!lines.contains(args[0])){
                        lines.add(args[0]);
                    }

                    reader.close();

                    BufferedWriter writer = new BufferedWriter(new FileWriter(this.plotNames));
                    for(int i = 0; i < lines.size(); i++){
                        writer.write(lines.get(i));
                        writer.newLine();
                    }
                    writer.close();

                    if(!this.plotNamesList.contains(args[0])){
                        this.plotNamesList.add(args[0]);
                    }

                    this.config.set("plots." + args[0] + ".costs", Integer.valueOf(args[1]));
                    this.config.set("plots." + args[0] + ".permission", args[2]);
                    this.config.set("plots." + args[0] + ".identification", args[3]);
                    this.config.set("plots." + args[0] + ".pps", args[4]);

                    Plot newPlot = new Plot(args[0], args[3], args[1], args[2], Integer.valueOf(args[4]));
                    if(!this.plotsFromConfig.contains(newPlot)){
                        this.plotsFromConfig.add(newPlot);
                    }
                    PlotListsList plotListsList = new PlotListsList(args[0], args[3]);
                    if(!this.listManager.getLists().contains(plotListsList)){
                        this.listManager.getLists().add(plotListsList);
                    }

                    this.config.save(this.configFile);
                }catch (IOException e){
                    e.printStackTrace();
                    sender.sendMessage(PLUGIN_NAME + this.lp.errorOccurred);
                    return true;
                }
                sender.sendMessage(PLUGIN_NAME + this.lp.successDefined);
                return true;
            }
        }

        if(cmd.getName().equalsIgnoreCase("plotinfo")){
            if(args.length > 1){
                sender.sendMessage(PLUGIN_NAME + this.lp.tooManyArguments);
            }else if(args.length < 1){
                sender.sendMessage(PLUGIN_NAME + this.lp.tooFewArguments);
                return false;
            }else{
                PlotIdentifier plotIdentifier = new PlotIdentifier(this.plotsFromConfig);
                if(plotIdentifier.isPlot(args[0])){
                    ProtectedRegion region = this.getWorldGuard().getRegionManager(player.getWorld()).getRegion(args[0]);
                    if(region != null){
                        String size = "";
                        int minX = region.getMinimumPoint().getBlockX();
                        int maxX = region.getMaximumPoint().getBlockX();
                        int minZ = region.getMinimumPoint().getBlockZ();
                        int maxZ = region.getMaximumPoint().getBlockZ();

                        int width = (maxX - minX) + 1;
                        int length = (maxZ - minZ) + 1;

                        int price = 0;

                        size = String.valueOf(width) + " x " + String.valueOf(length);
                        boolean owned = region.getOwners().size() > 0;
                        String type = this.utils.checkPlotType(args[0], this.listManager);

                        for(int i = 0; i < plotsFromConfig.size(); i++){
                            Plot plot = plotsFromConfig.get(i);
                            if(plot.getPlotType().equalsIgnoreCase(type)){
                                price += plot.getPlotCost();
                                price += plot.getPricePerSquare() * (width * length);
                                break;
                            }
                        }

                        String message = this.lp.plotInfo;
                        message = message.replace("$size", size);
                        message = message.replace("$type", type);
                        message = message.replace("$price", String.valueOf(price));
                        message = message.replace("$owned", String.valueOf(owned));

                        sender.sendMessage(PLUGIN_NAME + message);
                    }else{
                        sender.sendMessage(PLUGIN_NAME + this.lp.notExistingPlot);
                    }
                }else {
                    sender.sendMessage(PLUGIN_NAME + this.lp.notValidPlot);
                }
                return true;
            }
        }

        return false;
    }

    private WorldGuardPlugin getWorldGuard(){
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if(plugin == null || !(plugin instanceof  WorldGuardPlugin)){
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }

    private boolean setupEconomy(){
        if(getServer().getPluginManager().getPlugin("Vault") == null){
            return false;
        }
        RegisteredServiceProvider rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null){
            return false;
        }
        econ = (Economy)rsp.getProvider();
        return econ != null;
    }

    private void saveWorld(Player player){
        try{
            getWorldGuard().getRegionManager(player.getWorld()).save();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void saveYamls(){
        try{
            config.save(configFile);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadYamls(){
        try{
            config.load(configFile);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean hasOwner(String plotID, Player player){
        String sqlResult = sql.selectWhere(player.getWorld().getName(), "playername", "plot1", plotID);
        if(sqlResult != null){
            return true;
        }else{
            return false;
        }
    }

    private boolean alreadyHasPlot(Player player){
        try{
            String plot1 = null, plot2 = null;
            ResultSet rs = sql.selectWithAnd(player.getWorld().getName(), new String[]{"plot1", "plot2"}, new String[]{"uuid"}, new String[]{player.getUniqueId().toString()});
            while(rs.next()){
                plot1 = rs.getString("plot1");
                plot2 = rs.getString("plot2");
            }
            int owned = 0;
            if(!plot1.equalsIgnoreCase("null")){
                owned++;
            }
            if(!plot2.equalsIgnoreCase("null")){
                owned++;
            }
            if(owned > 0){
                return true;
            }else{
                return false;
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    //Muss nicht umgestellt werden.
    private ArrayList<ProtectedRegion> getPlots(Player player){
        ArrayList<ProtectedRegion> protectedRegions = new ArrayList<>();
        Map<String, ProtectedRegion> regionStringMap = this.getWorldGuard().getRegionManager(player.getWorld()).getRegions();
        for(Map.Entry<String, ProtectedRegion> entry : regionStringMap.entrySet()){
            protectedRegions.add(entry.getValue());
        }
        return protectedRegions;
    }

    private ProtectedRegion lookForOwnedPlot(Player player){
        String sqlResult = sql.selectWhere(player.getWorld().getName(), "plot1", "uuid", player.getUniqueId().toString());
        System.out.println(sqlResult);
        ProtectedRegion ownedPlot = this.getWorldGuard().getRegionManager(player.getWorld()).getRegion(sqlResult);
        return ownedPlot;
    }

    private ArrayList<ProtectedRegion> getStandingInPlot(CommandSender sender){
        Player player = (Player) sender;
        ArrayList<ProtectedRegion> plots = getPlots(player);
        Location location = player.getLocation();
        ArrayList<ProtectedRegion> standingInPlots = new ArrayList<>();
        for(int i = 0; i < plots.size(); i++){
            if(!plots.get(i).getId().equalsIgnoreCase("stadt")){
                if(plots.get(i).contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())){
                    standingInPlots.add(plots.get(i));
                }
            }
        }
        if(standingInPlots != null){
            return standingInPlots;
        }else{
            return null;
        }
    }

    /*private LWC getLWC(){
        Plugin lwcp = Bukkit.getPluginManager().getPlugin("LWC");
        return ((LWCPlugin)lwcp).getLWC();
    }*/

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        String uuidStr = uuid.toString();
        for(int i = 0; i < Bukkit.getServer().getWorlds().size(); i++){
            World world = (World)Bukkit.getServer().getWorlds().get(i);
            String worldName = world.getName();
            String sqlResult = sql.selectWhere(worldName, "playername", "playername", p.getName());
            String sqlResult2 = sql.selectWhere(worldName, "uuid", "uuid", uuidStr);
            if(sqlResult == null){
                String[] table = new String[this.ownPlots + this.onPlots + 2];
                String[] values = new String[this.ownPlots + this.onPlots + 2];
                table[0] = "playername";
                table[1] = "uuid";
                values[0] = p.getName();
                values[1] = p.getUniqueId().toString();
                int count = 1;
                for(int j = 2; j < this.ownPlots + 2; j++){
                    table[j] = "plot" + count;
                    values[j] = "null";
                    count++;
                }
                count = 1;
                for(int j = (2 + this.ownPlots); j < table.length; j++){
                    table[j] = "plotGuest" + count;
                    values[j] = "null";
                    count++;
                }

                sql.insert(worldName, table, values);
            }
            if(sqlResult != null && sqlResult2 == null){
                sql.update(worldName, new String[]{"uuid", uuidStr}, "playername", p.getName(), true);
            }
        }
    }

    private boolean readLanguagePack(String pack){
        this.currentLanguagePack = pack;
        boolean returnBool = true;
        currentLanguagePackFile = new File(this.getDataFolder() + "/languagepacks/" + currentLanguagePack + ".lp");
        if(!currentLanguagePackFile.exists()){
            this.currentLanguagePack = "en";
            currentLanguagePackFile = new File(this.getDataFolder() + "/languagepacks/en.lp");
            returnBool = false;
        }
        this.languagePackConfig = new YamlConfiguration();
        try{
            languagePackConfig.load(currentLanguagePackFile);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(pack != null && returnBool != false){
            File newConf = new File(this.getDataFolder() + "/config.yml");
            try{
                YamlConfiguration newConfig = new YamlConfiguration();
                newConfig.load(newConf);
                newConfig.set("general.languagePack", pack);
                newConfig.save(newConf);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        this.lp = new LanguagePack();
        lp.tooManyArguments = this.languagePackConfig.getString("tooManyArguments");
        lp.tooFewArguments = this.languagePackConfig.getString("tooFewArguments");
        lp.successBuy = this.languagePackConfig.getString("successBuy");
        lp.failedBuy = this.languagePackConfig.getString("failedBuy");
        lp.notEnoughMoney = this.languagePackConfig.getString("notEnoughMoney");
        lp.successGetPlot = this.languagePackConfig.getString("successGetPlot");
        lp.belongsToSomeone = this.languagePackConfig.getString("belongsToSomeone");
        lp.alreadyOwnOneOrMore = this.languagePackConfig.getString("alreadyOwnOneOrMore");
        lp.plotBuyNotEnoughPermissions = this.languagePackConfig.getString("plotBuyNotEnoughPermissions");
        lp.successSoldPlot = this.languagePackConfig.getString("successSoldPlot");
        lp.successLeftPlot = this.languagePackConfig.getString("successLeftPlot");
        lp.userIsAbleToBuildOnPlot = this.languagePackConfig.getString("userIsAbleToBuildOnPlot");
        lp.userIsAlreadyAbleToBuildOnPlot = this.languagePackConfig.getString("userIsAlreadyAbleToBuildOnPlot");
        lp.noLongerCanBuildOnPlot = this.languagePackConfig.getString("noLongerCanBuildOnPlot");
        lp.playerDoesntExist = this.languagePackConfig.getString("playerDoesntExist");
        lp.allMembersAndOwnersHaveBeenRemoved = this.languagePackConfig.getString("allMembersAndOwnersHaveBeenRemoved");
        lp.ownedByPlayer = this.languagePackConfig.getString("ownedByPlayer");
        lp.ownedByNobody = this.languagePackConfig.getString("ownedByNobody");
        lp.thePlayerOwnsList = this.languagePackConfig.getString("thePlayerOwnsList");
        lp.plotFrom = this.languagePackConfig.getString("plotFrom");
        lp.notStandingOnValidPlot = this.languagePackConfig.getString("notStandingOnValidPlot");
        lp.notStandingOnPlot = this.languagePackConfig.getString("notStandingOnPlot");
        lp.couldntLoadLP = this.languagePackConfig.getString("couldntLoadLP");
        lp.successLoadLP = this.languagePackConfig.getString("successLoadLP");
        lp.myPlotsSuccess = this.languagePackConfig.getString("myPlotsSuccess");
        lp.myPlotsNoPlots = this.languagePackConfig.getString("myPlotsNoPlots");
        lp.errorOccurred = this.languagePackConfig.getString("errorOccurred");
        lp.successDefined = this.languagePackConfig.getString("successDefined");
        lp.plotInfo = this.languagePackConfig.getString("plotInfo");
        lp.notValidPlot = this.languagePackConfig.getString("notValidPlot");
        lp.notExistingPlot = this.languagePackConfig.getString("notExistingPlot");

        return returnBool;
    }
}

package util.lists;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

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

public class PlotListsList {

    public ArrayList<String> mainList = new ArrayList<>();
    public ArrayList<ProtectedRegion> saveList = new ArrayList<>();

    public String plotType = null;
    public String alias = null;


    public PlotListsList(String plotType, String alias){
        this.plotType = plotType;
        this.alias = alias;
    }

    public String getListType(){
        return this.plotType;
    }

    public void resetList(){
        this.mainList = new ArrayList<>();
    }
}
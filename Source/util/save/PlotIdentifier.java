package util.save;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import util.lists.Plot;

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

public class PlotIdentifier {
    private ArrayList<Plot> plots = new ArrayList<>();

    public PlotIdentifier(ArrayList<Plot> plots){
        this.plots = plots;
    }

    public boolean isPlot(String plotID){
        for(int i = 0; i < plots.size(); i++){
            String ID = plots.get(i).getAlias();
            if(!(plotID.length() < ID.length()) && !(ID.length() == plotID.length())){
                int correct = 0;
                for(int j = 0; j < ID.length(); j++){
                    if(plotID.charAt(j) == ID.charAt(j)){
                        correct += 1;
                    }
                }
                if(correct == ID.length() && Character.isDigit(plotID.charAt(correct))){
                    return true;
                }
            }
        }
        return false;
    }

    public ArrayList<ProtectedRegion> getWorldPlotList(ArrayList<ProtectedRegion> protectedRegions){
        ArrayList<ProtectedRegion> worldPlotList = new ArrayList<>();
        for(int i = 0; i < protectedRegions.size(); i++){
            if(isPlot(protectedRegions.get(i).getId())){
                worldPlotList.add(protectedRegions.get(i));
            }
        }
        return worldPlotList;
    }
}

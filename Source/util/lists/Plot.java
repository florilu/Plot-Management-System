package util.lists;

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

public class Plot {
    private String plotType = null;
    private String plotCost = null;
    private String permission = null;
    private String alias = null;
    private int pricePerSquare = 0;

    public Plot(String plotType, String alias, String plotCost, String permission, int pricePerSquare){
        this.plotType = plotType;
        this.plotCost = plotCost;
        this.permission = permission;
        this.alias = alias;
        this.pricePerSquare = pricePerSquare;
    }

    public String getPlotType(){
        return plotType;
    }

    public int getPlotCost(){
        return Integer.valueOf(plotCost);
    }

    public String getPermission(){
        return permission;
    }

    public String getAlias(){
        return this.alias;
    }

    public int getPricePerSquare(){
        return this.pricePerSquare;
    }

    public String toString(){
        return this.plotType + "; " + this.plotCost + "; " + this.permission + "; " + this.alias;
    }
}

package util.save;

import org.bukkit.Material;

import java.io.Serializable;

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

public class SaveBlock implements Serializable{
    private int posX = 0, posY = 0, posZ = 0;
    private String[] signText;
    private Material material;

    public SaveBlock(int posX, int posY, int posZ, String[] signText, Material material){
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.signText = signText;
        this.material = material;
    }

    public int getPosX(){
        return this.posX;
    }

    public int getPosY(){
        return this.posY;
    }

    public int getPosZ(){
        return this.posZ;
    }

    public String[] getSignText(){
        return this.signText;
    }

    public Material getMaterial(){
        return this.material;
    }
}

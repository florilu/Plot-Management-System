package util.online;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

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


public class Updatecheck {

    private URL check;

    public Updatecheck(){
        try{
            this.check = new URL("http://www.pms.freecitycraft.de/version.txt");
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
    }

    public boolean checkForUpdate(int currentRev){
        try{
            int updateVersion;
            try (BufferedReader input = new BufferedReader(new InputStreamReader(check.openStream()))) {
                updateVersion = Integer.valueOf(input.readLine());
            }
            if(updateVersion > currentRev){
                return true;
            }else{
                return false;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }
}

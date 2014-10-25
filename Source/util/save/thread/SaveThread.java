package util.save.thread;

import util.save.SaveBlock;
import util.save.SaveStatus;

import java.io.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

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

public class SaveThread extends Thread{

    private ArrayList<SaveBlock> saveBlocks;
    private String plotID;
    private String path;
    private boolean last;
    private SaveStatus saveStatus;

    public SaveThread(ArrayList<SaveBlock> saveBlocks, String path, String regionID, SaveStatus saveStatus){
        this.saveBlocks = saveBlocks;
        this.plotID = regionID;
        this.path = path;
        this.saveStatus = saveStatus;
    }

    public void run(){
        SaveBlock[] saveBlockArray = new SaveBlock[saveBlocks.size()];
        for(int i = 0; i < saveBlocks.size(); i++){
            saveBlockArray[i] = saveBlocks.get(i);
        }
        try{
            for(int i = 0; i < 3; i++){
                if(new File(path).exists()){
                    new File(path).delete();
                }else{
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
                    oos.writeObject(saveBlockArray);
                    oos.close();
                    saveStatus.addOut();
                    System.out.println("Thread out " + saveStatus.out  + " of " + saveStatus.from + " Done!");
                    if(saveStatus.out == saveStatus.from){
                        saveStatus.out = 0;
                        saveStatus.from = 0;
                        saveStatus = null;
                    }
                    break;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }catch(ConcurrentModificationException e2){
            e2.printStackTrace();
        }finally{
            this.interrupt();
        }
    }
}

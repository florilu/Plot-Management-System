package util.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

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

public class SQLLogger {

    private File sqlLog;
    private BufferedWriter writer;

    public SQLLogger(File dataFolder){
        File logFolder = new File(dataFolder + "/logs");
        if(!logFolder.exists()){
            logFolder.mkdir();
        }
        File[] logs = logFolder.listFiles();
        sqlLog = new File(logFolder + "/log" + logs.length + ".txt");
        try{
            this.writer = new BufferedWriter(new FileWriter(sqlLog));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void writeLine(String line){
        try{
            writer.write(line);
            writer.newLine();
            writer.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void close(){
        if(writer != null){
            try{
                writer.close();
                sqlLog.createNewFile();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}

package sql;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

/*
 * SQLibrary
 * This is a Java library which manages your SQLite database.
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


//TODO SQL Statement "SELECT column_name FROM table_name WHERE column_name='column_datensatz' AND column_name='datensatz'" ermöglichen.
//TODO Obiges SQL Statement mit mehrfachem "AND" sowie mit mehrfachem column_name Einträgen.
//TODO Objekt für Namensreferenzierung über column_name ermöglichen so dass nicht alle dazugehören Datensätze ausgegeben werden.
//TODO Dafür Objekte anfertigen bestehend aus zwei Arrays die die Datensätze abspeichern und via dem gleichen Index abrufen lassen.

/**
 * @author Florian Ludewig
 * @version V1
 *
 * A simple SQLite library which you can use to create and manage your SQLite databases.
 * Simply just create an SQLibrary object and one SQLibrary object manages one database with an unlimited count of tables and columns.
 */

public class SQLibrary {
    private Connection c;
    private Statement stmt;
    private Boolean writeInConsole = true;

    public final String TEXT_TABLE = "TEXT";
    public final String INT_TABLE = "BIGINT";


    public SQLibrary(){

    }

    /**
     * @param path
     * @param dbName
     *
     * Description: This method lets you connect to a database. If it doesn't exist, it will be created immediately.
     *
     * Usage:
     *     The first param called path:
     *         Just type in the path of the database, just without the database name.
     *         Example: "sql/databases/test"
     *     The second param called dbName:
     *         Type in the name of your database.
     *         Example: "test.db"
     *             Hint: It doesn't matter if it's a .db, .sql, .sqlite file. It will be opened.
     */
    public Connection connectToDatabase(String path, String dbName){
        try{
            Class.forName("org.sqlite.JDBC");
            File pathFile = new File(path);
            if(!pathFile.exists()){
                pathFile.mkdirs();
            }
            this.c = DriverManager.getConnection("jdbc:sqlite:" + path + "/" + dbName);
            if(!c.isClosed()){
                if(getWriteInConsole()){
                    System.out.println("Connection Established with database: " + path + "/" + dbName + "!");
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }catch (ClassNotFoundException e2){
            e2.printStackTrace();
        }
        return this.c;
    }

    public void disconnect(){
        try{
            if(!c.isClosed()){
                c.close();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }


    /**
     * @param tableName
     * @param fields
     *
     * Description: This function creates a new Table with some columns.
     *
     * Usage:
     *     The first param tableName:
     *         Description: Type in the name of the table that should be created.
     *         Example: "test"
     *             Hint: Table names aren't allowed to begin with a number!
     *     The second param fields:
     *         Description: This param is there just to clarify which columns you want to have in your table.
     *         Example: new String[]{"firstName", "secondName"}
     *                  This will create the columns firstName and lastName.
     */
    public void createTable(String tableName, String[] fields, String tableType){
        String realFields = "";
        for(int i = 0; i < fields.length; i++){
            if(i != (fields.length - 1)){
                realFields = realFields + fields[i] + " " + tableType + ", ";
            }else{
                realFields = realFields + fields[i] + " " + tableType;
            }
        }
        try{
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " " +
                         "(" + realFields + ");";
            if(getWriteInConsole()){
                System.out.println(sql);
            }
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * @param tableName
     * @param fields
     * @param values
     *
     * Description: Insert some values in some fields.
     * Only use this if you have more than one value which you want to put in some tables!
     *
     * Usage:
     *     The first param tableName:
     *         Description: This param declares in which table you want to insert your values.
     *         Example: "test"
     *     The second param fields:
     *         Description: This declares in which field you want to put a value.
     *         Example: new String[]{"firstName", "lastName"}
     *                  With this you declare that you want to put a value in firstName and in lastName
     *     The third param values:
     *         Description: This declares which values you want to put in.
     *         Example: new String[]{"Bill", "Gates"}
     *                  With this you declare that you want to put in the value Bill and the value Gates.
     *     Full Example: insert("test", new String[]{"firstName", "lastName"}, new String[]{"Bill", "Gates"})
     *         Hint: This says that you want to put values in the table called "test"
     *
     */
    public void insert(String tableName, String[] fields, String[] values){
        String realFields = "";
        String realValues = "";
        for(int i = 0; i < fields.length; i++){
            if(i != (fields.length - 1)){
                realFields = realFields + fields[i] + ", ";
            }else{
                realFields = realFields + fields[i];
            }
        }
        for(int i = 0; i < values.length; i++){
            if(i != (values.length - 1)){
                realValues = realValues + "'" + values[i] + "', ";
            }else{
                realValues = realValues + "'" + values[i] + "'";
            }
        }

        try{
            String sql = "INSERT INTO " + tableName + " " +
                         "(" + realFields + ")" + " " +
                         "VALUES " + "(" + realValues + ");";
            if(getWriteInConsole()){
                System.out.println(sql);
            }
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void insertInt(String tableName, String[] fields, int[] values){
        String realFields = "";
        String realValues = "";
        for(int i = 0; i < fields.length; i++){
            if(i != (fields.length - 1)){
                realFields = realFields + fields[i] + ", ";
            }else{
                realFields = realFields + fields[i];
            }
        }
        for(int i = 0; i < values.length; i++){
            if(i != (values.length - 1)){
                realValues = realValues + "'" + values[i] + "', ";
            }else{
                realValues = realValues + "'" + values[i] + "'";
            }
        }

        try{
            String sql = "INSERT INTO " + tableName + " " +
                    "(" + realFields + ")" + " " +
                    "VALUES " + "(" + realValues + ");";
            if(getWriteInConsole()){
                System.out.println(sql);
            }
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void insert(String tableName, String field, String value){
        try{
            String sql = "INSERT INTO " + tableName + " " +
                         "(" + field + ") VALUES " + "('" + value + "');";
            if(getWriteInConsole()){
                System.out.println(sql);
            }
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public ArrayList<ArrayList<String>> select(String tablename, String[] columns){
        String realColumns = "";
        for(int i = 0; i < columns.length; i++){
            if(i != (columns.length - 1)){
                realColumns = realColumns + columns[i] + ", ";
            }else{
                realColumns = realColumns + columns[i];
            }
        }
        try{
            String sql = "SELECT " + realColumns + " FROM " + tablename + ";";
            if(getWriteInConsole()){
                System.out.println(sql);
            }
            stmt = c.createStatement();
            ResultSet set = stmt.executeQuery(sql);
            ArrayList<ArrayList<String>> list = new ArrayList<>();
            while(set.next()){
                for(int i = 0; i < columns.length; i++){
                    ArrayList<String> current = new ArrayList<>();
                    current.add(set.getString(columns[i]));
                    list.add(current);
                }
            }
            stmt.close();
            return list;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public String[] selectWhereAll(String tableName, String where1, String where2, String where3){
        try{
            String sql = "SELECT * FROM " + tableName + " " +
                         "WHERE " + where1 + "='" + where2 + "';";

            if(getWriteInConsole()){
                System.out.println(sql);
            }
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            ArrayList<String> list = new ArrayList<>();
            while(rs.next()){
                list.add(rs.getString(where3));
            }
            String[] finalArray = new String[list.size()];
            for(int i = 0; i < list.size(); i++){
                finalArray[i] = list.get(i);
            }
            return finalArray;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public String selectWhere(String tableName, String fromColumnName, String whereColumnName, String whereColumnValue){
        try{
            String sql = "SELECT " + fromColumnName + " " +
                         "FROM " + tableName + " "  +
                         "WHERE  " + whereColumnName + "='" + whereColumnValue + "';";
            if(getWriteInConsole()){
                System.out.println(sql);
            }
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            String result = null;
            while(rs.next()){
                result = rs.getString(fromColumnName);
            }
            return result;
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public void update(String tableName, String[] columnValues, String where1, String where2, boolean withWhere){
        String realColumnValues = "";
        if(!(columnValues.length % 2 != 0)){
            for(int i = 0; i < columnValues.length; i++){
                realColumnValues = realColumnValues + columnValues[i];
                if(i+1 != (columnValues.length - 1)){
                    realColumnValues = realColumnValues + "='" + columnValues[i+1] + "' ,";
                    i++;
                }else{
                    realColumnValues = realColumnValues + "='" + columnValues[i+1] + "'";
                    i++;
                    break;
                }
            }
        }
        try{
            String sql = null;
            if(withWhere){
                sql = "UPDATE " + tableName + " " +
                        "SET " + realColumnValues + " " +
                        "WHERE " + where1 + "='" + where2 + "';";
            }else{
                sql = "UPDATE " + tableName + " " +
                        "SET " + realColumnValues + ";";
            }
            if(getWriteInConsole()){
                System.out.println(sql);
            }
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void update(String tableName, String[] columns, String[] values, String whereColumn, String whereValue){
        String realColumnValues = "";

        if(columns.length == values.length){

            for(int i = 0; i < columns.length; i++){
                if(i != columns.length - 1){
                    realColumnValues = realColumnValues + columns[i] + "='" + values[i] + "' ,";
                }else{
                    realColumnValues = realColumnValues + columns[i] + "='" + values[i] + "'";
                }
            }

            try{
                String sql = null;

                sql = "UPDATE " + tableName + " " +
                        "SET " + realColumnValues + " " +
                        "WHERE " + whereColumn + "='" + whereValue + "';";

                if(getWriteInConsole()){
                    System.out.println(sql);
                }
                stmt = c.createStatement();
                stmt.executeUpdate(sql);
                stmt.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void portTable(String tableNameFrom, String tableNameTo, String[] fields, boolean deleteOldTable){

    }

    public ResultSet selectWithAnd(String tableName, String[] fromColumnNames, String[] andColumnNames, String[] andColumnValues){
        String realColumnNames = "";
        for(int i = 0; i < fromColumnNames.length; i++){
            if(i != fromColumnNames.length - 1){
                realColumnNames = realColumnNames + fromColumnNames[i] + ", ";
            }else{
                realColumnNames = realColumnNames + fromColumnNames[i];
            }
        }

        String realAndValues = "";
        if(andColumnNames.length == andColumnValues.length){
            for(int i = 0; i < andColumnValues.length; i++){
            if(i != andColumnNames.length - 1){
                realAndValues = realAndValues + andColumnNames[i] + "='" + andColumnValues[i] + "' AND ";
            }else{
                realAndValues = realAndValues + andColumnNames[i] + "='" + andColumnValues[i] + "'; ";
            }
        }

        String sql = "SELECT " + realColumnNames + " FROM " + tableName + " WHERE " + realAndValues;
        if(getWriteInConsole()){
            System.out.println(sql);
        }
        try{
            stmt = c.createStatement();
            ResultSet resultSet = stmt.executeQuery(sql);
            return resultSet;
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    return null;
    }

    public void setWriteInConsole(boolean writeInConsole){
        this.writeInConsole = writeInConsole;
    }

    public boolean getWriteInConsole(){
        return this.writeInConsole;
    }

    public Connection getConnection(){
        return this.c;
    }
}

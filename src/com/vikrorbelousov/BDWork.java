package com.vikrorbelousov;
import java.lang.reflect.Executable;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;

/**
 * Created by Belousov on 26.09.2016.
 */

public class BDWork {
    private final String URL = "jdbc:mysql://127.0.0.1:3306/mobiledata?autoReconnect=true&useSSL=false";
    private final String user = "root";
    private final String pass = "root";

    private final String userTable = "users";
    private final String servicesTable = "services";

    private Connection con = null;
    private Statement stmt = null;
    private ResultSet rs = null;

    private long userCnt = 0;
    private HashMap<Integer,String> idServisesList;

    private boolean isAutoUpdStatusUsers = true;

    public void setAutoUpdStatusUsers(boolean autoUpdStatusUsers) {
        isAutoUpdStatusUsers = autoUpdStatusUsers;
    }

    public BDWork()
    {
        try {
            con = DriverManager.getConnection(URL, user, pass);
            stmt = con.createStatement();
            rs = stmt.executeQuery(SQLQuery.getQuery_selectAllRowsInTable(userTable));
            rs.last();
            userCnt = rs.getInt(1);

            rs = stmt.executeQuery(SQLQuery.getQuery_selectAllRowsInTable(servicesTable));
            rs.last();
            int row = rs.getRow();
            rs.first();

            idServisesList = new HashMap<>(row);

            for (int i = 0; i < row; i++) {
                idServisesList.put(rs.getInt(1), rs.getString(2));
                rs.next();
            }

        }
        catch (SQLException E){
            System.out.println(E.getMessage());
            try { con.close(); } catch(SQLException se) { /*can't do anything */ }
            try { stmt.close(); } catch(SQLException se) { /*can't do anything */ }
            try { rs.close(); } catch(SQLException se) { /*can't do anything */ }
        }
    }



    private void addrRow(String table, String row)
    {
        try {
           stmt.execute(SQLQuery.getQuery_InsertInTable(table, row));
        }
        catch (SQLException E){
            System.out.println(E.getMessage());
        }
    }

    private void deleteRow(String table, String where )
    {
        try {
            stmt.execute(SQLQuery.getQuery_DeleteRowInTable(table, where));
        }
        catch (SQLException E){
            System.out.println(E.getMessage());
        }
    }

    private void updateValue(String table, String where, String set)
    {
        try {
            stmt.execute(SQLQuery.getQuery_UpadateRowInTable(table, where, set));
        }
        catch (SQLException E){
            System.out.println(E.getMessage());
        }
    }

    public void outALL(String table)
    {
        try {
            rs = stmt.executeQuery(SQLQuery.getQuery_selectAllRowsInTable(table));
            ResultSetMetaData rsMd  = rs.getMetaData();

            for(int i=0; i<rsMd.getColumnCount(); i++)
                System.out.print(rsMd.getColumnName(i+1) + "\t");
            System.out.println();

            while (rs.next()){
                for(int i=0; i<rsMd.getColumnCount(); i++)
                     System.out.print(rs.getString(i+1) + "\t");
                System.out.println();
            }
        }
        catch (SQLException E){
            System.out.println(E.getMessage());
            try { con.close(); } catch(SQLException se) { /*can't do anything */ }
            try { stmt.close(); } catch(SQLException se) { /*can't do anything */ }
            try { rs.close(); } catch(SQLException se) { /*can't do anything */ }
        }
    }

    public User getUser(int id){
        return new User(id);
    }

    public Admin getAdmin(){
        return new Admin();
    }

    class User
    {
        private long id = 0;
        private int balance = 0;
        private int idService = 0;
        private boolean isActive = true;

        private ResultSet rsUser;

        private User(int id){
            this.id = id;
            try {
                String st = SQLQuery.qetQuery_selectRowInTable(userTable, "idUsers=" + id);
                rsUser = stmt.executeQuery(SQLQuery.qetQuery_selectRowInTable(userTable, "idUsers=" + id));
                rsUser.next();
                idService = rsUser.getInt(2);
                balance = rsUser.getInt(3);
                isActive = rsUser.getInt(4) == 1;
            }
            catch (SQLException E) {
                System.out.println("User #"+id + " " + E.getMessage());
            }
        }

        public boolean changeService(int index)
        {
            if(idServisesList.containsKey((Integer)index)) {
                updateValue(userTable, "idUsers=" + id, "Service=" + index);
                return true;
            }
            return false;
        }

        public void addMoney(int money){
            balance += money;
            updateValue(userTable, "idUsers=" + id, "Balance=" + balance);

            if( isAutoUpdStatusUsers) {
                if (balance >= 0 && !isActive)
                    updateValue(userTable, "idUsers=" + id, "Status=1");
                else if (balance < 0 && isActive)
                    updateValue(userTable, "idUsers=" + id, "Status=0");
            }

        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", idService=" + idService +
                    ", balance=" + balance +
                    ", state=" + isActive +
                    '}';
        }
    }

    class Admin
    {
        private Admin(){}

        public void addUser(int serviceIndex, int balance, boolean status)
        {
            userCnt ++;
            int statusInt =0;
            if(status)
                statusInt = 1;

            String newRow = Long.toString(userCnt) + ", " + serviceIndex + ", " +  balance + ", " + statusInt;
            addrRow(userTable, newRow);
        }

        public void addUser(){
            addUser(1,0,false);
        }

        public void deleteUser(int idUser){
            deleteRow(userTable, "idUsers=" + idUser);
        }

        public void setStatusUser(int idUser, boolean status)
        {
            String statusStr = "0";
            if (status)
                statusStr = "1";
            try {
                stmt.execute(SQLQuery.getQuery_UpadateRowInTable(userTable, "idUsers=" + idUser, "Status=" +statusStr));
            }catch (SQLException E) {
                System.out.println(E.getMessage());
            }
        }

        public UserParam getUserParam(int id)
        {
            UserParam param = null;
            try{
                ResultSet rs = stmt.executeQuery(SQLQuery.qetQuery_selectRowInTable(userTable, "idUsers=" +id));
                rs.next();
                param = new UserParam(
                        rs.getInt(1),
                        rs.getInt(2),
                        rs.getInt(3),
                        rs.getInt(4) == 1
                );
            }
            catch (SQLException E){
                System.out.println(E.getMessage());
            }

            return param;
        }

        public String[] getArrayStringNotActiveUsers()
        {
            ArrayList<String> out = new ArrayList<>();

            try{
                rs = stmt.executeQuery(SQLQuery.qetQuery_selectRowInTable(userTable,"Status=0"));

                while (rs.next())
                    ///TODO  сделать вывод
                   // out.add(getUserParam(rs.getInt(1)).toString());
                    System.out.println(getUserParam(rs.getInt(1)).toString());

            }catch (SQLException E){
                System.out.println(E.getMessage());
            }
            catch (Exception E) {
                System.out.println(E.getMessage());
            }


            String[] s = new String[out.size()];
            for (int i = 0; i <s.length; i++) {
                s[i] = out.get(i);
            }
            return s;
        }

        public void outListNotActiveUsers()
        {
            for(String S : getArrayStringNotActiveUsers())
                System.out.println(S);
        }
        @Override
        public String toString() {
            return "Admin";
        }
    }

    class UserParam{
        private long id = 0;
        private int idService = 0;
        private int balance = 0;
        private boolean state = false;

        public UserParam(long id, int idService, int balance, boolean state) {
            this.id = id;
            this.idService = idService;
            this.balance = balance;
            this.state = state;
        }

        public long getId() {
            return id;
        }

        public int getIdService() {
            return idService;
        }

        public int getBalance() {
            return balance;
        }

        public boolean getStatus() {
            return state;
        }

        @Override
        public String toString() {
            return "UserParam{" +
                    "id=" + id +
                    ", idService=" + idService +
                    ", balance=" + balance +
                    ", state=" + state +
                    '}';
        }
    }

    private static class SQLQuery
    {
        public static final String SELECT_ALL_TABLES = "SHOW TABLES";

        public static String getQuery_InsertInTable(String table, String insertData){
            return "INSERT INTO " + table + " VALUES (" + insertData + ")";
        }

        public static String getQuery_selectAllRowsInTable(String table){
            return "SELECT * FROM " + table;
        }
        public static String getQuery_UpadateRowInTable(String table, String where, String set ){
            return "UPDATE " +table+  " SET "+ set +  " WHERE " + where;
        }

        public static String getQuery_DeleteRowInTable(String from, String where){
            return "DELETE FROM " + from + " WHERE " + where;
        }
        public static String qetQuery_selectRowInTable(String table, String condition){
            return "SELECT * FROM " + table + " WHERE " + condition;
        }


    }

}

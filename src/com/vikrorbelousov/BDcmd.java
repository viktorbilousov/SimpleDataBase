package com.vikrorbelousov;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Created by Belousov on 28.09.2016.
 */
public class BDcmd {
    private BDWork _bd;
    private BDWork.Admin _admin = null;
    private BDWork.User _user = null;

    public BDcmd(BDWork _bd) {
        this._bd = _bd;
    }

    private void initUser(int id){
        _user = _bd.getUser(id);
    }

    private void initAdmin(){
        _admin = _bd.getAdmin();
    }

    public void Start()
    {
        Scanner sc = new Scanner(System.in);
        LinkedList<String> listOper =  new LinkedList<>();
        while (true)
        {
            String input =  sc.nextLine();
            input = input.toLowerCase();
            listOper.clear();

            for (String s : input.split(" ")) {
                if(s.equals(""))
                    continue;
                listOper.add(s);
            }

            String first = listOper.getFirst();

            if(first.equals(Commands.ENTER))
                singInMenu(listOper);

            else if (first.equals(Commands.HELP))
                System.out.println(getHelp());

            else if(first.equals(Commands.SHOW))
                showMenu(listOper);

            else if(first.equals(Commands.ADD))
                addMenu(listOper);

            else if(first.equals(Commands.DELETE))
                deleteMenu(listOper);

            else if(first.equals(Commands.UPDATE))
                updateMenu(listOper);

            else if(first.equals(Commands.EXIT))
                return;

            else
                System.out.println("Error command");
        }
    }

    private void singInMenu(LinkedList<String> listOper)
    {
        if(listOper.size() <= 1 ) {
            System.out.println("Error arg");
            return;
        }
        if(listOper.get(1).equals(Commands.ADMIN)) {
            if(_user != null)
                System.out.println("User log out");
            _user = null;

            initAdmin();
            System.out.println("Admin sing in");
        }
        if(listOper.get(1).equals(Commands.USER)) {
            try {
                int id = Integer.parseInt(listOper.get(2));
                if(_admin != null)
                    System.out.println("Admin log out");
                _admin = null;

                initUser(id);
                System.out.println("User "+ id +" sing in");

            } catch (Exception E) {
                System.out.println("Error user id par");
            }
        }
    }

    private void showMenu(LinkedList<String> listOper)
    {
        if(listOper.size()<=1) {
            System.out.println("Error Command SHOW par");
            return;
        }

        try {
            if(listOper.get(1).equals(Commands.SERVICES)) {
                _admin.outServicesTable();
                return;
            }
            if(listOper.get(1).equals(Commands.USERS))
            {
                if(listOper.size() == 2){
                    _admin.outUserTable();
                    return;
                }
                if(listOper.get(2).equals(Commands.BLOCKED)){
                    _admin.outListUsers(false);
                    return;
                }
                if(listOper.get(2).equals(Commands.UNBLOKED)){
                    _admin.outListUsers(true);
                    return;
                }
            }
        }catch (ArrayIndexOutOfBoundsException E){
            System.out.println("Error args");
        }
        catch (NullPointerException E)
        {System.out.println("Admin dont sing in!");}


        System.out.println("Error Command SHOW par");
    }

    private void addMenu(LinkedList<String> listOper)
    {
        if(listOper.size()<=1) {
            System.out.println("Error Command Add par");
            return;
        }
        // ADD USER (int int int)
        if(listOper.get(1).equals(Commands.USER))
        {

            if(_admin == null) {
                System.out.println("Admin dont sing in!");
                return;
            }

            if(listOper.size() == 2) {
               long id = _admin.addUser();
                System.out.println("Add new user! id=" + id +" ServiseId=1 Balance=0 Status=false");
                return;
            }
            else {
                LinkedList<Integer> param = new LinkedList<>();
                if (isCorrectParam_AddAdmin( new LinkedList<>(listOper.subList(2, listOper.size())), param)) {

                    boolean isActive = true;

                    if (param.get(2) == 0)
                        isActive = false;
                    else if(param.get(2) != 1) {
                        System.out.println("Error rec Add User param");
                        return;
                    }
                    long id = _admin.addUser(param.get(0), param.get(1), isActive);
                     if( id == _admin.ERROR_ADD_USER) {
                         System.out.println("Error add user");
                         return;
                     }

                    System.out.println(
                    "Add new user! id=" + id +
                    " ServiseId=" + param.get(0) +
                    " Balance=" + param.get(1) +
                    " Status=" + isActive
                    );
                    return;
                }
                else{
                    System.out.println("Error rec Add User param");
                    return;
                }
            }
        }

        System.out.println("Error Command Add par");

    }

    private void deleteMenu(LinkedList<String> listOper)
    {
        if(listOper.size()<=1) {
            System.out.println("Error command Delete par");
            return;
        }

        if(listOper.get(1).equals(Commands.USER))
        {
            if(_admin == null) {
                System.out.println("Admin dont sing in!");
                return;
            }
            try{
                int id  = Integer.parseInt(listOper.get(2));
                if(_admin.deleteUser(id)) {
                    System.out.println("User id=" + id + " deleted!");
                    return;
                }
                else
                    System.out.println("Error delete User");
            }catch (Exception E){
                System.out.println("Error args");
            }
        }

        System.out.println("Error command delete args");
    }

    private void updateMenu(LinkedList<String> listOper)
    {
        if(listOper.size()<=1) {
            System.out.println("Error command Update par");
            return;
        }
        try{
            if(listOper.get(1).equals(Commands.STATUS))
            {
                int id = Integer.parseInt(listOper.get(2));
                int status = Integer.parseInt(listOper.get(3));
                boolean isActive = false;

                if(status == 1)
                    isActive = true;
                else if (status != 0)
                    throw new Exception();

                if( !_admin.setStatusUser(id, isActive))
                    throw new Exception();

                System.out.println("Status updated!");
                return;

            }
        }catch (NullPointerException E){
            System.out.println("Admin log out!");
        }
        catch (Exception E){
            System.out.println("Error args");
        }
    }

    private boolean isCorrectParam_AddAdmin(LinkedList<String> input, LinkedList<Integer> output)
    {
        //( int , int , int ) - input String line
        if(input.size() != 5) {
            System.out.println("error size input");
            return false;
        }

        if(input.getFirst().equals("(") && input.getLast().equals(")"))
        {
            Integer first = null;
            Integer second = null;
            Integer thirt = null;
            try{
                first = Integer.parseInt(input.get(1));
                second = Integer.parseInt(input.get(2));
                thirt = Integer.parseInt(input.get(3));
                output.addAll(Arrays.asList(first,second,thirt));
            }catch (NumberFormatException E){
                System.out.println("error parse input");
            }
            return true;
        }

        return false;
    }

     private static class Commands
     {
         public static final String EXIT = "exit";
         public static final String ADMIN = "admin";
         public static final String USER = "user";
         public static final String ENTER = "log";
         public static final String SHOW = "show";
         public static final String TABLE = "table";
         public static final String USERS = "users";
         public static final String SERVICES = "services";
         public static final String BLOCKED = "-b";
         public static final String UNBLOKED = "-ub";
         public static final String ADD = "add";
         public static final String HELP = "help";
         public static final String DELETE = "delete";
         public static final String UPDATE = "update";
         public static final String STATUS = "status";


     }

     private static String getHelp()
     {
         return  "HELP - display this help\n" +
                 "ADMIN - log in as Admin\n" +
                 "USER [id] - log in as User\n" +
                 "\n\t\tAdmin:\n"+
                 "SHOW USERS [non, -b, -ub] - display list of users (all, block, unblock)\n" +
                 "SHOW SERVICES - display list of services\n" +
                 "ADD USER (service, balance, status)- add new user with parameters\n" +
                 "ADD USER - add new default user\n" +
                 "DELETE USER [id] - delete user num 'id'\n"+
                 "UPDATE STATUS [id user] [state] - change state (block/unblock) for user num 'id'";
     }

}

package com.vikrorbelousov;

public class Main {

    public static void main(String[] args) {
	// write your code here
        BDWork bd = new BDWork();
       // bd.addUser();
        //bd.addrRowToTable("services" , " '7', 'test', '88', '1'");
       // bd.deleteRowFromTable("services" , "idServises=7");
      //  bd.updateValue("services", "idServises=1", "Price=34");


        BDWork.User user = bd.getUser(1);
        //user.addMoney(-1000);

        BDWork.Admin admin = bd.getAdmin();
       // admin.addUser();
        BDWork.UserParam param = admin.getUserParam(1);
       admin.outListNotActiveUsers();
        bd.outALL("users");




    }
}

package com.vikrorbelousov;

import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {
	// write your code here
        BDWork bd = new BDWork();
        BDcmd cmd = new BDcmd(bd);
        cmd.Start();

        BDWork.User user = bd.getUser(1);
        //user.addMoney(-1000);

        BDWork.Admin admin = bd.getAdmin();
       // admin.addUser();
      //  bd.outALL("users");

        LinkedList<Integer> list = null;



    }


}

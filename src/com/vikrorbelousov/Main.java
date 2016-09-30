package com.vikrorbelousov;

import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {
	// write your code here
        BDWork bd = new BDWork();
        BDcmd cmd = new BDcmd(bd);
        cmd.Start();

    }
}

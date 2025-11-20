package iuh.fit.se;

import iuh.fit.se.ui.LoginUI;
import service.UserService;

import java.rmi.Naming;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() throws Exception{
        UserService userService = (UserService) Naming.lookup("rmi://localhost:1262/userService");

        System.out.println("connetecd");
        System.out.println(userService.findById(1L));
         new LoginUI().show();
    }
}

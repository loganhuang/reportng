package org.uncommons.reportng.sender;


import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * Created by huangzhw on 2017/1/23.
 */
public class Auth extends Authenticator {

    String userName = "";
    String password = "";


    public Auth(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(userName, password);
    }
}

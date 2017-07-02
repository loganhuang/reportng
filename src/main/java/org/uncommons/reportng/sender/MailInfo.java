package org.uncommons.reportng.sender;

import java.util.Properties;

/**
 * Created by huangzhw on 2017/1/23.
 */
public class MailInfo {
    private String mailServerHost;
    private String mailServerPort;
    // 邮件发送者的地址
    private String fromAddress;
    // 登陆邮件发送服务器的用户名和密码
    private String userName;
    private String password;
    // 是否需要身份验证
    private boolean validate = true;

    // 邮件的文本路径
    private String htmlpath;


    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", this.mailServerHost);
        //properties.put("mail.smtp.port", this.mailServerPort);
        //or us starttls
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", validate ? "true" : "false");
        return properties;
    }


    public String getMailServerHost() {
        return mailServerHost;
    }

    public void setMailServerHost(String mailServerHost) {
        this.mailServerHost = mailServerHost;
    }

    public String getMailServerPort() {
        return mailServerPort;
    }

    public void setMailServerPort(String mailServerPort) {
        this.mailServerPort = mailServerPort;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }


    public String getHtmlpath() {
        return htmlpath;
    }

    public void setHtmlpath(String htmlpath) {
        this.htmlpath = htmlpath;
    }


}

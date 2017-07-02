package org.uncommons.reportng.sender;


import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * Created by logan on 2017/1/23.
 */
public class MailSender {

    private MailInfo mailInfo = new MailInfo();
    private int count = 0;
    private boolean existPropertiesFile = true;

    public MailSender() {

        Properties properties = new Properties();
        try {
            InputStream inputStream = new FileInputStream(new File("email.properties"));
            properties.load(inputStream);
            mailInfo.setMailServerHost(properties.getProperty("smtp"));
            mailInfo.setMailServerPort(properties.getProperty("port"));
            mailInfo.setUserName(properties.getProperty("username"));
            mailInfo.setPassword(properties.getProperty("password"));
            mailInfo.setFromAddress(properties.getProperty("from"));
            mailInfo.setHtmlpath(properties.getProperty("htmlpath"));
        } catch (Exception e) {
            this.existPropertiesFile = false;
            System.out.println("can not find properties file: email.properties");
        }


    }

    /**
     * 以HTML格式发送邮件
     */
    public boolean sendHtmlMail() {

        if (!existPropertiesFile) {
            return false;
        }
        // 判断是否需要身份认证
        Auth authenticator = null;
        Properties pro = mailInfo.getProperties();
        //如果需要身份认证，则创建一个密码验证器
        if (mailInfo.isValidate()) {
            authenticator = new Auth(mailInfo.getUserName(), mailInfo.getPassword());
        }
        // 根据邮件会话属性和密码验证器构造一个发送邮件的session
        Session sendMailSession = Session.getDefaultInstance(pro, authenticator);
        try {
            Message message = new MimeMessage(sendMailSession);
            message.setFrom(new InternetAddress(mailInfo.getFromAddress()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(System.getProperty("mail.tolist").trim()));
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(System.getProperty("mail.cclist").trim()));
            String env = System.getProperty("mail.test.env");
            String text = "[集成测试-" + System.getProperty("mail.subject") + "] " + env + "环境测试" + System.getProperty("mail.result");
            byte[] byteText = text.getBytes(Charset.forName("UTF-8"));
            message.setSubject(new String(byteText, "UTF-8"));
            Multipart mainPart = new MimeMultipart();
            // 创建一个包含HTML内容的MimeBodyPart
            BodyPart html = new MimeBodyPart();
            // 如果不存在，直接返回false
            File file = new File(mailInfo.getHtmlpath());
            if (!file.exists()) {
                System.out.println("html file is not existed: " + mailInfo.getHtmlpath());
                return false;
            }
            // 设置HTML内容
            String messageString = ReadFromFile.readFileByLines(mailInfo.getHtmlpath());
            html.setContent(messageString, "text/html; charset=utf-8");
            mainPart.addBodyPart(html);
            // 将MiniMultipart对象设置为邮件内容
            message.setContent(mainPart);
            // 发送邮件
            Transport.send(message);
            return true;
        } catch (MessagingException ex) {
            ex.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }
}

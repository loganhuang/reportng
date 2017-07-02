package org.uncommons.reportng.sender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by huangzhw on 2017/1/23.
 */
public class ReadFromFile {

    public static String readFileByLines(String fileName) {

        File file = new File(fileName);
        BufferedReader reader = null;
        StringBuilder msg = new StringBuilder("");
        try {
            //以行为单位读取文件内容，一次读一整行
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                msg.append(tempString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return msg.toString();
    }
}

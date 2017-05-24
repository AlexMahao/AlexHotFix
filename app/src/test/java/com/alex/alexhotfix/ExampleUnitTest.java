package com.alex.alexhotfix;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void testCommand(){
        String command = "/home/mahao/android-sdk-linux/build-tools/23.0.3/dx --dex --output  /home/mahao/project_my/AlexHotFix/hot/patch_dex.jar /home/mahao/project_my/AlexHotFix/hot\n";

        InputStream in = null;
        try {
            Process pro = Runtime.getRuntime().exec(command);
            pro.waitFor();
            in = pro.getInputStream();
            BufferedReader read = new BufferedReader(new InputStreamReader(in));
            String result = read.readLine();
            System.out.println("INFO:"+result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runCmd(String cmd) {
        Runtime rt = Runtime.getRuntime();
        BufferedReader br = null;
        InputStreamReader isr = null;
        try {
            // 执行
            Process p = rt.exec(cmd);
            // 获取对应流，一遍打印控制台输出的信息
            isr = new InputStreamReader(p.getInputStream());
            br = new BufferedReader(isr);
            String msg = null;
            while ((msg = br.readLine()) != null) {
                System.out.println(msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (isr != null) {
                    isr.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
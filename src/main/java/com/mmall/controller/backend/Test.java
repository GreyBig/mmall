package com.mmall.controller.backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) throws IOException {
        String pathName = "E:\\text.txt";
        File filename = new File(pathName);

        Path path = Paths.get(String.valueOf(filename));
        Scanner scanner = new Scanner(path);
        System.out.println("Read text file using Scanner");
        //逐行读取
        while(scanner.hasNextLine()){
            //逐行处理
            String line = scanner.nextLine();
            System.out.println(line);
        }
        scanner.close();//原文出自【易百教程】，商业转载请联系作者获得授权，非商业请保留原文链接：https://www.yiibai.com/java/java-read-text-file.html


    }
}

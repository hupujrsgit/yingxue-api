package com.exeplm.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.Random;

public class ImageUtils {

    static ArrayList<String> list;


    static  {
        list = new ArrayList<>();
        list.add("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.jj20.com%2Fup%2Fallimg%2F1113%2F092919113248%2F1Z929113248-8-1200.jpg&refer=http%3A%2F%2Fimg.jj20.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1659950169&t=9d5bca0ca9481f77bce8c535987ebfa5");
        list.add("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.jj20.com%2Fup%2Fallimg%2F4k%2Fs%2F02%2F210924233115O14-0-lp.jpg&refer=http%3A%2F%2Fimg.jj20.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1659950170&t=2125575c47a1a57f658f21cd96829bc5");
        list.add("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fpic.jj20.com%2Fup%2Fallimg%2F1114%2F0I120152936%2F200I1152936-5-1200.jpg&refer=http%3A%2F%2Fpic.jj20.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1659950170&t=5df3549036a15cb80bd002ef5e03b796");
    }

    public static String getPhoto(){
        Random random = new Random();
        int nextInt = random.nextInt(list.size());
        return list.get(nextInt);
    }
}

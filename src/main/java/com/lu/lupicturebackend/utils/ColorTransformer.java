package com.lu.lupicturebackend.utils;

public class ColorTransformer {


    public static String colorTranspose(String hexColor){
        // 如果不是6位的十六价值，要在第三位后面加一个0
        if(hexColor.length() == 7){
            hexColor = hexColor.substring(0, 4) + "0" + hexColor.substring(4,7);
        }
        return hexColor;
    }
}

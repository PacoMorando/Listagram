package com.sas.conectaractivitys;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public  class ListFormats {

    //AGREGAR EL FORMATO DE PESOS CON CENTAVOS

    public static String basicDate(String dateToFormat){

        String inFormat = "yyyy-MM-dd HH:mm:ss";
        String outFormat = "dd/MM/yy";

        SimpleDateFormat sdf1 = new SimpleDateFormat(inFormat);
        SimpleDateFormat sdf2 = new SimpleDateFormat(outFormat);

        try {
            return sdf2.format(sdf1.parse(dateToFormat));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}

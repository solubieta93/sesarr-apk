package com.example.myapplication2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;

public  class BD {
    String CREATE_TABLE= "CREATE TABLE IF NOT EXISTS Electros("
    		               + "id INTEGER PRIMARY KEY  AUTOINCREMENT,"
    		               + "Horas String not null,"
    		               + "Fichero String not null," 
    		               + "FC String not null,"
    		               + "FCMax String not null,"
    		               + "FCMin String not null,"
                           + "LatPre String not null"+")";	

    static SQLiteDatabase db;
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //abre o crea sino existe la de base de datos 
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public BD (Context ctx) {
      try{

          //db= ctx.openOrCreateDatabase("/mnt/sdcard/Android/data/BDRegistros.db", SQLiteDatabase.OPEN_READWRITE, null);
         // db= ctx.openOrCreateDatabase("/sdcard/BDRegistros.db", SQLiteDatabase.OPEN_READWRITE,null);
          db = SQLiteDatabase.openDatabase("/sdcard/BDRegistros.db", null, SQLiteDatabase.OPEN_READWRITE);
    	db.execSQL(CREATE_TABLE);
      }catch (Exception e)
       {
        Log.d("Basededatos","Exception initDB: "+e.toString());	
       }
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //adiciona registro en la base de datos
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public static long addDato(String dato,String dato1,String dato2,
		                    String dato3,String dato4,String dato5){
    //public static long addDato(String dato,String dato1,String dato2){
      ContentValues NuevoReg= new ContentValues();
      NuevoReg.put("Horas",dato);
      NuevoReg.put("Fichero",dato1);
      NuevoReg.put("FC",dato2);
      NuevoReg.put("FCMax",dato3);
      NuevoReg.put("FCMin",dato4);
      NuevoReg.put("LatPre",dato5);
      return db.insert("Electros",null,NuevoReg); 
     }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public static ArrayList<String> getDatos() {
      ArrayList<String> Reg =new ArrayList<String>(); 
	  String[] columns = {"Horas"}; 
      //String[] columns = {"Horas","Fichero"}; 
  	  Cursor c = db.query("Electros", null, null, null, null, null, null, null);
	  c.moveToFirst();
	  while (c.isAfterLast() == false) { 
		 //adiciono nada mas las que quiero mostrar por pantalla 
		 Reg.add(c.getString(1)+"|"+c.getString(2)+"|FC "+c.getString(3));
         /*se muestran todos los campos de la base de datos
	     Reg.add(c.getString(1)+c.getString(2)+c.getString(3)
				 +c.getString(4)+c.getString(5));*/
         c.moveToNext();
	  }
	  c.close();
	  return Reg;
	 }
    //////////////////////////////////////////////////////////////////////////////////////
    //borra base de datos
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public static void delete() {
	  db.delete("Electros", null,null);
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //busca un registro por Fichero
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public static String buscar() {
      //String sel=(String) busc.getText();
	  Cursor c = db.query("Electros", null, null, null, null, null, null, null);
      String NumReg=c.getString(1);
      return NumReg;
     }


}

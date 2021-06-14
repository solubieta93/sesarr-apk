package com.example.myapplication2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public  class BD extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION=5;
        private static final String DATABASE_NOMBRE="BDRegistros.db";
        private static final String TABLE_NOMBRE="Electros";

        public BD(@Nullable Context context ) {
            super(context, DATABASE_NOMBRE, null, DATABASE_VERSION);
        }
        //////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE "+TABLE_NOMBRE+"("
                    + "id INTEGER PRIMARY KEY  AUTOINCREMENT,"
                    + "Horas String not null,"
                    + "Fichero String not null,"
                    + "FC String not null,"
                    + "FCMax String not null,"
                    + "FCMin String not null,"
                    + "LatPre String not null"+")");
        }
        //////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //se ejecuta cuando se cambia l aversion de la base de datos
            // entonces borra l atabla y crea otra nueva
            db.execSQL("DROP TABLE "+TABLE_NOMBRE);
            onCreate(db);
        }
        ///////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////
        public static void delete(SQLiteDatabase db) {
            db.execSQL("DROP TABLE "+TABLE_NOMBRE);
        }
        ///////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////
        //adiciona registro en la base de datos
        //////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////
        public static long addDato(String dato, String dato1, String dato2,
                                   String dato3, String dato4, String dato5,SQLiteDatabase db){
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
        //adiciona registro en la base de datos
        ///////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////
        public static ArrayList<String> getDatos(SQLiteDatabase db) {
            ArrayList<String> Reg =new ArrayList<String>();
            String[] columns = {"Horas"};
            //String[] columns = {"Horas","Fichero"};
            Cursor c = db.query(TABLE_NOMBRE,null,null,null,null,null,null);
            c.moveToFirst();
            while (c.isAfterLast() == false) {
                //adiciono nada mas las que quiero mostrar por pantalla
                Reg.add(c.getString(1)+"  ||  "+c.getString(2)+"  ||  FC "+c.getString(3));
          /*se muestran todos los campos de la base de datos
	     Reg.add(c.getString(1)+c.getString(2)+c.getString(3)
				 +c.getString(4)+c.getString(5));*/
                c.moveToNext();
            }
            c.close();
            return Reg;
        }
        ///////////////////////////////////////////////////////////////////////////////////////
        public static String buscar(SQLiteDatabase db) {
            Cursor c = db.query("Electros", null, null, null, null, null, null, null);
            String NumReg=c.getString(1);
            return NumReg;
        }

    }

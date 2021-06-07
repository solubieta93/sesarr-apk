/*

 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.myapplication2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Date;
//libreria para subir files con SFTP
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;
///////////////////////////////
public class BluetoothChat extends Activity {
    Chronometer cronometro;
    CountDownTimer timer;
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;
    private static final int REQUEST_CODE = 10;

    //////subir files con SFTP
    private static final String user = "sesarr";
    private static final String host = "webpanel.biocubafarma.cu";
    //private static final String host = "sesarr.icid.cu";
  // private static final String host = "10.25.1.24";
    private static final Integer port = 2222;
   //private static final Integer port = 22;
    private static final String pass = "Sesarr/*2019";


    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final String URLstring = "http://192.168.12.60/subefile.php";
    byte[] buffer = new byte[300000];


    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_ERROR = 0;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    private TextView mTitle;
    private String mConnectedDeviceName = null;
    private ArrayAdapter<String> mConversationArrayAdapter;
    private StringBuffer mOutStringBuffer;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;
    private BluetoothDevice BTDevice;
    private Context context;
    FileOutputStream fout = null;
    ByteBuffer BlueBuffer;
    ShortBuffer bluebuf;
    String[] DatosECG = new String[50];

    File sdCard, mydirectory, mydirectoryTemp, directorysub, file = null;
    //////////////////////////////////////////
    /////////////////////////////////////////
    /* Get the BLuetoothDevice object
    walkecg
    direccionBT="10:00:E8:BE:7D:0B";
	 tarjeta 1
    direccionBT="10:00:E8:BE:7D:6C";
    tarjeta 2
    direccionBT="10:00:E8:BE:7C:F1";
  	 dongle margara
    direccionBT="00:15:83:07:C7:DE";*/
    //////////////////////////////////////////
    /////////////////////////////////////////

    int cantdatos = 0, indmuestraf = 0, cantveces = 0, FC = 0, FCMax = 0, FCMin = 0, intanno = 0, IndFC = 0;
    String directorio, directorioTemp, HoraReg, FechaMilitar, directoriosub, Camino, CaminoTemp,
            nombrefile, cantelectros, numeromuestras, nombreequipotemp = null,
            estadoBT = "none", cant = "20", anno = null, FREC, Fma, Fmi, direccionBT = null,
            RegMod = null, ApagarBECG = "02:80:41:00:00:C1:03:";
    boolean mioconectado = false, recogidaOK = false, redmobile = false, subiofile = false;
    TextView txttermino;

    //  variables para el filtro y detector de qrs
    //Cantidad de canales
    public static final short NC = 1;
    //la K y L es para FM=250
    //Longitud del buffer para filtro pasa-bajo
    public static final short K = 4;
    public static final short K2 = K * K;
    //Longitud del buffer para filtro pasa-alto 
    public static final short L = 410;
    public static final int L2 = L * L;
    //Longitud del buffer para calcular derivada
    public static final short ventana_deriv = 2;
    //Longitud del buffer para calcular la energia
    public static final short ventana_energia = 30;

    boolean primeravez = true, interrumpir = false;
    ;
    byte AnchoQRS, draw, YY;
    int prematuros, old_value, sample, pico, ini, ST, IndRR = 0, Indqrs = 0,
            totalmuestras, Threshold, cantmuestras, intervaloRR,
            RRinst, RRant, cantQRS, RRmax, RRmin, RRprom, varmuestra = 0, UmbralPRE,
            SumRR = 0, CantPre = 0, PromRR = 0;
    String nombrepaciente = null, cad, subir = null, nombresub = null;
    short energy_value, Max_Energy;
    int[] y1 = new int[3];


    //son 5min, 300segs  a 250muestrasXseg. son 150000m=300000bytes
    short[] bufMuestras = new short[300000];//antes era 300000
    static short[] bufFiltrada = new short[300000];
    short[] bufEnergia = new short[300000];
    ShortBuffer buf_ecg;
    static ShortBuffer shortbuffer;
    //Buffer para los valores de senal,array[1..k,1..nc] of Integer;  
    int bva1[][] = new int[K][NC];
    //Buffer para las sumas parciales,array[1..k,1..nc] of longint;  
    int bsp1[][] = new int[K][NC];
    //Enlace para recorrido circular,array[1..k] of Integer;
    short ind1[] = new short[K];
    //Suma parcial que sale
    int sps;
    //Suma parcial, array[1..nc] of longint;
    int sp1[] = new int[NC];
    // Suma total, array[1..nc] of longint; 
    int st1[] = new int[NC];
    //Buffer para los valores de senal: array[1..L,1..nc] of Integer; 
    int bva2[][] = new int[L][NC];
    //Buffer para las sumas parciales,  array[1..L,1..Nc] of longint;
    int bsp2[][] = new int[L][NC];
    //Enlace para recorrido circular: array[1..L] of Integer; 
    short ind2[] = new short[L];
    //Suma parcial 2 : array[1..nc] of longint;
    int sp2[] = new int[NC];
    // Suma total 2: array[1..nc] of longint;
    int st2[] = new int[NC];
    //Salida del filtro 2
    int vf2;
    //Puntero del Buffer
    short pb1, pb2; // Puntero del Buffer
    int ve, //Valor que entra
            vs,  // Valor que sale
            vf1;   //Salida del filtro
    //Buffer de valores para calculo derivada : array [1..ventana_deriv] of integer; 
    int bva3[] = new int[ventana_deriv];
    //Indices para recorrido circular : array [1..ventana_deriv] of integer;
    short ind3[] = new short[ventana_deriv];
    //Puntero del buffer 3
    int pb3;
    //Valor que sale buffer de valores
    int vs1;
    //Buffer de valores de derivada ^ 2: array [1..ventana_energia] of word;
    long bder[] = new long[ventana_energia];
    //Indices para recorrido circular : array [1..ventana_energia] of integer;
    short ind4[] = new short[ventana_energia];
    //Puntero del buffer bder
    short pb4;
    //Valor de energia (suma)
    long senerg;
    //Valor que sale buffer bder
    long vs2;
    ProgressDialog dialog;
    int ArrFC[] = new int[299];
    int ArrRR[] = new int[499];
    int ArrQRS[] = new int[499];
    TextView letreroaviso;

    //////////////////////////////////////////////////////////////////////////////////////		
    //////////////////////////////////////////////////////////////////////////////////////
    //esta funcion no permite que al dar la tecla back en la aplicacion, esta se destruya
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //////////////////////////////////////////////////////////////////////////////////////		
    //////////////////////////////////////////////////////////////////////////////////////
    //inicializa componentes de energia
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void InitEnergia() {
        for (short i = 0; i <= (ventana_deriv - 1); i++) {
            bva3[i] = 0;
            ind3[i] = (short) (i + 1);
        }
        ind3[ventana_deriv - 1] = 0;
        pb3 = 0;
        for (short i = 0; i <= (ventana_energia - 1); i++) {
            bder[i] = 0;
            ind4[i] = (short) (i + 1);
        }
        ind4[ventana_energia - 1] = 0;  //ventana_energia-1
        pb4 = 0;
        senerg = 0;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //calculo de energia
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public short valor_energia(short x) {
        int Der;
        vs1 = bva3[pb3];
        bva3[pb3] = x;
        pb3 = ind3[pb3];
        Der = (x - vs1) * (x - vs1);
        vs2 = bder[pb4];
        senerg = senerg + Der - vs2;
        bder[pb4] = Der;
        pb4 = ind4[pb4];
        return ((short) (senerg / ventana_energia));
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //Inicializa componentes del filtro
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void InitFiltro() {
        short i, j, mm;
        for (i = 0; i <= (K - 1); i++)
            for (j = 0; j <= (NC - 1); j++) {
                bva1[i][j] = 0;
                bsp1[i][j] = 0;
            }
        for (i = 0; i <= (K - 1); i++)
            ind1[i] = (short) (i + 1);
        ind1[K - 1] = 0;
        for (i = 0; i <= (L - 1); i++)
            for (j = 0; j <= (NC - 1); j++) {
                bva2[i][j] = 0;
                bsp2[i][j] = 0;
            }
        for (i = 0; i <= (L - 1); i++)
            ind2[i] = (short) (i + 1);
        ind2[L - 1] = 0;
        pb1 = 0;
        pb2 = 0;
        for (i = 0; i <= (NC - 1); i++) {
            sp1[i] = 0;
            sp2[i] = 0;
            st1[i] = 0;
            st2[i] = 0;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //calculo del filtro
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void VFiltrado(short x) {
        //Filtro pasa-bajo
        //Ve  = valor que entra
        //2047 porque el cero esta ahi
        ve = (short) (x + 2047);
        //Vs  = valor que sale
        vs = bva1[pb1][0];
        //Sps = suma parcial que sale
        sps = bsp1[pb1][0];
        //Actualiza suma parcial inc(sp1[1],ve-vs);
        sp1[0] = sp1[0] + ve - vs;
        //Actualiza suma total inc(st1[1],sp1[1]-sps);
        st1[0] = st1[0] + sp1[0] - sps;
        //Guarda valor que entra
        bva1[pb1][0] = ve;
        // Guarda suma parcial
        bsp1[pb1][0] = sp1[0];
        //Calcula salida del filtro 1
        ve = (short) (st1[0] / K2);
        //Filtro pasa-alto
        //vs  = valor que sale
        vs = bva2[pb2][0];
        // sps = suma parcial que sale
        sps = bsp2[pb2][0];
        //Actualiza suma parcial   inc(sp2[1],ve-vs);
        sp2[0] = sp2[0] + ve - vs;
        //Actualiza suma total    inc(st2[1],sp2[1] - sps);
        st2[0] = st2[0] + sp2[0] - sps;
        //Calcula salida del filtro 2
        vf2 = (short) (st2[0] / L2);
        //Calcula valor filtrado
        bufFiltrada[indmuestraf] = (short) (vs - vf2);
        indmuestraf++;
        //Guarda valor que entra
        bva2[pb2][0] = ve;
        //Guarda suma parcial
        bsp2[pb2][0] = sp2[0];
        //Actualiza punteros
        pb1 = ind1[pb1];
        pb2 = ind2[pb2];
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //detector de QRS
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void detectar() {
        boolean sube;
        int Inicio = 0, Final = 0, PicoR = 0, PicoRAnt = 0, Pico = 0, mer, Umbralfinal, ppp;
        int MaxFC = 0, IndAux = 0, MinFC = 0, SumFC = 0;
        for (int k = 0; k < 299; k++)
            ArrFC[k] = 0;
        //Maximo de la funcion de energia
        Max_Energy = 0;
        //Duracin del QRS
        AnchoQRS = 0;
        RRinst = 0;
        intervaloRR = 0;
        RRant = 0;
        RRmax = 0;
        RRmin = 0;
        PicoR = 0;
        PicoRAnt = 0;
        RRprom = 0;
        //cant de RR en un periodo
        cantQRS = 0;
        cantmuestras = 0;
        energy_value = 0;
        int i = 0;
        //copiado es para saber que todo en el file esta OK
        boolean copiado = false;
        //inicializa filtro
        InitFiltro();
        //las muestras quedan en bufMuestras
        //las muestras filtradas en bufFiltrada
        try {
            File f = new File(directorio + "/" + nombrefile);
            if (f.exists() && f.canRead() && (f.length() > 0)) {
                BufferedInputStream buf = new BufferedInputStream(
                        new FileInputStream(f));
                ByteBuffer byteBuffer = ByteBuffer.allocate((totalmuestras));
                buf.read(byteBuffer.array(), 0, (totalmuestras - 1));
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                buf_ecg = byteBuffer.asShortBuffer();
            }//if
            for (int j = 0; j < totalmuestras / 2; j++) {
                bufMuestras[j] = buf_ecg.get(j);
                VFiltrado(bufMuestras[j]);
            }
            copiado = true;
        }//try
        catch (FileNotFoundException e) {
            Toast.makeText(getBaseContext(), "Fichero no encontrado...", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            System.err.println("FileStreamsTest: " + e);
        }
        //tengo  file en  bufMuestras y en totalmuestras,la cantidad de bytes (muestra son 2 bytes) del file
        /////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////
        //prueba con sennal
        int maxsenal = bufMuestras[0];
        int minsenal = bufMuestras[0];
        for (int j = 0; j < (totalmuestras / 2); j++) {
            if (bufMuestras[j] > maxsenal)
                maxsenal = bufMuestras[j];
            else if (bufMuestras[j] < minsenal)
                minsenal = bufMuestras[j];
        }
        //fin de prueba
        /////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////
        if (copiado) {
            //file copiado en buffers
            //inicializa energia
            InitEnergia();
            Max_Energy = 0;
            short ciclo;
            //para desechar los primeros 2 segs del filtro
            if (primeravez)
                ciclo = 499;
            else ciclo = 0;
            for (varmuestra = ciclo; varmuestra < (totalmuestras / 2); varmuestra++) {
                //arreglo para prueba de amplitud  	 energy_value= valor_energia(bufFiltrada[varmuestra]);
                energy_value = valor_energia(bufMuestras[varmuestra]);
                bufEnergia[varmuestra] = energy_value;
                //el valor de energia calculado es mayor que el que estaba
                if (energy_value > Max_Energy)
                    Max_Energy = energy_value;
            }//for de varmuestra
            /////////////////////////////////////////////////////////////////////////////////////////////////
            /////////////////////////////////////////////////////////////////////////////////////////////////
            //prueba con energia
            int maxen = bufEnergia[ciclo];
            int minen = bufEnergia[ciclo];
            for (int j = ciclo; j < ((totalmuestras / 2) - 1); j++) {
                if (bufEnergia[j] > maxen)
                    maxen = bufEnergia[j];
                else if (bufEnergia[j] < minen)
                    minen = bufEnergia[j];
            }
            //fin de prueba
            /////////////////////////////////////////////////////////////////////////////////////////////////
            /////////////////////////////////////////////////////////////////////////////////////////////////
            //calculo el umbral de energia
            Threshold = (int) Math.round(0.15 * Max_Energy);
            //empieza la deteccion
            sube = false;
            //empiezo  en 500 porque los primeros 500,los desecho por el filtro
            for (mer = ciclo; mer < (totalmuestras / 2); mer++)
                if (bufEnergia[mer] < Threshold) break;
            //entro en el proximo ciclo con la energia por debajo del umbral,para asegurarme el QRS
            Final = mer;
            for (varmuestra = mer; varmuestra < (totalmuestras / 2); varmuestra++) {
                if (bufEnergia[varmuestra] >= Threshold) {
                    //posible QRS
                    if (!sube) {
                        //esto es para el periodo refractario
                        //tiempo minimo fisiologico entre dos QRS es de 200miliseg
                        //(250 muestras en 1000mseg, cuantas en 200)=50
                        if ((varmuestra - Final) >= 50) {
                            Inicio = varmuestra;
                            sube = true;
                        }
                    }
                } else if (sube) {
                    //subio y bajo
                    sube = false;
                    Final = varmuestra - 1;
                    long Max = 0;
                    int b;
                    //hallo el max de energia entre inicio y final
                    for (b = Inicio; b <= Final; b++)
                        if (bufEnergia[b] >= Max) {
                            Pico = b;
                            Max = bufEnergia[b];
                        }
                    //voy a buscar el final exactamente
                    int m;
                    Umbralfinal = (int) Math.round(0.90 * Max);
                    for (m = Pico; (m >= (Pico - 30)); m--)
                        if (bufEnergia[m] > Umbralfinal) {
                            Final = m;
                            break;
                        }
                    // con esto da bastante exacto el final
                    ///////////////////////////////////////
                    //voy a buscar exactamente el inicio
                    for (m = Inicio; (m >= (Inicio - 30)); m--)
                        if (bufMuestras[m - 1] < bufMuestras[m])
                            if (bufMuestras[m - 1] < bufMuestras[m - 2]) {
                                Inicio = m;
                                break;
                            }
                    /////////////////////////////////////
                    //QRS normal est entre 70 miliseg y 120miliseg.
                    //entre 17muestras y 30muestras
                    //QRS ancho si excede el 35% del promedio
                    //QRS aberrado si excede el 45% del promedio
                    //QRS adelantado si es menor que el 80% del promedio
                    ////////////////////////////////////
                    AnchoQRS = (byte) (Final - Inicio);
                    if (AnchoQRS >= 5) {
                        //es un QRS,voy a buscar donde esta la R
                        //hallo el max entre inicio y final de las muestras
                        PicoRAnt = PicoR;
                        Max = 0;
                        for (b = Inicio; b <= Final; b++)
                            if (Math.abs(bufMuestras[b]) >= Max) {
                                PicoR = b;
                                Max = Math.abs(bufMuestras[b]);
                            }
                        //en PicoR queda el indice donde se encontro el max
                        if (PicoRAnt != 0) {
                            cantQRS = cantQRS + 1;
                            //para el ancho del QRS en un array
                            ArrQRS[Indqrs] = AnchoQRS;
                            Indqrs++;
                            RRinst = (PicoR - PicoRAnt);//actualizo RR
                            //RRinst=60000/(RRinst*4);
                            if (RRinst > 0) {
                                RRinst = 15000 / RRinst;
                                ArrRR[IndRR] = RRinst;
                                IndRR++;
                            }
                            //tengo que hallar el promedio de RR
                            if (cantQRS < 7)
                                RRprom = RRprom + RRinst;
                            else {
                                //sumo RRprom+RRinst para coger el 7mo tambien
                                if (cantQRS > 0)
                                    intervaloRR = (RRprom + RRinst) / cantQRS;
                                ArrFC[IndFC] = intervaloRR;
                                IndFC++;
                                RRprom = 0;
                                cantQRS = 0;
                            }
                            RRant = RRinst;
                        }
                    }//ancho
                }//subio y bajo
            }//del for
        }  //del copiado en true
        ////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////
        //calculo de FC
        //hallo los dos mayores y los desecho
 /*    int l;  
     for (l=0; l<=1; l++){
      for (int k=0; k<=IndFC; k++)
         if (ArrFC[k]> MaxFC){
           MaxFC=ArrFC[k];
           IndAux=k;}
      for (int m=IndAux; m<=IndFC; m++)
    	 ArrFC[m]=ArrFC[m+1];
         if (l==0){ 
           RRmax=MaxFC;
           MaxFC=0;}
     }
     //hallo los dos menores y los desecho
     for (l=0; l<=1; l++){
       int aux;
       if (l==0)  aux=IndFC-3;
       else aux=IndFC-4; 
       MinFC=ArrFC[0];
       IndAux=0;
       for (int k=1; k<=aux; k++)
          if (ArrFC[k]< MinFC){
            MinFC=ArrFC[k];
            IndAux=k;}
       for (int m=IndAux; m<=aux; m++)
    	 ArrFC[m]=ArrFC[m+1];
       if (l==0) {
        RRmin=MinFC; 
        MinFC=ArrFC[0];}
      }
     //hallo promedio de los restantes
     for (int m=0; m<=(IndFC-5); m++)
    	 SumFC=SumFC +ArrFC[m];  
     //if ((IndFC-4)>0)
     FC=SumFC / (IndFC-4);
     */
        ////////////////////////////////////////////
        ////////////////////////////////////////////
        //hallo promedio de todos
        for (int m = 0; m < (IndFC); m++)
            SumFC = SumFC + ArrFC[m];
        if (IndFC != 0)
            FC = SumFC / (IndFC);
        ////////////////////////////////////////////
        ////////////////////////////////////////////
        FCMax = RRmax;
        FCMin = RRmin;
        ////////////////////////////////////////////
        ////////////////////////////////////////////
        //calculo de l acant, de latidos prematuros
        for (int y = 0; y < (IndRR); y++)
            SumRR = SumRR + ArrRR[y];
        if (IndRR != 0)
            PromRR = SumRR / (IndRR);
        UmbralPRE = (PromRR * 80) / 100;
        for (int w = 0; w < (IndRR); w++)
            if (ArrRR[w] < UmbralPRE)
                CantPre++;
        ////////////////////////////////////////////////
        //escribo en la BD los valores correspondientes
        FREC = Integer.toString(FC);
        Fma = Integer.toString(FCMax);
        Fmi = Integer.toString(FCMin);
        String Pre = Integer.toString(CantPre);
        String nombrebd = nombrefile.substring(0, 5);
        BD.addDato(HoraReg, nombrebd, FREC, Fma, Fmi, Pre);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //crea directorio y fichero donde se guardan las muestras
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void creafileTEMP() {
        try {
            // validamos si se encuentra montada nuestra memoria externa
            String estado = Environment.getExternalStorageState();
            if (estado.equals("mounted")) {
                // Obtenemos el directorio de la memoria externa
                sdCard = Environment.getExternalStorageDirectory();
                //creo file temporal
                mydirectoryTemp = new File(sdCard.getAbsolutePath() + "/ECGTemp");
                if (!mydirectoryTemp.exists())
                    //En caso de no existir, lo crea
                    mydirectoryTemp.mkdir();
                directorioTemp = sdCard.getAbsolutePath() + "/ECGTemp";
                //tengo que crear el nombre del file donde voy a leer lo que viene por bluetooth
                CaminoTemp = directorioTemp + "/Temp.beg";
                File archivotemp = new File(CaminoTemp);
                //Comprueba si el archivo existe
                if (!archivotemp.exists()) {
                    try {//creo file temporal
                        BufferedWriter ftemp = new BufferedWriter(new FileWriter(archivotemp));
                    } catch (IOException e) {
                        Toast.makeText(getBaseContext(), CaminoTemp, Toast.LENGTH_LONG).show();
                    }
                }
            }//if
            else
                Toast.makeText(getBaseContext(), "El almacenamiento externo no se encuentra disponible", Toast.LENGTH_LONG).show();
        }//try
        catch (Exception e) {
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //saca aviso!!!!
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void sacaAviso(String sms, final boolean foreg) {
        AlertDialog.Builder DialogoInfo = new AlertDialog.Builder(this);
        //para no poder salir del dialogo sino es por su boton
        DialogoInfo.setCancelable(false);
        DialogoInfo.setIcon(R.drawable.electro2);
        DialogoInfo.setTitle("Informacion");
        DialogoInfo.setMessage(sms);
        DialogoInfo.setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        DialogoInfo.show();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //crea directorio y fichero donde se guardan las muestras
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void creafile() {
        File Archivo = null;
        try {
            // validamos si se encuentra montada nuestra memoria externa
            String estado = Environment.getExternalStorageState();
            if (estado.equals("mounted")) {
                // Obtenemos el directorio de la memoria externa
                sdCard = Environment.getExternalStorageDirectory();
                //creo el directorio para ese paciente
                mydirectory = new File(sdCard.getAbsolutePath() + "/ECG" + nombrepaciente);
                //Comprueba si el directorio no existe
                if (!mydirectory.exists())
                    //creo file del paciente
                    mydirectory.mkdirs();
                directorio = sdCard.getAbsolutePath() + "/ECG" + nombrepaciente;
                ///////////////////////////////////////////////
                //creo el directorio de electros a la web
                directorysub = new File(sdCard.getAbsolutePath() + "/ECGSUB" + nombrepaciente);
                //Comprueba si el directorio no existe
                if (!directorysub.exists())
                    //creo file de registros a la web
                    directorysub.mkdirs();
                directoriosub = sdCard.getAbsolutePath() + "/ECGSUB" + nombrepaciente;
                int longname = nombrepaciente.length();
                nombresub = nombrepaciente.substring(9, longname);
                ///////////////////////////////////////////////
                HoraReg = new Date().toString();
                int hor = HoraReg.length();
                //PROBAR!!!!!!
                if (hor == 34)
                    //htc gratia, samsung
                    anno = HoraReg.substring(30, 34);
                else {
                    if (hor == 33)
                        //sony ericsson
                        anno = HoraReg.substring(29, 33);
                    else
                        //huawei de osmani
                        anno = HoraReg.substring(24, 28);
                }
                ////////////////////////////////
                intanno = Integer.parseInt(anno);
                //para cortar dar dia y hora, hasta minutos
                //mandar la hora a Ale sin modificacion en Ingles
                FechaMilitar = HoraReg.substring(4, 16);
                /////////////////////////////////
                //transformar la hora al espannol  para la BD
                String Resto = HoraReg.substring(11, 13);//hora militar
                int tempresto = Integer.parseInt(Resto);
                String media = "";
                if (tempresto >= 13) {
                    tempresto = tempresto - 12;
                    media = " pm";
                } else media = " am";
                if (tempresto >= 10)
                    Resto = String.valueOf(tempresto);//hora en 12
                else Resto = "0" + String.valueOf(tempresto);//hora en 12
                ////////////////////////////////////
                String resto1 = HoraReg.substring(13, 16) + media;//minutos
                //////////////////////////////////
                String mes = HoraReg.substring(4, 7);//mes en ingles
                if (mes.equals("Apr")) mes = "Abr";
                if (mes.equals("Aug")) mes = "Ago";
                if (mes.equals("Dec")) mes = "Dic";
                //////////////////////////////////////
                String dia = HoraReg.substring(8, 10);
                HoraReg = dia + "/" + mes + "/" + intanno + "  ||  " + Resto + resto1;

                //    sacaAviso(HoraReg, true);

                //Comprueba el numero de registro que corresponde crear con este paciente
                //por si se despareo el dispositivo y hubo que borrar los datos
                for (int i = 0; i <= 5000; i++) {
                    String itemp = Integer.toString(i);
                    if (i < 10)
                        cantelectros = "0" + itemp;
                    else
                        cantelectros = itemp;
                    nombrefile = "ECG" + cantelectros + ".beg";
                    //  	    sacaAviso(nombrepaciente, true);
                    //   	    sacaAviso(nombrefile, true);

                    //Comprueba si el file con ese numero de registro existe
                    Camino = directorio + "/" + nombrefile;
                    Archivo = new File(Camino);
                    if (!Archivo.exists()) break;
                }
                //	    sacaAviso(Camino, true);
                try {
                    //creo file del registro
                    Archivo.createNewFile();
                } catch (IOException e) {
                    Toast.makeText(getBaseContext(), Camino, Toast.LENGTH_LONG).show();
                }
            }//if
            else {
                Toast.makeText(getBaseContext(), "El almacenamiento externo no se encuentra disponible", Toast.LENGTH_LONG).show();
                //    sacaAviso("El almacenamiento externo no se encuentra disponible", true);
            }
        }//try
        catch (Exception e) {
            //  sacaAviso("NO PUDOOO  ", true);

            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////		
    //////////////////////////////////////////////////////////////////////////////////////
    //esta habilitado el bluetooth
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void habilitado() {
        if (mChatService == null) {
            setupChat();
            RecibirEquipo();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //inicializar el servicio
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    private void setupChat() {
        Log.d(TAG, "setupChat()");
        mChatService = new BluetoothChatService(this, mHandler, totalmuestras);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //PREPARA FICHERO PARA SUBIR AL SITIO
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void preparafileAsubir() {
        try {
            File inputFile1 = new File(directorio + "/" + nombrefile);
            File outputFile1 = new File(directorysub + "/ECG" + nombresub + "-" + cantelectros + ".SUB");
            //Comprueba si el archivo existe
            if (!outputFile1.exists()) {
                try {  //En caso de no existir
                    outputFile1.createNewFile();
                } catch (IOException e) {
                    Toast.makeText(getBaseContext(), directorysub + "/ECG" + nombresub + "-" + cantelectros + ".SUB",
                            Toast.LENGTH_LONG).show();
                }
            }
            FileInputStream mov = new FileInputStream(inputFile1);
            FileOutputStream upl = new FileOutputStream(outputFile1);
            //fecha y hora
            byte[] byteArray = HoraReg.getBytes("US-ASCII");
            byte[] byteArray1 = FechaMilitar.getBytes("US-ASCII");
            upl.write(byteArray1);
            //upl.write(byteArray);
            //estan del 0 al 11, fecha y hora
            int bytemas = intanno >> 8;
            int bytemen = intanno;
            upl.write(bytemen);
            upl.write(bytemas);
            //esta en el 12 y 13, anno

            bytemas = FC >> 8;
            bytemen = FC;
            upl.write(bytemen);
            upl.write(bytemas);
            //esta en el 14 y 15, FC de todo el registro

            bytemas = FCMax >> 8;
            bytemen = FCMax;
            upl.write(bytemen);
            upl.write(bytemas);
            //esta en el 16 y 17,FC maxima de todo el registro

            bytemas = FCMin >> 8;
            bytemen = FCMin;
            upl.write(bytemen);
            upl.write(bytemas);
            //esta en el 18 y 19,FC minima de todo el registro
            switch (totalmuestras) {
                case 30000:
                    bytemen = 1;
                    break;
                case 60000:
                    bytemen = 2;
                    break;
                case 90000:
                    bytemen = 3;
                    break;
                case 120000:
                    bytemen = 4;
                    break;
                case 150000:
                    bytemen = 5;
                    break;
            }
            upl.write(bytemen);
            //en el 20 ,cant de minutos de registro
            int d = 0;
            int size = mov.available();
            for (int i = 0; i < size; i++) {
                int mar = 0;
                if (i > (size - 5))
                    mar = 1;
                d = mov.read();
                //tengo que encriptarlo
                //voy a sumarle 4
                d = d + 4;
                upl.write(d);
            }
            mov.close();
            //a partir del 21 fichero de registro, sumandole 4 a cada byte
            bytemas = 255;
            for (int i = 0; i < 4; i++)
                upl.write(bytemas);
            //agregar fin de fichero de registro, FF FF FF FF
            try {
                File inputFile5 = new File(directorio + "/ECG" + cantelectros + ".RR");
                FileInputStream mov1 = new FileInputStream(inputFile5);
                int d1 = 0;
                int size1 = mov1.available();
                //fichero con los valores de RR del registro
                for (int i = 0; i < size1; i++) {
                    int mar = 0;
                    if (i > (size1 - 5))
                        mar = 1;
                    d1 = mov1.read();
                    upl.write(d1);
                }
                mov1.close();
            } catch (Exception e) {
                Log.e(TAG, "Fichero no existe", e);
            }
            //agregar valores de RR
       /*  
           // agregar cant de latidos prematuros para este registro
            bytemas=CantPre>>8;
            bytemen=CantPre;
            upl.write(bytemen);
            upl.write(bytemas);
        */
            upl.close();
        } catch (Exception e) {
            Log.e(TAG, "Datos NO Guardados", e);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////		
    //////////////////////////////////////////////////////////////////////////////////////
    //chequea estado de conexion a red mobile
    public void checkNetworkStatus() {
        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobile.isAvailable())
            redmobile = true;
        else
            redmobile = false;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////		
    ///////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////
    public void CompruebaWeb() {
        int x;
        //averiguar si hay algun registro que no se subio
        //porque en ese problema hubo fallo de conexion
        //los registros que no se subieron, no fueron borrados
        //preguntar si hay algun registro en el subdirectorio
        File d = new File(directoriosub);
        File[] ficheros = d.listFiles();
        for (x = 0; x < ficheros.length; x++) {
            if (ficheros[x].exists()) {
                subir = ficheros[x].getName();
                checkNetworkStatus();
                if (redmobile) {
                        SubeFile(directoriosub + "/" + subir,"/"+subir);
                        borraFileaWeb(directoriosub + "/" + subir);
                }
            }
        }
        subiofile = false;
        finish();
    }

    //////////////////////////////////////////////////////////////////////////////////////   
    //////////////////////////////////////////////////////////////////////////////////////		
    //////////////////////////////////////////////////////////////////////////////////////
    //borra ficheros en el phone enviados a web
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void borraFileaWeb(String sube) {
        File inputFile1;
        //se pone subiofile para garantizar que se envio a web el registro
        //porque aun cuando redmobile este en true,
        //puede ser que no se envie porque la conexion a mobile este en fallo
        if (subiofile) {
            //envio a web correctamente,borra fichero subido
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(1000);
            inputFile1 = new File(sube);
            boolean deleted = inputFile1.delete();
            if (deleted)
                System.out.println("El fichero ha sido borrado satisfactoriamente");
            else
                System.out.println("El fichero no pudo ser borrado");
        } else  //no se envio a la web
            finish();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////		
    //////////////////////////////////////////////////////////////////////////////////////
    //eliminar la conexion
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void terminarconexion() {
        if (mChatService != null)
            mChatService.stop();
        mBluetoothAdapter.disable();
        while (mBluetoothAdapter.isEnabled()) {
        }
        //los tablets sin SIM no funciona el gprs
        //cuando apago el bluetooth , el registrador se apaga
        if (recogidaOK) {
            preparafileAsubir();
            checkNetworkStatus();
            String sube = directorysub + "/ECG" + nombresub + "-" + cantelectros + ".SUB";
            if (redmobile) {
                SubeFile(sube,"/ECG" + nombresub + "-" + cantelectros + ".SUB");
                borraFileaWeb(sube);
            }
            CompruebaWeb();
            recogidaOK = false;
        } else
            finish();
    }

    ///////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////	  
    //para subir files al sitio
    ///////////////////////////////////////////////////////////
    public void SubeFilehttp(String subefichero) {
        File inputFile;
        boolean status = false;
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;
        String lineEnd = "\r\n", twoHyphens = "--", boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        ////////////////////////////
        //en esta rutina no se pueden poner toast porque dan errores de excepcion
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(subefichero));
            //cantidad de bytes de cada fichero
            bytesAvailable = fileInputStream.available();
            byte[] buffer = new byte[bytesAvailable];
            URL url = new URL(URLstring);
            connection = (HttpURLConnection) url.openConnection();
            //permitir entradas y salidas
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            // habilitar el metodo POST
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            //para la autenticacion
            //connection.setRequestProperty("Authorization", "Basic " + Autentication);
            //connection.setRequestProperty ("Authorization", encodedCredentials);
            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            //fichstring es el file que voy a enviar
            outputStream.writeBytes("Content-Disposition: form-data;name=\"uploadedfile\";filename=\"" + subefichero + "\"" + lineEnd);
            outputStream.writeBytes(lineEnd);
            bytesRead = fileInputStream.read(buffer, 0, bytesAvailable);
            outputStream.write(buffer, 0, bytesAvailable);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            // Responses from the server (code and message)
            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();
            fileInputStream.close();
            outputStream.flush();
            outputStream.close();
            //el codigo que todo se hizo correcto es 200
            if (serverResponseCode == 200) {
                subiofile = true;
                finish();
            } else subiofile = false;
        }//try
        catch (MalformedURLException ex) {
            subiofile = false;
        } catch (IOException ioe) {
            // Toast.makeText(getBaseContext(),ioe.toString(), Toast.LENGTH_LONG).show();
            subiofile = false;
            Toast.makeText(getBaseContext(), "No activa la Red Movil", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            subiofile = false;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////		
    //////////////////////////////////////////////////////////////////////////////////////
    //envia datos a registrar activity
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void finish() {
        //dialog.cancel();
        Intent data = new Intent();
        data.putExtra("EXITO", cant);
        setResult(RESULT_OK, data);
        //finish libera espacio de memoria de esta actividad
        //y pide que se active la anterior
        super.finish();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //iniciar la conexion
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void RecibirEquipo() {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(direccionBT);
        mChatService.connect(device);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //procesar registro realizado
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void procesaregistro() {
        cantveces = 0;
        mioconectado = false;
        creafile();
        //  sacaAviso("salio de creafile", true);
        try {
            File inputFile = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/ECGTemp/Temp.beg");
            File outputFile = new File(directorio + "/" + nombrefile);
            FileInputStream fis = new FileInputStream(inputFile);
            FileOutputStream fos = new FileOutputStream(outputFile);
            int c = 0;
            boolean bytemas = false;
            int size = fis.available();
            //de 0 a 29999 son 30000 eltos
            for (int i = 0; i < size; i++) {
                c = fis.read();
                //guardar solamente los 12 bits de sennal
                if (bytemas) {
                    //quito los bits de marcapaso y electrodo suelto
                    bytemas = false;
                    c = c & 0x0F;
                    fos.write(c);
                } else {
                    fos.write(c);
                    bytemas = true;
                }
            }
            fos.close();
            fis.close();
            //////////////////////////////////
            //probar para ver los ceros en algunos regsitros, no borrar el temporal
            //borra fichero temporal
            boolean deleted = inputFile.delete();
            if (deleted)
                System.out.println("El fichero ha sido borrado satisfactoriamente");
            else
                System.out.println("El fichero no pudo ser borrado");
            //borra subdirec tambien
            File file = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/ECGTemp");
            deleted = file.delete();
            if (deleted)
                System.out.println("El subdirectorio ha sido borrado satisfactoriamente");
            else
                System.out.println("El subdirectorio no pudo ser borrado");
            //////////////////////////////
            ///////////////////////////////
            detectar();
            //////////////////////////////////
            //file con valores de RR del registro
            try {
                File outputFile1 = new File(directorio + "/ECG" + cantelectros + ".RR");
                FileOutputStream fos1 = new FileOutputStream(outputFile1);
                for (int i = 0; (i < IndRR); i++) {
                    fos1.write(ArrRR[i]);
                }
                fos1.close();
            }//try
            catch (Exception e) {
                Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
            }
            //////////////////////////////////////

            recogidaOK = true;
            cant = "8";
            terminarconexion();
        }//try
        catch (Exception e) {
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void mandaApagar() {
        sendMessage(ApagarBECG);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //enviar por bluetooth a equipo remoto a apagar
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    private void sendMessage(String message) {
        // comprobar que esta actualmente conectado
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getBaseContext(), "El dispositivo no est� conectado", Toast.LENGTH_SHORT).show();
            return;
        }
        // coge el message y lo envia al BluetoothChatService to write
        byte[] send = message.getBytes();
        mChatService.write(send);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //create
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D) Log.e(TAG, "+++ ON CREATE +++");
        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main1);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        cronometro = (Chronometer) findViewById(R.id.chronometer1);
        //para iniciar cronometro cuando lo deseemos
        cronometro.setBase(SystemClock.elapsedRealtime());
        Bundle recibido = getIntent().getExtras();
        numeromuestras = recibido.getString("TiempoReg");
        //paso de string a entero
        totalmuestras = Integer.parseInt(numeromuestras);
        //totalmuestras es la cantidad de muestras que tengo que leer segun tiempo que se escogio*/ 
        nombrepaciente = recibido.getString("Paciente");
        direccionBT = recibido.getString("DireccionRemota");
        letreroaviso = (TextView) findViewById(R.id.muestrasuelto);
        letreroaviso.setVisibility(View.INVISIBLE);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText("Registrando...");
        mTitle = (TextView) findViewById(R.id.title_right_text);
        // Get local Bluetooth adapter, esto es unico
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth no es soportado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //Start Activity
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onStart() {
        super.onStart();
        if (D) Log.e(TAG, "++ ON START ++");
        //por si el phone tiene otro nombre de dispositivo, ponerle el que se definio
        //para la conexion
//nov 28-2017      if (!nombrepaciente.equals(mBluetoothAdapter.getName()))
//nov 28-2017           mBluetoothAdapter.setName(nombrepaciente);
        habilitado();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //onResume Activity
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D) Log.e(TAG, "+ ON RESUME +");
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE)
                mChatService.start();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //manejador de la info del servicio
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////
                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        ////////////////////////////////////////////////////
                        case BluetoothChatService.STATE_CONNECTED:
                            if (mioconectado) {
                                if (msg.arg2 == 2) {
                                    letreroaviso.setVisibility(View.VISIBLE);
                                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                    vibrator.vibrate(500);
                                    cronometro.setVisibility(View.INVISIBLE);

                                } else if (msg.arg2 == 3) {
                                    letreroaviso.setVisibility(View.INVISIBLE);
                                    Vibrator vibrator1 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                    vibrator1.vibrate(500);
                                    cronometro.stop();
                                    cronometro.setBase(SystemClock.elapsedRealtime());
                                    cronometro.setVisibility(View.VISIBLE);
                                    cronometro.start();
                                }
                            } else {
                               // mTitle.setText(R.string.title_connected_to);
                                mTitle.append(mConnectedDeviceName);
                                mioconectado = true;
                                creafileTEMP();
                                //1000 mseg es 1 seg
                                //64000 mseg son 64 seg, que es lo que demora registrar, procesar y enviar a web 1 minuto
                                cronometro.start();
                                //dialog = ProgressDialog.show(BluetoothChat.this,"Adquiriendo se�al","Espere por favor...", true);
                                //Mantener encendida la pantalla
                                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                            }
                            break;
                        ////////////////////////////////////////////////////
                        case BluetoothChatService.STATE_CONNECTING:
                            mTitle.setText("Conectando...");
                            break;
                        ////////////////////////////////////////////////////
                        case BluetoothChatService.STATE_LISTEN:
                            ////////////////////////////////////////////////////
                        case BluetoothChatService.STATE_NONE:
                            boolean pru = mioconectado;
                            if (mioconectado) {
                                if (msg.arg2 == -1) {
                                    mTitle.setText("Procesando datos..");
                                    procesaregistro();
                                    cronometro.stop();
                                } else if (msg.arg2 == 0) {
                                    mTitle.setText("Interrumpida la conexion..");
                                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                    vibrator.vibrate(1000);
                                    cant = "17";
                                    mioconectado = false;
                                    terminarconexion();
                                }
                            }//mioconectado
                            else if (msg.arg2 == 2) {
                                if (!cant.equals("17")) {
                                    cant = "16";
                                    terminarconexion();
                                }
                            }
                            break;
                    }//msg.arg1
                    break;
                ////////////////////////////////////////////////////
                ////////////////////////////////////////////////////
                /////////////////////////////////////////
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    break;
                /////////////////////////////////////////
                case MESSAGE_WRITE:
                    cant = "35";
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String();
                    break;
                /////////////////////////////////////////
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    break;
                /////////////////////////////////////////
                case MESSAGE_TOAST:
                    //Deshabilitada Conexi�n a Equipo
                    cant = "15";
                    terminarconexion();
                    break;
                //////////////////////////////////////////
                case MESSAGE_ERROR:
                    cant = "15";
                    terminarconexion();
            }
        }
    };

    //////////////////////////////////////////////////////////////////////////////////////
    //para subir files al sitio
    ///////////////////////////////////////////////////////////
  // public static void SubeFile(String dirTel, String[] args) throws Exception {
    public void SubeFile(String dirTel,String subeNombre) {
        try {
            //Set StrictHostKeyChecking property to no to avoid UnknownHostKey issue
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            UserInfo ui = new SUserInfo(pass, null);
            boolean paso=true;
            session.setUserInfo(ui);
            session.setPassword(pass);
            session.setConfig(config);
            session.setTimeout(60000);
            session.connect();
            paso=false;
            //salta de aqui
            //abro un canal de SFTP
            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
            //conectar con el SFTP
            sftp.connect();
            //subir un fichero  sftpChannel.put(sourcePath, destinationPath)

            //sftp.put(dirTel ,"/public_html/ficherosECG/ECG" + nombresub +"-" + cantelectros +".SUB");
            sftp.put(dirTel ,"/public_html/ficherosECG"+subeNombre);
            //bajar un fichero  sftpChannel.get(sourcePath, destinationPath)
            subiofile = true;
            if (subiofile) {
                // El archivo ha sido subido satisfactoriamente
            }
            sftp.exit();
            sftp.disconnect();
            //llega aqui
            session.disconnect();
        } catch (JSchException e) {
            System.out.println(e.getMessage().toString());
            e.printStackTrace();
        } catch (SftpException e) {
            System.out.println(e.getMessage().toString());
            e.printStackTrace();
        }
    }
}
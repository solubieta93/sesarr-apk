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

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.UUID;


/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothChatService {
    // Debugging
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;

    // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothChat";

    // Unique UUID for this application
    //private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    //este valor es el correspondiente para el perfil de puerto serial,SPP,0x1101
    //UUID(0x1101);
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    int cantdatos=0,iniciobuf=0,contcaido=0;
    int cantmuestras;
	byte[] bufLectura=new byte[300000];
	byte[] ApagaBECG=new byte[4];
    String parear;
    ProgressDialog dialog1;
    boolean electrocaido=false,iniciocaido=false;
    public static final int STATE_NONE = 0;       // no hace nada
    public static final int STATE_LISTEN = 1;     // escuchando conexiones
    public static final int STATE_CONNECTING = 2; // inicializando la conexion
    public static final int STATE_CONNECTED = 3;  // conectado
    boolean bytemas=false;
    boolean eletrodosuelto=false;	 


    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public BluetoothChatService(Context context, Handler handler,int muestras) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
        //cantidad de bytes de acuerdo al tiempo seleccionado
        cantmuestras=muestras;
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //setea el estado de conexion
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        // ENVIAMOS AL HANDLER PARA QUE SE ACTUALICE EN LA ACTIVIDAD
        mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //lee el estado de conexion
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public synchronized int getState() {
        return mState;
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //inicia el servicio, se queda escuchando
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public synchronized void start() {
        if (D) Log.d(TAG, "start");
        // CANCELA CUALQUIER HILO QUE QUIERA HACER UNA CONEXION
        //HILO DE CONEXION
        if (mConnectThread != null) {
        	mConnectThread.cancel();
        	mConnectThread = null;}
        //CANCELA CUALQUIER HILO QUE ESTE CORRIENDO UNA CONEXION
        //HILO CONECTADO
        if (mConnectedThread != null) {
        	mConnectedThread.cancel(); 
        	mConnectedThread = null;}
        // INICIA HILO QUE ESCUCHARA EN EL BluetoothServerSocket
        // HILO DE ACEPTACION
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();}
        setState(STATE_LISTEN);
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //para todos los hilos 
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        if (mConnectThread != null) {
        	mConnectThread.cancel();
        	mConnectThread = null; }
        if (mConnectedThread != null) {
        	mConnectedThread.cancel();
            mConnectedThread = null;}
        if (mAcceptThread != null) {
        	mAcceptThread.cancel(); 
        	mAcceptThread = null;}
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //INDICA CONEXION DE FALLO Y NOTIFICA A LA ACTIVIDAD
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    private void connectionFailed() {
        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChat.TOAST, "Deshabilitada Conexi�n a Equipo");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //INDICA CONEXION PERDIDA Y NOTIFICA A LA ACTIVIDAD
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    private void connectionLost() {
        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChat.TOAST, "Conexi�n perdida");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
     }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //ESCRIBE EN EL HILO CONECTADO DE MANERA ASINCRONA
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //INICIA HILO CONECTADO PARA INICIAR LA CONEXION CON UN DISPOSITIVO REMOTO    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);
        // CANCELA CUALQUIER HILO QUE INTENTE REALIZAR UNA CONEXION
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
            	mConnectThread.cancel(); 
            	mConnectThread = null;}
        }
        // CANCELA CUALQUIER HILO QUE SE ENCUENTRE CORRIENDO UNA CONEXION
        if (mConnectedThread != null) {
       	   mConnectedThread.cancel();
           mConnectedThread = null;}
        // INICIA EL HILO PARA CONECTAR CON UN DISPOSITIVO
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //INICIA EL HILO CONECTADO PARA INICIAR LA ADMON DE LA CONEXION BT
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");
        // CANCELA EL HILO QUE COMPLETO LA CONEXION
        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;}
        // CANCELA EL HILO QUE AQCTUALMENTE ESTA CORRIENDO LA CONEXION
        if (mConnectedThread != null) {
        	mConnectedThread.cancel();
        	mConnectedThread = null; }
        // CANCELA EL HILO DE ACEPTACION DEBIDO A QUE SOLO QUEREMOS CONECTAR CON UN DISPOSITIVO
        if (mAcceptThread != null) {
        	mAcceptThread.cancel();
        	mAcceptThread = null; }
        // INICIA EL HILO PARA LA ADMON DE LA CONEXION Y REALIZAR TRANSMISIONES
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        // ENVIA EL NOMBRE DEL DISPOSITIVO CONECTADO DE VUELTA
        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChat.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    // ENCARGADO DE ESCUCHAR TODAS LAS SOLICITUDES DE CONEXIONES ENTRANTE 
    // sE COMPORTA COMO EL LADO DEL SERVIDOR.
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    private class AcceptThread extends Thread {
        // SOCKET DE SERVIDOR LOCAL
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            // CREAMOS UN NUEVO LISTENING SERVER SOCKET
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
               }
            mmServerSocket = tmp;
         }

        public void run() {
            if (D) Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("HILOACEPTADO");
            BluetoothSocket socket = null;
            // ESCUCHA AL SERVER SOCKET SI NO ESTAMOS CONECTADOS
            while (mState != STATE_CONNECTED) {
                try {
                    // ESTO ES UN BLOQUE DE DONDE SOLO OBTENDREMOS UNA CONEXION O UNA EXCEPCION
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                   }
                //SI LA CONEXION FUE ACEPTADA
                if (socket != null) {
                    synchronized (BluetoothChatService.this) {
                        switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            //SITUACION NORMAL.INICIAMOS HILO CONECTADO
                            connected(socket, socket.getRemoteDevice());
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            // O NO ESTA LISTA O YA ESTA CONECTADA. TERMINA EL NUEVO SOCKET
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            if (D) Log.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //corre mientras esta atendiendo para hacer una conexion de salida
    //termina cuando es exitosa la conexion o cuando hay fallos EN EL CLIENTE
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // OBTIENE UN BLUETOOTHsOCKET PARA LA CONEXION CON EL DISPOSITIVO OBTENIDO
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("HILOCONECTADO");
            //CANCELA EL DESCUBRIMIENTO PARA QUE NO SEA LENTA LA CONEXION
            mAdapter.cancelDiscovery();
            // REALIZA LA CONEXION CON EL SOCKETBLUETOOTH
            try {
                // ESTO ES UN BLOQUE SOLO RECIBIREMOS LA CONEXION ESTABLECIDA O UNA EXCEPCION
                //2021 aqui se pierde
                mmSocket.connect();
                } 
                  catch (IOException e) {
                	connectionFailed();
                     try {
                          mmSocket.close();
                          } catch (IOException e2) {
                             Log.e(TAG, "unable to close() socket during connection failure", e2);
                         }
                  // INICIA EL SERVICIO A TRAVES DE REINICIAR EL MODO DE LISTENING
                      //BluetoothChatService.this.start();
                      return;
                    }
            //RESETEA EL HILO CONECTADO PUES Y ALO HEMOS USADO
            synchronized (BluetoothChatService.this) {
                  mConnectThread = null;
            }
            //INICIA EL HILO CONECTADO
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //corre durante la conexion con un equipo remoto
    //maneja todas las entradas y salidas
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    private class ConnectedThread extends Thread {
    	private final Context context = null;
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private int vecescero=0;
       public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
           // OBTIENE  DEL BluetoothSocket DE ENTRADA Y SALIDA
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
              }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
     //////////////////////////////////////////////////////////////////////////////////////
     //ESCRIBE AL STREAM DE SALIDA CONECTADO
     //////////////////////////////////////////////////////////////////////////////////////
     //////////////////////////////////////////////////////////////////////////////////////
     public void write(byte[] buffer) {
       try {
          mmOutStream.write(buffer);
          // Share the sent message back to the UI Activity
          mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
                      .sendToTarget();
         } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
           }
      }
     
      public void cancel() {
          try {
              mmSocket.close();
              } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
               }
      }

      public void run() {
         Log.i(TAG, "BEGIN mConnectedThread");
         //byte[] buffer = new byte[15000];
         byte[] buffer = new byte[300000];
         int bytes,b,m=0;
         while (true) {
           try {
             int p,pq,ph,phh;
             bytes = mmInStream.available();
             if (bytes>0) {
               vecescero=0;
               bytes = mmInStream.read(buffer);
   //cambio nov-2017
                 cantdatos=cantdatos+bytes;
               p=cantdatos;
               pq=bytes;
               ph= iniciobuf;
               //bit 1-3 electrodos sueltos, byte mas signif.
               //bit 1:tierra(pierna)
               //bit 2:negativo, brazo derecho
               //bit 3:positivo, brazo izquierdo
               //bit 0 marcapaso
               //Lo recibido por BT LO GUARDO EN BUFLECTURA
               for (b=0; (b<=(bytes-1)); b++){
 		    		int mm5=iniciobuf;
      		    	byte temp5=bufLectura[iniciobuf] ; 

 //cambio nov-2017
                   	    		bufLectura[iniciobuf]= buffer[b];
//cambio nov-2017
iniciobuf++;
            	  //analizo muestra a muestra 
      		      if (bytemas) {
      		    	//tengo el byte mas signif.
      		    	byte temp1=(byte)((buffer[b] >>4) & 0x07);
      		    	if (temp1 != 0){ 
      		    	  if (!electrocaido){
      		    		electrocaido=true;
      		    		mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE,STATE_CONNECTED,2).sendToTarget();
      		    	  }
      		    	}  
      		    	else
      		    	  if (electrocaido){
      		    		electrocaido=false;
      		    	    //como electrodo estaba caido y ya esta OK
      		    		//debo poner cantdatos en 0,PARA QUE RECOJA EL TIEMPO SEGUIDO SIN CAIDA DE ELECTRODO 
          		    	cantdatos=0;
          		    	iniciobuf=0;
          		    	int margara=cantdatos;
                          //cambio nov-2017	, se puso aqui
                        cantdatos=cantdatos+bytes;
                        bufLectura[iniciobuf]= buffer[b];
     	    		    iniciobuf++;
                          /////////////////////////////////////////
                        mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE,STATE_CONNECTED,3).sendToTarget();
          	    	 }
  		    		bytemas=false;
      		      }
      		      else bytemas=true;
      		      if (iniciobuf >= 5001){
      		    	  int mm3=iniciobuf;
      		    	  boolean mm=electrocaido;
      		    	  byte mm1=bufLectura[iniciobuf];
      		    	  byte mm2=bufLectura[iniciobuf-1];
      		    	  boolean mm8=bytemas;
      		    	  int mm6=mm3;
      		      }
               }
               ////////////////////////////////////////////
               ////////////////////////////////////////////
               //ANALISIS DE TRAMA                      
               ////////////////////////////////////////////                      
               ///////////////////////////////////////////////////    		    	  
               //////////////////////////////////////////////////////////////////////////////////////
               //SE LEYERON TODAS LAS MUESTRAS
               //////////////////////////////////////////////////////////////////////////////////////
    		   if (cantdatos>=cantmuestras) {
                 //CREANDO FILE TEMPORAL
    		     try {
                    RandomAccessFile fout = new RandomAccessFile (Environment.getExternalStorageDirectory()
                                  			.getAbsolutePath()+ "/ECGTemp/Temp.beg","rw");
                    phh= iniciobuf;
                 	for (b=0; (b<=(iniciobuf-1)); b++)
                       m=bufLectura[b];
                    for (b=(iniciobuf-10); (b<=(iniciobuf-1)); b++)
                   		   m=bufLectura[b];
                	p=b;
                    pq=m;
                    //ale fout.write(bufLectura,0,(iniciobuf-1));
                    fout.write(bufLectura,0,(iniciobuf));
                    fout.close();
                  }catch (Exception e) {
                    //  Log.e(TAG,"Datos NO Guardados",e);
                    Log.e(TAG,e.toString(),e);
                   }
                  cantdatos=0;
                  iniciobuf=0;
                  //mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE,-1).sendToTarget();
                  mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE,STATE_NONE,-1).sendToTarget();
                  break;
               }
             }//bytes>0
             else{ 
               vecescero++;
               //estaba 100000,ok
               //hay que poner este aunque se demore un poco
               // esto para los telefonos normales de prueba
               //if(vecescero > 100000){
               //esto para un samsung S5
               if(vecescero > 500000){
            	   
                 vecescero=0;
                 //PASO UN TIEMPO SIN RECIBIR MUESTRAS
                 mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE,STATE_NONE, 0).sendToTarget();
                 break;}
             }
          } catch (IOException e) {
           Log.e(TAG, "disconnected", e);
           break;}
        }//true
     }
   }
}
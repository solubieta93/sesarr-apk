
package com.example.myapplication2;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public  class RegistrarActivity extends Activity {
    ListView Arreglo ;
    private static final int REQUEST_CODE=10;
    public final static String nombre="Paciente";
	private static final String TAG = null;
	private static final boolean D = false;
	private static final int REQUEST_ENABLE_BT = 2;

    //con BD y HorasRegistro 
    BD db;
	String camino="/mnt/sdcard/Android/data/BDRegistros.db";
    String  nombrepaciente=null,idpaciente=null,DirBT,conectar="2",
			soloicono="0",directorio=null,nombrefile=null,
			directoriotemp=null,
            direccionremota=null,fichAsub=null;
    //1 min
	int tiemporegistro=30000; 
	BluetoothAdapter mBluetoothAdapter=null;
	Button B3;
	boolean subiofile,esperaXclick=false,parearse=false,escribirdatos=false,tiempo=false,noactivoBT=false,
			 guardar=false,primeravez=false,deregistrar=false,nombreoculto=false,
			 botonmedico=false,borrar=false,DirectoRegistrar=false, 
			 enviandoWeb=false,	habilitadoBT=false,desconfig=false;
	 TextView B7;

   ///////////////////////////////////////////////////////////////////////////////////////		
   ///////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
		File myFile = null;
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl("https://jsonplaceholder.typicode.com/")
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
		Call<List<Post>> call = jsonPlaceHolderApi.getPosts();
		call.enqueue(new Callback<List<Post>>() {
			@Override
			public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
				if (!response.isSuccessful()) {

					return;
				}
				List<Post> posts = response.body();
				for (Post post : posts) {
					String content = "";
					content += "ID: " + post.getId() + "\n";
					content += "User ID: " + post.getUserId() + "\n";
					content += "Title: " + post.getTitle() + "\n";
					content += "Text: " + post.getText() + "\n\n";

				}
			}
			@Override
			public void onFailure(Call<List<Post>> call, Throwable t) {

			}
		});





		//SubeFile("/sdcard/ECGSUBCOMBIOMED4567/ECG4567-00.SUB");

		/////////////////////////////////////////
		//seguridad para no permitir copiar la app
		/*myFile = new File("/mnt/sdcard/Android/data/mphone.pho");
		//Comprueba si el file no existe
		if (!myFile.exists()) {
			//   enviaSMS();
			sacaAviso("Instalación Ilegal!!!!",true);
			finish();
		}
		*/




//		else {*/
//            setContentView(R.layout.main);
//
//			//con BD y HorasRegistro
//			db = new BD(this);
//			File FBD = new File(camino);
//			//Comprueba si el file no existe
//			if (!FBD.exists()) {
////			Toast.makeText(getBaseContext(), " Nuevo Paciente", Toast.LENGTH_LONG).show();
//			}
////		    else {
////			   Toast.makeText(getBaseContext(), "Ya hay Paciente", Toast.LENGTH_LONG).show();
//			   int cantreg = BD.getDatos().size();
//			   if (cantreg == 0)
////		          Toast.makeText(getBaseContext(), "BD vacia", Toast.LENGTH_LONG).show();
////		       else
////			      Toast.makeText(getBaseContext(), "BD NO vacia", Toast.LENGTH_LONG).show();
//		       //orientacion vertical de la pantalla
//			   this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//			   //boton conectar dispositivos
//			   //B7=(Button)findViewById(R.id.BBT);
//			   B7 = (TextView) findViewById(R.id.textView1);
//			   //donde voy poner nombre de equipo pareado
//			   B7.setText("");
//			   //boton configurar aplicacion
//			   B3 = (Button) findViewById(R.id.BMedico);
//		       //4-dic
//			   cargarConfiguracion();
//			   botonesalinicio();
////		    }
      }

	//para subir files al sitio http para subir por head
	///////////////////////////////////////////////////////////
	public void SubeFile(String subefichero) {
    	try {
			Retrofit retrofit = new Retrofit.Builder()
					.baseUrl("https://combiomed-api.biocubafarma.cu/")
					.addConverterFactory(GsonConverterFactory.create())
					.build();

			ApiInterface api = retrofit.create(ApiInterface.class);
			Call<AuthModel> call = api.authProof("316", "Combio@316");
			call.enqueue(new Callback<AuthModel>() {
				@Override
				public void onResponse(Call<AuthModel> call, Response<AuthModel> response) {
					if (!response.isSuccessful()) {
						subiofile = false;
					}
					String token = response.body().getToken();
					Call<String> callFile = api.uploadFile(""+subefichero);
					callFile.enqueue(new Callback<String>() {
						@Override
						public void onResponse(Call<String> call, Response<String> response) {
							if(!response.isSuccessful()){
								subiofile = false;
							}
						}

						@Override
						public void onFailure(Call<String> call, Throwable t) {
							subiofile = false;
						}
					});
				}

				@Override
				public void onFailure(Call<AuthModel> call, Throwable t) {
					subiofile = false;
				}
			});
		} catch (Exception ex) {
			subiofile = false;
		}
	}

	///////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	public void enviaSMS(){
		try {
			SmsManager smsManager = SmsManager.getDefault();
			ArrayList<String> msgArray=smsManager.divideMessage("Uso de APP Ilegal!!!!");
			smsManager.sendMultipartTextMessage("5352881756", null, msgArray, null, null);
			Toast.makeText(getApplicationContext(), "Son borrados los datos del telefono",
					Toast.LENGTH_LONG).show();
		} catch (Exception ex) {
			Toast.makeText(getApplicationContext(),
					ex.getMessage().toString(),
					Toast.LENGTH_LONG).show();
			ex.printStackTrace();
		}
	}
    ///////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////
    /*onPause: es el primer m�todo que se llama cuando la aplicaci�n se est� yendo de la pantalla.
     *  Si tenemos bucles, procesos, animaciones que deber�an estar corriendo 
     *  cuando la actividad est� en pantalla este m�todo es el id�neo para pararlos. 
     *  Este m�todo tambi�n se llama cuando lanzamos otra actividad 
     *  desde la que se est� ejecutando actualmente. 
     *  Este m�todo es importante porque puede ser el �nico en avisarnos de 
     *  que la actividad o incluso toda la aplicaci�n se est� cerrando. 
     *  En este m�todo deber�amos guardar cualquier informaci�n importante a disco,
     *   base de datos o preferencias. */
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void onStart() {
		super.onStart();
	}
    ///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void onPause() {
        /*esta variable es para no salvar aqui
	    porque ya guardo anteriormente*/
        if (guardar ){ 
	       guardarConfiguracion();
	       guardar=false;
		}
	    super.onPause();
 	}
    ///////////////////////////////////////////////////////////////////////////////////////  
    ///////////////////////////////////////////////////////////////////////////////////////		
	@Override
	public void onResume() {
	  super.onResume();
      if(primeravez) 
	     pareado();
	}
    ///////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////
    //para ir a registrar tan solo dar en el icono, RESTART VIENE DESPUES DE UN STOP
    //entra aqui cuando ya BECG se ha pareado y cada vez que va a registrar
    ///////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void onRestart() {
		super.onRestart();
		if (!deregistrar) {
			cargarConfiguracion();
			botonesalinicio();
		}
		else //para cuando me pide activar el BT y digo NO
			deregistrar=false;
    }
    ///////////////////////////////////////////////////////////////////////////////////////  
    ///////////////////////////////////////////////////////////////////////////////////////	
    /*Cuando se llama a onStop lo que sabemos es que la actividad est� oficialmente fuera de pantalla. 
    * No siginifica que la actividad se est� apagando, aunque podr�a ser. 
    * Solo se puede asumir que el usuario ha dejado tu actividad por otra. 
    * Si est�s haciendo alg�n proceso que solo deber�a estar corriendo 
    * cuando la actividad est� en ejecuci�n este es un buen momento para pararla. */
    ///////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onStop() {
      if (soloicono.equals ("2")){
          DirectoRegistrar=true;
      	  soloicono="0";
	  }
      super.onStop();
    }   
    //////////////////////////////////////////////////////////////////////////////////////		
    //////////////////////////////////////////////////////////////////////////////////////
    //esta funcion no permite que al dar la tecla back en la aplicacion, esta se destruya
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    @Override 
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
       if ((keyCode == KeyEvent.KEYCODE_BACK)  ||
			   (keyCode == KeyEvent.KEYCODE_HOME) ){
		   //resetea la app cuando doy tecla atras
		   if (!(mBluetoothAdapter==null)) {
			   if (mBluetoothAdapter.isEnabled()) {
				   mBluetoothAdapter.disable();
				   while (mBluetoothAdapter.isEnabled()) {
				   }
			   }
		   }
			   moveTaskToBack(false);
			   System.exit(0);
			   return true;
	   }
       return super.onKeyDown(keyCode, event); 
    } 
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //cuando el password es correcto
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void passOK(){
    	//B3.setVisibility(View.INVISIBLE);
		B3.setClickable(false);
		//		.setEnabled(false);
		B3.setText("Esperando pareo...");
	    if (!parearse){
         onclickBT(null);
         if (noactivoBT){
        	 botonesalinicio();
	       /*para no guardar cuando sale de esta rutina
	       porque no se hizo ningun cambio */
	       guardar=false;
		 }
	    }
	    else IUpareada();
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //saca mensaje de problema!!!!
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void sacamensaje(String sms, boolean esconde){
	   //para  ir a registrar directo, con solo click en el icono
	   soloicono="2";
       DirectoRegistrar=false;
       deregistrar=true;
       //esconder configurar
       B3.setVisibility(View.INVISIBLE);
	   sacaAviso(sms,esconde);
    }
	//////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //saca aviso!!!!
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void sacaAviso(String sms, final boolean foreg){
    	AlertDialog.Builder DialogoInfo = new AlertDialog.Builder(this); 
    	//para no poder salir del dialogo sino es por su boton
    	DialogoInfo.setCancelable(false);
    	DialogoInfo.setIcon(R.drawable.electro2);
    	DialogoInfo.setTitle("Información"); 
    	DialogoInfo.setMessage(sms); 
    	DialogoInfo.setPositiveButton("Aceptar", new OnClickListener() { 
	    public void onClick(DialogInterface dialog, int which) { 
	       dialog.cancel();
	   	   //lleva la aplicacion al background
	       if (foreg)
	    	    moveTaskToBack(true); 
	    }
	    }); 
    	DialogoInfo.show();
    }
	//////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void finish(){
		//despareo el registrador del movil
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothAdapter.enable();
		while (!mBluetoothAdapter.isEnabled()) {
		}
		Set<BluetoothDevice> pairedDevices1 = mBluetoothAdapter.getBondedDevices();
		// si tengo algun BECG pareado
		if (pairedDevices1.size() > 0) {
			for (BluetoothDevice device : pairedDevices1) {
				if (device.getName().equals("BECG"))
					unpairDevice(device);
			}
		}
		mBluetoothAdapter.disable();
		while (mBluetoothAdapter.isEnabled()) {
		}
		//no borro BD ni files asociados al paciente para no perder informacion
		//reseteo la app, pero no debo borrar ConfBECG.BECG
		//porque tuve que resetear por problemas de conexion
		// y voy a seguir con el mismo paciente
		//en ConfBECG.BECG lo que hay es el nombre del BT para ese paciente
		desconfig=true;
		DirectoRegistrar=false;
		parearse=false;
		soloicono="0";
		direccionremota=null;
		guardarConfiguracion();
		moveTaskToBack(true);
//		System.exit(0);
//		super.finish();
	}
	/////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	public void DosBotones() {
		DirectoRegistrar = false;
		soloicono = "2";
		new AlertDialog.Builder(this)
			.setTitle("Verificar encendido del registrador")
			.setMessage("- Si está apagado, enciéndalo.\n  Seleccione NO para registrar.\n\n"
					     +"- Si está encendido, falló la comunicación." +
					      " \n  Seleccione SI para resetear la aplicación")
			.setIcon(R.drawable.electro2)
			.setPositiveButton("SI",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					  		  dialog.cancel();
						      finish();

					}
			})
			.setNegativeButton("NO", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					moveTaskToBack(true);
				}
				}).show();
	}
		//////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
		if (requestCode == REQUEST_ENABLE_BT)
			if (resultCode == 0) {
				//no activo Bt
				noactivoBT = false;
				botonmedico = false;
				moveTaskToBack(true);
				B3.setVisibility(View.VISIBLE);
//				Toast.makeText(this, "Ud eligió no activar el Bluetooth", Toast.LENGTH_LONG).show();
				sacaAviso("Ud eligió no activar el Bluetooth, la aplicación no puede ejecutarse", true);
				finish();
			} else {
				//activo BT y descubrible
				DescBT();
				habilitadoBT = true;
			}
		else {
			//////////////////////////////////////////////////////////////////////////////////////
			if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
				if (data.hasExtra("EXITO")) {
					//esta variable para no guardarconfiguracion
					guardar = false;
					String ValorRecibido = data.getExtras().getString("EXITO");
					int Valor = Integer.parseInt(ValorRecibido);
					switch (Valor) {
						//////////////////////////////////////////////////////////////////////////////////////
						case 0:
							//password INCORRECTO
							B3.setVisibility(View.VISIBLE);
							B7.setVisibility(View.INVISIBLE);
							botonmedico = false;
							moveTaskToBack(true);
							break;
						//////////////////////////////////////////////////////////////////////////////////////
						case 1:
							//password correcto
							passOK();
							break;
						//////////////////////////////////////////////////////////////////////////////////////
						case 8:
							//vengo de registrar
							//incremento numero de registro para la proxima vez de registrar
							//con sonido funciona OK
							MediaPlayer reproSonido = MediaPlayer.create(RegistrarActivity.this, R.raw.fin);
							reproSonido.start();
							guardar = true;
							deregistrar = true;
							//lleva la aplicacion al background
							moveTaskToBack(true);
							DirectoRegistrar = false;
							soloicono = "2";
							break;
						//////////////////////////////////////////////////////////////////////////////////////
						case 15:
							//vengo de registrar, pero no se recibio sennal en un tiempo
							//con sonido funciona OK
							//MediaPlayer reproSonido = MediaPlayer.create(RegistrarActivity.this, R.raw.revisar);
							//reproSonido.start();
							DosBotones();
							//sacamensaje("Debe revisar encendido de BECG o esta desconfigurado  (debe desvincular a BECG)", true);
							break;
						//////////////////////////////////////////////////////////////////////////////////////
						case 16:
							//vengo de registrar, pero los electrodos estan caidos
							MediaPlayer reproSonido1 = MediaPlayer.create(RegistrarActivity.this, R.raw.elecaidos);
							reproSonido1.start();
							sacamensaje("Electrodos sueltos", true);
							break;
						//////////////////////////////////////////////////////////////////////////////////////
						case 17:
							//vengo de registrar, pero se perdio la conexion cuando estaba registrando
							MediaPlayer reproSonido2 = MediaPlayer.create(RegistrarActivity.this, R.raw.perdidacom);
							reproSonido2.start();
							sacamensaje("Se perdió la conexión con BECG", true);
							break;
						case 30:
							if (data.hasExtra("EXITO1")) {
								String ValorRecibido1 = data.getExtras().getString("EXITO1");
								//cambio el nombre BT por el identificador en web
								nombrepaciente="COMBIOMED"+ValorRecibido1;
								//para ser usado en Control Paciente
								idpaciente=ValorRecibido1;
								guardarConfiguracionApp();
								guardarConfiguracion();
								Vibrator vibrator =(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
								vibrator.vibrate(1000);
								//no necesito hacerlo cuando solo estoy cambiando de paciente
								if (!DirectoRegistrar){
									//para cuando estoy pareando
									sacaAviso("El móvil esta pareado a BECG. Espere unos segundos para Registrar", true);
								}
								else {
									sacaAviso("Puede comenzar el registro del nuevo paciente. Espere unos segundos para Registrar", true);
//18 dic									onclickregistrar(null);
//18 dic									DirectoRegistrar=false;
								}
								break;
							}
					}
				} else //resultCode diferente de RESULT_OK
					sacamensaje("Errorrrrr", true);
			}
		}
	}
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void botonesalinicio(){
       //boton Configurar Tiempo
       if (DirectoRegistrar){ 
    	  //para el caso en que apague el telefono
    	  //y voy a seguir registrando 
          B3.setVisibility(View.INVISIBLE);
			   //voy a preguntar por la BD para saber
			   //si esta vacia=> borre paciente y empiezo uno nuevo
			   //tengo que cambiar el ID y el nombre del BT
//18 dic			   int cantreg1 = BD.getDatos().size();
		   File Archivo= new File("/mnt/sdcard/Android/data/ConfBECG.BECG");
//18 dic 	      if ((cantreg1 == 0) &&  (!(Archivo.exists() ))){
//				   Toast.makeText(getBaseContext(), "Nuevo Paciente", Toast.LENGTH_LONG).show();
		   if  (!(Archivo.exists() )){
			   Intent i = new Intent(RegistrarActivity.this, entraID.class);
			   i.putExtra("IdPaciente",idpaciente);
			   startActivityForResult(i, REQUEST_CODE);
		   }
		   else {
			   onclickregistrar(null);
			   DirectoRegistrar=false;
		   }

          // 24/10 onclickregistrar(null);
	   }
       else {
           B3.setVisibility(View.VISIBLE);
           B3.setText("Parearse a BECG");
           B3.setOnClickListener(new View.OnClickListener(){
           	public void onClick(View view){
           		onclickmedico(null);}
           });
       }
    }   
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //interfaz usuario cuando ya se esta pareado
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void IUpareada(){
	   botonmedico=true;
	   onclickmedico(null);
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //interfaz usuario cuando ya se esta pareado
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void salir(){
   	  //click de salir de configuracion
      botonmedico=false;
	  soloicono="2";
      /*esta variable es para guardarconfiguracion
      de tiempo, y de pareo en pause*/ 
	  guardar=true;
	  // esconder configurar
	  B3.setVisibility(View.INVISIBLE);
      B3.setText("Parearse a BECG");
	  Intent i = new Intent(RegistrarActivity.this,entraID.class);
		i.putExtra("IdPaciente",idpaciente);
		startActivityForResult(i,REQUEST_CODE);
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //boton configurar
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void onclickmedico(View BMedico){
     	//BOTON PARA EL MEDICO DE CONTRASENNA Y SE PERMITA VER LOS DATOS DEL PACIENTE
     	if (botonmedico) 
     		salir();
     	else  {
     	  //click de configuracion
          //esta variable es para no guardarconfiguracion 
     	  //porque llama a PoderEntrar y pasa por pause de registrar
  		  guardar=false;
  	      botonmedico=true;
 //    	  Intent i = new Intent(RegistrarActivity.this,PoderEntrar.class);
 //         startActivityForResult(i,REQUEST_CODE);
			passOK();
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //boton registrar datos
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void onclickregistrar(View BRegistrar){
      /*cuando hago el movetask, ella va al start de registraractivity
      y entra en el bluetoochat, cuando esta ahi es que entra el evento stop
      hago esta pregunta para el caso en que llamo a movetask
      no entre aqui y entre al stop directamente*/
      if (!soloicono.equals("2")){
         Intent intent = new Intent(RegistrarActivity.this,BluetoothChat.class);
         String tiempo= Integer.toString(tiemporegistro);
         intent.putExtra("TiempoReg", tiempo);
         intent.putExtra("Paciente", nombrepaciente); 
         intent.putExtra("DireccionRemota",direccionremota); 
         /*esta variable es para no guardarconfiguracion 
   	     porque llama a BluetoothChat y pasa por pause de registrar*/
         guardar=false; 
         startActivityForResult(intent,REQUEST_CODE);
      }
      else 	 onStop();
    } 
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //permiso para descubrir nuestro dispositivo
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    private void ensureDiscoverable() {
      //para que no guarde configuracion al pasar por el pause de registrar
      //y entrar en la pantalla de Visible
      guardar=false; 
      //si el BT no esta activo cuando activas el descubrible se activa automaticamente el BT
      Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
      //estaba en 300 seg. y lo puse en 600 para estar mas tiempo visible
      discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
      startActivityForResult(discoverableIntent, REQUEST_ENABLE_BT);
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //para desvincular un dispositivo
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    private void unpairDevice(BluetoothDevice device) {
      try {
    	   Method m = device.getClass().getMethod("removeBond", (Class[]) null);
    	   m.invoke(device, (Object[]) null);
    	  } catch (Exception e) {
    	    Log.e(TAG, e.getMessage());
    	   }
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //poner a parpadear un texto
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void blinkText(){
       Animation anim = new AlphaAnimation(0.0f, 1.0f);
       anim.setDuration(300); //You can manage the time of the blink with this parameter
       anim.setStartOffset(20);
       anim.setRepeatMode(Animation.REVERSE);
       anim.setRepeatCount(Animation.INFINITE);
       B7.startAnimation(anim);
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //habilita y descubre dispositivo
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void DescBT() {
      //dispositivo esta descubrible y conectable
      //debo ver si hay algun BECG pareado
      boolean HAY=false;
		//nov 28-2017     nombrepaciente=mBluetoothAdapter.getName();
		String nombredispos=mBluetoothAdapter.getName();
      Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
      if (pairedDevices.size() > 0) {
        for (BluetoothDevice device : pairedDevices) {
			String pp=device.getName();
          if (device.getName().equals ("BECG"))
             HAY=true;}
      }
      if (HAY) //hay un BECG pareado
        pareado();
      else {
         //no hay ningun BECG pareado
        blinkText();
 	    B7.setVisibility(View.VISIBLE);
    	B7.setTextColor(Color.RED);
    	B7.setTextSize(20);
        B7.setText("       Buscando ...");
        //Mantener encendida la pantalla
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sacaAviso("Debe encender el Registrador y esperar por el proceso de pareo",false);
        primeravez=true;
        noactivoBT=true;
        }		  
    }	
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //ya esta pareado
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void pareado() {
    	//es importante esta espera, no quitarla!!!
    	//para dar tiempo a que se efectue el pareo
        try{
  	         Thread.sleep(8000);//2 segundos
  	    }catch(Exception ex){
  	         ex.printStackTrace();
  	         }
       // ya debe estar pareado,se busca el registrador
	   Set<BluetoothDevice> pairedDevices1 = mBluetoothAdapter.getBondedDevices();
 	   // si se pareo algun dispositivo
	   if (pairedDevices1.size() > 0) {
	     for (BluetoothDevice device : pairedDevices1) {
	     	//debo validar el becg
	        if (device.getName().equals ("BECG")){
	      	   direccionremota=device.getAddress();
	           B7.clearAnimation();
	           B7.setVisibility(View.INVISIBLE);
               //habilitar configurar
    		   //BECG se apaga solo despues del pareo
				mBluetoothAdapter.disable();
				while (mBluetoothAdapter.isEnabled()) {
				}
     		   salir();
    		   parearse=true;
    		   primeravez=false;
			}
	     }
	   }
    }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //boton activar bluetooth y  parear
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void onclickBT(View BBT){
       if (!primeravez) { 
    	   // lee Bluetooth local
    	   mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	   //no soporta el bluetooth
    	   if (mBluetoothAdapter == null) {
//              Toast.makeText(this, "Bluetooth no es soportado", Toast.LENGTH_LONG).show();
			   sacaAviso("Bluetooth no es soportado",true);
    	      finish();
    	    }
    	    else  ensureDiscoverable();
    	        /*en pruebas realizadas hay que tener el BT activo para poner visible el dispositivo
    	        inclusive si uno va por ajustes del BT, da error entrar a caracteristicas del BT, hay que
    	        desactivar el BT para poder entrar a caracteristicas
    	    	esta pregunta no la voy a hacer para que pregunte siempre, aunque este activado el BT 
    	      	 if (mBluetoothAdapter.getScanMode() !=
    	   	        BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) 
    	   	         //dispositivo no esta descubrible y conectable
    	   	  	     //con intervencion del usuario
    	   	         //para hacerlo sin intervencion del ususario debo tener acceso root en el phone
    	   	         //por lo que para cada telefono donde corra la aplicacion debo tener acceso root
    	   	         //esto es diferente para cada phone y tiene sus inconvenientes*/
    	   	        
       }
    }   
    /////////////////////////////////////////////////////////////////////////////////
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
	}
	/************************************************************************************/
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
	//guardar configuraci�n aplicaci�n Android usando SharedPreferences
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
	public void guardarConfiguracion()  {
	    SharedPreferences prefs=getSharedPreferences("ConfBECG", Context.MODE_WORLD_WRITEABLE);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString("DirBECG", direccionremota);
	    editor.putInt("CantAleer", tiemporegistro);
	    editor.putString("NombrePac", nombrepaciente);
	    editor.putBoolean("DirectoRegistrar", parearse); 
	    editor.putBoolean("Pareado", parearse);
	    editor.putBoolean("Desconfig", desconfig);
		editor.putString("IdPac",idpaciente);
		editor.commit();
	}
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //cargar configuraci�n aplicaci�n Android usando SharedPreferences
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
	  public void cargarConfiguracion() {
		  byte cont = 0, tiempo = 0;
		  String cadena = "";

		  //SharedPreferences prefs=getSharedPreferences("ConfBECG", Context.MODE_PRIVATE);
		  SharedPreferences prefs = getSharedPreferences("ConfBECG", Context.MODE_WORLD_WRITEABLE);
		  direccionremota = prefs.getString("DirBECG", direccionremota);
		  tiemporegistro = prefs.getInt("CantAleer", tiemporegistro);
		  nombrepaciente = prefs.getString("NombrePac", nombrepaciente);
		  DirectoRegistrar = prefs.getBoolean("DirectoRegistrar", DirectoRegistrar);
		  parearse = prefs.getBoolean("Pareado", parearse);
		  desconfig = prefs.getBoolean("Desconfig", desconfig);
		  idpaciente = prefs.getString("IdPac", idpaciente);
		  //////////////////////////////////////////////////
		  //////////////////////////////////////////////////
		  //LEE FILE ConfBECG que solo tiene el IdPaciente
		  //para ser usado en Comtrol Paciente
		  File Archivo = new File("/mnt/sdcard/Android/data/ConfBECG.BECG");
		  if (Archivo.exists()) {
			  try {
				  RandomAccessFile ConfBECG = new RandomAccessFile("/mnt/sdcard/Android/data/ConfBECG.BECG", "rw");
				  // Nos situamos en el byte 0 del fichero.
				  try {
					  ConfBECG.seek(0);
					  //tengo que extraer el valor de tiempo de registro por si fue modificado
		//			  tiempo = (byte) ConfBECG.read();
					  for (int i = 0; i < ConfBECG.length(); i++) {
						  char c = (char) ConfBECG.read();
						  cadena = cadena + c;
					  }
					  ConfBECG.close();
					  nombrepaciente = cadena;
				  } catch (IOException e) {
					  // TODO Auto-generated catch block
					  e.printStackTrace();
				  }
			  } catch (FileNotFoundException e) {
				  // TODO Auto-generated catch block
				  e.printStackTrace();
			  }
			  //////////////////////////////////////////////////
			  //////////////////////////////////////////////////
			  if (parearse) {
        	/*ya el BECG estaba pareado y se desconfiguro
            el BECG puede desconfigurarse porque
            el paciente quite las pilas en el momento de parearse 
            y tambien porque dejen deprimido por accidente el boton de encendido, por mas de 5 segs.
            la aplicacion da entonces que el BECG no esta encendido,a pesar de estar encendido	
            el medico debe desparear el equipo y correr la aplicacion,como no debe encontrar
            ningun BECG pareado,debe darse la posibilidad de entrar en configuracion
            para volver a parearse y no perder ningun registro anterior grabado*/	
            /*aqui habilito el BT porque cuando se apaga el telefono
            y se vuelve a encender la lista de dispositivos pareados 
            no aparece hasta que se habilta el BT, por lo que para 
            ver los dispositivos pareados habilito y desahabilito el BT
            ya despues de esta primera vez qu eel BT fue habilitado, 
            aunque este deshabilitado se tiene acceso a la lista de dispositivos pareados*/
				  mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				  mBluetoothAdapter.enable();
				  while (!mBluetoothAdapter.isEnabled()) {
				  }
				  Set<BluetoothDevice> pairedDevices1 = mBluetoothAdapter.getBondedDevices();
				  // si tengo algun BECG pareado
				  if (pairedDevices1.size() > 0) {
					  for (BluetoothDevice device : pairedDevices1) {
						  if (!device.getName().equals("BECG"))
							  cont++;
					  }
				  }//>0
				  if ((cont == pairedDevices1.size()) || (pairedDevices1.isEmpty()))
					  //no hay BECG pareado
					  parearsedenuevo();
			  }//parearse
		  }
		  else
		  //no existe file confbecg
		  //sino existe este file es que borre paciente en control paciente
		  //por lo que idpaciente debe ser null
		  //porque voy a registrar un nuevo paciente
			  idpaciente=null;
	  }
 	//////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
	public void parearsedenuevo()  {   
	if (!desconfig) {
	     desconfig=true;
	     DirectoRegistrar=false;
	     parearse=false;
         soloicono="0";
	     direccionremota=null;
	     B3.setVisibility(View.INVISIBLE);
	     B7.setText("");
	     mBluetoothAdapter.disable();
 	     while (mBluetoothAdapter.isEnabled()) {
 	     }
	     sacaAviso("Registrador desconfigurado",true);
		 finish();
	     }
   }
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //guardar configuraci�n para la otra appa
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void guardarConfiguracionApp()  {
      ////////////////////////////////////////////////////////
		//escribe file ConfBECG
		try {
			File Archivo= new File("/mnt/sdcard/Android/data/ConfBECG.BECG");
			if (!Archivo.exists()) {
				try {  //En caso de no existir
					Archivo.createNewFile();
				} catch (IOException e) {
					Toast.makeText(getBaseContext(),e.toString(), Toast.LENGTH_LONG).show();
				}
			}
			RandomAccessFile ConfBECG = new RandomAccessFile ("/mnt/sdcard/Android/data/ConfBECG.BECG", "rw");
			// Nos situamos en el byte 0 del fichero.
			try {
				ConfBECG.seek(0);
				// machacamos el nombrepaciente
				ConfBECG.writeBytes(nombrepaciente);
				ConfBECG.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


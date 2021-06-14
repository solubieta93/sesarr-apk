package com.example.myapplication2;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PoderEntrar extends Activity{
	  private EditText password;
	  private Button btnSubmit;
	  String cant;
	  
	  //////////////////////////////////////////////////////////////////////////////////////
	  //////////////////////////////////////////////////////////////////////////////////////
	  //////////////////////////////////////////////////////////////////////////////////////
	  //////////////////////////////////////////////////////////////////////////////////////
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main3);
	  }
	  //////////////////////////////////////////////////////////////////////////////////////		
	  //////////////////////////////////////////////////////////////////////////////////////
	  //esta funcion no permite que al dar la tecla back en la aplicacion, esta se destruya
	  //////////////////////////////////////////////////////////////////////////////////////
	  //////////////////////////////////////////////////////////////////////////////////////
	  @Override 
	  public boolean onKeyDown(int keyCode, KeyEvent event) { 
	    if (keyCode == KeyEvent.KEYCODE_BACK) { 
	      moveTaskToBack(true); 
	      return true; }
	    return super.onKeyDown(keyCode, event); 
	   } 
	   ///////////////////////////////////////////////////////////	  
	   ///////////////////////////////////////////////////////////	  
	   ///////////////////////////////////////////////////////////	  
       public void onclickIR(View mybutton){/*boton Ir*/  
    	  password = (EditText) findViewById(R.id.password);
    	  //esconder teclado
          InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
    	  String passUsuario=password.getText().toString();
	        if (passUsuario.equals("")) {
	        	cant="1"; }
	        else{
	        	Toast.makeText(PoderEntrar.this,"NO TIENE PERMISO PARA ACCESAR", Toast.LENGTH_LONG).show();
        	    cant="0"; } 
	        finish();
       }	 
       ///////////////////////////////////////////////////////////	  
       ///////////////////////////////////////////////////////////	  
       ///////////////////////////////////////////////////////////	  
       @Override
       public void finish(){
    	  Intent data= new Intent();
    	  data.putExtra("EXITO",cant);
    	  setResult(RESULT_OK,data);
    	  super.finish();
       }      
 }   	  
    	  
    	  



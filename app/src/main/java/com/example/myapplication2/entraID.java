package com.example.myapplication2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by mmulet on 24/08/2017.
 */
public class entraID extends Activity {
    private EditText password;
    private Button BAceptar;
    String cant;
    String passUsuario;
    boolean salir=false;
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main4);
        password = (EditText) findViewById(R.id.password);
        //esconder teclado
        Bundle recibido = getIntent().getExtras();
        passUsuario=recibido.getString("IdPaciente");
      //  if (!(passUsuario.equals(""))) {
        if (!(passUsuario==null)) {
            password.setText(passUsuario);
            finish();
        }
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(password.getWindowToken(), 0);

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
        //esconder teclado
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(password.getWindowToken(), 0);

        passUsuario=password.getText().toString();
        if (salir)
            finish();
        if (passUsuario.equals("")) {
            Toast.makeText(entraID.this, "Debe entrar el Id Paciente", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(entraID.this, "Verifique ID Paciente", Toast.LENGTH_LONG).show();
            salir=true;
        }
     }
    ///////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////
    @Override
    public void finish(){
        Intent data= new Intent();
        data.putExtra("EXITO","30");
        data.putExtra("EXITO1",passUsuario);
        setResult(RESULT_OK,data);
        super.finish();
    }

}

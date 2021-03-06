package tcc.iesgo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import tcc.iesgo.activity.R;
import tcc.iesgo.http.connection.HttpClientFactory;
import tcc.iesgo.persistence.SQLiteAdapter;

public class PresentationActivity extends Activity {

	SQLiteAdapter mySQLiteAdapter;
	HttpClient httpclient = HttpClientFactory.getThreadSafeClient();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.presentation); //Layout da activity
		
		Thread timer = new Thread(){
			@Override
			public void run(){
				try{

					sleep(3000); //Duração da apresentação
					
					//Verifica se o usuário salvou os seus dados de autenticação no celular
					
					mySQLiteAdapter = new SQLiteAdapter(getApplicationContext());
			        mySQLiteAdapter.openToRead(); //Abre para leitura

			        Cursor cursor = mySQLiteAdapter.queueAll(); //Dados salvos no banco
			        startManagingCursor(cursor);
			        
			        if(cursor.getCount() > 0){ //Caso seja encontrado algum registro
			        	
				        cursor.moveToFirst(); //Move para o primeiro registro do cursor
				        
				        //Salva o nome e a senha do registro selecionado
				        String username = cursor.getString(cursor.getColumnIndex(SQLiteAdapter.KEY_USERNAME));
				        String password = cursor.getString(cursor.getColumnIndex(SQLiteAdapter.KEY_PASSWORD));

				        mySQLiteAdapter.close(); //Fecha a conexão
				        
				        HttpPost httppost = new HttpPost(getString(R.string.url_webservice) + getString(R.string.url_authentication));

				        try {
				            // Autentica o usuário na nuvem
				            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				            nameValuePairs.add(new BasicNameValuePair("name", username));
				            nameValuePairs.add(new BasicNameValuePair("pass", password));
				            nameValuePairs.add(new BasicNameValuePair("form_id", getString(R.string.form_id)));

				            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				            HttpResponse response = httpclient.execute(httppost); //Resposta do servidor
				            
				            //Caso os dados estejam corretos, redirecona p/ o mapa, se não, p/ a tela inicial.
				            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				                Intent i = new Intent(PresentationActivity.this, MainTabActivity.class);
				                startActivity(i);
				            } else {
					    	    Intent i = new Intent(PresentationActivity.this, MainActivity.class);
					            startActivity(i);
				            }
				            	
				       } catch(IOException e) {
				    	    Intent i = new Intent(PresentationActivity.this, MainActivity.class);
				            startActivity(i);
				       }
			       } else { //Caso não exista nenhum registro, redireciona o usuário para página principal
			    	   Intent i = new Intent(getApplicationContext(), MainActivity.class);
			           startActivity(i);
			       }
			        
				}catch (InterruptedException e){
					final AlertDialog.Builder dialog = new AlertDialog.Builder(getParent());
					dialog.setTitle(getString(R.string.ad_title_error));
					dialog.setMessage(getString(R.string.ad_content_error_unexpected));
					dialog.setCancelable(false);
					dialog.setNegativeButton(getString(R.string.ad_button_negative),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,	int id) {
									finish();
								}
							});
					dialog.show();
				}
			}
		};
		timer.start(); //Inicia a thread
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	//Chamado quando a activity não é visível para o usuário
    @Override
	protected void onStop() {
        super.onStop();
        mySQLiteAdapter.close();
        PresentationActivity.this.finish();
    }
}
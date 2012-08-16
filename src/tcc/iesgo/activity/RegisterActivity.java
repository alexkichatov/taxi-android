package tcc.iesgo.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import tcc.iesgo.activity.R;
import tcc.iesgo.persistence.SQLiteAdapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	
	Spinner language;
	EditText inputName;
	EditText inputEmail;
	EditText inputCpf;
	EditText inputPhone;
	EditText inputPassword;
	TextView registerErrorMsg;
	Button btnRegister;
	ImageButton btnHelp;
	
	TextView textLang;
	TextView textName;
	TextView textEmail;
	TextView textCpf;
	TextView textPhone;
	TextView textPassword;
	
	SQLiteAdapter mySQLiteAdapter;
	HttpClient httpclient = new DefaultHttpClient();
	
	ProgressDialog progressDialog;
	
	ArrayAdapter<CharSequence> adapter;
	ArrayAdapter<CharSequence> adapter2;
	
	private Integer listNum = 0;
	private String lang = "pt";

	//Verifica se o e-mail informado é válido
	public final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
			  "[a-zA-Z0-9+._%-+]{1,256}" + "@"
			+ "[a-zA-Z0-9][a-zA-Z0-9-]{0,64}" + "(" + "."
			+ "[a-zA-Z0-9][a-zA-Z0-9-]{0,25}" + ")+");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.register); //Layout da Activity

		//Instância dos componentes do layout
		language = (Spinner) findViewById(R.id.sp_lang);
		inputName = (EditText) findViewById(R.id.et_name);
		inputName.setHint(getString(R.string.register_name_description));
		inputEmail = (EditText) findViewById(R.id.et_email);
		inputEmail.setHint(getString(R.string.register_email_description));
		inputCpf = (EditText) findViewById(R.id.et_cpf);
		inputCpf.setHint(getString(R.string.register_cpf_description));
		inputPhone = (EditText) findViewById(R.id.et_phone);
		inputPhone.setHint(getString(R.string.register_phone_description));
		inputPassword = (EditText) findViewById(R.id.et_pw);
		btnRegister = (Button) findViewById(R.id.bt_register);
		btnHelp = (ImageButton) findViewById(R.id.ib_help);
		registerErrorMsg = (TextView) findViewById(R.id.tv_error);
		textLang = (TextView) findViewById(R.id.tv_lang);
		textName = (TextView) findViewById(R.id.tv_name);
		textEmail = (TextView) findViewById(R.id.tv_email);
		textCpf = (TextView) findViewById(R.id.tv_cpf);
		textPhone = (TextView) findViewById(R.id.tv_phone);
		textPassword = (TextView) findViewById(R.id.tv_pw);
		
		//Inicializa o Spinner
		adapter = ArrayAdapter.createFromResource(this, R.array.spinner_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		language.setAdapter(adapter);
		
		if (Locale.getDefault().toString().equals("pt_BR"))
			language.setSelection(0);
		else if (Locale.getDefault().toString().equals("en_US"))
			language.setSelection(1);
		else if (Locale.getDefault().toString().equals("es_ES"))
			language.setSelection(2);
		
		//Botão de ajuda
		btnHelp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(), HelpActivity.class);
				startActivity(i);
			}
		});
	
		//Botão de registro
		btnRegister.setOnClickListener(new View.OnClickListener() {
			//Verifica se todos os campos foram preenchidos corretamente
			@Override
			public void onClick(View view) {
				String[] data = {inputName.getText().toString(), inputEmail.getText().toString(), inputCpf.getText().toString(),
						 inputPhone.getText().toString(), inputPassword.getText().toString()};
				
				if (inputName.getText().toString().length() <= 3)
					Toast.makeText(getApplicationContext(), getString(R.string.register_error_name), Toast.LENGTH_SHORT).show();
				else if(!checkEmail(inputEmail.getText().toString()))
					Toast.makeText(getApplicationContext(), getString(R.string.register_error_email), Toast.LENGTH_SHORT).show();
				else if(!validateCpf(inputCpf.getText().toString()))
					Toast.makeText(getApplicationContext(), getString(R.string.register_error_cpf), Toast.LENGTH_SHORT).show();
				else if(inputPhone.getText().toString().length() < 8)
					Toast.makeText(getApplicationContext(), getString(R.string.register_error_phone), Toast.LENGTH_SHORT).show();
				else if(inputPassword.getText().toString().length() < 5)
					Toast.makeText(getApplicationContext(), getString(R.string.register_error_pw), Toast.LENGTH_SHORT).show();
				else
					register(data);
			}
		});
		
		//Caso o idioma seja trocado
		language.setOnItemSelectedListener(new OnItemSelectedListener(){
			  @Override
			  public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				  
				  lang = parentView.getItemAtPosition(position).toString();
				  Locale appLoc = null;
			
				  //Troca o idioma local do aplicativo pela selecionada
				  if(lang.equals("Português") || lang.equals("Portuguese") || lang.equals("Portugués"))
				    	appLoc = new Locale("pt_BR");
				  else   if(lang.equals("Inglês") || lang.equals("English") || lang.equals("Inglés"))
				    	appLoc = new Locale("en_US");
				  else if(lang.equals("Espanhol") || lang.equals("Spanish") || lang.equals("Español"))
				    	appLoc = new Locale("es_ES");
				  else 
					  	appLoc = new Locale(Locale.getDefault().toString());
				  
				  lang = appLoc.toString();
				  
				  //Se o idioma corrente for diferente do selecionado
				  if (!Locale.getDefault().toString().equals(appLoc.toString()) && listNum > 0) {
				    	Locale.setDefault(appLoc);
				    	Configuration appConfig = new Configuration();
				    	appConfig.locale = appLoc;
				    	getBaseContext().getResources().updateConfiguration(appConfig,
						getBaseContext().getResources().getDisplayMetrics());
				    	//Efetua as alterações dos textos, botões etc.
				    	textLang.setText(R.string.register_lang);
				    	textName.setText(R.string.register_name);
					    textEmail.setText(R.string.register_email);
					    textCpf.setText(R.string.register_cpf);
					    textPhone.setText(R.string.register_phone);
					    textPassword.setText(R.string.register_pw);
					    btnRegister.setText(R.string.register_button);
					    
						adapter2 = ArrayAdapter.createFromResource(parentView.getContext(), R.array.spinner_array,	android.R.layout.simple_spinner_item);
						adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						language.setAdapter(adapter2);
						language.setSelection(position); //Obrigatório
				  }
				  listNum++;
		      }
		   
			  @Override
		      public void onNothingSelected(AdapterView<?> arg0)
		      {
		        // TODO Auto-generated method stub
		      }
		});	
	}
		
	
	public void register(final String[] data) {
		
		progressDialog = ProgressDialog.show(RegisterActivity.this, 
				getString(R.string.pd_title), getString(R.string.pd_content));
		
		new Thread() {
			@Override
			public void run() {
				try {
					// Autentica como admin
					HttpPost httppost = new HttpPost(getString(R.string.url_authentication));
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
					nameValuePairs.add(new BasicNameValuePair("name", getString(R.string.login_name)));
					nameValuePairs.add(new BasicNameValuePair("pass", getString(R.string.login_pass)));
					nameValuePairs.add(new BasicNameValuePair("form_id", getString(R.string.form_id_login)));
					try {
							//Executa a requisição
							httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
							httpclient.execute(httppost);
							//HttpResponse rp = httpclient.execute(post);
						    //if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
							//resultCod = EntityUtils.toString(rp.getEntity());

					} catch (IOException e) {
							//Servidor fora do ar
							registerErrorMsg.setText(getString(R.string.register_error_off));		
					}
					//Cria usuario	
	
					HttpPost post = new HttpPost(getString(R.string.url_create_user));
					List<NameValuePair> userValuePairs = new ArrayList<NameValuePair>(2);
					
					userValuePairs.add(new BasicNameValuePair("name", data[0]));
					userValuePairs.add(new BasicNameValuePair("pass", data[4]));
					userValuePairs.add(new BasicNameValuePair("email", data[1]));
					userValuePairs.add(new BasicNameValuePair("form_id", getString(R.string.form_id_new)));
	
					try {
						//Salva usuário na nuvem
						post.setEntity(new UrlEncodedFormEntity(userValuePairs));
						HttpResponse rp = httpclient.execute(post);
						String user = EntityUtils.toString(rp.getEntity());
						
						//Salva usuário no DB do Aplicativo
						mySQLiteAdapter = new SQLiteAdapter(getApplicationContext());
						mySQLiteAdapter.openToWrite();
						mySQLiteAdapter.insert(user, data[4], lang);
	
						mySQLiteAdapter.close();
		
						gotoMap(); //Abre o mapa
	
					} catch (IOException e) {
						registerErrorMsg.setText(getString(R.string.register_error_off));							
					}
			
				} catch (Exception e) {
					registerErrorMsg.setText(getString(R.string.register_error_off));
				}
				progressDialog.dismiss();
			}
		}.start();
	}
	
	private void gotoHome() {
		Intent i = new Intent(getApplicationContext(), MainActivity.class);
		startActivity(i);
	}
	
	private void gotoMap() {
		Intent i = new Intent(getApplicationContext(), ClientMapActivity.class);
		startActivity(i);
	}
	
    public boolean validateCpf(String cpfNum) {
    	try {
	        int[] cpf = new int[cpfNum.length()]; //Define o valor com o tamanho da string  
	        int resultP = 0;  
	        int resultS = 0;  
	        
	        //Converte a string para um array de integer  
	        for (int i = 0; i < cpf.length; i++) {  
	            cpf[i] = Integer.parseInt(cpfNum.substring(i, i + 1));  
	        }  
	  
	        //Calcula o primeiro número(DIV) do cpf  
	        for (int i = 0; i < 9; i++) {  
	            resultP += cpf[i] * (i + 1);  
	        }  
	        int divP = resultP % 11;
	  
	        //Se o resultado for diferente ao 10º digito do cpf retorna falso  
	        if (divP != cpf[9]) { 
	            return false;  
	        } else {  
	            //Calcula o segundo número(DIV) do cpf  
	            for (int i = 0; i < 10; i++) {  
	                resultS += cpf[i] * (i);  
	            }  
	            int divS = resultS % 11;  
	  
	            //Se o resultado for diferente ao 11º digito do cpf retorna falso  
	            if (divS != cpf[10]) {  
	                return false;  
	            }  
	        }
    	} catch (Exception e) {
			return false;
		}
        return true;  
    }

	
    public boolean checkEmail(String email) {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }
    
	@Override
	public void onBackPressed() {
		gotoHome();
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}

	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}
}
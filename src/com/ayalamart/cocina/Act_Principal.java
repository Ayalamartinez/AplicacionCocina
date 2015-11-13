package com.ayalamart.cocina;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.ayalamart.adapter.PostAdapter;
import com.ayalamart.adapter.PostData;

import com.ayalamart.util.AppController;
import com.shephertz.app42.paas.sdk.android.App42API;
import com.shephertz.app42.paas.sdk.android.App42CallBack;
import com.shephertz.app42.paas.sdk.android.App42Log;
import com.shephertz.app42.paas.sdk.android.App42Response;
import com.shephertz.app42.push.plugin.App42GCMController;
import com.shephertz.app42.push.plugin.App42GCMService;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class Act_Principal extends ListActivity {

	//	static final int DIALOG_CONFIRM = 0;
	protected static final int REQUEST_CODE = 10;
	private PostAdapter adapter;
	ArrayList<PostData> data;
	private Button btMarkRead;
	private static String TAG = Act_Principal.class.getSimpleName();
	private ProgressDialog pDialog;
	String fecha_ped = null; 
	String idpedido = null; 
	String plato_ped= null; 
	String idplato_ped = null; 
	private static final String GoogleProjectNo = "913405012262";
	JSONObject datocliente;
	String url_pedidos = "http://10.10.0.99:8080/Restaurante/rest/pedido/getPedidosAll"; 
	String url_borrarpedido = "http://10.10.0.99:8080/Restaurante/rest/pedido/updateEstatusPedido/"; 
	@Override
	public void onCreate( final Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_list);
		pDialog = new ProgressDialog(this); 
		pDialog.setMessage("Cargando...");
		pDialog.show(); 

		App42API.initialize(
				this,
				"f63ba0f76db8e93c8b85f4f140c7adc539965eced4adb0dd055ea37df5a34ff7",
				"cfef88516cd746711b09dc2040995ad813ddba1abeacea658a7659470abdfbfe");
		App42Log.setDebug(true);
		App42API.setLoggedInUser("APPCOCINA");

		// JSONObject datocliente = new JSONObject(); 
		btMarkRead = (Button) findViewById(R.id.btAnular);
		btMarkRead.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (adapter.haveSomethingSelected())
				{
					pedidolisto(); 
					adapter.cancelSelectedPost();
				}
				else 
					Toast.makeText(getApplicationContext()
							,R.string.no_post_selected, Toast.LENGTH_LONG)
					.show();
			}
		});

		Calendar rightnow =Calendar.getInstance();
		SimpleDateFormat fechaact = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		String fecha = fechaact.format(rightnow.getTime());
		data = new ArrayList<PostData>();
		
		llenarLV(savedInstanceState); 
		
		final Handler h = new Handler();
		final int delay = 20000; //milliseconds

		h.postDelayed(new Runnable(){
		    public void run(){
		    	llenarLV(savedInstanceState); 
		        h.postDelayed(this, delay);
		    }
		}, delay);
	}
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		adapter.setCheck(position);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_post_list, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.cancel_post:
			if (adapter.haveSomethingSelected())
			{pedidolisto(); 
			adapter.cancelSelectedPost();}

			else 
				Toast.makeText(getApplicationContext()
						,R.string.no_post_selected, Toast.LENGTH_LONG)
				.show();
			break;
		case R.id.select_all:
			adapter.checkAll(true);
			adapter.notifyDataSetChanged();
			break;

		case R.id.unselect_all:
			adapter.checkAll(false);
			adapter.notifyDataSetChanged();
			break;
		default:
			break;
		}

		return true;
	}
	public void llenarmenu(Bundle savedInstanceState){
		if (savedInstanceState == null){
			adapter = new PostAdapter(Act_Principal.this, data);

		} else{
			data = savedInstanceState.getParcelableArrayList("savedData");
			adapter = new PostAdapter(Act_Principal.this, data);
			hidepDialog();
		}
		setListAdapter(adapter);
	}
	public void llenarLV(final Bundle savedInstanceState){
		JsonArrayRequest pedidosReq = new JsonArrayRequest(url_pedidos, new Response.Listener<JSONArray>(){
			//	JsonObjectRequest pedidosReq = new JsonObjectRequest(Method.GET, 
			//		url_pedidos, null, new Response.Listener<JSONObject>() {
			private ArrayList<String> datopedido = new ArrayList<String>();
			private Object statuspedido;
			int k_cant_ped = 0;
			
			@Override
			public void onResponse(JSONArray response) {

				// TODO Auto-generated method stub
				Log.d(TAG, response.toString()); 
				String interm = response.toString(); 
				try {
					Log.d(TAG + "chequeo", interm); 
					for (int i = 0; i < response.length(); i++) { 

						final JSONObject objetosDetalle = (JSONObject)response.get(i); 
						

						final JSONArray obj_detalle1 = objetosDetalle.getJSONArray("detalles"); 
						for (int k = 0; k < obj_detalle1.length(); k++) {
							String cantidad_platos = obj_detalle1.getJSONObject(k).getString("cant");

							Log.d(TAG, cantidad_platos.toString()); 	

							Log.d(TAG, obj_detalle1.getJSONObject(k).getString("pedido")); 
							JSONObject objpedido = new JSONObject(obj_detalle1.getJSONObject(k).getString("pedido")); 
							fecha_ped = objpedido.getString("fechapedido"); 
							idpedido =objpedido.getString("idpedido"); 
							statuspedido = objpedido.get("estatus"); 
							
							datocliente =  (JSONObject) objpedido.get("cliente"); 
							


							Log.d(TAG, obj_detalle1.getJSONObject(k).getString("plato")); 
							JSONObject objPlato = new JSONObject(obj_detalle1.getJSONObject(k).getString("plato")); 
							plato_ped = objPlato.getString("nomplato");
							idplato_ped = objPlato.getString("idplato"); 
							if (statuspedido.toString().equals("1")) {
								datopedido.add(plato_ped + "(" + cantidad_platos + ")"); 
							}

						}
						if (!datopedido.isEmpty()) {
							for (int j = 0; j < data.size(); j++) {
							if (!data.get(j).getidPedido().equals(idpedido)) {
							k_cant_ped++; 
							}}
							if (k_cant_ped == data.size()) {
								String pedidocompleto = datopedido.toString(); 
								data.add(new PostData(fecha_ped, pedidocompleto, idpedido , false));
								datopedido.clear();
							}
						}
	
					}} catch (JSONException e) {
						e.printStackTrace();
					}
				hidepDialog(); 
				llenarmenu(savedInstanceState); 
			}


		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error){
				VolleyLog.d(TAG, "Error:" + error.getMessage());
				hidepDialog(); 
			}
		});
		AppController.getInstance().addToRequestQueue(pedidosReq);
		
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList("savedData", data);
		super.onSaveInstanceState(outState);
	}
	

	public void onApp42Response(final String responseMessage) {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//responseTv.setText(responseMessage);
			}
		});
	}
	public void onRegisterApp42(final String responseMessage) {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//responseTv.setText(responseMessage);
				App42GCMController.saveRegisterationSuccess(Act_Principal.this);
			}
		});
	}
	private void pedidolisto(){
		Log.d(TAG, "borrando pedido"); 
		for (int i = 0; i < data.size(); i++){
			if (data.get(i).getChecked()){
				showpDialog();
				Log.d(TAG, "posicion del pedido" + "/" +  i + "/" + "5"); 
				String urldef = url_borrarpedido + data.get(i).getidPedido() + "/" + "5"; 
				JSONObject pedidolisto = new JSONObject(); 
				String messagestr = "Su orden esta lista"; 
				
				
				try {
					if (datocliente.has("correo") && datocliente.has("cedula")) {
						
						String correocliente = (String) datocliente.get("correo");
						String cedulacliente_s = (String) datocliente.get("cedula");
						String cedulacliente = cedulacliente_s.substring(3); 
						
						Log.d(TAG, "pruebadedata"+ correocliente); 
						String user = correocliente + "_" + cedulacliente; 
						App42API.buildPushNotificationService().sendPushMessageToUser(user,
		                        messagestr, new App42CallBack() {
		                        
		                        @Override
		                        public void onSuccess(Object arg0) {
		                            // TODO Auto-generated method stub
		                            App42Response response=(App42Response) arg0;
		                            onApp42Response(response.getStrResponse());
		                        }
		                        
		                        @Override
		                        public void onException(Exception arg0) {
		                            // TODO Auto-generated method stub
		                            onApp42Response(arg0.getMessage());
		                        }
		                    });
					}

				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				
				
				try {
					pedidolisto.put("idpedido", data.get(i).getidPedido());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST, 
						urldef, pedidolisto, null , new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.d(TAG, "pedido no borrado"); 
					}});
				AppController.getInstance().addToRequestQueue(jsonObjReq);
				hidepDialog();
			}	}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (App42GCMController.isPlayServiceAvailable(this)) {
			App42GCMController.getRegistrationId(Act_Principal.this,
					GoogleProjectNo);

		} else {
			Log.i("App42PushNotification",
					"No valid Google Play Services APK found.");
		}
	}
	public void onStop() {
		super.onStop();
		App42GCMService.isActivtyActive = false;
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		App42GCMService.isActivtyActive = false;
		hidepDialog();
	}
	public void onReStart() {
		super.onRestart();

	}
	public void onResume() {
		super.onResume();
		String message = getIntent().getStringExtra(
				App42GCMService.ExtraMessage);
		if (message != null)
			Log.d("MainActivity-onResume", "Message Recieved :" + message);
		IntentFilter filter = new IntentFilter(
				App42GCMService.DisplayMessageAction);
		filter.setPriority(2);
		registerReceiver(mBroadcastReceiver, filter);

	}
	final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String message = intent
					.getStringExtra(App42GCMService.ExtraMessage);
			Log.i("MainActivity-BroadcastReceiver", "Message Recieved " + " : "
					+ message);
		}
	};

	public void onGCMRegistrationId(String gcmRegId) {
		// TODO Auto-generated method stub
		//responseTv.setText("Registration Id on GCM--" + gcmRegId);
		App42GCMController.storeRegistrationId(this, gcmRegId);
		if(!App42GCMController.isApp42Registerd(Act_Principal.this))
			App42GCMController.registerOnApp42(this, gcmRegId, App42API.getLoggedInUser());
	}
	
	
	private void showpDialog() {
		if (!pDialog.isShowing())
			pDialog.show();
	}
	private void hidepDialog() {
		if (pDialog.isShowing())
			pDialog.dismiss();
	}
}

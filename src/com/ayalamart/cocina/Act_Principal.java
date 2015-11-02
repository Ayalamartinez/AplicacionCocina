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


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
	String url_pedidos = "http://10.10.0.99:8080/Restaurante/rest/pedido/getPedidosAll"; 
	String url_borrarpedido = "http://10.10.0.99:8080/Restaurante/rest/pedido/updateEstatusPedido/"; 
	@Override
	public void onCreate( final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_list);
		pDialog = new ProgressDialog(this); 
		pDialog.setMessage("Cargando...");
		pDialog.show(); 

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


		JsonArrayRequest pedidosReq = new JsonArrayRequest(url_pedidos, new Response.Listener<JSONArray>(){
			//	JsonObjectRequest pedidosReq = new JsonObjectRequest(Method.GET, 
			//		url_pedidos, null, new Response.Listener<JSONObject>() {
			private ArrayList<String> datopedido = new ArrayList<String>();
			private Object statuspedido;


			@Override
			public void onResponse(JSONArray response) {

				// TODO Auto-generated method stub
				Log.d(TAG, response.toString()); 
				String interm = response.toString(); 
				try {
					// JSONObject obj_interm = new JSONObject(interm); 
					Log.d(TAG + "chequeo", interm); 
					//PC

					for (int i = 0; i < response.length(); i++) { 

						final JSONObject objetosDetalle = (JSONObject)response.get(i); 

						//objDetalle
						final JSONArray obj_detalle1 = objetosDetalle.getJSONArray("detalles"); 
						for (int k = 0; k < obj_detalle1.length(); k++) {
							String cantidad_platos = obj_detalle1.getJSONObject(k).getString("cant");

							Log.d(TAG, cantidad_platos.toString()); 	

							Log.d(TAG, obj_detalle1.getJSONObject(k).getString("pedido")); 
							JSONObject objpedido = new JSONObject(obj_detalle1.getJSONObject(k).getString("pedido")); 
							fecha_ped = objpedido.getString("fechapedido"); 
							idpedido =objpedido.getString("idpedido"); 
							statuspedido = objpedido.get("estatus"); 


							Log.d(TAG, obj_detalle1.getJSONObject(k).getString("plato")); 
							JSONObject objPlato = new JSONObject(obj_detalle1.getJSONObject(k).getString("plato")); 
							plato_ped = objPlato.getString("nomplato");
							idplato_ped = objPlato.getString("idplato"); 
							if (statuspedido.toString().equals("1")) {
								datopedido.add(plato_ped + "(" + cantidad_platos + ")"); 
							}

						}
						if (!datopedido.isEmpty()) {
							String pedidocompleto = datopedido.toString(); 
							data.add(new PostData(fecha_ped, pedidocompleto, idpedido , false));
							datopedido.clear();
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
	/*	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_CONFIRM:
			dialog = createConfirmDialog();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}*/
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

	/*public Dialog createConfirmDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.dialog_confirm_cancel_post)
		.setCancelable(false)
		.setPositiveButton(R.string.dialog_confirm_cancel_post_yes,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {


			}
		})
		.setNegativeButton(R.string.dialog_confirm_cancel_post_no,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		return builder.create();
	}

	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList("savedData", data);
		super.onSaveInstanceState(outState);
	}

	private void pedidolisto(){
		Log.d(TAG, "borrando pedido"); 
		for (int i = 0; i < data.size(); i++){
			if (data.get(i).getChecked()){
				showpDialog();
				Log.d(TAG, "posicion del pedido" + "/" +  i + "/" + "5"); 
				String urldef = url_borrarpedido + data.get(i).getidPedido() + "/" + "5"; 
				JSONObject pedidolisto = new JSONObject(); 
				try {
					pedidolisto.put("idpedido", i);
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
				/*StringRequest stringRequest = new StringRequest(Request.Method.POST, urldef,
							new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
							Log.d(TAG, "pedido borrado"); 
						}
					}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							Log.d(TAG, "pedido no borrado"); 
						}
					});*/
				AppController.getInstance().addToRequestQueue(jsonObjReq);
				hidepDialog();
			}	}
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		hidepDialog();
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

package com.ayalamart.cocina;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
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

	static final int DIALOG_CONFIRM = 0;
	protected static final int REQUEST_CODE = 10;

	private PostAdapter adapter;
	private ArrayList<PostData> data;
	private Button btMarkRead;
	private static String TAG = Act_Principal.class.getSimpleName();
	private ProgressDialog pDialog;
	String fecha_ped = null; 
	String idpedido = null; 
	String plato_ped= null; 
	String idplato_ped = null; 
	private String url_pedidos = "//http://10.10.0.99:8080/Restaurante/rest/pedido/getPedidosAll"; 
	private String url_borrarpedido = "//http://10.10.0.99:8080/Restaurante/rest/pedido/deletePedido/"; 

	@Override
	public void onCreate( Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_post_list);

		pDialog = new ProgressDialog(this); 
		pDialog.setMessage("Cargando...");
		pDialog.show(); 

		btMarkRead = (Button) findViewById(R.id.btAnular);
		btMarkRead.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View arg0) {
				if (adapter.haveSomethingSelected())
					showDialog(DIALOG_CONFIRM);
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
		if (savedInstanceState == null){
			
			JsonArrayRequest pedidosReq = new JsonArrayRequest(url_pedidos, new Response.Listener<JSONArray>(){

				private ArrayList<String> datopedido;

				@Override
				public void onResponse(JSONArray response) {
					datopedido = null; 

					// TODO Auto-generated method stub
					Log.d(TAG, response.toString()); 
					try {
						//PC
						for (int i = 0; i < response.length(); i++) { 

							final JSONArray objetosDetalle = response.getJSONArray(i); 


							//objDetalle
							final JSONArray objDETALLE = objetosDetalle.getJSONArray(1); 

							for (int k = 0; k < objDETALLE.length(); k++) {
								String cantidad_platos = objDETALLE.getJSONObject(k).getString("cant");
								if (objDETALLE.getJSONObject(k).getString("pedido")!=null) {
									JSONObject objpedido = new JSONObject(objDETALLE.getJSONObject(k).getString("pedido")); 
									fecha_ped = objpedido.getString("fechapedido"); 
									idpedido =objpedido.getString("idPedido"); 
								}
								if (objDETALLE.getJSONObject(k).getString("plato")!= null) {
									JSONObject objPlato = new JSONObject(objDETALLE.getJSONObject(k).getString("plato")); 
									plato_ped = objPlato.getString("nomplato");
									idplato_ped = objPlato.getString("idplato"); 
								}	

								datopedido.add(plato_ped + " , "); 
							}
							String pedidocompleto = datopedido.toString(); 

							data.add(new PostData(fecha_ped, pedidocompleto , false));


						}} catch (JSONException e) {
							e.printStackTrace();
						}


				}

			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error){
					VolleyLog.d(TAG, "Error:" + error.getMessage());
					hidepDialog(); 
				}
			});
			adapter = new PostAdapter(Act_Principal.this, data);
			AppController.getInstance().addToRequestQueue(pedidosReq);
		} else{
			data = savedInstanceState.getParcelableArrayList("savedData");
			adapter = new PostAdapter(Act_Principal.this, data);
		}
		setListAdapter(adapter);


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
				showDialog(DIALOG_CONFIRM);
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

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_CONFIRM:
			dialog = createConfirmDialog();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	public Dialog createConfirmDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.dialog_confirm_cancel_post)
		.setCancelable(false)
		.setPositiveButton(R.string.dialog_confirm_cancel_post_yes,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				adapter.cancelSelectedPost();
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


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList("savedData", data);
		super.onSaveInstanceState(outState);
	}




	private void pedidolisto(String status, String idpedido){
		if(status.equals("1")){
			String url_Ped_i = url_borrarpedido + idpedido; 
			StringRequest requestD = new StringRequest(Method.POST, url_Ped_i, new Response.Listener<String>() {

				@Override
				public void onResponse(String response) {
					Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
				}
			}, new Response.ErrorListener() {

				@Override
				public void onErrorResponse(VolleyError arg0) {
					// TODO Auto-generated method stub

				}
			}); 
		}
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

package com.ayalamart.adapter;

import android.os.Parcel;
import android.os.Parcelable;

public class PostData implements Parcelable {

	private String fecha_publicacion;
	private String titulo;
	private boolean leido;

	public PostData(String fecha, String equip, boolean checked) {
		this.fecha_publicacion = fecha;
		this.titulo = equip;
		this.leido = checked;

	}
	
	public PostData(Parcel in){
		this.titulo= in.readString();
		this.fecha_publicacion = in.readString();
		this.leido = in.readInt() == 1 ? true:false;
	}

	public void setFecha(String fecha) {
		this.fecha_publicacion = fecha;
	}

	public String getFecha() {
		return fecha_publicacion;
	}

	public void setChecked(boolean value) {
		this.leido = value;
	}

	public boolean getChecked() {
		return leido;
	}

	public String getEquipos() {
		return titulo;
	}

	public void setEquipos(String equipos) {
		this.titulo = equipos;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getEquipos());
		dest.writeString(getFecha());
		dest.writeInt(getChecked() ? 1 : 0);
	}

	public static final Parcelable.Creator<PostData> CREATOR = new Parcelable.Creator<PostData>() {
		public PostData createFromParcel(Parcel in) {
			return new PostData(in);
		}

		public PostData[] newArray(int size) {
			return new PostData[size];
		}
	};
}

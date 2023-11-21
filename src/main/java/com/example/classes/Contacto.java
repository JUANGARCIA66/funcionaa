package com.example.classes;

/**
 * Contacto
 * 
 * usuario
 * nombre
 * telefono
 * edad
 * 
 * @since 2022-01-25
 * @author Amadeo
 */
public class Contacto {

	private String usuario;
	private String nombre;
	private String telefono;
	private int edad;

	/**
	 * Constructor
	 * 
	 * @param usuario
	 * @param nombre
	 * @param telefono
	 * @param edad
	 */
	public Contacto(String usuario, String nombre, String telefono, int edad) {
		this.usuario = usuario.trim();
		this.nombre = nombre.trim();
		this.telefono = telefono.trim();
		this.edad = edad;
	}

	/**
	 * @return the usuario
	 */
	public String getUsuario() {
		return usuario;
	}

	/**
	 * @param usuario the usuario to set
	 */
	public void setUsuario(String usuario) {
		this.usuario = usuario.trim();
	}

	/**
	 * @return the nombre
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * @param nombre the nombre to set
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre.trim();
	}

	/**
	 * @return the telefono
	 */
	public String getTelefono() {
		return telefono;
	}

	/**
	 * @param telefono the telefono to set
	 */
	public void setTelefono(String telefono) {
		this.telefono = telefono.trim();
	}

	/**
	 * @return the edad
	 */
	public int getEdad() {
		return edad;
	}

	/**
	 * @param edad the edad to set
	 */
	public void setEdad(int edad) {
		this.edad = edad;
	}

	@Override
	public String toString() {
		return "Contacto [usuario=" + usuario + ", nombre=" + nombre + ", telefono=" + telefono + ", edad=" + edad
				+ "]";
	}
}

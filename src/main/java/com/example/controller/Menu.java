package com.example.controller;

import java.io.IOException;

import com.example.io.IO;
import com.example.model.Agenda;
import com.example.classes.Contacto;

/**
 * Menú de la agenda
 * 
 * @since 2022-01-25
 * @author Amadeo
 */
public class Menu {

	/**
	 * Agenda a gestionar
	 */
	private Agenda a;

	/**
	 * Constructor
	 * 
	 * @param a
	 * @throws IOException
	 */
	public Menu(Agenda a) throws IOException {
		this.a = a;
		while (menu())
			;
	}

	/**
	 * Interfaz de alta
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean alta() throws IOException {
		IO.println("Usuario ? ");
		String usuario = IO.readStringNotBlank();
		IO.println("Nombre ? ");
		String nombre = IO.readStringNotBlank();
		IO.println("Tfno ? ");
		String telefono = IO.readStringNotBlank();
		IO.println("Edad ? ");
		int edad = IO.readInt();
		Contacto c = new Contacto(usuario, nombre, telefono, edad);
		return a.create(c);
	}

	/**
	 * Interfaz de baja
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean baja() throws IOException {
		IO.println("Usuario a borrar ? ");
		String usuario = IO.readStringNotBlank();
		return a.delete(usuario);
	}

	/**
	 * Interfaz de modificación
	 * No se puede modificar el campo usuario
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean modifica() throws IOException {
		IO.println("Usuario a modificar ? ");
		String usuario = IO.readStringNotBlank();
		Contacto c = a.read(usuario);
		if (c == null) {
			return false;
		}
		String nombreAntiguo = c.getUsuario();
		IO.println("Nombre [" + c.getNombre() + "] ? ");
		String nombre = IO.readString();
		c.setNombre(nombre.isBlank() ? c.getNombre() : nombre);
		IO.println("Tfno [" + c.getTelefono() + "] ? ");
		String telefono = IO.readString();
		c.setTelefono(telefono.isBlank() ? c.getTelefono() : telefono);
		IO.println("Edad [0= " + c.getEdad() + "] ? ");
		int edad = IO.readInt();
		c.setEdad(edad == 0 ? c.getEdad() : edad);

		return a.update(c, nombreAntiguo);
	}

	/**
	 * Interfaz de consulta
	 * 
	 * @return
	 * @throws IOException
	 */
	private String consulta() throws IOException {
		IO.println("Usuario a buscar ? ");
		String usuario = IO.readStringNotBlank();
		Contacto c = a.read(usuario);
		return c == null ? null : c.toString();
	}

	/**
	 * Menú de opciones (controlador)
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean menu() throws IOException {
		IO.print("Alta|Baja|Modifica|Consulta|Listado|Todo|Salir");
		switch (IO.readUpperChar()) {
			case 'A':
				if (alta()) {
					IO.println("Nuevo usuario dado de alta");
				} else {
					IO.println("No se ha podido dar de alta a un nuevo usuario");
				}
				break;
			case 'B':
				if (baja()) {
					IO.println("Usuario dado de baja");
				} else {
					IO.println("No se ha podido dar de baja al usuario");
				}
				break;
			case 'M':
				if (modifica()) {
					IO.println("Usuario modificado");
				} else {
					IO.println("No se ha podido modificar al usuario");
				}
				break;
			case 'C':
				String c = consulta();
				if (c == null) {
					IO.println("Usuario no encontrado");
				} else {
					IO.println(c);
				}
				break;
			case 'L':
				IO.println(a.list());
				break;

			case 'S':
				return false;
			default:
				IO.println("Seleccione una opcion correcta");
		}

		return true;
	}

}

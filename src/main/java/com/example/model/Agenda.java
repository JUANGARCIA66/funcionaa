package com.example.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.example.classes.Contacto;

/**
 * Agenda
 * 
 * Fichero de registros de longitud fija
 * 
 * Los datos de cada contacto son:
 * usuario String 10,
 * nombre String 100,
 * telefono String 13,
 * edad int
 * 
 * Para almacenar los String Se utiliza writeUTF.
 * 
 * @see Contacto
 * @since 2022-03-15
 * @author Amadeo
 */
public class Agenda {

	/**
	 * Tamaño de un registro de la agenda
	 */
	private final String url = "jdbc:sqlite:" + "C:/Users/Juan/Desktop/funcionaa/db/prueba.db";

	/**
	 * Fichero de datos de la agenda
	 */

	/**
	 * Constructor
	 * 
	 * @throws IOException
	 */
	public Connection c;
	public Statement st;

	public Agenda() throws IOException, SQLException {
		this.c = DriverManager.getConnection(url);
		this.st = c.createStatement();

		try {
			String sql = "CREATE TABLE agenda " +
					"(usuario VARCHAR(100) not NULL, " +
					" nombre VARCHAR(100), " +
					" telefono VARCHAR(100), " +
					" edad INTEGER, " +
					" PRIMARY KEY ( usuario ))";
			this.st.executeUpdate(sql);
		} catch (SQLException e) {

		}

	}

	/**
	 * Añade un contacto a la agenda
	 * 
	 * @param c
	 * @throws IOException
	 */
	public boolean create(Contacto c) throws IOException {
		write(c);
		return true;
	}

	/**
	 * Devuelve el contacto de la agenda que corresponde al usuario
	 * 
	 * @param usuario
	 * @return contacto correspondiente al usuario o null si no existe
	 * @throws IOException
	 */
	public Contacto read(String usuarioBuscar) throws IOException {
		ResultSet rs;
		try {
			PreparedStatement ps = this.c.prepareStatement("select * from agenda WHERE usuario = ?;");
			ps.setString(1, usuarioBuscar);
			rs = ps.executeQuery();
			while (rs.next()) {
				String usuario = rs.getString("usuario");
				String nombre = rs.getString("nombre");
				String telefono = rs.getString("telefono");
				int edad = rs.getInt("edad");

				return new Contacto(usuario, nombre, telefono, edad);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Actualiza un registro
	 * 
	 * @param Contacto
	 * @return true si actualizado
	 * @throws IOException
	 */
	public boolean update(Contacto c, String nombreAntiguo) throws IOException {
		Contacto buscado = read(c.getUsuario());
		if (buscado == null) {
			return false;
		}
		PreparedStatement ps;
		try {
			ps = this.c.prepareStatement("UPDATE agenda SET nombre = ?, telefono = ?, edad = ? WHERE usuario = ? ");
			ps.setString(1, c.getNombre());
			ps.setString(2, c.getTelefono());
			ps.setInt(3, c.getEdad());
			ps.setString(4, nombreAntiguo);

			int filasModificadas = ps.executeUpdate();
			if (filasModificadas > 0) {
				System.out.println("¡Alumno modificado exitosamente!");
			} else {
				System.out.println("No se pudo modificar el alumno.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("No se pudo modificar el alumnno");
		}
		return true;
	}

	/**
	 * Marca como borrado un registro de la agenda
	 * 
	 * @param usuario
	 * @return true si borrado
	 * @throws IOException
	 */
	public boolean delete(String usuario) throws IOException {

		PreparedStatement ps;
		try {
			ps = c.prepareStatement("DELETE FROM agenda WHERE usuario = ?");
			ps.setString(1, usuario);
			int filasBorradas = ps.executeUpdate();
			if (filasBorradas > 0) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Escribe un contacto en la agenda
	 * 
	 * @param c
	 * @throws IOException
	 */
	private void write(Contacto c) throws IOException {
		PreparedStatement ps;
		try {
			ps = this.c.prepareStatement("INSERT INTO agenda (usuario, nombre, telefono, edad) VALUES (?, ?, ?, ?)");
			ps.setString(1, c.getUsuario());
			ps.setString(2, c.getNombre());
			ps.setString(3, c.getTelefono());
			ps.setInt(4, c.getEdad());

			int filasInsertadas = ps.executeUpdate();
			if (filasInsertadas > 0) {
				System.out.println("¡Fila insertada exitosamente!");
			} else {
				System.out.println("No se pudo insertar la fila.");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("No se pudo insertar la fila.");
		}
	}

	/**
	 * Lista todos los contactos activos
	 * 
	 * @return
	 * @throws IOException
	 */
	public String list() throws IOException {
		String out = "";
		ResultSet rs;
		try {
			rs = this.st.executeQuery("select * from agenda");
			while (rs.next()) {
				String usuario = rs.getString("usuario");
				String nombre = rs.getString("nombre");
				String telefono = rs.getString("telefono");
				int edad = rs.getInt("edad");

				out = out + ("[ Usuario = " + usuario + ", Nombre = " + nombre + ", Telefono = " + telefono
						+ ", Edad = " + edad + "]\n");

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Problemas al listar agenda");
		}
		return out;
	}

	/**
	 * Cierra la agenda
	 * 
	 * @throws IOException
	 * @throws SQLException
	 */
	public void close() throws IOException, SQLException {
		this.c.close();
		this.st.close();

	}

	/**
	 * DEBUG: Limpia el fichero de datos y añade algunos registros
	 * 
	 * @throws IOException
	 */
	/*
	 * public void init() throws IOException {
	 * this.c.close();
	 * this.st.close();
	 * File agenda = new File(FIL_AGENDA);
	 * agenda.delete();
	 * agenda.createNewFile();
	 * // fichero = new RandomAccessFile(agenda, "rw");
	 * 
	 * create(new Contacto("luis", "Luis Rato", "34612345678", 27));
	 * create(new Contacto("ana", "Ana Mota", "915432145", 34));
	 * }
	 */

	/**
	 * DEBUG: Muestra toda la agenda incluyendo borrados
	 * 
	 * @throws IOException
	 */

}

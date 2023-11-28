package com.example.model;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.example.classes.Contacto;
import com.example.utils.Util;

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
	private final String url = "jdbc:sqlite:" + "sqlite/agenda.db";
	/**
	 * Ruta del fichero que almacena la agenda
	 */
	private final String FIL_AGENDA = "./agenda.fijo.dat";

	/**
	 * Marca de registro borrado
	 */
	private final String MRK_DELETED = "#";

	/**
	 * Longitud del campo usuario
	 */
	private final int LEN_USUARIO = 10;

	/**
	 * Longitud del campo nombre
	 */
	private final int LEN_NOMBRE = 100;

	/**
	 * Longitud del campo telefono
	 */
	private final int LEN_TELEFONO = 13;

	private final int LEN_CURSO = 20;
	/**
	 * Tamaño de un registro de la agenda
	 */
	private final int LEN_REGISTRO = LEN_USUARIO + Short.BYTES
			+ LEN_NOMBRE + Short.BYTES
			+ LEN_TELEFONO + Short.BYTES
			+ LEN_CURSO + Short.BYTES
			+ Integer.BYTES;

	/**
	 * Fichero de datos de la agenda
	 */
	private RandomAccessFile fichero;
	public Connection cn;
	public Statement st;

	public static String borrar = "DROP TABLE IF EXISTS agenda";
	public static String crear = "CREATE TABLE agenda " +
			"(usuario VARCHAR(100) not NULL, " +
			" nombre VARCHAR(100), " +
			" telefono VARCHAR(100), " +
			" edad INTEGER, " +
			" curso VARCHAR(100)," +
			" PRIMARY KEY ( usuario ))";

	public Agenda() throws IOException {
		fichero = new RandomAccessFile(FIL_AGENDA, "rw");
	}

	/**
	 * Añade un contacto a la agenda
	 * 
	 * @param c
	 * @throws IOException
	 */
	public boolean create(Contacto c) throws IOException {
		long posicion = fichero.length();
		fichero.seek(posicion);
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
	public Contacto read(String usuario) throws IOException {
		long posicion = 0L;
		fichero.seek(posicion);
		while (fichero.getFilePointer() < fichero.length()) {
			Contacto c = read();
			if (c != null && c.getUsuario().equals(usuario)) {
				return c;
			}
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
	public boolean update(Contacto c) throws IOException {
		Contacto buscado = read(c.getUsuario());
		if (buscado == null) {
			return false;
		}
		fichero.seek(fichero.getFilePointer() - LEN_REGISTRO);
		write(c);
		return true;
	}

	public boolean _update(Contacto c) throws IOException {
		long posicion = 0L;
		fichero.seek(posicion);
		while (fichero.getFilePointer() < fichero.length()) {
			posicion = fichero.getFilePointer();
			Contacto r = read();
			if (r != null && r.getUsuario().equals(c.getUsuario())) {
				fichero.seek(posicion);
				write(c);
				return true;
			}
		}
		return false;
	}

	/**
	 * Marca como borrado un registro de la agenda
	 * 
	 * @param usuario
	 * @return true si borrado
	 * @throws IOException
	 */
	public boolean delete(String usuario) throws IOException {
		long posicion = 0L;
		fichero.seek(posicion);
		while (fichero.getFilePointer() < fichero.length()) {
			posicion = fichero.getFilePointer();
			Contacto r = read();
			if (r != null && r.getUsuario().equals(usuario)) {
				fichero.seek(posicion);
				fichero.writeUTF(Util.format(MRK_DELETED, LEN_USUARIO));
				return true;
			}
		}
		return false;
	}

	/**
	 * Escribe un contacto en la agenda
	 * 
	 * @param c
	 * @throws IOException
	 */
	private void write(Contacto c) throws IOException {
		fichero.writeUTF(Util.format(c.getUsuario(), LEN_USUARIO));
		fichero.writeUTF(Util.format(c.getNombre(), LEN_NOMBRE));
		fichero.writeUTF(Util.format(c.getTelefono(), LEN_TELEFONO));
		fichero.writeUTF(Util.format(c.getCurso(), LEN_CURSO));
		fichero.writeInt(c.getEdad());
	}

	/**
	 * Lee un contacto de la agenda
	 * 
	 * @return Contacto o null si está borrado
	 * @throws IOException
	 */
	private Contacto read() throws IOException {
		Contacto c = new Contacto(
				fichero.readUTF(),
				fichero.readUTF(),
				fichero.readUTF(),
				fichero.readUTF(),
				fichero.readInt());
		return c.getUsuario().equals(MRK_DELETED) ? null : c;
	}

	/**
	 * Lista todos los contactos activos
	 * 
	 * @return
	 * @throws IOException
	 */
	public String list() throws IOException {
		String out = "";
		long posicion = 0L;
		fichero.seek(posicion);
		while (fichero.getFilePointer() < fichero.length()) {
			Contacto r = read();
			if (r != null) {
				out += r + "\n";
			}
		}
		return out;
	}

	public ArrayList<Contacto> list_contacto() {
		ArrayList<Contacto> ac = new ArrayList<Contacto>();
		long posicion = 0L;
		try {
			fichero.seek(posicion);
			while (fichero.getFilePointer() < fichero.length()) {
				Contacto r = read();
				if (r != null) {
					ac.add(r);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ac;
	}

	/**
	 * Cierra la agenda
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		fichero.close();
	}

	/**
	 * DEBUG: Limpia el fichero de datos y añade algunos registros
	 * 
	 * @throws IOException
	 */
	public void init() throws IOException {
		fichero.close();
		File agenda = new File(FIL_AGENDA);
		agenda.delete();
		agenda.createNewFile();
		fichero = new RandomAccessFile(agenda, "rw");

		create(new Contacto("luis", "Luis Rato", "34612345678", "Base de datos", 27));
		create(new Contacto("ana", "Ana Mota", "915432145", "Interfaces", 34));
		create(new Contacto("pepe", "Pepe Botella", "915432145", "Historia", 55));
	}

	/**
	 * DEBUG: Muestra toda la agenda incluyendo borrados
	 * 
	 * @throws IOException
	 */
	public void listRaw() throws IOException {
		String debug = "";

		fichero.seek(0L);
		while (fichero.getFilePointer() < fichero.length()) {
			Contacto c = read();
			debug += "\n" + (c == null ? "#borrado#" : c);
		}

		System.err.println("\n[[ DEBUG >> \n" + debug + "\n >> DEBUG ]]\n");
	}

	/**
	 * Constructor
	 * 
	 * @throws IOException
	 */
	public boolean trasferir() {
		try {
			this.cn = DriverManager.getConnection(url);
			this.st = cn.createStatement();

			this.st.executeUpdate(borrar);
			this.st.executeUpdate(crear);

			ArrayList<Contacto> ac = list_contacto();

			for (Contacto c : ac) {
				create_sql(c);
			}
			return true;

		} catch (SQLException e) {
			System.out.println("HOLAAAA");
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * Añade un contacto a la agenda
	 * 
	 * @param c
	 * @throws IOException
	 */
	public boolean create_sql(Contacto c) {
		try {
			write_sql(c);
			return true;
		} catch (IOException | SQLException e) {
			// TODO Auto-generated catch block
			return false;
		}
	}

	/**
	 * Devuelve el contacto de la agenda que corresponde al usuario
	 * 
	 * @param usuario
	 * @return contacto correspondiente al usuario o null si no existe
	 * @throws IOException
	 */
	public Contacto read_sql(String usuarioBuscar) throws IOException {
		ResultSet rs;
		try {
			PreparedStatement ps = this.cn.prepareStatement("select * from agenda WHERE usuario = ?;");
			ps.setString(1, usuarioBuscar);
			rs = ps.executeQuery();
			while (rs.next()) {
				String usuario = rs.getString("usuario");
				String nombre = rs.getString("nombre");
				String telefono = rs.getString("telefono");
				String curso = rs.getString("curso");
				int edad = rs.getInt("edad");

				return new Contacto(usuario, nombre, telefono, curso, edad);
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
	public boolean update_sql(Contacto c, String nombreAntiguo) throws IOException {
		Contacto buscado = read_sql(c.getUsuario());
		if (buscado == null) {
			return false;
		}
		PreparedStatement ps;
		try {
			ps = this.cn.prepareStatement(
					"UPDATE agenda SET nombre = ?, telefono = ?, contacto = ?, edad = ? WHERE usuario = ? ");
			ps.setString(1, c.getNombre());
			ps.setString(2, c.getTelefono());
			ps.setString(3, c.getCurso());
			ps.setInt(4, c.getEdad());
			ps.setString(5, nombreAntiguo);

			int filasModificadas = ps.executeUpdate();
			if (filasModificadas > 0) {
				return true;
			} else {
				return false;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("No se pudo modificar el usuario");
			return false;
		}
	}

	/**
	 * Marca como borrado un registro de la agenda
	 * 
	 * @param usuario
	 * @return true si borrado
	 * @throws IOException
	 */
	public boolean delete_sql(String usuario) throws IOException {

		PreparedStatement ps;
		try {
			ps = cn.prepareStatement("DELETE FROM agenda WHERE usuario = ?");
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
	 * @throws SQLException
	 */
	private void write_sql(Contacto c) throws IOException, SQLException {

		PreparedStatement ps;

		ps = this.cn
				.prepareStatement("INSERT INTO agenda (usuario, nombre, telefono, curso, edad) VALUES (?, ?, ?, ?, ?)");
		ps.setString(1, c.getUsuario());
		ps.setString(2, c.getNombre());
		ps.setString(3, c.getTelefono());
		ps.setInt(4, c.getEdad());
		ps.setString(5, c.getCurso());

		ps.executeUpdate();

	}

	/**
	 * Lista todos los contactos activos
	 * 
	 * @return
	 * @throws IOException
	 */
	public String list_sql() throws IOException {
		String out = "";
		ResultSet rs;
		try {
			rs = this.st.executeQuery("select * from agenda");
			while (rs.next()) {
				String usuario = rs.getString("usuario");
				String nombre = rs.getString("nombre");
				String telefono = rs.getString("telefono");
				String curso = rs.getString("curso");
				int edad = rs.getInt("edad");

				out = out + ("[ Usuario = " + usuario + ", Nombre = " + nombre + ", Telefono = " + telefono
						+ ", Edad = " + edad + ", Curso = " + curso + "]\n");

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
	public void close_sql() throws IOException, SQLException {
		this.cn.close();
		this.st.close();

	}

}

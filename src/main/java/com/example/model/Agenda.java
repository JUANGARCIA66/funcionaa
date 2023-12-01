package com.example.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

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

	private static String rutaXml = "";

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

	public Agenda() throws IOException, SQLException {
		fichero = new RandomAccessFile(FIL_AGENDA, "rw");
		this.cn = DriverManager.getConnection(url);
		this.st = cn.createStatement();
		this.st.executeUpdate(borrar);
		this.st.executeUpdate(crear);
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
		create(new Contacto("asdasd", "asdasdasd", "515225", "Historia", 56));
		create(new Contacto("123", "123", "123", "Interfaces", 23));
		create(new Contacto("1243", "1243", "1243", "Historia", 12));
		create(new Contacto("sara", "Sara García", "611223344", "Base de datos", 25));
		create(new Contacto("alberto", "Alberto Hernández", "622334455", "Interfaces", 30));
		create(new Contacto("lucia", "Lucía Fernández", "633445566", "Historia", 28));
		create(new Contacto("daniel", "Daniel Martín", "644556677", "Base de datos", 33));
		create(new Contacto("carmen", "Carmen Pérez", "655667788", "Interfaces", 26));
		create(new Contacto("victor", "Víctor Sánchez", "666778899", "Historia", 29));
		create(new Contacto("patricia", "Patricia Rodríguez", "677889900", "Base de datos", 27));
		create(new Contacto("roberto", "Roberto Gómez", "688990011", "Interfaces", 32));
		create(new Contacto("lucas", "Lucas Martínez", "699001122", "Historia", 31));
		create(new Contacto("silvia", "Silvia Hernández", "600112233", "Base de datos", 24));
		create(new Contacto("javier", "Javier García", "611223344", "Interfaces", 34));
		create(new Contacto("irene", "Irene Fernández", "622334455", "Historia", 29));
		create(new Contacto("oscar", "Óscar Rodríguez", "633445566", "Base de datos", 26));
		create(new Contacto("raquel", "Raquel Pérez", "644556677", "Interfaces", 31));
		create(new Contacto("sergio", "Sergio Sánchez", "655667788", "Historia", 30));
		create(new Contacto("eva", "Eva Rodríguez", "666778899", "Base de datos", 28));
		create(new Contacto("alejandro", "Alejandro Gómez", "677889900", "Interfaces", 33));
		create(new Contacto("claudia", "Claudia Martínez", "688990011", "Historia", 27));
		create(new Contacto("manuela", "Manuela Hernández", "699001122", "Base de datos", 25));
		create(new Contacto("diego", "Diego García", "600112233", "Interfaces", 30));
		create(new Contacto("luisa", "Luisa Fernández", "611223344", "Matemáticas", 25));
		create(new Contacto("carolina", "Carolina Gómez", "622334455", "Ciencias", 30));
		create(new Contacto("sergio", "Sergio Martínez", "633445566", "Literatura", 28));
		create(new Contacto("ana", "Ana Martín", "644556677", "Física", 33));
		create(new Contacto("david", "David Pérez", "655667788", "Química", 26));
		create(new Contacto("natalia", "Natalia Sánchez", "666778899", "Biología", 29));
		create(new Contacto("juan", "Juan Rodríguez", "677889900", "Geografía", 27));
		create(new Contacto("marina", "Marina Gómez", "688990011", "Historia del Arte", 32));
		create(new Contacto("carlos", "Carlos Martínez", "699001122", "Música", 31));
		create(new Contacto("lucía", "Lucía Hernández", "600112233", "Educación Física", 24));
		create(new Contacto("javier", "Javier García", "611223344", "Informática", 34));
		create(new Contacto("raquel", "Raquel Pérez", "622334455", "Economía", 29));
		create(new Contacto("lucas", "Lucas Fernández", "633445566", "Dibujo", 26));
		create(new Contacto("sara", "Sara Martín", "644556677", "Psicología", 31));
		create(new Contacto("pablo", "Pablo Pérez", "655667788", "Filosofía", 30));
		create(new Contacto("teresa", "Teresa Sánchez", "666778899", "Lengua", 28));
		create(new Contacto("alberto", "Alberto Rodríguez", "677889900", "Historia Universal", 27));
		create(new Contacto("clara", "Clara Gómez", "688990011", "Arte Dramático", 25));
		create(new Contacto("diego", "Diego Martínez", "699001122", "Ética", 30));
		create(new Contacto("laura", "Laura Hernández", "600112233", "Religión", 29));

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
	public boolean transferir() {

		ArrayList<Contacto> ac = list_contacto();
		for (Contacto c : ac) {
			create_sql(c);
		}
		return true;

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

	public boolean gruposCursos() {
		try {
			ResultSet rs;
			File xml;
			xml = crearDocumento();

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;

			builder = factory.newDocumentBuilder();
			DOMImplementation impl = builder.getDOMImplementation();
			Document document = impl.createDocument(null, "Cursos", null);
			document.setXmlVersion("1.0");
			Element raiz = document.createElement("Tabla");

			PreparedStatement ps = this.cn.prepareStatement("SELECT COUNT(*), curso FROM agenda GROUP BY curso;");
			rs = ps.executeQuery();
			while (rs.next()) {
				String numContactos = rs.getString("COUNT(*)");
				String curso = rs.getString("curso");

				if (numContactos != null && curso != null) {
					try {
						Element registro = document.createElement("registro");
						document.getDocumentElement().appendChild(registro);
						crearElemento("curso", curso, registro, document);
						crearElemento("participantes", numContactos, registro, document);

					} catch (DOMException e) {
						e.printStackTrace();
						System.err.println("Ha petao");
					}
				}

			}
			DOMSource fuente = new DOMSource(document);
			StreamResult result = new StreamResult(xml);
			Transformer transfor;

			transfor = TransformerFactory.newInstance().newTransformer();
			transfor.transform(fuente, result);
			read_xml();
			return true;
		} catch (IOException | ParserConfigurationException | SQLException | TransformerFactoryConfigurationError
				| TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	static void crearElemento(String curso, String num, Element raiz, Document documento) {
		Element elem = documento.createElement(curso);
		Text text = documento.createTextNode(num);
		raiz.appendChild(elem);
		elem.appendChild(text);
	}

	private File crearDocumento() throws IOException {

		String rutaXML = "xml/ContactoResumen.xml";
		File xml = new File(rutaXML);
		int numArchivo = 001;
		while (xml.exists()) {
			String numCeros = String.format("%03d", numArchivo);
			rutaXML = "xml/ContactoResumen" + numCeros + ".xml";
			xml = new File(rutaXML);
			numArchivo++;
		}

		if (xml.createNewFile()) {
			rutaXml = xml.getAbsolutePath();
			return xml;
		} else {
			return null;
		}
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
		ps.setString(4, c.getCurso());
		ps.setInt(5, c.getEdad());

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

	public void read_xml() {

		DocumentBuilderFactory factoria = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factoria.newDocumentBuilder(); // Creamos una instancia
			Document documento = builder.parse(new File(rutaXml)); // Y cargamos el documento xml
			documento.getDocumentElement().normalize(); // Pone todos los nodos texto en documento

			// Crea una lista con sólo los nodos empleados
			NodeList registros = documento.getElementsByTagName("registro");

			// Recorre la lista
			for (int i = 0; i < registros.getLength(); ++i) {
				Node reg = registros.item(i);
				if (reg.getNodeType() == Node.ELEMENT_NODE) {
					Element elemento = (Element) reg;
					String curso = elemento.getElementsByTagName("curso").item(0).getTextContent();
					int participantes = Integer
							.valueOf(elemento.getElementsByTagName("participantes").item(0).getTextContent());
					introducirEnTxt(curso, participantes);
				}
			}
			introducirEnTxt("==============================", 0);// final del registro de xml, en el txt no se borran
																	// los registros, se acumulan
			leerTxt();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

	}

	public void introducirEnTxt(String curso, int participantes) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(new File("./agendaVariable.txt"), true));
			String out = curso + "/" + participantes + "\n";
			writer.write(out);
			System.out.println("Datos introducidos en txt");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void leerTxt() {

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(new File("./agendaVariable.txt")));

			String linea;
			while ((linea = reader.readLine()) != null) {

				String[] re = linea.split("/");
				System.out.println("Curso: " + re[0] + ", participantes: " + re[1]);
			}
			System.out.println("Datos leidos en txt");
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

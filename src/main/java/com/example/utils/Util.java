package com.example.utils;

public class Util {

	/**
	 * Añade espacios a un String hasta la longitud indicada
	 * o trunca la información si es necesario para obtener
	 * un campo de un tamaño determinado
	 * 
	 * @param str
	 * @param length
	 */
	static public String format(String str, int length) {
		str = str.trim() + " ".repeat(length);
		return str.substring(0, length);
	}
}

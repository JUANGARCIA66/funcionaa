package com.example.main;

import java.io.IOException;
import java.sql.SQLException;

import com.example.controller.Menu;
import com.example.model.Agenda;

/**
 * Agenda de contactos
 * 
 * @since 2022-01-25
 * @author Amadeo
 *
 */
public class Main {

	public static void main(String[] args) {
		Agenda a;
		try {
			a = new Agenda();
			a.init(); // DEBUG
			new Menu(a);
			a.close();
		} catch (IOException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
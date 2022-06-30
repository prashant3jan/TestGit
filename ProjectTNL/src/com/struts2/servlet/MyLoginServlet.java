package com.struts2.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet
public class MyLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
   
    public MyLoginServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    	      throws ServletException, IOException {
    	System.out.println("controller servlet called");

    	getServletContext().getRequestDispatcher("/DisplayLogin.action").forward(request, response);  
    }

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

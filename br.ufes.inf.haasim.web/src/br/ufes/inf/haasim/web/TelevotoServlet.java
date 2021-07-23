package br.ufes.inf.haasim.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
public class TelevotoServlet extends HttpServlet {
 
	private static final long serialVersionUID = 2L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        resp.getWriter().write("<html><body>");
        
        resp.getWriter().write("<style rel=\"stylesheet\" type=\"text/css\">");
        resp.getWriter().write(".title { font-weight: bold; }    ");
        resp.getWriter().write("#container1 { float:left; width:100%; }");
        resp.getWriter().write("#col1 { float:left; width:25%; }");
        resp.getWriter().write("#col2 { float:left; width:25%; }");
        resp.getWriter().write("#col3 { float:left; width:25%; }");
        resp.getWriter().write("#col4 { float:left; width:25%; }");
        resp.getWriter().write("</style>");
        resp.getWriter().write("</head>");

        resp.getWriter().write("<body>");
        resp.getWriter().write("<H1>Televoto</H1>");
        resp.getWriter().write("<div id=\"container1\">");
        		
        resp.getWriter().write("<div id=\"col1\">");
        resp.getWriter().write("<p class=\"title\">Hist�rico de Chamadas</p>");
        resp.getWriter().write("	<p>Totais: </p>");
        resp.getWriter().write("<p>Encerradas: </p>");
        resp.getWriter().write("<p>Perdidas: </p>");
        resp.getWriter().write("<p>Canceladas: </p>");
        resp.getWriter().write("</div>");
		
        resp.getWriter().write("<div id=\"col2\">");
		resp.getWriter().write("<p class=\"title\">Chamadas em Tempo Real</p>");
		resp.getWriter().write("<p>Totais: </p>");
		resp.getWriter().write("<p>N�o atendidas: </p>");
		resp.getWriter().write("<p>Em negocia��o: </p>");
		resp.getWriter().write("<p>Estabelecidas: </p>");
		resp.getWriter().write("<p>Encerrando: </p>");
		resp.getWriter().write("</div>");
    		
		resp.getWriter().write("<div id=\"col3\">");
    	resp.getWriter().write("<p class=\"title\">Agentes em Tempo Real</p>");
    	resp.getWriter().write("<p>Dispon�veis: </p>");
    	resp.getWriter().write("<p>Livres: </p>");
    	resp.getWriter().write("<p>Ocupados: </p>");
    	resp.getWriter().write("</div>");
		
		resp.getWriter().write("<div id=\"col4\">");
		resp.getWriter().write("<p class=\"title\">Chamadas e Agentes em Tempo Real</p>");
		resp.getWriter().write("</div>	 ");
		resp.getWriter().write("</div>  ");
        
        resp.getWriter().write("</body></html>");
    }
	

 
}

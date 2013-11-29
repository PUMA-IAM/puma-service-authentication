package puma.sp.authentication.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import puma.sp.authentication.controllers.WAYFController;
import puma.sp.mgmt.model.organization.Tenant;
import puma.sp.authentication.util.FlowDirecter;
import puma.util.exceptions.flow.FlowException;

/**
 *	A servlet handling the initial authentication service access request
 * 	@author Jasper Bogaerts
 */
public class ServiceAccessServlet extends HttpServlet {
	private static Logger logger = Logger.getLogger(ServiceAccessServlet.class.getCanonicalName());
	private static final long serialVersionUID = 1L;
    
    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        try {
        	WAYFController wayfCtrl = new WAYFController();
            String relayState, tenantIdentifier;        	
            // RelayState
            if (request.getSession().getAttribute("RelayState") == null) {
            	relayState = (String) request.getParameter("RelayState");
            	if (relayState == null || relayState.isEmpty()) {
            		throw new FlowException("Could not start authentication flow: no relay state given");
            	}
            	request.getSession().setAttribute("RelayState", relayState);
            } else {
            	relayState = (String) request.getSession().getAttribute("RelayState");
            }
            // Tenant Identifier
            Tenant tenantObject = null;
            if (request.getSession().getAttribute("Tenant") == null) {
            	tenantIdentifier = (String) request.getParameter("Tenant");
            	if (tenantIdentifier == null || tenantIdentifier.isEmpty()) {
            		request.getSession().setAttribute("FlowRedirectionElement", new FlowDirecter("/ServiceAccessServlet"));
            		response.sendRedirect("index.jsp");
            		return;
            	} else {
            		tenantObject = wayfCtrl.getTenant(Long.parseLong(tenantIdentifier));
            		logger.log(Level.INFO, null, "Tenant: " + tenantObject.getName());
            	}
            } else {
            	tenantObject = (Tenant) request.getSession().getAttribute("Tenant");
            }
            if (tenantObject == null) {
        		request.getSession().setAttribute("FlowRedirectionElement", new FlowDirecter("/ServiceAccessServlet"));
        		response.sendRedirect("index.jsp");
        		return;
            } else {
            	request.getSession().setAttribute("Tenant", tenantObject);
            }
            // Redirect to next flow element
            request.getRequestDispatcher("/AuthenticationRequestServlet").forward(request, response);
        } catch (FlowException ex) {
        	logger.log(Level.SEVERE, "Unable to process request", ex);
		} finally {            
            out.close();
        }
    }

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Main entry point for authentication flow";
    }
}
/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
 
package se.anatom.ejbca.webdist.cainterface;

import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;

import se.anatom.ejbca.SecConst;
import se.anatom.ejbca.authorization.AuthorizationDeniedException;
import se.anatom.ejbca.authorization.AvailableAccessRules;
import se.anatom.ejbca.ca.caadmin.HardCATokenInfo;
import se.anatom.ejbca.ca.exception.CATokenAuthenticationFailedException;
import se.anatom.ejbca.ca.exception.CATokenOfflineException;
import se.anatom.ejbca.webdist.webconfiguration.EjbcaWebBean;

/**
 * Contains help methods used to parse a viewcainfo jsp page requests.
 *
 * @author  Philip Vendil
 * @version $Id: ViewCAInfoJSPHelper.java,v 1.1.2.1 2004-11-13 00:24:36 herrvendil Exp $
 */
public class ViewCAInfoJSPHelper {
		 
	public static final String CA_PARAMETER             = "caid";

	public static final String CERTSERNO_PARAMETER      = "certsernoparameter"; 
	  
	public static final String PASSWORD_AUTHENTICATIONCODE  = "passwordactivationcode";
	  
	public static final String BUTTON_ACTIVATE          = "buttonactivate";
	public static final String BUTTON_MAKEOFFLINE       = "buttonmakeoffline";
	public static final String BUTTON_CLOSE             = "buttonclose"; 


    /** Creates new LogInterfaceBean */
    public ViewCAInfoJSPHelper(){     	    	
    }
    // Public methods.
    /**
     * Method that initialized the bean.
     *
     * @param request is a reference to the http request.
     */
    public void initialize(HttpServletRequest request, EjbcaWebBean ejbcawebbean,
                           CAInterfaceBean cabean) throws  Exception{

      if(!initialized){
        this.ejbcawebbean = ejbcawebbean;
        this.cabean = cabean;                
        		
        initialized = true;
        can_activate = false;
        authorized = false;
		try{
			authorized = ejbcawebbean.isAuthorizedNoLog(AvailableAccessRules.REGULAR_CABASICFUNCTIONS);
			can_activate = ejbcawebbean.isAuthorizedNoLog(AvailableAccessRules.REGULAR_ACTIVATECA);
		}catch(AuthorizationDeniedException ade){}
      }
    }

    /**
     * Method that parses the request and take appropriate actions.
     * @param request the http request
     * @throws Exception
     */
    public void parseRequest(HttpServletRequest request) throws Exception{
    	  generalerrormessage = null;
    	  activationerrormessage = null;   
    	  activationmessage = null;
    	  ishardcatoken = false;
    	 
    	  if( request.getParameter(CA_PARAMETER) != null ){
    	    caid = Integer.parseInt(request.getParameter(CA_PARAMETER));
    	             	    
    	    if(request.getParameter(BUTTON_ACTIVATE) != null || request.getParameter(BUTTON_MAKEOFFLINE) != null){
    	      // Get currentstate
    	      status = SecConst.CA_OFFLINE;
    	      try{
    	      	cainfo = cabean.getCAInfo(caid);
    	      	status = cainfo.getCAInfo().getStatus();
    	      	if( cainfo.getCAInfo().getCATokenInfo() instanceof HardCATokenInfo ){
    	      		ishardcatoken = true;
    	      	}
    	      } catch(AuthorizationDeniedException e){
    	      	generalerrormessage = "NOTAUTHORIZEDTOVIEWCA";
    	      	return;
    	      } 
    	      
    	      // If Activate button is pressed, the admin is authorized and the current status is offline then activate.
    	      if(request.getParameter(BUTTON_ACTIVATE) != null &&
    	      	 can_activate &&
    	      	 ishardcatoken &&
				 status == SecConst.CA_OFFLINE){
    	         
    	         String authorizationcode = request.getParameter(PASSWORD_AUTHENTICATIONCODE);
    	         if(authorizationcode != null && !authorizationcode.trim().equals("")){
    	         	try{
    	         	  cabean.getCADataHandler().activateCAToken(caid,authorizationcode);
    	         	  activationmessage = "CAACTIVATIONSUCCESSFUL";
    	         	}catch(CATokenAuthenticationFailedException catafe){
    	         		activationerrormessage = "AUTHENTICATIONERROR" + ": " + catafe.getMessage();
    	         	}catch(CATokenOfflineException catoe){
    	         		activationerrormessage = "ERROR" + ": " + catoe.getMessage();
    	         	}
    	         }else{
    	         	activationerrormessage = "MUSTENTERAUTHCODE";
    	         }
    	      }
    	      // If Activate button is pressed, the admin is authorized and the current status is offline then activate.
    	      if(request.getParameter(BUTTON_MAKEOFFLINE) != null &&
    	      	 can_activate &&
    	      	 ishardcatoken &&
				 status == SecConst.CA_ACTIVE){
    	         
    	      	 try{
    	      	   cabean.getCADataHandler().deactivateCAToken(caid);
    	      	   activationmessage = "MAKEOFFLINESUCCESSFUL";
    	      	 }catch(Exception e){
    	      	 	throw e;
    	      	 }
    	      }   	    
    	    }
    	        	        	        	        	        	      	  
    	    
    	    try{
    	      cainfo = cabean.getCAInfo(caid);
    	      status = cainfo.getCAInfo().getStatus();
  	       	  if( cainfo.getCAInfo().getCATokenInfo() instanceof HardCATokenInfo ){
	      		ishardcatoken = true;
	      	  }
    	      ocspcert = cainfo.getOCSPSignerCertificate();
    	    } catch(AuthorizationDeniedException e){
    	    	generalerrormessage = "NOTAUTHORIZEDTOVIEWCA";
    	    }

    	    if(cainfo==null){
    	      generalerrormessage = "CADOESNTEXIST";	
    	    }
    	  }else{
    	  	generalerrormessage = "YOUMUSTSPECIFYCAID";
    	  }
  
    }

    
       
    // Private fields.
    private EjbcaWebBean ejbcawebbean;
    private CAInterfaceBean cabean;
    private boolean initialized=false;
	public String   generalerrormessage = null;
	public String   activationerrormessage = null;
	public String   activationmessage = null;
    public boolean  can_activate = false;    
    public boolean  authorized = false; 
    public boolean  ishardcatoken = false;
    public CAInfoView cainfo = null;
    public  int status = 0; 
    public  int caid = 0; 
    public  X509Certificate ocspcert = null;
    
	
}

<%@ page pageEncoding="ISO-8859-1"%>
<%@page errorPage="/errorpage.jsp" import="java.util.*, se.anatom.ejbca.webdist.webconfiguration.EjbcaWebBean,se.anatom.ejbca.ra.raadmin.GlobalConfiguration, se.anatom.ejbca.SecConst
               ,se.anatom.ejbca.webdist.hardtokeninterface.HardTokenInterfaceBean, se.anatom.ejbca.hardtoken.HardTokenIssuer, se.anatom.ejbca.hardtoken.HardTokenIssuerData, se.anatom.ejbca.hardtoken.HardTokenIssuerExistsException,
               se.anatom.ejbca.hardtoken.HardTokenIssuerDoesntExistsException,  se.anatom.ejbca.webdist.rainterface.CertificateView, se.anatom.ejbca.authorization.AdminGroup"%>

<html>
<jsp:useBean id="ejbcawebbean" scope="session" class="se.anatom.ejbca.webdist.webconfiguration.EjbcaWebBean" />
<jsp:setProperty name="ejbcawebbean" property="*" /> 
<jsp:useBean id="tokenbean" scope="session" class="se.anatom.ejbca.webdist.hardtokeninterface.HardTokenInterfaceBean" />
<jsp:useBean id="cabean" scope="session" class="se.anatom.ejbca.webdist.cainterface.CAInterfaceBean" />

<%! // Declarations 
  static final String ACTION                        = "action";
  static final String ACTION_EDIT_ISSUERS           = "editissuers";
  static final String ACTION_EDIT_ISSUER            = "editissuer";

  static final String CHECKBOX_VALUE                = "true";

//  Used in profiles.jsp
  static final String BUTTON_EDIT_ISSUER       = "buttoneditissuer"; 
  static final String BUTTON_DELETE_ISSUER     = "buttondeleteissuer";
  static final String BUTTON_ADD_ISSUER        = "buttonaddissuer"; 
  static final String BUTTON_RENAME_ISSUER     = "buttonrenameissuer";
  static final String BUTTON_CLONE_ISSUER      = "buttoncloneissuer";

  static final String SELECT_ISSUER            = "selectissuer";
  static final String SELECT_ADMINGROUP        = "selectadmingroup";
  static final String TEXTFIELD_ALIAS          = "textfieldalias";
  static final String HIDDEN_ALIAS             = "hiddenalias";  
  
 
// Buttons used in profile.jsp
  static final String BUTTON_SAVE              = "buttonsave";
  static final String BUTTON_CANCEL            = "buttoncancel";
 
  static final String SELECT_AVAILABLEHARDTOKENPROFILES            = "selectavailablehardtokenprofiles";
  static final String TEXTFIELD_DESCRIPTION    = "textfielddescription";



  static final String SELECT_TYPE                         = "selecttype";
  String alias = null;
  String certsn = null;
%>
<% 

  // Initialize environment
  String includefile = "hardtokenissuerspage.jsp";

  boolean  issuerexists             = false;
  boolean  issuerdeletefailed       = false;

  String value=null;
  HardTokenIssuer issuer=null;     

  GlobalConfiguration globalconfiguration = ejbcawebbean.initialize(request,"/hardtoken_functionality/edit_hardtoken_issuers"); 
                                            tokenbean.initialize(request, ejbcawebbean);
                                            cabean.initialize(request, ejbcawebbean); 

  String THIS_FILENAME                    = globalconfiguration.getHardTokenPath() + "/edithardtokenissuers.jsp";

  HashMap caidtonamemap = cabean.getCAIdToNameMap(); 
%>
 
<head>
  <title><%= globalconfiguration .getEjbcaTitle() %></title>
  <base href="<%= ejbcawebbean.getBaseUrl() %>">
  <link rel=STYLESHEET href="<%= ejbcawebbean.getCssFile() %>">
  <script language=javascript src="<%= globalconfiguration .getAdminWebPath() %>ejbcajslib.js"></script>
</head>
<body>

<%  

   // Determine action 
  if( request.getParameter(ACTION) != null){
    if( request.getParameter(ACTION).equals(ACTION_EDIT_ISSUERS)){
      if( request.getParameter(BUTTON_EDIT_ISSUER) != null){
          // Display  profilepage.jsp
         alias = request.getParameter(SELECT_ISSUER);
         if(alias != null){
           if(!alias.trim().equals("")){
             includefile="hardtokenissuerpage.jsp"; 
           } 
           else{ 
            alias= null;
          } 
        }
        if(alias == null){   
          includefile="hardtokenissuerspage.jsp";     
        }
      }
      if( request.getParameter(BUTTON_DELETE_ISSUER) != null) {
          // Delete profile and display profilespage. 
          alias = request.getParameter(SELECT_ISSUER);
          if(alias != null){
            if(!alias.trim().equals("")){
              issuerdeletefailed = !tokenbean.removeHardTokenIssuer(alias);
            }
          }
          includefile="hardtokenissuerspage.jsp";          
      }
      if( request.getParameter(BUTTON_RENAME_ISSUER) != null){ 
         // Rename selected profile and display profilespage.
       String newalias  = request.getParameter(TEXTFIELD_ALIAS);       
       String oldalias = request.getParameter(SELECT_ISSUER);
       int admingroupid = Integer.parseInt(request.getParameter(SELECT_ADMINGROUP));
       
       if(oldalias != null && newalias != null){
         if(!newalias.trim().equals("") && !oldalias.trim().equals("")){
           try{
             tokenbean.renameHardTokenIssuer(oldalias,newalias.trim(), admingroupid);
           }catch( HardTokenIssuerExistsException e){
             issuerexists=true;
           }        
         }
       }      
       includefile="hardtokenissuerspage.jsp"; 
      }
      if( request.getParameter(BUTTON_ADD_ISSUER) != null){
         // Add profile and display profilespage.         
         alias = request.getParameter(TEXTFIELD_ALIAS);        
         int admingroupid = Integer.parseInt(request.getParameter(SELECT_ADMINGROUP));
         if(alias != null){
           if(!alias.trim().equals("")){
             try{              
               tokenbean.addHardTokenIssuer(alias.trim(), admingroupid);
             }catch( HardTokenIssuerExistsException e){
               issuerexists=true;
             }
           }      
         }
         includefile="hardtokenissuerspage.jsp"; 
      }
      if( request.getParameter(BUTTON_CLONE_ISSUER) != null){
         // clone profile and display profilespage.
       String newalias  = request.getParameter(TEXTFIELD_ALIAS);       
       String oldalias = request.getParameter(SELECT_ISSUER);
       int admingroupid = Integer.parseInt(request.getParameter(SELECT_ADMINGROUP));
       if(oldalias != null && newalias != null){
         if(!oldalias.trim().equals("") && !newalias.trim().equals("")){
             try{ 
               tokenbean.cloneHardTokenIssuer(oldalias.trim(),newalias.trim(), admingroupid);
             }catch( HardTokenIssuerExistsException e){
               issuerexists=true;
             }
         }
       }      
       includefile="hardtokenissuerspage.jsp"; 
      }
    }
    if( request.getParameter(ACTION).equals(ACTION_EDIT_ISSUER)){
         // Display edit access rules page.
       alias = request.getParameter(HIDDEN_ALIAS);       
       if(alias != null){
         if(!alias.trim().equals("")){
           if(request.getParameter(BUTTON_SAVE) != null){
             issuer = tokenbean.getHardTokenIssuerData(alias).getHardTokenIssuer();
             // Save changes.
             ArrayList availableprofiles = new ArrayList();
 
             String[] values = request.getParameterValues(SELECT_AVAILABLEHARDTOKENPROFILES);
             
             if(values!= null){
               for(int i=0; i< values.length; i++){
                 availableprofiles.add(new Integer(values[i]));                     
               }
             } 
             issuer.setAvailableHardTokenProfiles(availableprofiles);
                      
             String description = request.getParameter(TEXTFIELD_DESCRIPTION);
             if(description == null)
               description = "";
             issuer.setDescription(description);


             tokenbean.changeHardTokenIssuer(alias,issuer);
             includefile="hardtokenissuerspage.jsp";
           }
           if(request.getParameter(BUTTON_CANCEL) != null){
              // Don't save changes.
             includefile="hardtokenissuerspage.jsp";
           }
         }
      }
    }
  }

  Collection authgroups = ejbcawebbean.getInformationMemory().getHardTokenIssuingAdminGroups();
  HashMap adminidtonamemap = ejbcawebbean.getInformationMemory().getAdminGroupIdToNameMap();

 // Include page
  if( includefile.equals("hardtokenissuerspage.jsp")){ %>
   <%@ include file="hardtokenissuerspage.jsp" %>
<%}
  if( includefile.equals("hardtokenissuerpage.jsp")){ %>
   <%@ include file="hardtokenissuerpage.jsp" %> 
<%}

   // Include Footer 
   String footurl =   globalconfiguration.getFootBanner(); %>
   
  <jsp:include page="<%= footurl %>" />

</body>
</html>

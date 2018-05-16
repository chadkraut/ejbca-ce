/*************************************************************************
 *                                                                       *
 *  EJBCA Community: The OpenSource Certificate Authority                *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/

package org.ejbca.ui.web.rest.api.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.cesecore.authentication.AuthenticationFailedException;
import org.cesecore.authentication.tokens.AuthenticationToken;
import org.cesecore.authorization.AuthorizationDeniedException;
import org.cesecore.certificates.ca.CaSessionLocal;
import org.ejbca.core.ejb.rest.EjbcaRestHelperSessionLocal;
import org.ejbca.core.model.era.RaAuthorizationResult;
import org.ejbca.core.model.era.RaMasterApiProxyBeanLocal;
import org.ejbca.ui.web.rest.api.types.CaType;
import org.ejbca.ui.web.rest.common.BaseRestResource;

/**
 * JAX-RS resource handling CA related requests.
 * 
 * @version $Id$
 *
 */
@Path("/v1/ca")
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class CaResource extends BaseRestResource {
    
    private static final Logger log = Logger.getLogger(CaResource.class);
    
    private static final String VERSION = "1";
    
    @EJB
    private CaSessionLocal caSession;
    @EJB
    private EjbcaRestHelperSessionLocal ejbcaRestHelperSession;
    @EJB
    private RaMasterApiProxyBeanLocal raMasterApiProxyBean;
    
    @GET
    public Response getCAs() {
        log.trace(">getCAs");
        
        List<CaType> caList = new ArrayList<CaType>();
        
        for (final Entry<Integer, String> caEntry : caSession.getCAIdToNameMap().entrySet()) {
            caList.add(new CaType(caEntry.getKey(), caEntry.getValue()));
        }

        log.trace("<getCAs");
        return Response.ok(caList).build();
    }

    @GET
    @Path("/version")
    @Produces(MediaType.TEXT_HTML)
    public Response getApiVersion() {
        return Response.ok(VERSION).build();
    }
    
    /**
     * TODO Mainly used for auth testing. Keep this anyway (under some other base url)?
     * @param requestContext Context
     * @return granted access for the requesting administrator
     */
    @GET
    @Path("/get-authorization")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuthorization(@Context HttpServletRequest requestContext) {
        if (requestContext == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing request context").build();
        }
        try {
            final AuthenticationToken admin = getAdmin(requestContext, false);
            final RaAuthorizationResult authResult = raMasterApiProxyBean.getAuthorization(admin);
            final Map<String, Boolean> authResultSorted = new TreeMap<String, Boolean>(authResult.getAccessRules()); 
            return Response.ok(authResultSorted).build();
        } catch (AuthorizationDeniedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (AuthenticationFailedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }
}

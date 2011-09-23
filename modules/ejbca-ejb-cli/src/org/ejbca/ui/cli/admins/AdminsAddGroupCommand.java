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
 
package org.ejbca.ui.cli.admins;

import org.ejbca.ui.cli.ErrorAdminCommandException;

/**
 * Adds a new admin role
 * @version $Id$
 */
public class AdminsAddGroupCommand extends BaseAdminsCommand {

    public String getMainCommand() {
        return MAINCOMMAND;
    }

    public String getSubCommand() {
        return "addrole";
    }

    public String getDescription() {
        return "Adds an administrative role.";
    }

    /** @see org.ejbca.ui.cli.CliCommandPlugin */
    public void execute(String[] args) throws ErrorAdminCommandException {
        args = parseUsernameAndPasswordFromArgs(args);
        
        try {
            if (args.length < 4) {
                getLogger().info("Description: " + getDescription());
                getLogger().info("Usage: " + getCommand() + " <name of role>");
                return;
            }
            String roleName = args[1];
            ejb.getRoleManagementSession().create(getAdmin(cliUserName, cliPassword), roleName);
        } catch (Exception e) {
            throw new ErrorAdminCommandException(e);
        }
    }
}

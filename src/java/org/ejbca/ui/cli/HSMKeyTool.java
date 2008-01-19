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

package org.ejbca.ui.cli;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;



/**
 * @author lars
 * @version $Id: HSMKeyTool.java,v 1.22.2.1 2008-01-19 01:36:24 primelars Exp $
 *
 */
public class HSMKeyTool {
//    private static String CREATE_CA_SWITCH = "createca";
    private static String ENCRYPT_SWITCH = "encrypt";
    private static String DECRYPT_SWITCH = "decrypt";
    private static String GENERATE_SWITCH = "generate";
    private static String DELETE_SWITCH = "delete";
    private static String TEST_SWITCH = "test";
    private static String CREATE_KEYSTORE_SWITCH = "createkeystore";
    private static String CREATE_KEYSTORE_MODULE_SWITCH = "createkeystoremodule";
    private static String MOVE_SWITCH = "move";
    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            final boolean isP11 = args.length>4 && KeyStoreContainer.isP11(args[4]);
            final String sKeyStore =  isP11 ? "<slot number>" : "<keystore ID>";
            final String commandString = args[0] + " " + args[1] + (isP11 ? " <shared library name> " : " ");
            /*if ( args.length > 1 && args[1].toLowerCase().trim().equals(CREATE_CA_SWITCH)) {
                try {
                    new HwCaInitCommand(args).execute();
                } catch (Exception e) {
                    System.out.println(e.getMessage());            
                    //e.printStackTrace();
                    System.exit(-1);
                }
                return;
            } else */
            if ( args.length > 1 && args[1].toLowerCase().trim().equals(GENERATE_SWITCH)) {
                if ( args.length < 6 ) {
                    System.err.print(commandString + "<key size> <key entry name> ");
                    if ( isP11 ) {
                        System.err.println("<slot number> [<attribute file name>]");
                        System.err.println("The <attribute file name> could be used if you want to change the default attributes of your HSM");
                        System.err.println("The format of the file is described in the Sun PKCS#11 provider documentation.");
                        System.err.println("Please note that the attributes \"library\" and \"name\" and \"slot\" should not be specified since they are specified on the command line.");
                        System.err.println("Here follows an example on how the file could look:");
                        System.err.println("attributes(generate,*,*) = {");
                        System.err.println("  CKA_UNWRAP = true");
                        System.err.println("  CKA_DECRYPT = true");
                        System.err.println("  CKA_SIGN = true");
                        System.err.println("}");
                    } else
                        System.err.println("[<keystore ID>]");
                } else
                    KeyStoreContainer.getIt(args[4], args[2], args[3], args.length>7 ? args[7] : null, args.length>8 ? args[8] : null).generate(Integer.parseInt(args[5].trim()), args.length>6 ? args[6] :"myKey");
                return;
            } else if ( args.length > 1 && args[1].toLowerCase().trim().equals(DELETE_SWITCH)) {
                if ( args.length < 6 )
                    System.err.println(commandString + sKeyStore + " [<key entry name>]");
                else
                    KeyStoreContainer.getIt(args[4], args[2], args[3], args[5]).delete(args.length>6 ? args[6] : null);
                return;
            } else if ( args.length > 1 && args[1].toLowerCase().trim().equals(ENCRYPT_SWITCH)) {
                if ( args.length < 9 )
                    System.err.println(commandString + sKeyStore + " <input file> <output file> <key alias>");
                else
                    KeyStoreContainer.getIt(args[4], args[2], args[3], args[5]).encrypt(new FileInputStream(args[6]), new FileOutputStream(args[7]), args[8]);
                return;
            } else if ( args.length > 1 && args[1].toLowerCase().trim().equals(DECRYPT_SWITCH)) {
                if ( args.length < 9 )
                    System.err.println(commandString + sKeyStore + " <input file> <output file> <key alias>");
                else
                    KeyStoreContainer.getIt(args[4], args[2], args[3], args[5]).decrypt(new FileInputStream(args[6]), new FileOutputStream(args[7]), args[8]);
                return;
            } else if( args.length > 1 && args[1].toLowerCase().trim().equals(TEST_SWITCH)) {
                if ( args.length < 6 )
                    System.err.println(commandString + sKeyStore + " [<# of tests>]");
                else
                    KeyStoreContainerTest.test(args[2], args[3], args[4], args[5], args.length>6 ? Integer.parseInt(args[6].trim()) : 1);
                return;
            } else if( args.length > 1 && args[1].toLowerCase().trim().equals(MOVE_SWITCH)) {
                if ( args.length < 7 )
                    System.err.println(commandString +
                                       (isP11 ? "<from slot number> <to slot number>" : "<from keystore ID> <to keystore ID>"));
                else
                    KeyStoreContainer.move(args[2], args[3], args[4], args[5], args[6]);
                return;
            } else if ( !isP11 ){
                if( args.length > 1 && args[1].toLowerCase().trim().equals(CREATE_KEYSTORE_SWITCH)) {
                    KeyStoreContainer.getIt(args[4], args[2], args[3], null).storeKeyStore();
                    return;
                } else if( args.length > 1 && args[1].toLowerCase().trim().equals(CREATE_KEYSTORE_MODULE_SWITCH)) {
                    System.setProperty("protect", "module");
                    KeyStoreContainer.getIt(args[4], args[2], args[3], null).storeKeyStore();
                    return;
                }
            }
            PrintWriter pw = new PrintWriter(System.err);
            pw.println("Use one of following commands: ");
//            pw.println("  "+args[0]+" "+CREATE_CA_SWITCH);
            pw.println("  "+args[0]+" "+GENERATE_SWITCH);
            pw.println("  "+args[0]+" "+DELETE_SWITCH);
            pw.println("  "+args[0]+" "+TEST_SWITCH);
            if ( !isP11 ){
                pw.println("  "+args[0]+" "+CREATE_KEYSTORE_SWITCH);
                pw.println("  "+args[0]+" "+CREATE_KEYSTORE_MODULE_SWITCH);
            }
            pw.println("  "+args[0]+" "+ENCRYPT_SWITCH);
            pw.println("  "+args[0]+" "+DECRYPT_SWITCH);
            pw.println("  "+args[0]+" "+MOVE_SWITCH);
            pw.flush();
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
    }
}

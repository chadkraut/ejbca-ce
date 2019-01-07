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
package org.ejbca.webtest.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * End Entity Profile helper class for EJBCA Web Tests.
 *
 * @version $Id$
 */
public class EndEntityProfileHelper extends BaseHelper {

    /**
     * Contains constants and references of the 'End Entity Profiles' page.
     */
    private static class Page {
        // General
        static final String PAGE_URI = "/ejbca/adminweb/ra/editendentityprofiles/editendentityprofiles.jsp";
        static final By PAGE_LINK = By.id("raEditendentityprofiles");
        // End Entity Profiles Form
        static final By TEXT_MESSAGE = By.xpath("//td[contains(text(), 'End Entity Profile saved.')]");
        static final By INPUT_NAME = By.xpath("//input[@name='textfieldprofilename']");
        static final By BUTTON_ADD = By.xpath("//input[@name='buttonaddprofile']");
        static final By BUTTON_EDIT = By.xpath("//input[@name='buttoneditprofile']");
        static final By BUTTON_CLONE = By.xpath("//input[@name='buttoncloneprofile']");
        static final By BUTTON_RENAME = By.xpath("//input[@name='buttonrenameprofile']");
        static final By BUTTON_DELETE = By.xpath("//input[@name='buttondeleteprofile']");
        static final By SELECT_EE_PROFILES = By.xpath("//select[@name='selectprofile']");
        // End Entity Profile Form
        static final By TEXT_TITLE_END_ENTITY_PROFILE = By.xpath("//div/h3");
        static final By INPUT_USERNAME_AUTO_GENERATED = By.id("checkboxautogeneratedusername");
        static final By INPUT_USE_ISSUANNCE_REVOCATION_REASON = By.id("checkboxuseissuancerevocationreason");
        // Maximum number of failed login attempts
        static final By INPUT_USE_MAX_FAILED_LOGINS = By.id("checkboxusemaxfailedlogins");
        // Other certificate data / Certificate Validity Start Time /Use
        static final By INPUT_USE_START_TIME = By.id("checkboxsusetarttime");
        // Other certificate data / Certificate Validity Start Time /Use
        static final By INPUT_USE_END_TIME = By.id("checkboxuseendtime");
        // Other certificate data / Certificate Validity Start Time /Value
        static final By INPUT_START_TIME = By.xpath("//input[@name='textfieldstarttime']");
        // Other certificate data / Certificate Validity END Time /Value
        static final By INPUT_END_TIME = By.xpath("//input[@name='textfieldendtime']");
        //Other certificate data/ Name Constraints, Permitted
        static final By INPUT_USE_NAME_CONSTRAINTS = By.id("checkboxusencpermitted");
        // Other certificate data/ Custom certificate extension data
        static final By INPUT_USE_EXTENSION_DATA = By.id("checkboxuseextensiondata");
        // Other Data / Number of allowed requests / Use
        static final By INPUT_USE_NUMBER_OF_ALLOWED_REQUESTS = By.id("checkboxuseallowedrequests");
        // Other Data / Key Recoverable / Use
        static final By INPUT_USE_KEY_RECOVERABLE = By.id("checkboxusekeyrecoverable");
        // Other Data / Revocation reason to set after certificate issuance /Use
        static final By INPUT_USE_ISSUANCE_REVOCATION_REASON = By.id("checkboxuseissuancerevocationreason");
        // Other Data / Notification Recipient
        static final By INPUT_NOTIFICATION_RECIPIENT = By.xpath("//input[@name='textfieldnotificationrcpt']");
        // Other Data / Notification Subject
        static final By INPUT_NOTIFICATION_SUBJECT = By.xpath("//input[@name='textfieldnotificationsubject']");
        // Other Data / Notification Message
        static final By INPUT_NOTIFICATION_MESSAGE = By.xpath("//textarea[@name='textareanotificationmessage']");
        // Other Data / Notification Sender
        static final By INPUT_NOTIFICATION_SENDER = By.xpath("//input[@name='textfieldnotificationsender']");
        // Other Data / Send Notification  / Use
        static final By INPUT_USE_SEND_NOTIFICATION = By.id("checkboxusesendnotification");
        // Other Data / Send Notification  / Default
        static final By INPUT_DEFAULT_SEND_NOTIFICATION = By.id("checkboxsendnotification");
        // Other Data / Send Notification  / Required
        static final By INPUT_REQUIRED_SEND_NOTIFICATION = By.id("checkboxrequiredsendnotification");
        // Other Data / Notification Events
        static final By SELECT_NOTIFICATION_EVENTS = By.xpath("//select[@name='selectnotificationevents']");
        static final By SELECT_DEFAULT_CERTIFICATE_PROFILE = By.xpath("//select[@name='selectdefaultcertprofile']");
        static final By SELECT_AVAILABLE_CERTIFICATE_PROFILES = By.xpath("//select[@name='selectavailablecertprofiles']");
        static final By SELECT_ISSUANCE_REVOCATION_REASON = By.xpath("//select[@name='selectissuancerevocationreason']");
        static final By SELECT_DEFAULT_CA = By.xpath("//select[@name='selectdefaultca']");
        static final By SELECT_AVAILABLE_CAS = By.xpath("//select[@name='selectavailablecas']");
        static final By NOTIFICATION = By.xpath("//tr[td/strong[text()='Send Notification']]/following-sibling::tr[1]/td[2][contains(text(), 'Notification')]");
        // Other Data / Send Notification  / Add
        static final By BUTTON_ADD_NOTIFICATION = By.xpath("//input[@name='buttonaddnotification']");
        // Other Data / Send Notification  / Add Another
        static final By BUTTON_ADD_ANOTHER_NOTIFICATION = By.xpath("//input[@name='buttonaddanothernotification']");
        // Other Data / Send Notification  / Cancel
        static final By BUTTON_CANCEL_NOTIFICATION = By.xpath("//input[@name='buttondeletetemporarynotification']");
        // Other Data / Send Notification  / Delete all
        static final By BUTTON_DELETE_ALL_NOTIFICATION = By.xpath("//input[@name='buttondeleteallnotification']");
        static final By BUTTON_SAVE_PROFILE = By.xpath("//input[@name='buttonsave']");
        static final By BUTTON_CANCEL_EDITING_PROFILE = By.xpath("//input[@name='buttoncancel']");
        static final By BUTTON_BACK_TO_END_ENTITY_PROFILES = By.xpath("//td/a[contains(@href,'editendentityprofiles.jsp')]");

        // Dynamic references
        static By getEEPOptionContainingText(final String text) {
            return By.xpath("//option[@value='" + text + "']");
        }

        static By getSubjectAttributesSelectByAttributeType(final String attributeType) {
            return By.xpath("//select[@name='selectadd" + attributeType + "']");
        }

        static By getSubjectAttributesAddButtonByAttributeType(final String attributeType) {
            return By.xpath("//input[@name='buttonadd" + attributeType + "']");
        }

        static By getSubjectAttributesAttributeByAttributeName(final String attributeName) {
            return By.xpath("//td[contains(text(), '" + attributeName + "')]");
        }

        static By getSubjectAttributesAttributeModifiableByAttributeTypeAndAttributeIndex(final String attributeType, final int attributeIndex) {
            return By.id("checkboxmodifyable" + attributeType + attributeIndex);
        }

        static By getSubjectAttributesAttributeTextfieldByAttributeTypeAndAttributeIndex(final String attributeType, final int attributeIndex) {
            return By.xpath("//input[@name='textfield" + attributeType + +attributeIndex + "']");
        }

        // Other Data / Send Notification  / Delete
        static final By getDeleteNotificationButtonByIndex(final int index) {
            return By.xpath("//input[@name='buttondeleltenotification" + index + "']");
        }

        // Other Data / Send Notification  / Notification Sender
        static final By getNotificationSenderByIndex(final int index) {
            return By.xpath("//input[@name='textfieldnotificationsender_newvalue" + index + "']");
        }

        // Other Data / Send Notification  / Notification Sender
        static final By getNotificationSubjectByIndex(final int index) {
            return By.xpath("//input[@name='textfieldnotificationsubject_newvalue" + index + "']");
        }

        // Other Data / Send Notification  / Notification Recipient
        static final By getNotificationRecipientByIndex(final int index) {
            return By.xpath("//input[@name='textfieldnotificationrcpt_newvalue" + index + "']");
        }

        // Other Data / Send Notification / Notification Events
        static final By getNotificationEventsByIndex(final int index) {
            return By.xpath("//select[@name='selectnotificationevents_newvalue" + index + "']");
        }

        // Other Data / Send Notification / Notification Message
        static final By getNotificationMessageByIndex(final int index) {
            return By.xpath("//textarea[@name='textareanotificationmessage_newvalue" + index + "']");
        }
    }

    public EndEntityProfileHelper(final WebDriver webDriver) {
        super(webDriver);
    }

    /**
     * Opens the 'End Entity Profiles' page and asserts the correctness of URI path.
     *
     * @param webUrl home page URL.
     */
    public void openPage(final String webUrl) {
        openPageByLinkAndAssert(webUrl, Page.PAGE_LINK, Page.PAGE_URI);
    }

    /**
     * Asserts the current URI equals to End Entity Profile page URI.
     */
    public void assertIsOnStartPage() {
        assertPageUri(Page.PAGE_URI);
    }

    /**
     * Adds a new End Entity Profile, and asserts that it appears in End Entities Profiles table.
     *
     * @param endEntityProfileName an End Entity Profile name.
     */
    public void addEndEntityProfile(final String endEntityProfileName) {
        fillInput(Page.INPUT_NAME, endEntityProfileName);
        clickLink(Page.BUTTON_ADD);
        assertEndEntityProfileNameExists(endEntityProfileName);
    }

    /**
     * Opens the edit page for an End Entity Profile, then asserts that the correct End Entity Profile is being edited.
     *
     * @param endEntityProfileName an End Entity Profile name.
     */
    public void openEditEndEntityProfilePage(final String endEntityProfileName) {
        selectOptionByName(Page.SELECT_EE_PROFILES, endEntityProfileName);
        clickLink(Page.BUTTON_EDIT);
        assertEndEntityProfileTitleExists(endEntityProfileName);
    }

    /**
     * Edits the End Entity Profile
     *
     * @param defaultCertificateProfileName the value for 'Default Certificate Profile'.
     * @param selectedCertificateProfiles   the list for 'Available Certificate Profiles' selector.
     * @param defaultCAName                 the value for 'Default CA'.
     * @param selectedCAs                   the list for 'Available CAs' selector.
     */
    public void editEndEntityProfile(
            final String defaultCertificateProfileName,
            final List<String> selectedCertificateProfiles,
            final String defaultCAName,
            final List<String> selectedCAs
    ) {
        selectOptionByName(Page.SELECT_DEFAULT_CERTIFICATE_PROFILE, defaultCertificateProfileName);
        selectOptionsByName(Page.SELECT_AVAILABLE_CERTIFICATE_PROFILES, selectedCertificateProfiles);
        selectDefaultCa(defaultCAName);
        selectOptionsByName(Page.SELECT_AVAILABLE_CAS, selectedCAs);
    }

    /**
     * Sets a value for default CA
     *
     * @param defaultCAName а name of default CA to be selected
     */
    public void selectDefaultCa(String defaultCAName) {
        selectOptionByName(Page.SELECT_DEFAULT_CA, defaultCAName);
    }

    /**
     * Saves the End Entity Profile with success assertion.
     */
    public void saveEndEntityProfile() {
        saveEndEntityProfile(true);
    }

    /**
     * Saves the End Entity Profile with assertion.
     *
     * @param withAssertion true for success assertion, false otherwise.
     */
    public void saveEndEntityProfile(final boolean withAssertion) {
        clickLink(Page.BUTTON_SAVE_PROFILE);
        if (withAssertion) {
            assertEndEntityProfileSaved();
        }
    }

    /**
     * Clones the End Entity Profile.
     *
     * @param endEntityProfileName    source End Entity Profile name.
     * @param newEndEntityProfileName target End Entity Profile name.
     */
    public void cloneEndEntityProfile(final String endEntityProfileName, final String newEndEntityProfileName) {
        // Select End Entity Profile in list
        selectOptionByName(Page.SELECT_EE_PROFILES, endEntityProfileName);
        // Clone the End Entity Profile
        fillInput(Page.INPUT_NAME, newEndEntityProfileName);
        clickLink(Page.BUTTON_CLONE);
    }

    /**
     * Renames the End Entity Profile.
     *
     * @param oldEndEntityProfileName source End Entity Profile name.
     * @param newEndEntityProfileName target End Entity Profile name.
     */
    public void renameEndEntityProfile(final String oldEndEntityProfileName, final String newEndEntityProfileName) {
        // Select End Entity Profile in list
        selectOptionByName(Page.SELECT_EE_PROFILES, oldEndEntityProfileName);
        // Rename the End Entity Profile
        fillInput(Page.INPUT_NAME, newEndEntityProfileName);
        clickLink(Page.BUTTON_RENAME);
    }

    /**
     * Calls the delete dialog of the End Entity Profile by name.
     *
     * @param endEntityProfileName End Entity Profile name.
     */
    public void deleteEndEntityProfile(final String endEntityProfileName) {
        // Select End Entity Profile in list
        selectOptionByName(Page.SELECT_EE_PROFILES, endEntityProfileName);
        clickLink(Page.BUTTON_DELETE);
    }

    /**
     * Confirms/Discards the deletion of End Entity Profile with expected message.
     *
     * @param message     expected message.
     * @param isConfirmed true to confirm deletion, false otherwise.
     */
    public void confirmEndEntityProfileDeletion(final String message, final boolean isConfirmed) {
        assertAndConfirmAlertPopUp(message, isConfirmed);
    }

    /**
     * Asserts the End Entity Profile name exists in the list of End Entity Profiles.
     *
     * @param endEntityProfileName End Entity Profile name.
     */
    public void assertEndEntityProfileNameExists(final String endEntityProfileName) {
        final WebElement selectWebElement = findElement(Page.SELECT_EE_PROFILES);
        if (findElement(selectWebElement, Page.getEEPOptionContainingText(endEntityProfileName)) == null) {
            fail(endEntityProfileName + " was not found in the List of End Entity Profiles.");
        }
    }

    /**
     * Asserts the End Entity Profile name exists in the list of End Entity Profiles.
     *
     * @param endEntityProfileName End Entity Profile name.
     */
    public void assertEndEntityProfileNameDoesNotExist(final String endEntityProfileName) {
        final WebElement selectWebElement = findElement(Page.SELECT_EE_PROFILES);
        if (findElement(selectWebElement, Page.getEEPOptionContainingText(endEntityProfileName)) != null) {
            fail(endEntityProfileName + " was found in the List of End Entity Profiles.");
        }
    }

    /**
     * Triggers the checkbox 'Username' Auto-generated.
     */
    public void triggerUsernameAutoGenerated() {
        clickLink(Page.INPUT_USERNAME_AUTO_GENERATED);
    }

    /**
     * Triggers the checkbox 'Revocation reason to set after certificate issuance'
     */
    public void triggerRevocationReasonSetAfterCertIssuance() {
        clickLink(Page.INPUT_USE_ISSUANNCE_REVOCATION_REASON);
    }

    /**
     * Triggers the checkbox 'Use' for 'Maximum number of failed login attempts '.
     */
    public void triggerMaximumNumberOfFailedLoginAttempts() {
        clickLink(Page.INPUT_USE_MAX_FAILED_LOGINS);
    }

    /**
     * Triggers the checkbox 'Use' for 'Certificate Validity Start Time'.
     */
    public void triggerCertificateValidityStartTime() {
        clickLink(Page.INPUT_USE_START_TIME);
    }

    /**
     * Triggers the checkbox 'Use' for 'Certificate Validity End Time'.
     */
    public void triggerCertificateValidityEndTime() {
        clickLink(Page.INPUT_USE_END_TIME);
    }

    /**
     * Triggers the checkbox 'Use' for 'Name Constraints, Permitted'.
     */
    public void triggerNameConstraints() {
        clickLink(Page.INPUT_USE_NAME_CONSTRAINTS);
    }

    /**
     * Triggers the checkbox 'Use' for 'Custom certificate extension data'.
     */
    public void triggerExtensionData() {
        clickLink(Page.INPUT_USE_EXTENSION_DATA);
    }

    /**
     * Triggers the checkbox 'Use' for 'Number of allowed requests '.
     */
    public void triggerNumberOfAllowedRequests() {
        clickLink(Page.INPUT_USE_NUMBER_OF_ALLOWED_REQUESTS);
    }

    /**
     * Triggers the checkbox 'Use' for 'Key Recoverable'.
     */
    public void triggerKeyRecoverable() {
        clickLink(Page.INPUT_USE_KEY_RECOVERABLE);
    }

    /**
     * Triggers the checkbox 'Use' for 'Revocation reason to set after certificate issuance'.
     */
    public void triggerIssuanceRevocationReason() {
        clickLink(Page.INPUT_USE_ISSUANCE_REVOCATION_REASON);
    }

    /**
     * Add Notification.
     */
    public void addNotification() {
        clickLink(Page.BUTTON_ADD_NOTIFICATION);
    }

    /**
     * Add Another Notification.
     */
    public void addAnotherNotification() {
        clickLink(Page.BUTTON_ADD_ANOTHER_NOTIFICATION);
    }

    /**
     * Cancel Notification.
     */
    public void cancelNotification() {
        clickLink(Page.BUTTON_CANCEL_NOTIFICATION);
    }

    /**
     * Delete All Notification.
     */
    public void deleteAllNotifications() {
        clickLink(Page.BUTTON_DELETE_ALL_NOTIFICATION);
    }

    /**
     * Delete Notification.
     *
     * @param buttonIndex the index of notification to be deleted
     */
    public void deleteNotification(final int buttonIndex) {
        clickLink(Page.getDeleteNotificationButtonByIndex(buttonIndex));
    }

    /**
     * Triggers the checkbox 'Use' for 'Send Notification'.
     */
    public void triggerSendNotification() {
        clickLink(Page.INPUT_USE_SEND_NOTIFICATION);
    }

    /**
     * Fills  Certificate Validity Start Time
     */
    public void setCertificateValidityStartTime(String startTime) {
        fillInput(Page.INPUT_START_TIME, startTime);
    }

    /**
     * Fills  Certificate Validity End Time
     */
    public void setCertificateValidityEndTime(String endTime) {
        fillInput(Page.INPUT_END_TIME, endTime);
    }

    /**
     * Fills Notification Sender
     */
    public void setNotificationSender(String sender) {
        fillInput(Page.INPUT_NOTIFICATION_SENDER, sender);
    }

    /**
     * Fills Notification Subject
     */
    public void setNotificationSubject(String subject) {
        fillInput(Page.INPUT_NOTIFICATION_SUBJECT, subject);
    }

    /**
     * Fills Notification Message
     *
     * @param message
     */
    public void setNotificationMessage(String message) {
        fillInput(Page.INPUT_NOTIFICATION_MESSAGE, message);
    }

    /**
     * Fills Notification Message
     *
     * @param text the text to inserted as notification sender, recipient, subject and message
     */
    public void fillNotification(String text) {
        setNotificationSender(text);
        fillInput(Page.INPUT_NOTIFICATION_RECIPIENT, text);
        setNotificationSubject(text);
        setNotificationMessage(text);
    }

    /**
     * Selects the desired issuance revocation reason from the 'Revocation reason to set after certificate issuance'
     * drop down menu.
     *
     * @param reason revocation reason. E.g. 'Suspended: Certificate hold'
     */
    public void setIssuanceRevocationReason(final String reason) {
        selectOptionByName(Page.SELECT_ISSUANCE_REVOCATION_REASON, reason);
    }

    /**
     * Asserts the element 'Username' Auto-generated is selected/de-selected.
     *
     * @param isSelected true for selected and false for de-selected.
     */
    public void assertUsernameAutoGeneratedIsSelected(final boolean isSelected) {
        assertEquals(
                "'Username' Auto-generated field isSelected [" + isSelected + "]",
                isSelected,
                isSelectedElement(Page.INPUT_USERNAME_AUTO_GENERATED)
        );
    }

    /**
     * Asserts the element 'Use' in Send Notification is selected/de-selected.
     *
     * @param isSelected true for selected and false for de-selected.
     */
    public void assertUseSendNotificationIsSelected(final boolean isSelected) {
        assertEquals(
                "'Use' for 'Send Notification' isSelected [" + isSelected + "]",
                isSelected,
                isSelectedElement(Page.INPUT_USE_SEND_NOTIFICATION)
        );
    }

    /**
     * Asserts the element 'Default' in Send Notification is selected/de-selected.
     *
     * @param isSelected true for selected and false for de-selected.
     */
    public void assertDefaultSendNotificationIsSelected(final boolean isSelected) {
        assertEquals(
                "'Default' for 'Send Notification' isSelected [" + isSelected + "]",
                isSelected,
                isSelectedElement(Page.INPUT_DEFAULT_SEND_NOTIFICATION)
        );
    }

    /**
     * Asserts the element 'Default' in Send Notification is enabled/disabled.
     *
     * @param isEnabled true for enabled and false for disabled.
     */
    public void assertDefaultSendNotificationIsEnabled(final boolean isEnabled) {
        assertEquals(
                "'Default' for 'Send Notification' isEnabled [" + isEnabled + "]",
                isEnabled,
                isEnabledElement(Page.INPUT_DEFAULT_SEND_NOTIFICATION)
        );
    }

    /**
     * Asserts the element 'Required' in Send Notification is selected/de-selected.
     *
     * @param isSelected true for selected and false for de-selected.
     */
    public void assertRequiredSendNotificationIsSelected(final boolean isSelected) {
        assertEquals(
                "'Required' for 'Send Notification' isSelected [" + isSelected + "]",
                isSelected,
                isSelectedElement(Page.INPUT_REQUIRED_SEND_NOTIFICATION)
        );
    }

    /**
     * Asserts the element 'Required' in Send Notification is enabled/disabled.
     *
     * @param isEnabled true for enabled and false for disabled.
     */
    public void assertRequiredSendNotificationIsEnabled(final boolean isEnabled) {
        assertEquals(
                "'Required' for 'Send Notification' isEnabled [" + isEnabled + "]",
                isEnabled,
                isEnabledElement(Page.INPUT_REQUIRED_SEND_NOTIFICATION)
        );
    }

    /**
     * Asserts the button 'Cancel' Send Notification is enabled/disabled.
     *
     * @param isEnabled true for enabled and false for disabled.
     */
    public void assertAddNotificationButtonIsEnabled(final boolean isEnabled) {
        assertEquals(
                "'Add notification' button for 'Send Notification' isEnabled [" + isEnabled + "]",
                isEnabled,
                isEnabledElement(Page.BUTTON_ADD_NOTIFICATION)
        );
    }

    /**
     * Asserts the button 'Delete' Send Notification is enabled/disabled.
     *
     * @param isEnabled true for enabled and false for disabled.
     */
    public void assertDeleteNotificationButtonIsEnabled(final boolean isEnabled, final int buttonIndex) {
        assertEquals(
                "'Delete notification' button with index " + buttonIndex + " isEnabled [" + isEnabled + "]",
                isEnabled,
                isEnabledElement(Page.getDeleteNotificationButtonByIndex(buttonIndex))
        );
    }

    /**
     * Asserts the input 'Notification Sender' in Send Notification is enabled/disabled.
     *
     * @param isEnabled true for enabled and false for disabled.
     */
    public void assertNotificationSenderInputIsEnabled(final boolean isEnabled, final int buttonIndex) {
        assertEquals(
                "'Notification Sender' input text with index " + buttonIndex + " isEnabled [" + isEnabled + "]",
                isEnabled,
                isEnabledElement(Page.getNotificationSenderByIndex(buttonIndex))
        );
    }

    /**
     * Asserts the 'Notification Subject' input is enabled/disabled.
     *
     * @param isEnabled true for enabled and false for disabled.
     */
    public void assertNotificationSubjectInputIsEnabled(final boolean isEnabled, final int inputIndex) {
        assertEquals(
                "'Notification Subject' input for field with index " + inputIndex + " isEnabled [" + isEnabled + "]",
                isEnabled,
                isEnabledElement(Page.getNotificationSubjectByIndex(inputIndex))
        );
    }

    /**
     * Asserts the 'Notification Recipient' input is enabled/disabled.
     *
     * @param isEnabled true for enabled and false for disabled.
     */
    public void assertNotificationRecipientInputIsEnabled(final boolean isEnabled, final int inputIndex) {
        assertEquals(
                "'Notification Recipient' input for field with index " + inputIndex + " isEnabled [" + isEnabled + "]",
                isEnabled,
                isEnabledElement(Page.getNotificationRecipientByIndex(inputIndex))
        );
    }

    /**
     * Asserts the 'Notification Events' select is enabled/disabled.
     *
     * @param isEnabled true for enabled and false for disabled.
     */
    public void assertNotificationEventsInputIsEnabled(final boolean isEnabled, final int inputIndex) {
        assertEquals(
                "'Notification Events' select for field with index " + inputIndex + " isEnabled [" + isEnabled + "]",
                isEnabled,
                isEnabledElement(Page.getNotificationEventsByIndex(inputIndex))
        );
    }

    /**
     * Asserts the 'Notification Message' textarea is enabled/disabled.
     *
     * @param isEnabled true for enabled and false for disabled.
     */
    public void assertNotificationTextareaInputIsEnabled(final boolean isEnabled, final int inputIndex) {
        assertEquals(
                "'Notification Message' textarea for field with index " + inputIndex + " isEnabled [" + isEnabled + "]",
                isEnabled,
                isEnabledElement(Page.getNotificationMessageByIndex(inputIndex))
        );
    }

    /**
     * Asserts button 'Cancel Notification' is enabled/disabled.
     *
     * @param isEnabled true for enabled and false for disabled.
     */
    public void assertCancelNotificationButtonIsEnabled(final boolean isEnabled) {
        assertEquals(
                "'Cancel' button for 'Notification' isEnabled [" + isEnabled + "]",
                isEnabled,
                isEnabledElement(Page.BUTTON_CANCEL_NOTIFICATION)
        );
    }

    /**
     * Asserts the element 'Required' Send Notification is enabled/disabled.
     *
     * @param isEnabled true for enabled and false for disabled.
     */
    public void assertDeleteAllNotificationButtonIsEnabled(final boolean isEnabled) {
        assertEquals(
                "'Delete All' button for 'Notification' isEnabled [" + isEnabled + "]",
                isEnabled,
                isEnabledElement(Page.BUTTON_DELETE_ALL_NOTIFICATION)
        );
    }

    /**
     * Clicks the back link.
     */
    public void triggerBackToEndEntityProfiles() {
        clickLink(Page.BUTTON_BACK_TO_END_ENTITY_PROFILES);
    }

    /**
     * Adds an attribute to 'Subject DN Attributes', 'Subject Alternative Name' or
     * 'Subject Directory Attributes' while editing an End Entity Profile.
     *
     * @param attributeType either 'subjectdn', 'subjectaltname' or 'subjectdirattr'
     * @param attributeName the displayed name of the attribute, e.g. 'O, Organization'
     */
    public void addSubjectAttribute(final String attributeType, final String attributeName) {
        selectOptionByName(Page.getSubjectAttributesSelectByAttributeType(attributeType), attributeName);
        clickLink(Page.getSubjectAttributesAddButtonByAttributeType(attributeType));
    }

    /**
     * Asserts the attribute exists in the section 'Subject DN Attributes'.
     *
     * @param attributeName name of the attribute.
     */
    public void assertSubjectAttributeExists(final String attributeName) {
        final WebElement subjectAttributeWebElement = findElement(Page.getSubjectAttributesAttributeByAttributeName(attributeName));
        assertNotNull(
                "The attribute " + attributeName + " does not exist",
                subjectAttributeWebElement
        );
    }

    /**
     * Triggers the input 'Modifiable' for an attribute specified by its type and index in the section 'Subject DN Attributes'.
     *
     * @param attributeType  attribute's type.
     * @param attributeIndex attribute's index.
     */
    public void triggerSubjectAttributesAttributeModifiable(final String attributeType, final int attributeIndex) {
        clickLink(Page.getSubjectAttributesAttributeModifiableByAttributeTypeAndAttributeIndex(attributeType, attributeIndex));
    }

    /**
     * Asserts the appearance of alert dialog with expected message
     *
     * @param alertMessage expected alert message.
     * @param isConfirmed  true to confirm, false otherwise.
     */
    public void assertSubjectAttributesAttributeModifiableAlert(final String alertMessage, final boolean isConfirmed) {
        assertAndConfirmAlertPopUp(alertMessage, isConfirmed);
    }

    /**
     * Asserts the appearance of alert dialogs with expected messages for notification empty fields.
     */
    public void assertNotificationNotFilledAllerts() {
        assertAndConfirmAlertPopUp("You must fill in a notification sender if notification is to be used.", true);
        assertAndConfirmAlertPopUp("You must fill in a notification subject if notification is to be used.", true);
        assertAndConfirmAlertPopUp("You must fill in a notification message if notification is to be used.", true);
    }

    /**
     * Sets the value for the attribute specified by its type and index in the section 'Subject DN Attributes'.
     *
     * @param attributeType  attribute's type.
     * @param attributeIndex attribute's index.
     * @param value          attribute's value.
     */
    public void fillSubjectAttributesAttributeValue(final String attributeType, final int attributeIndex, final String value) {
        fillInput(Page.getSubjectAttributesAttributeTextfieldByAttributeTypeAndAttributeIndex(attributeType, attributeIndex), value);
    }

    // Asserts the title text
    private void assertEndEntityProfileTitleExists(final String endEntityProfileName) {
        final WebElement endEntityProfileTitle = findElement(Page.TEXT_TITLE_END_ENTITY_PROFILE);
        if (endEntityProfileTitle == null) {
            fail("End Entity Profile title was not found.");
        }
        assertEquals(
                "Unexpected title on End Entity Profile 'Edit' page",
                "End Entity Profile : " + endEntityProfileName,
                endEntityProfileTitle.getText()
        );
    }

    // Asserts the success of profile save
    private void assertEndEntityProfileSaved() {
        final WebElement endEntityProfileSaveMessage = findElement(Page.TEXT_MESSAGE);
        if (endEntityProfileSaveMessage == null) {
            fail("End Entity Profile save message was not found.");
        }
        assertEquals(
                "Expected profile save message was not displayed",
                "End Entity Profile saved.",
                endEntityProfileSaveMessage.getText()
        );
    }

    /**
     * Clicks the Cancel button when editing an End Entity Profile.
     */
    public void cancel() {
        webDriver.findElement(Page.BUTTON_CANCEL_EDITING_PROFILE).click();
    }

    /**
     * Asserts no notification exists on page
     */
    public void assertNotificationDoesNotExist() {
        assertElementDoesNotExist(Page.NOTIFICATION, "There were notifications displayed upon creation of EEP");
    }

    /**
     * Asserts existence of 'Notification sender' input
     */
    public void assertNotificationSenderExists() {
        assertElementExists(Page.INPUT_NOTIFICATION_SENDER, "Notification sender is not present on edit EEP page after adding notification");
    }

    /**
     * Asserts 'Notification sender' input is not present on page
     */
    public void assertNotificationSenderDoesNotExist() {
        assertElementDoesNotExist(Page.INPUT_NOTIFICATION_SENDER, "Notification sender is displayed on edit EEP page");
    }

    /**
     * Returns the text value of notification sender
     *
     * @return the text value of notification sender or null
     */
    public String getNotificationSenderText() {
        return getElementText(Page.INPUT_NOTIFICATION_SENDER);
    }

    /**
     * Asserts existence of 'Notification sender' input for notification with provided index
     *
     * @param senderIndex the index of notification to check
     */
    public void assertNotificationSenderExists(int senderIndex) {
        assertElementExists(Page.getNotificationSenderByIndex(senderIndex), "Notification sender with index " + senderIndex + " is displayed on edit EEP page");
    }

    /**
     * Asserts 'Notification sender' input for notification with provided index does not exist on page
     *
     * @param senderIndex the index of notification to check
     */
    public void assertNotificationSenderDoesNotExist(int senderIndex) {
        assertElementDoesNotExist(Page.getNotificationSenderByIndex(senderIndex), "Notification sender with index " + senderIndex + " is displayed on edit EEP page");
    }

    public String getNotificationSubjectValueText(final int inputIndex) {
        return getElementValue(Page.getNotificationSubjectByIndex(inputIndex));
    }

    /**
     * Returns the text value of notification sender for notification with provided index
     *
     * @param inputIndex the index of notification to check
     * @return the text value of notification sender or null
     */
    public String getNotificationSenderValueText(final int inputIndex) {
        return getElementValue(Page.getNotificationSenderByIndex(inputIndex));
    }

    /**
     * Returns the value (attribute 'value') of delete notification button with provided index
     *
     * @param buttonIndex the index of notification to check
     * @return button's value or null.
     */
    public String getNotificationDeleteButtonValueText(final int buttonIndex) {
        return getElementValue(Page.getDeleteNotificationButtonByIndex(buttonIndex));
    }

    /**
     * Asserts existence of 'Notification subject' input
     */
    public void assertNotificationSubjectExists() {
        assertElementExists(Page.INPUT_NOTIFICATION_SUBJECT, "Notification subject is not present on edit EEP page after adding notification");
    }

    /**
     * Asserts an existence of 'Notification message' textarea.
     */
    public void assertNotificationMessageExists() {
        assertElementExists(Page.INPUT_NOTIFICATION_MESSAGE, "Notification message is not present on edit EEP page after adding notification");
    }

    /**
     * Asserts an existence of 'Notification Recipient' input.
     */
    public void assertNotificationRecipientExists() {
        assertElementExists(Page.INPUT_NOTIFICATION_RECIPIENT, "Notification recipient is not present on edit EEP page after adding notification");
    }

    /**
     * Asserts an existence of 'Notification Events' select.
     */
    public void assertNotificationEventsExists() {
        assertElementExists(Page.SELECT_NOTIFICATION_EVENTS, "Notification events is not present on edit EEP page after adding notification");
    }

    /**
     * Asserts an existence of 'Notification Add another' button.
     */
    public void assertAddAnotherNotificationButtonExists() {
        assertElementExists(Page.BUTTON_ADD_ANOTHER_NOTIFICATION, "Add Another notification button is not present on edit EEP page after adding notification");
    }

    /**
     * Verify notification fields are enabled/disabled for all present notifications
     *
     * @param isEnabled true for enabled and false for disabled
     * @param maxFieldIndex the maximun index for existing notifications. Count starts from 0;
     */
    public void verifyNotificationFieldsEnabled(boolean isEnabled, int maxFieldIndex) {
        assertDefaultSendNotificationIsEnabled(isEnabled);
        assertRequiredSendNotificationIsEnabled(isEnabled);
        assertDeleteAllNotificationButtonIsEnabled(isEnabled);
        assertAddNotificationButtonIsEnabled(isEnabled);
        for (int i = 0; i <= maxFieldIndex; i++) {
            assertDeleteNotificationButtonIsEnabled(isEnabled, i);
            assertNotificationSenderInputIsEnabled(isEnabled, i);
            assertNotificationSubjectInputIsEnabled(isEnabled, i);
            assertNotificationRecipientInputIsEnabled(isEnabled, i);
            assertNotificationEventsInputIsEnabled(isEnabled, i);
            assertNotificationTextareaInputIsEnabled(isEnabled, i);
        }
    }
}
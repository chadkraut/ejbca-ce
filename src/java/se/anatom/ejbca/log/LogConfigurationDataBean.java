package se.anatom.ejbca.log;

import javax.ejb.EntityContext;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

/**
 * Entity bean should not be used directly, use though Session beans.
 *
 * Entity Bean representing the log configuration data.
 * Information stored:
 * <pre>
 * Id (Should always be 0)
 * logConfiguration  is the actual log configuration
 * logentryrownumber is the number of the last row number in the log entry database.
 * </pre>
 *
 * @version $Id: LogConfigurationDataBean.java,v 1.3 2003-02-12 11:23:18 scop Exp $
 */
public abstract class LogConfigurationDataBean implements javax.ejb.EntityBean {

    private static Logger log = Logger.getLogger(LogConfigurationDataBean.class);
    protected EntityContext ctx;

    public abstract Integer getId();
    public abstract void setId(Integer id);
    
    public abstract LogConfiguration getLogConfiguration();
    public abstract void setLogConfiguration(LogConfiguration logconfiguration);
    
    public abstract int getLogEntryRowNumber();
    public abstract void setLogEntryRowNumber(int rownumber);
    
    
    public LogConfiguration loadLogConfiguration(){
      LogConfiguration logconfiguration = getLogConfiguration();
      
      // Fill in new information from LogEntry constants.
      for(int i=0 ; i < LogEntry.EVENTNAMES_INFO.length; i++){
        if(logconfiguration.getLogEvent(i) == null)
          logconfiguration.setLogEvent(i,true);  
      }
      
      for(int i=0 ; i < LogEntry.EVENTNAMES_ERROR.length; i++){
        int index = i + LogEntry.EVENT_ERROR_BOUNDRARY;  
        if(logconfiguration.getLogEvent(index) == null)
          logconfiguration.setLogEvent(index,true);  
      } 
      
      return logconfiguration;
    }
    
    public void saveLogConfiguration(LogConfiguration logconfiguration){
      setLogConfiguration(logconfiguration);    
    }
    
    public Integer getAndIncrementRowCount(){
      int returnval = getLogEntryRowNumber();
      setLogEntryRowNumber(returnval +1);
      return new Integer(returnval);  
    }
    
    
    //
    // Fields required by Container
    //


    /**
     * Entity Bean holding data of log configuration.
     * Create by sending in the id.
     * @param id the unique id of logconfiguration (should always be 0).
     * @param logconfiguration is the serialized representation of the log configuration.
     * @return the given id
     *
     **/


    public Integer ejbCreate(Integer id, LogConfiguration logconfiguration) throws CreateException {

        setId(id);
        setLogConfiguration(logconfiguration);
        setLogEntryRowNumber(0);

        return id;
    }

    public void ejbPostCreate(Integer id, LogConfiguration logconfiguration) {
        // Do nothing. Required.
    }

    public void setEntityContext(EntityContext ctx) {
        this.ctx = ctx;
    }

    public void unsetEntityContext() {
        this.ctx = null;
    }

    public void ejbActivate() {
        // Not implemented.
    }

    public void ejbPassivate() {
        // Not implemented.
    }

    public void ejbLoad() {
        // Not implemented.
    }

    public void ejbStore() {
        // Not implemented.
    }

    public void ejbRemove() {
        // Not implemented.
    }

}

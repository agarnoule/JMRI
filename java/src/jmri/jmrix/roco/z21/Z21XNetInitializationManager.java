package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.XNetConsistManager;
import jmri.jmrix.lenz.XNetInitializationManager;
import jmri.jmrix.lenz.XNetLightManager;
import jmri.jmrix.lenz.XNetProgrammerManager;
import jmri.jmrix.lenz.XNetSensorManager;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs Command Station dependant initialization for the Roco
 * z21/Z21 XPressNet implementation. It adds the appropriate Managers via the
 * Initialization Manager based on the Command Station Type.
 *
 * @author	Paul Bender Copyright (C) 2015
 */
public class Z21XNetInitializationManager extends XNetInitializationManager {

    public Z21XNetInitializationManager(XNetSystemConnectionMemo memo) {
        super(memo);
    }

    @Override
    protected void init() {
        if (log.isDebugEnabled()) {
            log.debug("Init called");
        }
/*        float CSSoftwareVersion = systemMemo.getXNetTrafficController()
                .getCommandStation()
                .getCommandStationSoftwareVersion();*
        int CSType = systemMemo.getXNetTrafficController()
                .getCommandStation()
                .getCommandStationType();*/

        jmri.InstanceManager.store(systemMemo.getPowerManager(), jmri.PowerManager.class);
        jmri.InstanceManager.setThrottleManager(systemMemo.getThrottleManager());
        systemMemo.setProgrammerManager(new XNetProgrammerManager(new Z21XNetProgrammer(systemMemo.getXNetTrafficController()), systemMemo));
        jmri.InstanceManager.setProgrammerManager(systemMemo.getProgrammerManager());
        /* the "raw" Command Station only works on systems that support   
         Ops Mode Programming */
        systemMemo.setCommandStation(systemMemo.getXNetTrafficController().getCommandStation());
        jmri.InstanceManager.setCommandStation(systemMemo.getCommandStation());
        /* the consist manager has to be set up AFTER the programmer, to 
         prevent the default consist manager from being loaded on top of it */
        systemMemo.setConsistManager(new XNetConsistManager(systemMemo));
        jmri.InstanceManager.setConsistManager(systemMemo.getConsistManager());
        systemMemo.setTurnoutManager(new Z21XNetTurnoutManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
        jmri.InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());
        systemMemo.setLightManager(new XNetLightManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
        jmri.InstanceManager.setLightManager(systemMemo.getLightManager());
        systemMemo.setSensorManager(new XNetSensorManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
        jmri.InstanceManager.setSensorManager(systemMemo.getSensorManager());

        if (log.isDebugEnabled()) {
            log.debug("XPressNet Initialization Complete");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Z21XNetInitializationManager.class.getName());

}

// ShowTrackMovesAction.java
package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.setup.Setup;

/**
 * Track tool to enable the display of track moves.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2014
 * @version $Revision: 17977 $
 */
public class ShowTrackMovesAction extends AbstractAction {

    public ShowTrackMovesAction() {
        super(Bundle.getMessage("MenuItemShowTrackMoves"));
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        // toggle
        Setup.setShowTrackMovesEnabled(!Setup.isShowTrackMovesEnabled());
    }
}

/* @(#)ShowTrackMovesAction.java */

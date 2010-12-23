/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tc.l2.handler;

import com.tc.async.api.AbstractEventHandler;
import com.tc.async.api.ConfigurationContext;
import com.tc.async.api.EventContext;
import com.tc.l2.msg.IndexSyncCompleteMessage;
import com.tc.l2.msg.IndexSyncMessage;
import com.tc.l2.state.StateSyncManager;
import com.tc.logging.TCLogger;
import com.tc.logging.TCLogging;
import com.tc.objectserver.core.api.ServerConfigurationContext;
import com.tc.objectserver.search.IndexHACoordinator;

public class L2IndexSyncHandler extends AbstractEventHandler {

  private static final TCLogger    logger = TCLogging.getLogger(L2IndexSyncHandler.class);

  // private Sink sendSink;
  private final IndexHACoordinator indexHACoordinator;
  private StateSyncManager         stateSyncManager;

  public L2IndexSyncHandler(final IndexHACoordinator indexHACoordinator) {
    this.indexHACoordinator = indexHACoordinator;
  }

  @Override
  public void handleEvent(final EventContext context) {
    if (context instanceof IndexSyncMessage) {
      final IndexSyncMessage syncMsg = (IndexSyncMessage) context;
      doSyncIndex(syncMsg);
    } else if (context instanceof IndexSyncCompleteMessage) {
      final IndexSyncCompleteMessage msg = (IndexSyncCompleteMessage) context;
      if (logger.isDebugEnabled()) {
        logger.debug("Received IndexSyncCompleteMessage Msg from : " + msg.messageFrom() + " msg : " + msg);
      }
      stateSyncManager.indexSyncComplete();
    } else {
      throw new AssertionError("Unknown context type : " + context.getClass().getName() + " : " + context);
    }
  }

  private void doSyncIndex(final IndexSyncMessage syncMsg) {
    this.indexHACoordinator.applyIndexSync(syncMsg.getCacheName(), syncMsg.getFileName(), syncMsg.getData());
  }

  @Override
  public void initialize(final ConfigurationContext context) {
    super.initialize(context);
    final ServerConfigurationContext oscc = (ServerConfigurationContext) context;
    this.stateSyncManager = oscc.getL2Coordinator().getStateSyncManager();

  }

}
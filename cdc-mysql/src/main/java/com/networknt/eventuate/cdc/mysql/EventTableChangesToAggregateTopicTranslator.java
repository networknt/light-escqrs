package com.networknt.eventuate.cdc.mysql;

import com.networknt.eventuate.server.common.BinLogEvent;
import com.networknt.eventuate.server.common.CdcConfig;
import com.networknt.eventuate.cdc.mysql.exception.EventuateLocalPublishingException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

public class EventTableChangesToAggregateTopicTranslator<M extends BinLogEvent> {

  private final LeaderSelector leaderSelector;
  private MySQLCdcKafkaPublisher<M> mySQLCdcKafkaPublisher;
  private MySQLCdcProcessor<M> mySQLCdcProcessor;
  private CdcConfig cdcConfig;

  private Logger logger = LoggerFactory.getLogger(getClass());

  public EventTableChangesToAggregateTopicTranslator(MySQLCdcKafkaPublisher<M> mySQLCdcKafkaPublisher, MySQLCdcProcessor<M> mySQLCdcProcessor, CuratorFramework client, CdcConfig cdcConfig) {
    this.mySQLCdcKafkaPublisher = mySQLCdcKafkaPublisher;
    this.mySQLCdcProcessor = mySQLCdcProcessor;
    this.cdcConfig = cdcConfig;
    this.leaderSelector = new LeaderSelector(client, "/eventuatelocal/cdc/leader", new LeaderSelectorListener() {

      @Override
      public void takeLeadership(CuratorFramework client) throws Exception {
        takeLeadership();
      }

      private void takeLeadership() throws InterruptedException {
        logger.info("Taking leadership");
        try {
          startCapturingChanges();
        } catch (Throwable t) {
          logger.error("In takeLeadership", t);
          throw t instanceof RuntimeException ? (RuntimeException) t : new RuntimeException(t);
        } finally {
          logger.debug("TakeLeadership returning");
        }
      }

      @Override
      public void stateChanged(CuratorFramework client, ConnectionState newState) {

        logger.debug("StateChanged: {}", newState);

        switch (newState) {
          case SUSPENDED:
            resignLeadership();
            break;

          case RECONNECTED:
            try {
              takeLeadership();
            } catch (InterruptedException e) {
              logger.error("While handling RECONNECTED", e);
            }
            break;

          case LOST:
            resignLeadership();
            break;
        }
      }

      private void resignLeadership() {
        logger.info("Resigning leadership");
        try {
          stopCapturingChanges();
        } catch (InterruptedException e) {
          logger.error("While handling SUSPEND", e);
        }
      }
    });
  }

  @PostConstruct
  public void start() {
    logger.info("CDC initialized. Ready to become leader");
    leaderSelector.start();
  }

  public void startCapturingChanges() throws InterruptedException {
    logger.debug("Starting to capture changes");
    CdcStartupValidator cdcStartupValidator = new CdcStartupValidator(cdcConfig.getJdbcUrl(), cdcConfig.getDbUser(), cdcConfig.getDbPass(), cdcConfig.getKafka());
    cdcStartupValidator.validateEnvironment();

    mySQLCdcKafkaPublisher.start();
    try {
      mySQLCdcProcessor.start(publishedEvent -> {
        try {
          mySQLCdcKafkaPublisher.handleEvent(publishedEvent);
        } catch (EventuateLocalPublishingException e) {
          throw new RuntimeException(e);
        }
      });
    } catch (Exception e) {
      if (e.getCause() instanceof EventuateLocalPublishingException) {
        logger.error("Stopping capturing changes due to exception:", e);
        this.stopCapturingChanges();
      }
    }

    logger.debug("Started CDC Kafka publisher");
  }

  public void stop() throws InterruptedException {
    //stopCapturingChanges();
    leaderSelector.close();
  }

  public void stopCapturingChanges() throws InterruptedException {
    logger.debug("Stopping to capture changes");

    mySQLCdcKafkaPublisher.stop();
    mySQLCdcProcessor.stop();
  }
}

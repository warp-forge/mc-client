package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.gui.task.DataFetcher;
import com.mojang.realmsclient.gui.task.RepeatedDelayStrategy;
import com.mojang.realmsclient.util.RealmsPersistence;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.minecraft.util.Util;

public class RealmsDataFetcher {
   public final DataFetcher dataFetcher;
   private final List tasks;
   public final DataFetcher.Task notificationsTask;
   public final DataFetcher.Task serverListUpdateTask;
   public final DataFetcher.Task pendingInvitesTask;
   public final DataFetcher.Task trialAvailabilityTask;
   public final DataFetcher.Task newsTask;
   public final DataFetcher.Task onlinePlayersTask;
   public final RealmsNewsManager newsManager;

   public RealmsDataFetcher(final RealmsClient realmsClient) {
      this.dataFetcher = new DataFetcher(Util.ioPool(), TimeUnit.MILLISECONDS, Util.timeSource);
      this.newsManager = new RealmsNewsManager(new RealmsPersistence());
      this.serverListUpdateTask = this.dataFetcher.createTask("server list", () -> {
         com.mojang.realmsclient.dto.RealmsServerList realmsServerList = realmsClient.listRealms();
         return RealmsMainScreen.isSnapshot() ? new ServerListData(realmsServerList.servers(), realmsClient.listSnapshotEligibleRealms()) : new ServerListData(realmsServerList.servers(), List.of());
      }, Duration.ofSeconds(60L), RepeatedDelayStrategy.CONSTANT);
      DataFetcher var10001 = this.dataFetcher;
      Objects.requireNonNull(realmsClient);
      this.pendingInvitesTask = var10001.createTask("pending invite count", realmsClient::pendingInvitesCount, Duration.ofSeconds(10L), RepeatedDelayStrategy.exponentialBackoff(360));
      var10001 = this.dataFetcher;
      Objects.requireNonNull(realmsClient);
      this.trialAvailabilityTask = var10001.createTask("trial availablity", realmsClient::trialAvailable, Duration.ofSeconds(60L), RepeatedDelayStrategy.exponentialBackoff(60));
      var10001 = this.dataFetcher;
      Objects.requireNonNull(realmsClient);
      this.newsTask = var10001.createTask("unread news", realmsClient::getNews, Duration.ofMinutes(5L), RepeatedDelayStrategy.CONSTANT);
      var10001 = this.dataFetcher;
      Objects.requireNonNull(realmsClient);
      this.notificationsTask = var10001.createTask("notifications", realmsClient::getNotifications, Duration.ofMinutes(5L), RepeatedDelayStrategy.CONSTANT);
      var10001 = this.dataFetcher;
      Objects.requireNonNull(realmsClient);
      this.onlinePlayersTask = var10001.createTask("online players", realmsClient::getLiveStats, Duration.ofSeconds(10L), RepeatedDelayStrategy.CONSTANT);
      this.tasks = List.of(this.notificationsTask, this.serverListUpdateTask, this.pendingInvitesTask, this.trialAvailabilityTask, this.newsTask, this.onlinePlayersTask);
   }

   public List getTasks() {
      return this.tasks;
   }

   public static record ServerListData(List serverList, List availableSnapshotServers) {
   }
}

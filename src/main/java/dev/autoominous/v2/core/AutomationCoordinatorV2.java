package dev.autoominous.v2.core;

import dev.autoominous.v2.broker.CommandBroker;
import dev.autoominous.v2.broker.GuiBroker;
import dev.autoominous.v2.broker.InputBroker;
import dev.autoominous.v2.broker.InventoryBroker;
import dev.autoominous.v2.job.RecoveryJob;
import dev.autoominous.v2.service.AutoUseService;
import dev.autoominous.v2.service.HomeService;
import dev.autoominous.v2.service.SwordRecoveryService;
import dev.autoominous.v2.service.VexSafetyService;

public final class AutomationCoordinatorV2 {
   private final SessionManager session = new SessionManager();
   private final ResourceArbiter arbiter = new ResourceArbiter();
   private final CommandBroker commands = new CommandBroker(this.arbiter);
   private final GuiBroker gui = new GuiBroker(this.arbiter);
   private final InputBroker input = new InputBroker(this.arbiter);
   private final InventoryBroker inventory = new InventoryBroker(this.arbiter);
   private final HomeService home = new HomeService(this.commands);
   private final VexSafetyService vex = new VexSafetyService();
   private final SwordRecoveryService sword = new SwordRecoveryService();
   private final AutoUseService autoUse = new AutoUseService(this.input);
   private AutomationState state = AutomationState.SESSION_OFFLINE;
   private RecoveryJob recovery;
   private long nextJobId = 1L;
   private boolean reconcilePending;

   public AutomationState state() {
      return this.state;
   }

   public long sessionEpoch() {
      return this.session.epoch();
   }

   public RecoveryJob recovery() {
      return this.recovery;
   }

   public CommandBroker commandBroker() {
      return this.commands;
   }

   public ResourceArbiter arbiter() {
      return this.arbiter;
   }

   public InputBroker inputBroker() {
      return this.input;
   }

   public CoordinatorDecision tick(RuntimeSnapshot var1, FeatureSettings var2, InputBroker.UseIntent var3) {
      if (this.session.updateConnected(var1.connected())) {
         this.arbiter.clear();
         this.commands.clear();
         this.input.release();
         this.home.cancel();
         this.vex.reset();
         if (this.recovery != null && this.recovery.running()) {
            this.recovery.cancel();
         }

         this.recovery = null;
         this.reconcilePending = false;
      }

      if (!var1.connected()) {
         this.state = AutomationState.SESSION_OFFLINE;
         this.input.release();
         return this.decision(var2, false, false, false, false, false, false, InputBroker.UseIntent.NONE, "offline");
      }

      if (this.recovery == null && var1.deathScreen()) {
         this.recovery = new RecoveryJob(this.nextJobId++, this.session.epoch(), var1.nowMs(), var1.nowMs() + 120000L, var1.vexDangerArmed());
      }

      if (this.recovery != null && this.recovery.running() && this.recovery.sessionEpoch() != this.session.epoch()) {
         this.recovery.cancel();
         this.recovery = null;
      }

      if (this.recovery != null && this.recovery.running() && this.recovery.expired(var1.nowMs())) {
         this.recovery.fail();
         this.reconcilePending = true;
      }

      if (this.recovery != null && this.recovery.running()) {
         return this.tickRecovery(var1, var2, var3);
      }

      if (this.reconcilePending) {
         this.state = AutomationState.RECOVERY_RESUME;
         this.input.release();
         this.reconcilePending = false;
         return this.decision(var2, false, false, false, false, false, false, InputBroker.UseIntent.NONE, "reconcile barrier");
      }

      if (var1.vexScan() != VexScan.PRESENT && !var1.vexDangerArmed()) {
         this.state = AutomationState.ALIVE_NORMAL;
      } else {
         this.state = AutomationState.VEX_DANGER;
      }

      PermissionSnapshot var4 = PolicyEngine.permissions(this.state, var2);
      this.autoUse.reconcile(var2, var4, var3, this.session.epoch(), 0L, var1.nowMs());
      return new CoordinatorDecision(
         this.state,
         var4,
         false,
         false,
         false,
         false,
         var4.canSell() && var1.sellRequested(),
         var4.canAutoTpa() && var1.tpaRequested(),
         this.input.intent(),
         "normal"
      );
   }

   private CoordinatorDecision tickRecovery(RuntimeSnapshot var1, FeatureSettings var2, InputBroker.UseIntent var3) {
      this.input.release();
      switch (this.recovery.stage()) {
         case WAIT_VEX_CLEAR:
            this.state = AutomationState.VEX_DEATH_WAIT;
            if (this.vex.clearConfirmed(var1)) {
               this.recovery.stage(RecoveryJob.Stage.RESPAWN);
            }

            return this.decision(var2, false, false, false, false, false, false, InputBroker.UseIntent.NONE, "waiting vex clear");
         case RESPAWN:
            this.state = AutomationState.RESPAWN;
            if (var1.playerAlive() && !var1.deathScreen()) {
               this.recovery.stage(RecoveryJob.Stage.HOME);
               this.home.start(this.recovery.jobId(), this.session.epoch(), Owner.DEATH, var1.nowMs());
               return this.decision(var2, false, false, false, false, false, false, InputBroker.UseIntent.NONE, "respawn observed");
            }

            return this.decision(var2, true, false, false, false, false, false, InputBroker.UseIntent.NONE, "request respawn");
         case HOME:
            this.state = AutomationState.HOME_TRANSIT;
            this.home.tick(var1, this.session.epoch());
            if (this.home.job() != null && this.home.job().result() == TerminalResult.SUCCESS) {
               this.recovery.stage(RecoveryJob.Stage.SWORD);
            } else if (this.home.job() != null && this.home.job().result() != TerminalResult.RUNNING) {
               this.recovery.fail();
               this.reconcilePending = true;
            }

            return this.decision(var2, false, false, false, false, false, false, InputBroker.UseIntent.NONE, "home");
         case SWORD:
            this.state = AutomationState.SWORD_RECOVERY;
            if (!var2.recoverSwordAfterDeath()) {
               this.recovery.stage(RecoveryJob.Stage.RESUME);
               return this.decision(var2, false, false, false, false, false, false, InputBroker.UseIntent.NONE, "sword recovery disabled");
            } else {
               return switch (this.sword.decide(var1)) {
                  case COMPLETE -> {
                     this.recovery.stage(RecoveryJob.Stage.RESUME);
                     yield this.decision(var2, false, false, false, false, false, false, InputBroker.UseIntent.NONE, "sword ready");
                  }
                  case MOVE_FROM_INVENTORY -> this.decision(var2, false, true, false, false, false, false, InputBroker.UseIntent.NONE, "move sword to hotbar");
                  case NEED_FREE_SPACE -> {
                     int var5 = this.recovery.nextFreeSpaceAttempt();
                     if (var5 > 3) {
                        this.recovery.fail();
                        this.reconcilePending = true;
                        yield this.decision(var2, false, false, false, false, false, false, InputBroker.UseIntent.NONE, "free-space budget exhausted");
                     } else {
                        yield this.decision(var2, false, false, false, true, false, false, InputBroker.UseIntent.NONE, "recovery sell for free slot");
                     }
                  }
                  case NEED_ORDER -> {
                     int var4 = this.recovery.nextOrderAttempt();
                     if (var4 > 3) {
                        this.recovery.fail();
                        this.reconcilePending = true;
                        yield this.decision(var2, false, false, false, false, false, false, InputBroker.UseIntent.NONE, "order budget exhausted");
                     } else {
                        yield this.decision(var2, false, false, true, false, false, false, InputBroker.UseIntent.NONE, "request sword order");
                     }
                  }
               };
            }
         case RESUME:
            this.state = AutomationState.RECOVERY_RESUME;
            this.recovery.succeed();
            this.arbiter.releaseOwner(Owner.DEATH);
            this.arbiter.releaseOwner(Owner.SWORD_RECOVERY);
            this.reconcilePending = true;
            return this.decision(var2, false, false, false, false, false, false, InputBroker.UseIntent.NONE, "recovery complete");
         default:
            throw new IllegalStateException("unreachable");
      }
   }

   private CoordinatorDecision decision(
      FeatureSettings var1, boolean var2, boolean var3, boolean var4, boolean var5, boolean var6, boolean var7, InputBroker.UseIntent var8, String var9
   ) {
      PermissionSnapshot var10 = PolicyEngine.permissions(this.state, var1);
      return new CoordinatorDecision(this.state, var10, var2, var3, var4, var5, var6, var7, var8, var9);
   }
}

package dev.autoominous.v2.core;

import dev.autoominous.v2.broker.InputBroker;

public record CoordinatorDecision(
   AutomationState state,
   PermissionSnapshot permissions,
   boolean requestRespawn,
   boolean requestSwordInventoryMove,
   boolean requestOrder,
   boolean requestRecoverySell,
   boolean normalSellAllowed,
   boolean autoTpaAllowed,
   InputBroker.UseIntent inputIntent,
   String note
) {
}

package dev.autoominous;

public final class HomeAnchorRulesChecks {
   private static int checks;
   private static void check(boolean value,String label) {
      checks++;
      if (!value) throw new AssertionError(label);
   }
   public static void main(String[] args) {
      check(HomeAnchorRules.withinPosition(0,64,0,0,64,0,.3),"same position");
      check(HomeAnchorRules.withinPosition(.3,64,0,0,64,0,.3),"horizontal boundary");
      check(!HomeAnchorRules.withinPosition(.301,64,0,0,64,0,.3),"horizontal drift");
      check(!HomeAnchorRules.withinPosition(.25,64,.25,0,64,0,.3),"radial diagonal drift");
      check(HomeAnchorRules.withinPosition(0,64.2,0,0,64,0,.3),"vertical within tolerance");
      check(!HomeAnchorRules.withinPosition(0,64.301,0,0,64,0,.3),"vertical drift");
      check(!HomeAnchorRules.withinPosition(0,65,0,0,64,0,.3),"one block up");
      check(!HomeAnchorRules.withinPosition(Double.NaN,64,0,0,64,0,.3),"invalid live position");
      check(!HomeAnchorRules.withinPosition(0,64,0,Double.POSITIVE_INFINITY,64,0,.3),"invalid anchor");
      check(!HomeAnchorRules.withinPosition(0,64,0,0,64,0,-.3),"negative tolerance");
      check(HomeAnchorRules.withinLook(179,20,-179,20,2),"yaw wrap boundary");
      check(!HomeAnchorRules.withinLook(178.9,20,-179,20,2),"yaw wrap outside");
      check(HomeAnchorRules.withinLook(0,-20,360,-20,.1),"full rotation same direction");
      check(HomeAnchorRules.withinLook(-720,-20,0,-20,.1),"multiple rotations same direction");
      check(HomeAnchorRules.withinLook(182,20,180,20,2),"yaw exact tolerance");
      check(!HomeAnchorRules.withinLook(182.001,20,180,20,2),"yaw drift");
      check(HomeAnchorRules.withinLook(90,22,90,20,2),"pitch exact tolerance");
      check(!HomeAnchorRules.withinLook(90,22.001,90,20,2),"pitch drift");
      check(!HomeAnchorRules.withinLook(90,-89,90,89,2),"pitch does not wrap");
      check(!HomeAnchorRules.withinLook(Double.NaN,20,90,20,2),"invalid yaw");
      check(!HomeAnchorRules.withinLook(90,20,90,Double.NaN,2),"invalid saved pitch");
      check(!HomeAnchorRules.withinLook(90,20,90,20,Double.POSITIVE_INFINITY),"invalid look tolerance");
      for(int yaw=-1080;yaw<=1080;yaw+=45) {
         check(HomeAnchorRules.withinLook(yaw,30,yaw+360,30,.1),"wrap invariant "+yaw);
         check(!HomeAnchorRules.withinLook(yaw+5,30,yaw,30,2),"drift invariant "+yaw);
      }
      System.out.println("Home anchor rules: "+checks+" checks passed.");
   }
}

package me.rothes.protocolstringreplacer.upgrades;

public enum UpgradeEnum {

    FROM_1_TO_2((short) 1, new UpgradeHandler1To2()),
    FROM_2_TO_3((short) 2, new UpgradeHandler2To3());

    private short currentVersion;
    private AbstractUpgradeHandler upgradeHandler;

    UpgradeEnum(short currentVersion, AbstractUpgradeHandler upgradeHandler) {
        this.currentVersion = currentVersion;
        this.upgradeHandler = upgradeHandler;
    }

    public short getCurrentVersion() {
        return currentVersion;
    }

    public AbstractUpgradeHandler getUpgradeHandler() {
        return upgradeHandler;
    }

}

package me.rothes.protocolstringreplacer.upgrades;

public enum UpgradeEnum {

    FROM_1_TO_2((short) 1, new UpgradeHandler_1());

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

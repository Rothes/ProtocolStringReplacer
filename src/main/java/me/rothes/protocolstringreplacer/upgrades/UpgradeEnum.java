package me.rothes.protocolstringreplacer.upgrades;

public enum UpgradeEnum {

    FROM_1_TO_2((short) 1, UpgradeHandler1To2.class),
    FROM_2_TO_3((short) 2, UpgradeHandler2To3.class),
    FROM_3_TO_4((short) 3, UpgradeHandler3To4.class);

    private short currentVersion;
    private Class<? extends AbstractUpgradeHandler> upgradeHandler;

    UpgradeEnum(short currentVersion, Class<? extends AbstractUpgradeHandler> upgradeHandler) {
        this.currentVersion = currentVersion;
        this.upgradeHandler = upgradeHandler;
    }

    public short getCurrentVersion() {
        return currentVersion;
    }

    public Class<? extends AbstractUpgradeHandler> getUpgradeHandler() {
        return upgradeHandler;
    }

}

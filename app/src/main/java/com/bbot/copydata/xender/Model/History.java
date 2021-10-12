package com.bbot.copydata.xender.Model;

public class History {

    String filePath, type;
    int isSendReceived;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIsSendReceived() {
        return isSendReceived;
    }

    public void setIsSendReceived(int isSendReceived) {
        this.isSendReceived = isSendReceived;
    }
}

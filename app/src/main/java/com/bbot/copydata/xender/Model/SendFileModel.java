package com.bbot.copydata.xender.Model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SendFileModel {
    public String type;
    public boolean isSend;
    public boolean isReceived;

    public List<File> files;
    public ArrayList<String> fileName = new ArrayList<>();


}

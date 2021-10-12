package com.bbot.copydata.xender.filter.callback;

import com.bbot.copydata.xender.filter.entity.BaseFile;
import com.bbot.copydata.xender.filter.entity.Directory;

import java.util.List;

public interface ApkResultCallback <T extends BaseFile> {
    void onResult(List<Directory<T>> directories);
}
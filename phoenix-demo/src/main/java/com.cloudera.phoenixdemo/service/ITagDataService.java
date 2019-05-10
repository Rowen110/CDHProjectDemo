package com.cloudera.phoenixdemo.service;


import com.cloudera.phoenixdemo.entity.TagData;

import java.util.List;

public interface ITagDataService {
    List<TagData> findTagDataByIds(List<Integer> tagDataIds);
}

package com.cloudera.phoenixdemo.service.impl;

import com.cloudera.phoenixdemo.dao.outer.TagDataMapper;
import com.cloudera.phoenixdemo.entity.TagData;
import com.cloudera.phoenixdemo.entity.TagDataCriteria;
import com.cloudera.phoenixdemo.service.ITagDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Charles
 * @package com.hdjt.bigdata.service.impl
 * @classname TagDataService
 * @description TODO
 * @date 2019-4-29 11:06
 */
@Service
public class TagDataService implements ITagDataService {

    @Autowired
    private TagDataMapper tagDataMapper;


    @Override
    public List<TagData> findTagDataByIds(List<Integer> tagDataIds) {

        List<TagData> list = new ArrayList<TagData>();
        if (tagDataIds != null && tagDataIds.size() > 0) {
//            TagDataCriteria example=new TagDataCriteria();
//            example.createCriteria().andIdIn(tagDataIds);
//            list = tagDataMapper.selectByExample(example);
            list = tagDataMapper.findTagDataByIds(tagDataIds);
        }
        return  list;
    }
}

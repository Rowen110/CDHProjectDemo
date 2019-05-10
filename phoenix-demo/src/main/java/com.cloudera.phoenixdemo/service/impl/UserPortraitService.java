package com.cloudera.phoenixdemo.service.impl;

import com.cloudera.phoenixdemo.constant.PageConstant;
import com.cloudera.phoenixdemo.dao.phoenix.UserPortraitMapper;
import com.cloudera.phoenixdemo.entity.BaseUserPortrait;
import com.cloudera.phoenixdemo.entity.PageModel;
import com.cloudera.phoenixdemo.entity.TagData;
import com.cloudera.phoenixdemo.service.ITagDataService;
import com.cloudera.phoenixdemo.service.IUserPortraitService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author Charles
 * @package com.hdjt.bigdata.service.impl
 * @classname UserPortraitService
 * @description TODO
 * @date 2019-4-26 15:39
 */
@Service
public class UserPortraitService implements IUserPortraitService {
    @Autowired
    private UserPortraitMapper mapper;

    @Autowired
    private ITagDataService tagDataService;

    @Override
    public int upsertUserPortrait(BaseUserPortrait bean) {
        return mapper.upsertUserPortrait(bean);
    }

    @Override
    public PageModel<BaseUserPortrait> getBaseUserByTagDataId(PageModel pageModel) {
        Integer[] ids = pageModel.getIds();
        PageModel result = null;

        if (ids!= null ) {

            List<Integer> tagDataIds = Arrays.asList(ids);
            List<TagData> tagDataList = tagDataService.findTagDataByIds(tagDataIds);

            Integer pageNum = pageModel.getPageNum();
            Integer pageSize = pageModel.getLimit();

            if (pageNum == null || pageNum < 0 ) {
                pageNum = PageConstant.pageNum;
            }
            if (pageSize == null || pageSize <= 0) {
                pageSize = PageConstant.pageSize;
            }
            PageHelper.startPage(pageNum, pageSize);
            List<BaseUserPortrait> list = mapper.getBaseUserByTagDataId(tagDataList);
            PageInfo<BaseUserPortrait> pageInfo = new PageInfo<BaseUserPortrait>(list);
            result = PageModel.convertToPageModel(pageInfo);
        }

        return result;
    }
}
